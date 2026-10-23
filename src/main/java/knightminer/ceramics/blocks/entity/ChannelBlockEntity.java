package knightminer.ceramics.blocks.entity;

import knightminer.ceramics.Registration;
import knightminer.ceramics.blocks.ChannelBlock;
import knightminer.ceramics.blocks.ChannelBlock.ChannelConnection;
import knightminer.ceramics.network.ChannelFlowPacket;
import knightminer.ceramics.network.FluidUpdatePacket;
import knightminer.ceramics.network.FluidUpdatePacket.FluidUpdater;
import knightminer.ceramics.util.tank.ChannelSideTank;
import knightminer.ceramics.util.tank.ChannelTank;
import knightminer.ceramics.util.tank.FillOnlyFluidHandler;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Plane;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import net.neoforged.neoforge.fluids.capability.templates.EmptyFluidHandler;
import org.jetbrains.annotations.Nullable;
import slimeknights.mantle.network.PacketHelper;

import java.util.EnumMap;
import java.util.Map;

/**
 * Logic for channel fluid transfer
 */
public class ChannelBlockEntity extends CrackableBlockEntity implements FluidUpdater {
	public static final BlockEntityTicker<ChannelBlockEntity> SERVER_TICKER = (level, pos, state, te) -> te.tick(state);
	/** Channel internal tank */
	private final ChannelTank tank = new ChannelTank(FaucetBlockEntity.MB_PER_TICK * 4, this);
	/** Handler to return from channel top */
	private final IFluidHandler topHandler = new FillOnlyFluidHandler(tank);
	/** Tanks for inserting on each side */
	private final Map<Direction,IFluidHandler> sideTanks = Util.make(new EnumMap<>(Direction.class), map -> {
		for (Direction direction : Plane.HORIZONTAL) {
			map.put(direction, new ChannelSideTank(this, tank, direction));
		}
	});

	/** Cache of tanks on all neighboring sides */
	private final Map<Direction, BlockCapabilityCache<IFluidHandler, @Nullable Direction>> neighborTanks = new EnumMap<>(Direction.class);

	/** Stores if the channel is currently flowing, set to 2 to allow a small buffer */
	private final byte[] isFlowing = new byte[5];

	public ChannelBlockEntity(BlockPos pos, BlockState state, boolean crackable) {
		super(Registration.CHANNEL_BLOCK_ENTITY.get(), pos, state, crackable);
	}

	public ChannelBlockEntity(BlockPos pos, BlockState state) {
		this(pos, state, isCrackable(state));
	}

	/**
	 * Gets the central fluid tank of this channel
	 * @return  Central tank
	 */
	public FluidStack getFluid() {
		return this.tank.getFluid();
	}


	/* Fluid handlers */

	/** Gets the fluid handler exposed to the given side */
	@Nullable
	public IFluidHandler getFluidHandler(@Nullable Direction side) {
		// top side gets the insert direct
		if (side == null || side == Direction.UP) {
			return topHandler;
		}
		// on down, return an empty handler
		BlockState state = getBlockState();
		// side tanks keep track of which side inserts
		if (side != Direction.DOWN) {
			ChannelConnection connection = state.getValue(ChannelBlock.DIRECTION_MAP.get(side));
			if (connection == ChannelConnection.IN) {
				return sideTanks.get(side);
			}
			// for out, return an empty fluid handler so the block we are pouring into knows we support fluids, even though we disallow any interaction
			// this will get invalidated when the connection goes back to in later
			if (connection == ChannelConnection.OUT) {
				return EmptyFluidHandler.INSTANCE;
			}
		} else if (state.getValue(ChannelBlock.DOWN)) {
			// empty fluid handler when flowing down, null when not
			return EmptyFluidHandler.INSTANCE;
		}
		return null;
	}

	@Override
	public void onLoad() {
		super.onLoad();
		if (level instanceof ServerLevel server) {
			for (Direction direction : Direction.values()) {
				if (direction != Direction.UP) {
					neighborTanks.put(direction, BlockCapabilityCache.create(Capabilities.FluidHandler.BLOCK, server, worldPosition.relative(direction), direction.getOpposite()));
				}
			}
		}
	}

	/**
	 * Gets the fluid handler from a neighbor
	 * @param side  Side of the neighbor to fetch
	 * @return  Fluid handler, or empty
	 */
	@Nullable
	protected IFluidHandler getNeighborHandler(Direction side) {
		BlockCapabilityCache<IFluidHandler, @Nullable Direction> cache = neighborTanks.get(side);
		return cache != null ? cache.getCapability() : null;
	}

	/**
	 * Refreshes a neighbor based on the new connection
	 * @param newState  The state that will later be put in the world, may not be the state currently in the world
	 * @param side   Side to update
	 */
	public void refreshNeighbor(BlockState newState, Direction side) {
		// for below, only thing that needs to invalidate is if we are no longer connected down, remove the listener below
		BlockState oldState = getBlockState();
		boolean needsInvalidate = false;
		if (side == Direction.DOWN) {
			if (newState.getValue(ChannelBlock.DOWN) != oldState.getValue(ChannelBlock.DOWN)) {
				needsInvalidate = true;
			}
		} else if (side != Direction.UP) {
			EnumProperty<ChannelConnection> property = ChannelBlock.DIRECTION_MAP.get(side);
			if (newState.getValue(property) != oldState.getValue(property)) {
				needsInvalidate = true;
			}
		}
		if (needsInvalidate && level != null) {
			level.invalidateCapabilities(worldPosition);
		}
	}

	/**
	 * Called on random tick to increase or decrease cracks level
	 */
	public void randomTick() {
		getCracksHandler().updateCracks(tank.getFluid());
	}


	/* Flowing property */

	/**
	 * Gets the index for the given side for flowing. Same as regular index but without up
	 * @param side  Side to index
	 * @return Flow index
	 */
	private int getFlowIndex(Direction side) {
		if (side.getAxis().isVertical()) {
			return 0;
		}
		return side.get3DDataValue() - 1;
	}

	/**
	 * Marks the given side as flowing for the sake of rendering
	 * @param side     Side to set
	 * @param flowing  True to mark it as flowing
	 */
	public void setFlow(Direction side, boolean flowing) {
		if (side == Direction.UP) {
			return;
		}
		// update flowing state
		int index = getFlowIndex(side);
		boolean wasFlowing = isFlowing[index] > 0;
		isFlowing[index] = (byte)(flowing ? 2 : 0);

		// send packet to client if it changed
		if(wasFlowing != flowing) {
			syncFlowToClient(side, flowing);
		}
	}

	/**
	 * Checks if the given side is flowing
	 * @param side  Side to check
	 * @return  True if flowing
	 */
	public boolean isFlowing(Direction side) {
		if (side == Direction.UP) {
			return false;
		}

		return isFlowing[getFlowIndex(side)] > 0;
	}


	/* Utilities */

	/**
	 * Gets the connection for a side
	 * @param side  Side to query
	 * @return  Connection on the specified side
	 */
	protected boolean isOutput(Direction side) {
		// just always return in for up, thats fine
		if(side == Direction.UP) {
			return false;
		}
		// down is boolean, sides is multistate
		if(side == Direction.DOWN) {
			return this.getBlockState().getValue(ChannelBlock.DOWN);
		}
		return this.getBlockState().getValue(ChannelBlock.DIRECTION_MAP.get(side)) == ChannelConnection.OUT;
	}

	/**
	 * Counts the number of side outputs on the given side
	 * @param state  State to check
	 * @return  Number of outputs
	 */
	private static int countOutputs(BlockState state) {
		int count = 0;
		for (Direction direction : Plane.HORIZONTAL) {
			if (state.getValue(ChannelBlock.DIRECTION_MAP.get(direction)) == ChannelConnection.OUT) {
				count++;
			}
		}
		return count;
	}

	/**
	 * Syncs the given flowing state to the client side
	 * @param side     Side to sync
	 * @param flowing  Flowing state to sync
	 */
	private void syncFlowToClient(Direction side, boolean flowing) {
		if (level != null && !level.isClientSide) {
			setChangedFast();
			PacketHelper.sendToPlayersTrackingPosition(level, worldPosition, new ChannelFlowPacket(worldPosition, side, flowing));
		}
	}


	/* Flow */

	/**
	 * Ticking logic
	 */
	private void tick(BlockState state) {
		// must have fluid first
		FluidStack fluid = tank.getFluid();
		if(!fluid.isEmpty()) {
			// if we have down and can flow, skip sides
			boolean hasFlown = false;
			if(state.getValue(ChannelBlock.DOWN)) {
				hasFlown = trySide(Direction.DOWN, FaucetBlockEntity.MB_PER_TICK);
			}
			// try sides if we have any sides
			int outputs = countOutputs(state);
			if(!hasFlown && outputs > 0) {
				// split the fluid evenly between sides
				int flowRate = Mth.clamp(tank.getMaxUsable() / outputs, 1, FaucetBlockEntity.MB_PER_TICK);
				// then transfer on each side
				for(Direction side : Plane.HORIZONTAL) {
					trySide(side, flowRate);
				}
			}
		}

		// clear flowing if we should no longer flow on a side
		for (int i = 0; i < 5; i++) {
			if (isFlowing[i] > 0) {
				isFlowing[i]--;
				if (isFlowing[i] == 0) {
					Direction direction;
					if (i == 0) {
						direction = Direction.DOWN;
					} else {
						direction = Direction.from3DDataValue(i + 1);
					}
					syncFlowToClient(direction, false);
				}
			}
		}

		tank.freeFluid();
	}

	/**
	 * Tries transferring fluid on a single side of the channel
	 * @param side      Side to transfer from
	 * @param flowRate  Maximum amount to output
	 * @return  True if the side transferred fluid
	 */
	protected boolean trySide(Direction side, int flowRate) {
		if (tank.isEmpty() || !this.isOutput(side)) {
			return false;
		}
		IFluidHandler handler = getNeighborHandler(side);
		if (handler != null) {
			return fill(side, handler, flowRate);
		}
		return false;
	}

	/**
	 * Fill the fluid handler on the given side
	 * @param side     Side to fill
	 * @param handler  Handler to fill
	 * @param amount   Amount to fill
	 * @return  True if the side successfully filled something
	 */
	protected boolean fill(Direction side, IFluidHandler handler, int amount) {
		// make sure we do not allow more than the fluid allows, should not happen but just in case
		int usable = Math.min(tank.getMaxUsable(), amount);
		if (usable > 0) {
			// see how much works
			FluidStack fluid = tank.drain(usable, FluidAction.SIMULATE);
			int filled = handler.fill(fluid, FluidAction.SIMULATE);
			if (filled > 0) {
				// drain the amount that worked
				fluid = tank.drain(filled, FluidAction.EXECUTE);
				handler.fill(fluid, FluidAction.EXECUTE);

				// mark that the side is flowing
				setFlow(side, true);
				return true;
			}
		}

		// failed to flow, mark side as not flowing
		setFlow(side, false);
		return false;
	}


	/* NBT and sync */
	private static final String TAG_IS_FLOWING = "is_flowing";
	private static final String TAG_TANK = "tank";

	/**
	 * Sends a fluid update to the client with the current fluid
	 */
	public void sendFluidUpdate() {
		if (level != null && !level.isClientSide) {
			PacketHelper.sendToPlayersTrackingPosition(level, worldPosition, new FluidUpdatePacket(worldPosition, getFluid(), false));
		}
	}

	@Override
  public void updateFluid(FluidStack fluid, boolean unused) {
		tank.setFluid(fluid);
	}

	@Override
	protected boolean shouldSyncOnUpdate() {
		return true;
	}

	@Override
	protected void saveSynced(CompoundTag nbt, HolderLookup.Provider registries) {
		super.saveSynced(nbt, registries);
		nbt.putByteArray(TAG_IS_FLOWING, isFlowing);
		nbt.put(TAG_TANK, tank.writeToNBT(registries, new CompoundTag()));
	}

	@Override
	public void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.loadAdditional(nbt, registries);

		// isFlowing
		if (nbt.contains(TAG_IS_FLOWING)) {
			byte[] nbtFlowing = nbt.getByteArray(TAG_IS_FLOWING);
			int max = Math.min(5, nbtFlowing.length);
			for (int i = 0; i < max; i++) {
				byte b = nbtFlowing[i];
				if (b > 2) {
					isFlowing[i] = 2;
				} else if (b < 0) {
					isFlowing[i] = 0;
				} else {
					isFlowing[i] = b;
				}
			}
		}

		// tank
		CompoundTag tankTag = nbt.getCompound(TAG_TANK);
		tank.readFromNBT(registries, tankTag);
	}
}
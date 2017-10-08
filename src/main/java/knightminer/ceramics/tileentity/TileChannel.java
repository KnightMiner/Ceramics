package knightminer.ceramics.tileentity;

import java.util.Locale;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import knightminer.ceramics.library.TagUtil;
import knightminer.ceramics.library.Util;
import knightminer.ceramics.library.tank.ChannelSideTank;
import knightminer.ceramics.library.tank.ChannelTank;
import knightminer.ceramics.library.tank.IFluidUpdateReciever;
import knightminer.ceramics.network.CeramicsNetwork;
import knightminer.ceramics.network.ChannelConnectionPacket;
import knightminer.ceramics.network.ChannelFlowPacket;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TileChannel extends TileEntity implements ITickable, IFluidUpdateReciever {

	/** Stores if the channel can be connected on the side */
	private ChannelConnection[] connections;
	/** Connection on the bottom as its boolean */
	private boolean connectedDown;
	/** Stores if the channel is capable of connecting on that side */
	private boolean[] canConnect;
	/** Stores if the channel can connect downwards */
	private boolean canConnectDown;

	private boolean wasEmpty;


	/** Stores if the channel is currently flowing, byte will determine for how long */
	private byte[] isFlowing;
	/** Stores if the channel is currently flowing down */
	private boolean isFlowingDown;

	private int numOutputs;

	private ChannelTank tank;
	private ChannelSideTank[] sideTanks;

	public TileChannel() {
		this.connections = new ChannelConnection[4];
		this.connectedDown = false;
		this.canConnect = new boolean[4];
		this.canConnectDown = false;

		this.isFlowing = new byte[4];

		this.tank = new ChannelTank(144, this);
		this.sideTanks = new ChannelSideTank[4];
		this.numOutputs = 0;
		this.wasEmpty = true;
	}

	/* Flow */

	/**
	 * Ticking logic
	 */
	@Override
	public void update() {
		if(getWorld().isRemote) {
			return;
		}

		FluidStack fluid = tank.getFluid();
		if(fluid != null) {

			// if we have down, use only that
			if(isConnectedDown()) {
				trySide(EnumFacing.DOWN, TileFaucet.LIQUID_TRANSFER);
				// otherwise, ensure we have a connection before pouring
			} else if(numOutputs > 0) {
				// we want to split the fluid if needed rather than favoring a side
				int flowRate = Math.max(1, Math.min(tank.maxOutput() / numOutputs, TileFaucet.LIQUID_TRANSFER));
				// then just try each side
				for(EnumFacing side : EnumFacing.HORIZONTALS) {
					trySide(side, flowRate);
				}
			}
		}

		// clear flowing if we should no longer flow on a side
		for(int i = 0; i < 4; i++) {
			if(isFlowing[i] > 0) {
				isFlowing[i]--;
			}
		}

		tank.freeFluid();
	}

	protected void trySide(@Nonnull EnumFacing side, int flowRate) {
		if(tank.getFluid() == null || this.getConnection(side) != ChannelConnection.OUT) {
			return;
		}

		// what are we flowing into
		TileEntity te = world.getTileEntity(pos.offset(side));
		// for channels, we have slightly quicker logic
		if(te instanceof TileChannel) {
			TileChannel channel = (TileChannel)te;
			// only flow if the other channel is receiving
			EnumFacing opposite = side.getOpposite();
			if(channel.getConnection(opposite) == ChannelConnection.IN) {
				fill(side, channel.getTank(opposite), flowRate);
			}
		}
		else {
			IFluidHandler toFill = getFluidHandler(te, side.getOpposite());
			if(toFill != null) {
				fill(side, toFill, flowRate);
			}
		}
	}

	protected void fill(EnumFacing side, @Nonnull IFluidHandler handler, int amount) {
		FluidStack fluid = tank.getFluid().copy();
		// use the smaller of the rate or the amount divided by connections
		fluid.amount = amount;
		int filled = handler.fill(fluid, false);
		if(filled > 0) {
			setFlow(side, true);
			filled = handler.fill(fluid, true);
			tank.drainInternal(filled, true);
		} else {
			setFlow(side, false);
		}
	}

	protected TileChannel getChannel(BlockPos pos) {
		TileEntity te = getWorld().getTileEntity(pos);
		if(te != null && te instanceof TileChannel) {
			return (TileChannel) te;
		}
		return null;
	}

	protected IFluidHandler getFluidHandler(TileEntity te, EnumFacing direction) {
		if(te != null && te.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, direction)) {
			return te.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, direction);
		}
		return null;
	}

	/* Fluid interactions */
	@Override
	public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
		if(capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
			// we do not allow inserting from the bottom
			return facing != null && facing != EnumFacing.DOWN;
		}
		return super.hasCapability(capability, facing);
	}

	@Nonnull
	@Override
	public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
		if(capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
			return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(getTank(facing));
		}
		return super.getCapability(capability, facing);
	}


	/**
	 * Called on block placement to fill data from blocks on all sides
	 * @param side   Side clicked
	 * @param sneak  If true, player was sneaking
	 */
	public void onPlaceBlock(EnumFacing hit, boolean sneak) {
		EnumFacing side = hit.getOpposite();
		// start by setting down as it can skip a bit of the logic in the loop with raw connections
		this.setCanConnect(EnumFacing.DOWN, connectionValid(pos.down(), EnumFacing.DOWN));

		// first, set canConnects
		for(EnumFacing facing : EnumFacing.HORIZONTALS) {
			this.setCanConnect(facing, connectionValid(pos.offset(facing), facing.getOpposite()));
		}

		// if placed below a channel, connect it to us
		if(side == EnumFacing.UP) {
			TileEntity te = world.getTileEntity(pos.offset(side));
			if(te instanceof TileChannel) {
				((TileChannel)te).connectedDown = true;
			}
		}

		// for the rest, go with our preset connection
		else if(this.canConnect(side)) {
			TileEntity te = world.getTileEntity(pos.offset(side));
			// if its a channel, update ours and their connections to each other
			if(te instanceof TileChannel) {
				// if we hit the bottom of a channel, make it flow into us
				if(side == EnumFacing.DOWN) {
					this.connectedDown = true;
				} else {
					// default to out on click, but do in if sneaking
					ChannelConnection connection = sneak ? ChannelConnection.IN : ChannelConnection.OUT;
					this.setConnection(side, connection.getOpposite());
					((TileChannel)te).setConnection(hit, connection);
				}
			} else {
				// we already know we can connect, so just set out
				this.setConnection(side, ChannelConnection.OUT);
			}
		}
	}

	/**
	 * Handles an update from another block to update the shape
	 * @param fromPos  Block that changed
	 */
	public void handleBlockUpdate(BlockPos fromPos) {
		EnumFacing side = Util.facingFromNeighbor(this.pos, fromPos);
		// we don't care about up as we don't connect on up
		if(side != null && side != EnumFacing.UP) {
			if(connectionValid(fromPos, side)) {
				this.setCanConnect(side, true);
			} else {
				// if we cannot connect, clear the connection type
				this.setCanConnect(side, false);
				this.setConnection(side, ChannelConnection.NONE);
			}


			// a regular block update seems to run 1 tick before the client receives the update, so send a pack that handles our update
			CeramicsNetwork.sendToAllAround(world, pos, new ChannelConnectionPacket(pos, side, this.canConnect(side)));
		}
	}

	protected boolean connectionValid(BlockPos pos, EnumFacing side) {
		TileEntity te = world.getTileEntity(pos);
		if(te == null) {
			return false;
		}

		return te instanceof TileChannel || te.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, side.getOpposite());
	}

	/**
	 * Interacts with the tile entity, setting the side to show or hide
	 * @param side  Side clicked
	 */
	public void interact(EnumFacing side, boolean sneak) {
		// if down, just reverse the connection
		if(side == EnumFacing.DOWN) {
			this.connectedDown = !this.connectedDown;
			this.updateBlock(pos);
		} else {
			// otherwise, we rotate though connections
			ChannelConnection newConnect = this.getConnection(side).getNext(sneak);
			this.setConnection(side, newConnect);

			// if we have a neighbor, update them as well
			BlockPos offset = this.pos.offset(side);
			TileEntity te = world.getTileEntity(offset);
			if(te instanceof TileChannel) {
				((TileChannel) te).setConnection(side.getOpposite(), newConnect.getOpposite());
			}
			// block updates
			this.updateBlock(pos);
			this.updateBlock(offset);
		}
	}

	/* Helper methods */

	public ChannelTank getTank() {
		return this.tank;
	}

	protected IFluidHandler getTank(@Nonnull EnumFacing side) {
		if(side == EnumFacing.UP) {
			return tank;
		}

		int index = side.getHorizontalIndex();
		if(index >= 0) {
			if(sideTanks[index] == null) {
				sideTanks[index] = new ChannelSideTank(this, tank, side);
			}

			return sideTanks[index];
		}

		return null;
	}

	/**
	 * Checks if a channel can connect on the specified side
	 * @param side  Side to check
	 * @return  True if the channel can connect
	 */
	public boolean canConnect(@Nonnull EnumFacing side) {
		// special case down
		if(side == EnumFacing.DOWN) {
			return canConnectDown;
		}

		int index = side.getHorizontalIndex();
		if(index < 0) {
			return false;
		}

		return canConnect[index];
	}

	/**
	 * Sets if the channel can connect on the side
	 * @param side        Side to set
	 * @param canConnect  New status
	 */
	public void setCanConnect(@Nonnull EnumFacing side, boolean canConnect) {
		if(side == EnumFacing.DOWN) {
			this.canConnectDown = canConnect;
			return;
		}

		int index = side.getHorizontalIndex();
		if(index >= 0) {
			this.canConnect[index] = canConnect;
		}
	}

	/**
	 * Gets the raw connection for a horizontal
	 * @param side  Side to query
	 * @return  Connection for the horzontal without taking canConnect into account
	 */
	@Nonnull
	public ChannelConnection getConnectionRaw(@Nonnull EnumFacing side) {
		if(side == EnumFacing.DOWN) {
			return this.connectedDown ? ChannelConnection.OUT : ChannelConnection.NONE;
		}

		int index = side.getHorizontalIndex();
		if(index < 0) {
			return null;
		}

		// not nullable
		ChannelConnection connection = connections[index];
		return connection == null ? ChannelConnection.NONE : connection;
	}

	/**
	 * Gets the functional connection for a side
	 * @param side  Side to query
	 * @return  Connection taking if the channel can connect into account
	 */
	@Nonnull
	public ChannelConnection getConnection(@Nonnull EnumFacing side) {
		if(side == EnumFacing.UP) {
			return ChannelConnection.IN;
		}

		if(!canConnect(side)) {
			return ChannelConnection.NONE;
		}

		return getConnectionRaw(side);
	}

	public boolean isConnectedDown() {
		return canConnectDown && connectedDown;
	}

	public void setConnection(@Nonnull EnumFacing side, @Nonnull ChannelConnection connection) {
		if(side == EnumFacing.DOWN) {
			this.connectedDown = connection == ChannelConnection.OUT;
			return;
		}

		int index = side.getHorizontalIndex();
		if(index >= 0) {
			ChannelConnection oldConnection = this.connections[index];
			// if we changed from or to none, adjust connections
			if(oldConnection != ChannelConnection.OUT && connection == ChannelConnection.OUT) {
				numOutputs++;
			} else if (oldConnection == ChannelConnection.OUT && connection != ChannelConnection.OUT) {
				numOutputs--;
			}

			this.connections[index] = connection;
		}
	}

	public void setFlow(@Nonnull EnumFacing side, boolean isFlowing) {
		if(side == EnumFacing.UP) {
			return;
		}

		boolean wasFlowing = setFlowRaw(side, isFlowing);
		if(wasFlowing != isFlowing) {
			CeramicsNetwork.sendToAllAround(world, pos, new ChannelFlowPacket(pos, side, isFlowing));
		}
	}

	private boolean setFlowRaw(@Nonnull EnumFacing side, boolean isFlowing) {
		boolean wasFlowing;
		if(side == EnumFacing.DOWN) {
			wasFlowing = this.isFlowingDown;
			this.isFlowingDown = isFlowing;
		} else {
			int index = side.getHorizontalIndex();
			wasFlowing = this.isFlowing[index] > 0;
			this.isFlowing[index] = (byte) (isFlowing ? 5 : 0);
		}

		return wasFlowing;
	}

	public boolean isFlowing(@Nonnull EnumFacing side) {
		if(side == EnumFacing.DOWN) {
			return this.isFlowingDown;
		}

		int index = side.getHorizontalIndex();
		if(index >= 0) {
			return this.isFlowing[index] > 0;
		}

		return false;
	}

	private void updateBlock(BlockPos pos) {
		IBlockState state = world.getBlockState(pos);
		world.notifyBlockUpdate(pos, state, state, 2);
	}


	/* NBT */
	private static final String TAG_CONNECTIONS = "connections";
	private static final String TAG_CONNECTED_DOWN = "connected_down";
	private static final String TAG_CAN_CONNECT = "can_connect";
	private static final String TAG_CAN_CONNECT_DOWN = "can_connect_down";
	private static final String TAG_IS_FLOWING = "is_flowing";
	private static final String TAG_IS_FLOWING_DOWN = "is_flowing_down";
	private static final String TAG_TANK = "tank";

	// load and save
	@Nonnull
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		nbt = super.writeToNBT(nbt);

		byte[] bytes = new byte[4];
		ChannelConnection connection;
		for(int i = 0; i < 4; i++) {
			connection = connections[i];
			bytes[i] = connection == null ? 0 : connection.getIndex();
		}
		nbt.setByteArray(TAG_CONNECTIONS, bytes);
		nbt.setBoolean(TAG_CONNECTED_DOWN, connectedDown);
		nbt.setByteArray(TAG_CAN_CONNECT, TagUtil.toByteArray(canConnect));
		nbt.setBoolean(TAG_CAN_CONNECT_DOWN, canConnectDown);
		nbt.setByteArray(TAG_IS_FLOWING, isFlowing);
		nbt.setBoolean(TAG_IS_FLOWING_DOWN, isFlowingDown);
		nbt.setTag(TAG_TANK, tank.writeToNBT(new NBTTagCompound()));

		return nbt;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);

		// connections
		if(nbt.hasKey(TAG_CONNECTIONS)) {
			this.connections = new ChannelConnection[4];
			this.numOutputs = 0;
			byte[] bytes = nbt.getByteArray(TAG_CONNECTIONS);
			for(int i = 0; i < 4 && i < bytes.length; i++) {
				this.connections[i] = ChannelConnection.fromIndex(bytes[i]);

				// just calc this instead of storing it
				if(this.connections[i] != ChannelConnection.NONE) {
					this.numOutputs++;
				}
			}
		}
		this.connectedDown = nbt.getBoolean(TAG_CONNECTED_DOWN);

		// can connect
		if(nbt.hasKey(TAG_CAN_CONNECT)) {
			this.canConnect = TagUtil.toBoolArray(nbt.getByteArray(TAG_CAN_CONNECT));
		}
		this.canConnectDown = nbt.getBoolean(TAG_CAN_CONNECT_DOWN);

		// isFlowing
		if(nbt.hasKey(TAG_IS_FLOWING)) {
			this.isFlowing = nbt.getByteArray(TAG_IS_FLOWING);
		}
		this.isFlowingDown = nbt.getBoolean(TAG_IS_FLOWING_DOWN);

		// tank
		NBTTagCompound tankTag = nbt.getCompoundTag(TAG_TANK);
		if(tankTag != null) {
			tank.readFromNBT(tankTag);
		}
	}

	// networking
	@Override
	public void updateFluidTo(FluidStack fluid) {
		tank.setFluid(fluid);
	}

	@SideOnly(Side.CLIENT)
	public void updateCanConnect(EnumFacing side, boolean canConnect) {
		this.setCanConnect(side, canConnect);
		if(!canConnect) {
			this.setConnection(side, ChannelConnection.NONE);
		}
		this.updateBlock(pos);
	}

	@SideOnly(Side.CLIENT)
	public void updateFlow(EnumFacing side, boolean flow) {
		this.setFlowRaw(side, flow);
	}

	@Override
	public SPacketUpdateTileEntity getUpdatePacket() {
		NBTTagCompound tag = new NBTTagCompound();
		writeToNBT(tag);
		return new SPacketUpdateTileEntity(this.getPos(), this.getBlockMetadata(), tag);
	}

	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
		super.onDataPacket(net, pkt);
		readFromNBT(pkt.getNbtCompound());
	}

	@Nonnull
	@Override
	public NBTTagCompound getUpdateTag() {
		// new tag instead of super since default implementation calls the super of writeToNBT
		return writeToNBT(new NBTTagCompound());
	}

	@Override
	public void handleUpdateTag(@Nonnull NBTTagCompound tag) {
		readFromNBT(tag);
	}

	public static enum ChannelConnection implements IStringSerializable {
		NONE,
		IN,
		OUT;

		byte index;
		ChannelConnection() {
			index = (byte)ordinal();
		}

		public byte getIndex() {
			return index;
		}

		public ChannelConnection getOpposite() {
			switch(this) {
				case IN:  return OUT;
				case OUT: return IN;
			}
			return NONE;
		}

		public ChannelConnection getNext(boolean reverse) {
			if(reverse) {
				switch(this) {
					case NONE: return IN;
					case IN:   return OUT;
					case OUT:  return NONE;
				}
			} else {
				switch(this) {
					case NONE: return OUT;
					case OUT:  return IN;
					case IN:   return NONE;
				}
			}
			// not possible
			throw new UnsupportedOperationException();
		}

		public static ChannelConnection fromIndex(int index) {
			if(index < 0 || index >= values().length) {
				return NONE;
			}

			return values()[index];
		}

		@Override
		public String getName() {
			return this.toString().toLowerCase(Locale.US);
		}

		public boolean canFlow() {
			return this != NONE;
		}

		public static boolean canFlow(ChannelConnection connection) {
			return connection != null && connection != NONE;
		}
	}
}

package knightminer.ceramics.blocks.entity;

import knightminer.ceramics.Ceramics;
import knightminer.ceramics.Registration;
import knightminer.ceramics.blocks.CisternBlock;
import knightminer.ceramics.blocks.entity.CrackableBlockEntityHandler.ICrackableBlockEntity;
import knightminer.ceramics.items.BaseClayBucketItem;
import knightminer.ceramics.network.FluidUpdatePacket;
import knightminer.ceramics.network.FluidUpdatePacket.FluidUpdater;
import knightminer.ceramics.util.tank.CisternTank;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import net.neoforged.neoforge.fluids.capability.templates.EmptyFluidHandler;
import slimeknights.mantle.network.PacketHelper;

import javax.annotation.Nullable;

public class CisternBlockEntity extends CrackableBlockEntity implements FluidUpdater {
  /** Max capacity per cistern block */
  private static final String TAG_FLUID = "fluid";
  private static final String TAG_EXTENSIONS = "extensions";

  /* Base fields */
  /** Tank instance for this cistern */
  private final CisternTank tank = new CisternTank(this);
  /** Number of extensions on this tank */
  private int extensions = 0;

  /** Fluid handler instance. Will be either the parent or {@link #tank}. */
  @Nullable
  private IFluidHandler fluidHandler;
  /** Index of the cistern for rendering and cracking */
  private int heightIndex = 0;

  public CisternBlockEntity(BlockPos pos, BlockState state, boolean crackable) {
    super(Registration.CISTERN_BLOCK_ENTITY.get(), pos, state, crackable);
  }

  public CisternBlockEntity(BlockPos pos, BlockState state) {
    this(pos, state, isCrackable(state));
  }

  /* Getters */

  /**
   * Gets the number of extensions on this cistern. Only meaningful if this is a base.
   * @return  Number of extensions
   */
  public int getExtensions() {
    return extensions;
  }

  /**
   * Gets the index of this cistern in the structure for the sake of rendering.
   * @return  Render index of cistern
   */
  public int getHeightIndex() {
    return heightIndex;
  }

  /**
   * Gets the capacity per layer of the tank.
   * @return  Capacity per layer
   */
  public int capacityPerLayer() {
    Block block = getBlockState().getBlock();
    if (Registration.PORCELAIN_CISTERN.contains(block)) {
      return 6 * FluidType.BUCKET_VOLUME;
    }
    return 4 * FluidType.BUCKET_VOLUME;
  }

  /**
   * Gets the capacity for the given height of the block entity.
   * @param height  Cistern height
   * @return  Capacity
   */
  public final int capacityFor(int height) {
    return height * capacityPerLayer();
  }


  /* Parent behavior */

  /** Invalidates our fluid handler, allowing us to return to a new parent */
  private void invalidateHandler() {
    if (fluidHandler != null) {
      fluidHandler = null;
      if (level != null) {
        level.invalidateCapabilities(worldPosition);
      }
    }
  }

  /** Invalidates capabilities for all extensions starting from the given index */
  private void invalidateExtensions() {
    if (level != null) {
      for (int i = 1; i < extensions; i++) {
        if (level.getBlockEntity(worldPosition.above(i)) instanceof CisternBlockEntity be) {
          be.invalidateHandler();
        }
      }
    }
  }

  /**
   * Transfers the relevant amount of fluid into a new base
   * @param index  Number of blocks the new base is above this block
   */
  private void transferBaseTo(Block block, int index) {
    assert level != null;
    // ensure the block above is the right type and is an extension
    BlockPos newBase = worldPosition.above(index);
    BlockState newState = level.getBlockState(newBase);
    if (newState.is(block) && newState.getValue(CisternBlock.EXTENSION) && level.getBlockEntity(newBase) instanceof CisternBlockEntity cistern) {
      // if we have fluid in the new base, extract that into that base
      FluidStack fluid = FluidStack.EMPTY;
      int amountAboveRemoved = tank.getFluidAmount() - capacityFor(index);
      if (amountAboveRemoved > 0) {
        fluid = tank.getFluid().copyWithAmount(amountAboveRemoved);
      }
      // subtract index, that is extensions below the index
      cistern.makeBase(fluid, extensions - index);
    }
  }

  /**
   * Called when a base block is broken to transfer relevant fluid to extensions
   * @param block  Block type, used since the block at the position may be air at the time of calling
   */
  public void onBroken(Block block) {
    if (extensions > 0) {
      // move fluid into the given block
      transferBaseTo(block, 1);
    }
  }

  /**
   * Attempts to merge a cistern at the given position with this one
   * @param checkPos  Position to check
   */
  public void tryMerge(BlockPos checkPos) {
    assert level != null;
    // check if there is another cistern above the new one that should also be connected
    if (level.getBlockState(checkPos).is(getBlockState().getBlock()) && level.getBlockEntity(checkPos) instanceof CisternBlockEntity cistern) {
      cistern.makeExtension(this);
    }
  }

  /**
   * Adds a new cistern as an extension to this one. Called by the new cistern when its placed in the world.
   * @param extPos  Position of extension
   */
  public void addExtension(BlockPos extPos) {
    assert level != null;

    // update extensions if the new position is outside the cistern (mostly safety)
    int newExtensions = extPos.getY() - worldPosition.getY();
    if (newExtensions > extensions) {
      extensions = newExtensions;

      // check if there is another cistern above the new one that should also be connected
      tryMerge(extPos.above());
    }
  }

  /**
   * Removes the extension at the given location. Called on the parent when a cistern extension above is removed
   * @param extPos  Extension to remove
   */
  public void removeExtension(BlockPos extPos) {
    assert extPos.getY() > worldPosition.getY();
    assert level != null;

    // index of the extension that was removed
    int removed = extPos.getY() - worldPosition.getY();
    // check if the removed position was within our current cistern
    if (removed <= extensions) {
      // if not the top, make a new base
      if (removed < extensions) {
        // transfer fluid above the removed extension
        transferBaseTo(getBlockState().getBlock(), removed + 1);
      }

      // our new extensions simply one less than the removed index
      extensions = removed - 1;
      // shrink the fluid if needed
      tank.validateAmount();
    }
  }

  /** Called by the tank when fluid is added */
  public void onFluidAdded(int amountAdded) {
    onTankChanged(false);

    // if cracking is active and the fluid cracks the block, crack all blocks impacted by the fluid
    FluidStack fluid = tank.getFluid();
    if (getCracksHandler().isActive() && level != null && !level.isClientSide() && BaseClayBucketItem.doesCrack(fluid.getFluid())) {
      int capacityPerLayer = capacityPerLayer();
      // first index needing an update
      int startIndex = (fluid.getAmount() - amountAdded + capacityPerLayer - 1) / capacityPerLayer;
      // last index needing an update
      int endIndex = (fluid.getAmount() - 1) / capacityPerLayer;
      // loop through indexes, updating them
      for (int i = startIndex; i <= endIndex; i++) {
        if (level.getBlockEntity(worldPosition.above(i)) instanceof ICrackableBlockEntity crackable) {
          crackable.getCracksHandler().fluidAdded(fluid);
        }
      }
    }
  }


  /* Extension behavior */

  /**
   * Gets the internal fluid handler for this cistern. This handler should only be used by other cisterns, prevents a long recursive chain every fluid interaction
   * @return  Fluid handler optional
   */
  public IFluidHandler getFluidHandler() {
    if (fluidHandler == null) {
      // if this is an extension, we want to find a parent handler
      if (getBlockState().getValue(CisternBlock.EXTENSION)) {
        if (level == null) {
          Ceramics.LOG.error("No world when trying to get cistern's fluid handler");
          return EmptyFluidHandler.INSTANCE;
        }
        BlockEntity te = level.getBlockEntity(worldPosition.below());
        if (te instanceof CisternBlockEntity parent) {
          // fetch handler from the parent, which will fetch from its parent and so on
          fluidHandler = parent.getFluidHandler();
          // render index is the render index of the tank below plus 1, nifty trick
          heightIndex = parent.getHeightIndex() + 1;
        } else {
          // render thread often has a few ticks with no cistern for an extension the base is broken
          if (!level.isClientSide()) {
            Ceramics.LOG.error("Missing cistern block entity below cistern extension");
          }
          return EmptyFluidHandler.INSTANCE;
        }
      } else {
        // if a base, return ourself
        fluidHandler = tank;
        heightIndex = 0;
      }
    }
    return fluidHandler;
  }

  /** Called on random block tick to update the heat */
  public void randomTick() {
    CrackableBlockEntityHandler cracksHandler = getCracksHandler();
    if (cracksHandler.isActive()) {
      if (getBlockState().getValue(CisternBlock.EXTENSION)) {
        // extensions: if fluid does not reach here, treat as empty
        FluidStack fluid = getFluidHandler().getFluidInTank(0);
        int localAmount = fluid.getAmount() - capacityFor(heightIndex);
        if (localAmount > 0) {
          cracksHandler.updateCracks(fluid.getFluid(), localAmount);
        }
      } else {
        FluidStack fluid = tank.getFluid();
        cracksHandler.updateCracks(fluid.getFluid(), fluid.getAmount());
      }
    }
  }


  /* State updating */

  /**
   * Makes the cistern a base. Capability invalidations will be run after this method.
   * @param fluid       Fluid to contain
   * @param extensions  Number of extensions
   */
  protected void makeBase(FluidStack fluid, int extensions) {
    assert level != null;
    // extra data from parent
    this.extensions = extensions;
    tank.setFluid(fluid);
    // notify client and any listening blocks of changes
    onTankChanged(true);
    heightIndex = 0;
    // update block state in world
    level.setBlockAndUpdate(worldPosition, getBlockState().setValue(CisternBlock.EXTENSION, false));
    invalidateHandler();
    // invalidate all our new extensions so they parent to us
    invalidateExtensions();
  }

  /**
   * Checks if the tank contains the given fluid
   * @param fluid  Fluid to check
   * @return  True if the fluid is contained, false otherwise
   */
  public boolean containsFluid(FluidStack fluid) {
    return tank.getFluidAmount() == 0 || FluidStack.isSameFluidSameComponents(tank.getFluid(), fluid);
  }

  /**
   * Merges the contents of another cistern into this
   * @param fluid          Fluid contents to merge in
   * @param newExtensions  New extensions to add
   */
  protected void mergeCisterns(FluidStack fluid, int newExtensions) {
    extensions += newExtensions;
    if (!fluid.isEmpty()) {
      tank.fill(fluid, FluidAction.EXECUTE);
    }
  }

  /**
   * Converts a given base cistern into an extension. Called by the base when a new cistern is added below an existing one
   * @param parent  New parent to the extension
   */
  protected void makeExtension(CisternBlockEntity parent) {
    assert level != null;

    // ensure the fluid is valid for the parent
    FluidStack fluid = tank.getFluid();
    if (fluid.isEmpty() || parent.containsFluid(fluid)) {
      // update block to extension block
      level.setBlockAndUpdate(worldPosition, getBlockState().setValue(CisternBlock.EXTENSION, true));
      // merge contents into the parent, add an extra 1 for ourself
      parent.mergeCisterns(fluid, extensions + 1);

      // invalidate ourself
      tank.setFluid(FluidStack.EMPTY);
      onTankChanged(true);
      invalidateHandler();
      // invalidate everyone above - they have a new parent
      invalidateExtensions();
      extensions = 0;
    }
  }


  /* Networking */

  /**
   * Called when the tank contents change
   */
  public void onTankChanged(boolean shouldRefreshCapabilities) {
    if (level != null && !level.isClientSide) {
      PacketHelper.sendToPlayersTrackingPosition(level, worldPosition, new FluidUpdatePacket(worldPosition, tank.getFluid(), shouldRefreshCapabilities));
    }
  }

  /**
   * Called client side to update the tank fluid
   * @param fluid                      New fluid
   * @param refreshHandlers  If true, capability handlers should be invalidated. Typically, means the block was changed from extension to base
   */
  @Override
  public void updateFluid(FluidStack fluid, boolean refreshHandlers) {
    tank.setFluid(fluid);
    if (refreshHandlers) {
      invalidateHandler();
      // cannot use invalidateExtensions as that number is not present client side
      if (level != null) {
        Block block = getBlockState().getBlock();
        int max = level.getMaxBuildHeight();
        for (int i = 1; i < max; i++) {
          BlockPos pos = worldPosition.above(i);
          BlockState state = level.getBlockState(pos);
          // if its an extension, need to invaliate it
          if (state.is(block) && state.getValue(CisternBlock.EXTENSION)) {
            if (level.getBlockEntity(pos) instanceof CisternBlockEntity cistern) {
              cistern.invalidateHandler();
            }
          } else {
            // not an extension? that is the end of the tower
            break;
          }
        }
      }
    }
  }

  @Override
  protected boolean shouldSyncOnUpdate() {
    return true;
  }


  /* Serialization */

  @Override
  protected void loadAdditional(CompoundTag tags, HolderLookup.Provider registries) {
    super.loadAdditional(tags, registries);
    extensions = tags.getInt(TAG_EXTENSIONS);
    if (tags.contains(TAG_FLUID, Tag.TAG_COMPOUND)) {
      tank.readFromNBT(registries, tags.getCompound(TAG_FLUID));
    }
  }

  @Override
  protected void saveSynced(CompoundTag tags, HolderLookup.Provider registries) {
    super.saveSynced(tags, registries);
    if (tank.getFluidAmount() > 0) {
      tags.put(TAG_FLUID, tank.writeToNBT(registries));
    }
  }

  @Override
  public void saveAdditional(CompoundTag tags, HolderLookup.Provider registries) {
    super.saveAdditional(tags, registries);
    tags.putInt(TAG_EXTENSIONS, extensions);
  }
}

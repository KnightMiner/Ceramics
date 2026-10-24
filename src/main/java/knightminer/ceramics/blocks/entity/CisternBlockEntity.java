package knightminer.ceramics.blocks.entity;

import knightminer.ceramics.Ceramics;
import knightminer.ceramics.Registration;
import knightminer.ceramics.blocks.CisternBlock;
import knightminer.ceramics.blocks.entity.CrackableBlockEntityHandler.ICrackableBlockEntity;
import knightminer.ceramics.items.BaseClayBucketItem;
import knightminer.ceramics.network.CeramicsNetwork;
import knightminer.ceramics.network.CisternFluidUpdatePacket;
import knightminer.ceramics.network.CisternSizeUpdatePacket;
import knightminer.ceramics.util.tank.CisternTank;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fluids.capability.templates.EmptyFluidHandler;
import org.jetbrains.annotations.ApiStatus.Internal;
import slimeknights.mantle.block.entity.MantleBlockEntity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CisternBlockEntity extends MantleBlockEntity implements ICrackableBlockEntity {
  private static final String TAG_FLUID = "fluid";
  private static final String TAG_EXTENSIONS = "extensions";

  /* Base fields */
  /** Tank instance for this cistern */
  private final CisternTank tank = new CisternTank(this);
  /** Number of extensions on this tank */
  private int extensions = 0;

  /** Handler for internal use, passed between cisterns. Will be either the parent handler or {@link #tank} */
  @Nullable
  private IFluidHandler internalHandler;
  /** Handler exposed to Forge capabilities. Lazy optional referencing {@link #internalHandler}. Invalidated on parent change. */
  @Nullable
  private LazyOptional<IFluidHandler> publicHandler;
  /** Index of the cistern for rendering */
  private int renderIndex = 0;

  /** Cracking logic */
  private final CrackableBlockEntityHandler cracksHandler;

  public CisternBlockEntity(BlockPos pos, BlockState state, boolean crackable) {
    super(Registration.CISTERN_BLOCK_ENTITY.get(), pos, state);
    cracksHandler = new CrackableBlockEntityHandler(this, crackable);
  }

  public CisternBlockEntity(BlockPos pos, BlockState state) {
    this(pos, state, false);
  }

  /* Getters */

  /**
   * Gets the number of extensions on this cistern
   * @return  Number of extensions
   */
  public int getExtensions() {
    return extensions;
  }

  /**
   * Gets the index of this cistern in the structure for the sake of rendering
   * @return  Render index of cistern
   */
  public int getRenderIndex() {
    return renderIndex;
  }

  /**
   * Gets the capacity per layer of the tile entity
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
   * Gets the capacity for the given height of the tile entity
   * @param height  Cistern height
   * @return  Capacity
   */
  public final int capacityFor(int height) {
    return height * capacityPerLayer();
  }

  @Override
  public CrackableBlockEntityHandler getCracksHandler() {
    return cracksHandler;
  }

  @Nonnull
  @Override
  public ModelData getModelData() {
    return cracksHandler.getModelData();
  }


  /* Parent behavior */

  /** Invalidates our fluid handler, allowing us to return to a new parent */
  private void invalidateHandler() {
    // no need to do anything with the public handler, it still references internal even if internal is not currently present
    // make internal invalid for other cisterns
    if (internalHandler != null) {
      internalHandler = null;
    }
  }

  /** Invalidates capabilities for all extensions starting from the given index */
  private static void invalidateExtensions(Level level, BlockPos pos, int start, int end, boolean clearContents) {
    for (int i = start; i <= end; i++) {
      if (level.getBlockEntity(pos.above(i)) instanceof CisternBlockEntity be) {
        be.invalidateHandler();
        // if requested, clear out tank and extensions. Only used client side
        if (clearContents && be.getBlockState().getValue(CisternBlock.EXTENSION)) {
          be.tank.setFluid(FluidStack.EMPTY);
          be.extensions = 0;
        }
      }
    }
  }

  /** Invalidates capabilities for all extensions starting from the given index */
  private void invalidateExtensions() {
    if (level != null) {
      invalidateExtensions(level, worldPosition, 1, extensions, false);
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
        fluid = new FluidStack(tank.getFluid(), amountAboveRemoved);
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
   * @return true if it merged with the above cistern.
   */
  public boolean tryMerge(BlockPos checkPos) {
    assert level != null;
    // check if there is another cistern above the new one that should also be connected
    if (level.getBlockState(checkPos).is(getBlockState().getBlock()) && level.getBlockEntity(checkPos) instanceof CisternBlockEntity cistern) {
      cistern.makeExtension(this);
      return true;
    }
    return false;
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
      if (!tryMerge(extPos.above())) {
        onSizeChange();
      }
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
      if (!tank.validateAmount()) {
        onSizeChange();
      }
    }
  }

  /** Called by the tank when fluid is added */
  public void onFluidAdded(int amountAdded) {
    onTankChanged(false);

    // if cracking is active and the fluid cracks the block, crack all blocks impacted by the fluid
    FluidStack fluid = tank.getFluid();
    if (cracksHandler.isActive() && level != null && !level.isClientSide() && BaseClayBucketItem.doesCrack(fluid.getFluid())) {
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
  @Internal
  public IFluidHandler getInternalHandler() {
    if (internalHandler == null) {
      // if this is an extension, we want to find a parent handler
      if (getBlockState().getValue(CisternBlock.EXTENSION)) {
        if (level == null) {
          Ceramics.LOG.error("No world when trying to get cistern's fluid handler");
          return EmptyFluidHandler.INSTANCE;
        }
        BlockEntity te = level.getBlockEntity(worldPosition.below());
        if (te instanceof CisternBlockEntity parent) {
          // fetch handler from the parent, which will fetch from its parent and so on
          IFluidHandler parentHandler = parent.getInternalHandler();
          // don't cache an empty parent
          if (parentHandler == EmptyFluidHandler.INSTANCE) {
            return EmptyFluidHandler.INSTANCE;
          }
          internalHandler = parentHandler;
          // render index is the render index of the tank below plus 1, nifty trick
          renderIndex = parent.getRenderIndex() + 1;
        } else {
          if (!level.isClientSide()) {
            // render thread often has a few ticks with no cistern for an extension the base is broken
            Ceramics.LOG.error("Missing cistern tile entity below cistern extension");
          }
          return EmptyFluidHandler.INSTANCE;
        }
      } else {
        // if a base, return ourself
        internalHandler = tank;
        renderIndex = 0;
      }
    }
    return internalHandler;
  }

  /**
   * Gets the public fluid handler associated with this cistern
   * @return  Fluid handler optional
   */
  public LazyOptional<IFluidHandler> getPublicHandler() {
    if (publicHandler == null) {
      publicHandler = LazyOptional.of(this::getInternalHandler);
    }
    return publicHandler;
  }

  /** Called on random block tick to update the heat */
  public void randomTick() {
    if (cracksHandler.isActive()) {
      if (getBlockState().getValue(CisternBlock.EXTENSION)) {
        // extensions: if fluid does not reach here, treat as empty
        FluidStack fluid = getInternalHandler().getFluidInTank(0);
        int localAmount = fluid.getAmount() - capacityFor(renderIndex);
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
   * Makes the barrel a base
   * @param fluid       Fluid to contain
   * @param extensions  Number of extensions
   */
  protected void makeBase(FluidStack fluid, int extensions) {
    assert level != null;
    // extra data from parent
    this.extensions = extensions;
    tank.setFluid(fluid);
    // update block state in world
    level.setBlockAndUpdate(worldPosition, getBlockState().setValue(CisternBlock.EXTENSION, false));
    invalidateHandler();
    // invalidate all our new extensions so they parent to us
    invalidateExtensions();
    // notify client of changes
    onTankChanged(true);
  }

  /**
   * Checks if the tank contains the given fluid
   * @param fluid  Fluid to check
   * @return  True if the fluid is contained, false otherwise
   */
  public boolean containsFluid(FluidStack fluid) {
    return tank.getFluidAmount() == 0 || tank.getFluid().isFluidEqual(fluid);
  }

  /**
   * Merges the contents of another cistern into this
   * @param fluid          Fluid contents to merge in
   * @param newExtensions  New extensions to add
   */
  protected void mergeCisterns(FluidStack fluid, int newExtensions) {
    extensions += newExtensions;
    // if given a new fluid, fill it into the new taller tank
    if (!fluid.isEmpty()) {
      tank.fill(fluid, FluidAction.EXECUTE);
    } else {
      // otherwise just update size client side
      onSizeChange();
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
      invalidateHandler();
      // invalidate everyone above - they have a new parent
      invalidateExtensions();
      extensions = 0;
      // no need to sync, the parent took care of it
    }
  }


  /* Capabilities */

  @Nonnull
  @Override
  public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
    if (cap == ForgeCapabilities.FLUID_HANDLER) {
      return getPublicHandler().cast();
    }
    return super.getCapability(cap, side);
  }

  @Override
  public void invalidateCaps() {
    super.invalidateCaps();
    invalidateHandler();
  }


  /* Networking */

  /**
   * Called when the tank contents change
   */
  public void onTankChanged(boolean refreshHandler) {
    if(level != null && !level.isClientSide) {
      setChangedFast();
      CeramicsNetwork.getInstance().sendToClientsAround(new CisternFluidUpdatePacket(worldPosition, tank.getFluid(), refreshHandler, extensions), level, worldPosition);
    }
  }

  /** Called when the size changes without a tank contents change */
  public void onSizeChange() {
    if (level != null && !level.isClientSide) {
      setChangedFast();
      CeramicsNetwork.getInstance().sendToClientsAround(new CisternSizeUpdatePacket(worldPosition, extensions), level, worldPosition);
    }
  }

  /**
   * Called client side to update the tank fluid
   *
   * @param fluid          New fluid
   * @param refreshHandler If true, invalidates our handler
   * @param extensions     Number of extensions in the serverside tank
   */
  public void updateFluid(FluidStack fluid, boolean refreshHandler, int extensions) {
    tank.setFluid(fluid);
    if (refreshHandler) {
      invalidateHandler();
    }
    updateSize(extensions);
  }

  /**
   * Called client side to update the tank size
   * @param extensions  Number of extensions in the serverside tank
   */
  public void updateSize(int extensions) {
    if (level != null && this.extensions != extensions) {
      int oldExtensions = this.extensions;
      this.extensions = extensions;
      // if we added new extensions, refresh them
      // no need to update if we lost extensions, those will be handled by a separate packet
      if (oldExtensions < extensions) {
        invalidateExtensions(level, worldPosition, oldExtensions, extensions, true);
      }
    }
  }

  @Override
  protected boolean shouldSyncOnUpdate() {
    return true;
  }


  /* Serialization */

  @Override
  public void load(CompoundTag tags) {
    super.load(tags);
    extensions = tags.getInt(TAG_EXTENSIONS);
    if (tags.contains(TAG_FLUID, Tag.TAG_COMPOUND)) {
      tank.readFromNBT(tags.getCompound(TAG_FLUID));
    }
    cracksHandler.readNBT(getBlockState(), tags);
  }

  @Override
  protected void saveSynced(CompoundTag compound) {
    super.saveSynced(compound);
    if (tank.getFluidAmount() > 0) {
      compound.put(TAG_FLUID, tank.writeToNBT());
    }
    cracksHandler.writeNBT(compound);
    compound.putInt(TAG_EXTENSIONS, extensions);
  }
}

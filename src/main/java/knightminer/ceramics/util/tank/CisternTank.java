package knightminer.ceramics.util.tank;

import knightminer.ceramics.tileentity.CisternTileEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;

import javax.annotation.Nonnull;

/**
 * Handles all tank behaviors for the cistern
 */
public class CisternTank implements IFluidHandler, IFluidTank {
  public static final int BASE_CAPACITY = FluidAttributes.BUCKET_VOLUME * 4;

  /** Current fluid in tank */
  protected FluidStack fluid = FluidStack.EMPTY;
  /** Relevant tile entity */
  protected final CisternTileEntity parent;

  public CisternTank(CisternTileEntity parent) {
    this.parent = parent;
  }

  @Override
  public boolean isFluidValid(FluidStack stack) {
    return !stack.getFluid().getAttributes().isGaseous(stack);
  }

  @Override
  public FluidStack getFluid() {
    return fluid;
  }

  /**
   * Gets the capacity for the given height
   * @param height  Height to check
   * @return  Capacity
   */
  public static int capacityFor(int height) {
    return height * BASE_CAPACITY;
  }

  @Override
  public int getCapacity() {
    // 1 for the base, plus 1 per extension
    return capacityFor(parent.getExtensions() + 1);
  }

  public void validateAmount() {
    int capacity = getCapacity();
    int extra = fluid.getAmount() - getCapacity();
    if (extra > 0) {
      drain(extra, FluidAction.EXECUTE);
    }
  }

  /**
   * Updates the stored fluid stack
   * @param stack  Stored fluid
   */
  public void setFluid(FluidStack stack) {
    this.fluid = stack;
  }

  @Override
  public int getFluidAmount() {
    return fluid.getAmount();
  }

  @Override
  public int fill(FluidStack resource, FluidAction action) {
    if (resource.isEmpty() || !isFluidValid(resource)) {
      return 0;
    }
    int capacity = getCapacity();
    // simulate: just do the math
    if (action.simulate()) {
      if (fluid.isEmpty()) {
        return Math.min(capacity, resource.getAmount());
      }
      if (!fluid.isFluidEqual(resource)) {
        return 0;
      }
      return Math.min(capacity - fluid.getAmount(), resource.getAmount());
    }
    // no existing fluid
    if (fluid.isEmpty()) {
      fluid = new FluidStack(resource, Math.min(capacity, resource.getAmount()));
      parent.onTankChanged(false);
      return fluid.getAmount();
    }
    // wrong type
    if (!fluid.isFluidEqual(resource)) {
      return 0;
    }
    // limit based on space
    int filled = capacity - fluid.getAmount();
    if (resource.getAmount() < filled) {
      fluid.grow(resource.getAmount());
      filled = resource.getAmount();
    }
    else {
      fluid.setAmount(capacity);
    }
    // updates
    if (filled > 0) {
      parent.onTankChanged(false);
    }
    return filled;
  }

  @Override
  public FluidStack drain(FluidStack resource, FluidAction action) {
    if (resource.isEmpty() || !resource.isFluidEqual(fluid)) {
      return FluidStack.EMPTY;
    }
    return drain(resource.getAmount(), action);
  }

  @Override
  public FluidStack drain(int maxDrain, FluidAction action) {
    int drained = maxDrain;
    if (fluid.getAmount() < drained) {
      drained = fluid.getAmount();
    }
    FluidStack stack = new FluidStack(fluid, drained);
    if (action.execute() && drained > 0) {
      fluid.shrink(drained);
      parent.onTankChanged(false);
    }
    return stack;
  }


  /* NBT */

  /**
   * Updates the tank contents from NBT
   * @param nbt  NBT
   */
  public void readFromNBT(CompoundNBT nbt) {
    FluidStack fluid = FluidStack.loadFluidStackFromNBT(nbt);
    setFluid(fluid);
  }

  /**
   * Writes the tank contents to NBT
   * @return  Contents as NBT
   */
  public CompoundNBT writeToNBT() {
    return fluid.writeToNBT(new CompoundNBT());
  }


  /* Required interface methods */

  @Override
  public int getTanks() {
    return 1;
  }

  @Override
  public FluidStack getFluidInTank(int tank) {
    return getFluid();
  }

  @Override
  public int getTankCapacity(int tank) {
    return getCapacity();
  }

  @Override
  public boolean isFluidValid(int tank, @Nonnull FluidStack stack) {
    return isFluidValid(stack);
  }
}
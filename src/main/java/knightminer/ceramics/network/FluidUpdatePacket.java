package knightminer.ceramics.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraftforge.fluids.FluidStack;
import slimeknights.mantle.network.packet.IThreadsafePacket;

/**
 * Base class for fluid updating behavior
 */
public abstract class FluidUpdatePacket implements IThreadsafePacket {
	protected final BlockPos pos;
	protected final FluidStack fluid;
	public FluidUpdatePacket(BlockPos pos, FluidStack fluid) {
		this.pos = pos;
		this.fluid = fluid;
	}

	public FluidUpdatePacket(FriendlyByteBuf buffer) {
		this.pos = buffer.readBlockPos();
		this.fluid = buffer.readFluidStack();
	}

	@Override
	public void encode(FriendlyByteBuf buffer) {
		buffer.writeBlockPos(pos);
		buffer.writeFluidStack(fluid);
	}
}

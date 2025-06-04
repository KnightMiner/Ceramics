package knightminer.ceramics.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.network.NetworkEvent.Context;
import slimeknights.mantle.client.SafeClientAccess;
import slimeknights.mantle.network.packet.IThreadsafePacket;
import slimeknights.mantle.util.BlockEntityHelper;

/**
 * Base class for fluid updating behavior
 */
public record FluidUpdatePacket(BlockPos pos, FluidStack fluid, boolean data) implements IThreadsafePacket {
	public FluidUpdatePacket(FriendlyByteBuf buffer) {
		this(buffer.readBlockPos(), buffer.readFluidStack(), buffer.readBoolean());
	}

	@Override
	public void encode(FriendlyByteBuf buffer) {
		buffer.writeBlockPos(pos);
		buffer.writeFluidStack(fluid);
		buffer.writeBoolean(data);
	}

	@Override
	public void handleThreadsafe(Context context) {
		HandleClient.handle(this);
	}

	/** Helper for a block entity to update a fluid */
	public interface FluidUpdater {
		/** Updates the fluid from the server */
		void updateFluid(FluidStack fluid, boolean data);
	}

	private static class HandleClient {
		private static void handle(FluidUpdatePacket packet) {
			Level level = SafeClientAccess.getLevel();
			if (BlockEntityHelper.isBlockLoaded(level, packet.pos) && level.getBlockEntity(packet.pos) instanceof FluidUpdater updater) {
				updater.updateFluid(packet.fluid, packet.data);
			}
		}
	}
}

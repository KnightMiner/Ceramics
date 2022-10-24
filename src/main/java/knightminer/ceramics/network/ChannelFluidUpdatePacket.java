package knightminer.ceramics.network;

import knightminer.ceramics.blocks.entity.ChannelBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.network.NetworkEvent.Context;
import slimeknights.mantle.util.BlockEntityHelper;

/** Packet sent when the fluid contained in the channel changes */
public class ChannelFluidUpdatePacket extends FluidUpdatePacket {
	public ChannelFluidUpdatePacket(BlockPos pos, FluidStack fluid) {
		super(pos, fluid);
	}

	public ChannelFluidUpdatePacket(FriendlyByteBuf buffer) {
		super(buffer);
	}

	@Override
	public void handleThreadsafe(Context context) {
		HandleClient.handle(this);
	}

	private static class HandleClient {
		private static void handle(ChannelFluidUpdatePacket packet) {
			BlockEntityHelper.get(ChannelBlockEntity.class, Minecraft.getInstance().level, packet.pos).ifPresent(te -> te.updateFluid(packet.fluid));
		}
	}
}

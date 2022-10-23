package knightminer.ceramics.network;

import knightminer.ceramics.tileentity.ChannelTileEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.network.NetworkEvent.Context;
import slimeknights.mantle.util.TileEntityHelper;

/** Packet sent when the fluid contained in the channel changes */
public class ChannelFluidUpdatePacket extends FluidUpdatePacket {
	public ChannelFluidUpdatePacket(BlockPos pos, FluidStack fluid) {
		super(pos, fluid);
	}

	public ChannelFluidUpdatePacket(PacketBuffer buffer) {
		super(buffer);
	}

	@Override
	public void handleThreadsafe(Context context) {
		HandleClient.handle(this);
	}

	private static class HandleClient {
		private static void handle(ChannelFluidUpdatePacket packet) {
			TileEntityHelper.getTile(ChannelTileEntity.class, Minecraft.getInstance().level, packet.pos).ifPresent(te -> te.updateFluid(packet.fluid));
		}
	}
}

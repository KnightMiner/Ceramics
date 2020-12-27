package knightminer.ceramics.network;

import knightminer.ceramics.tileentity.ChannelTileEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.fluid.Fluid;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent.Context;
import net.minecraftforge.registries.ForgeRegistries;
import slimeknights.mantle.network.packet.IThreadsafePacket;
import slimeknights.mantle.util.TileEntityHelper;

/** Packet sent when the fluid contained in the channel changes */
public class ChannelFluidUpdatePacket implements IThreadsafePacket {
	private final BlockPos pos;
	private final Fluid fluid;

	public ChannelFluidUpdatePacket(BlockPos pos, Fluid fluid) {
		this.pos = pos;
		this.fluid = fluid;
	}

	public ChannelFluidUpdatePacket(PacketBuffer buffer) {
		this.pos = buffer.readBlockPos();
		this.fluid = buffer.readRegistryIdUnsafe(ForgeRegistries.FLUIDS);
	}

	@Override
	public void encode(PacketBuffer buffer) {
		buffer.writeBlockPos(pos);
		buffer.writeRegistryIdUnsafe(ForgeRegistries.FLUIDS, fluid);
	}

	@Override
	public void handleThreadsafe(Context context) {
		HandleClient.handle(this);
	}

	private static class HandleClient {
		private static void handle(ChannelFluidUpdatePacket packet) {
			TileEntityHelper.getTile(ChannelTileEntity.class, Minecraft.getInstance().world, packet.pos).ifPresent(te -> te.updateFluid(packet.fluid));
		}
	}
}

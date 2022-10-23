package knightminer.ceramics.network;

import knightminer.ceramics.tileentity.CrackableTileEntityHandler.ICrackableTileEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent.Context;
import slimeknights.mantle.network.packet.IThreadsafePacket;
import slimeknights.mantle.util.BlockEntityHelper;

public class CrackableCrackPacket implements IThreadsafePacket {
	private final BlockPos pos;
	private final int cracks;

	public CrackableCrackPacket(BlockPos pos, int cracks) {
		this.pos = pos;
		this.cracks = cracks;
	}

	public CrackableCrackPacket(FriendlyByteBuf buffer) {
		this.pos = buffer.readBlockPos();
		this.cracks = buffer.readVarInt();
	}

	@Override
	public void encode(FriendlyByteBuf buffer) {
		buffer.writeBlockPos(pos);
		buffer.writeVarInt(cracks);
	}

	@Override
	public void handleThreadsafe(Context context) {
		HandleClient.handle(this);
	}

	private static class HandleClient {
		private static void handle(CrackableCrackPacket packet) {
			BlockEntityHelper.get(ICrackableTileEntity.class, Minecraft.getInstance().level, packet.pos).ifPresent(te -> te.getCracksHandler().setCracks(packet.cracks));
		}
	}
}

package knightminer.ceramics.network;

import knightminer.ceramics.blocks.entity.CrackableBlockEntityHandler.ICrackableBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent.Context;
import slimeknights.mantle.client.SafeClientAccess;
import slimeknights.mantle.network.packet.IThreadsafePacket;
import slimeknights.mantle.util.BlockEntityHelper;

public record CrackableCrackPacket(BlockPos pos, int cracks) implements IThreadsafePacket {
	public CrackableCrackPacket(FriendlyByteBuf buffer) {
		this(buffer.readBlockPos(), buffer.readVarInt());
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
			Level level = SafeClientAccess.getLevel();
			if (BlockEntityHelper.isBlockLoaded(level, packet.pos) && level.getBlockEntity(packet.pos) instanceof ICrackableBlockEntity crackable) {
				crackable.getCracksHandler().setCracks(packet.cracks);
			}
		}
	}
}

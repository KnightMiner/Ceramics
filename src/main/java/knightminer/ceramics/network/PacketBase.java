package knightminer.ceramics.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public abstract class PacketBase implements IMessage {
	protected static BlockPos readPos(ByteBuf buf) {
		int x = buf.readInt();
		int y = buf.readInt();
		int z = buf.readInt();
		return new BlockPos(x, y, z);
	}

	protected static void writePos(BlockPos pos, ByteBuf buf) {
		buf.writeInt(pos.getX());
		buf.writeInt(pos.getY());
		buf.writeInt(pos.getZ());
	}

	public abstract void handleClient(NetHandlerPlayClient handler);
}
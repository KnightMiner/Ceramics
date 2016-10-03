package knightminer.ceramics.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.util.IThreadListener;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public abstract class PacketBase implements IMessage {

	protected BlockPos readPos(ByteBuf buf) {
		int x = buf.readInt();
		int y = buf.readInt();
		int z = buf.readInt();
		return new BlockPos(x, y, z);
	}

	protected void writePos(BlockPos pos, ByteBuf buf) {
		buf.writeInt(pos.getX());
		buf.writeInt(pos.getY());
		buf.writeInt(pos.getZ());
	}

	protected static IThreadListener getMainThread(MessageContext ctx) {
		if(ctx.side.isClient()) {
			return Minecraft.getMinecraft();
		}
		else {
			return (WorldServer) ctx.getServerHandler().playerEntity.worldObj;
		}
	}
}

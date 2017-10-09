package knightminer.ceramics.network;

import io.netty.buffer.ByteBuf;
import knightminer.ceramics.tileentity.TileChannel;
import net.minecraft.client.Minecraft;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class ChannelConnectionPacket extends PacketBase {
	protected BlockPos pos;
	protected EnumFacing side;
	protected boolean connect;

	public ChannelConnectionPacket() {}

	public ChannelConnectionPacket(BlockPos pos, EnumFacing side, boolean connect) {
		this.pos = pos;
		this.side = side;
		this.connect = connect;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		pos = readPos(buf);
		side = EnumFacing.getFront(buf.readByte());
		connect = buf.readBoolean();
	}

	@Override
	public void toBytes(ByteBuf buf) {
		writePos(pos, buf);
		buf.writeByte(side.getIndex());
		buf.writeBoolean(connect);
	}

	public static class ChannelConnectionsHandler implements IMessageHandler<ChannelConnectionPacket, IMessage> {
		@Override
		public IMessage onMessage(final ChannelConnectionPacket message, MessageContext ctx) {
			getMainThread(ctx).addScheduledTask(() -> {
				TileEntity te = Minecraft.getMinecraft().world.getTileEntity(message.pos);
				if(te instanceof TileChannel) {
					((TileChannel) te).updateConnection(message.side, message.connect);
				}
			});
			return null;
		}

	}
}

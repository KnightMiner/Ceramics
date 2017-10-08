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

public class ChannelFlowPacket extends PacketBase {
	protected BlockPos pos;
	protected EnumFacing side;
	protected boolean flow;

	public ChannelFlowPacket() {}

	public ChannelFlowPacket(BlockPos pos, EnumFacing side, boolean flow) {
		this.pos = pos;
		this.side = side;
		this.flow = flow;

	}

	@Override
	public void fromBytes(ByteBuf buf) {
		pos = readPos(buf);
		side = EnumFacing.getFront(buf.readByte());
		flow = buf.readBoolean();
	}

	@Override
	public void toBytes(ByteBuf buf) {
		writePos(pos, buf);
		buf.writeByte(side.getIndex());
		buf.writeBoolean(flow);
	}

	public static class ChannelConnectionsHandler implements IMessageHandler<ChannelFlowPacket, IMessage> {
		@Override
		public IMessage onMessage(final ChannelFlowPacket message, MessageContext ctx) {
			getMainThread(ctx).addScheduledTask(() -> {
				TileEntity te = Minecraft.getMinecraft().world.getTileEntity(message.pos);
				if(te instanceof TileChannel) {
					((TileChannel) te).updateFlow(message.side, message.flow);
				}
			});
			return null;
		}

	}
}

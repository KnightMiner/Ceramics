package knightminer.ceramics.network;

import io.netty.buffer.ByteBuf;
import knightminer.ceramics.tileentity.TileChannel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

public class ChannelFlowPacket extends PacketBase {
	private BlockPos pos;
	private EnumFacing side;
	private boolean flow;
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

	@Override
	public void handleClient(NetHandlerPlayClient handler) {
		TileEntity te = Minecraft.getMinecraft().world.getTileEntity(pos);
		if(te instanceof TileChannel) {
			((TileChannel) te).updateFlow(side, flow);
		}
	}
}

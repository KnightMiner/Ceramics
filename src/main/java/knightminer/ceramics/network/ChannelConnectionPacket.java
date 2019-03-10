package knightminer.ceramics.network;

import io.netty.buffer.ByteBuf;
import knightminer.ceramics.tileentity.TileChannel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

public class ChannelConnectionPacket extends PacketBase {
	private BlockPos pos;
	private EnumFacing side;
	private boolean connect;
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

	@Override
	public void handleClient(NetHandlerPlayClient handler) {
		TileEntity te = Minecraft.getMinecraft().world.getTileEntity(pos);
		if(te instanceof TileChannel) {
			((TileChannel) te).updateConnection(side, connect);
		}
	}
}

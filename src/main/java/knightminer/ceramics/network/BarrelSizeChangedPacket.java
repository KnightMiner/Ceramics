package knightminer.ceramics.network;

import io.netty.buffer.ByteBuf;
import knightminer.ceramics.tileentity.TileBarrel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

public class BarrelSizeChangedPacket extends PacketBase {

	private BlockPos pos;
	private int capacity, height;
	public BarrelSizeChangedPacket() {}
	public BarrelSizeChangedPacket(BlockPos pos, int capacity, int height) {
		this.pos = pos;
		this.capacity = capacity;
		this.height = height;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		pos = readPos(buf);
		capacity = buf.readInt();
		height = buf.readInt();
	}

	@Override
	public void toBytes(ByteBuf buf) {
		writePos(pos, buf);
		buf.writeInt(capacity);
		buf.writeInt(height);
	}

	@Override
	public void handleClient(NetHandlerPlayClient handler) {
		TileEntity te = Minecraft.getMinecraft().world.getTileEntity(pos);
		if(te instanceof TileBarrel) {
			((TileBarrel) te).updateSize(capacity, height);
		}
	}
}

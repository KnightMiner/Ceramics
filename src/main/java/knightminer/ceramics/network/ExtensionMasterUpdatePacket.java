package knightminer.ceramics.network;

import io.netty.buffer.ByteBuf;
import knightminer.ceramics.tileentity.TileBarrelExtension;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

public class ExtensionMasterUpdatePacket extends PacketBase {
	private BlockPos pos;
	private BlockPos master;
	public ExtensionMasterUpdatePacket() {}
	public ExtensionMasterUpdatePacket(BlockPos pos, BlockPos master) {
		this.pos = pos;
		this.master = master;
	}

	@Override
	public void toBytes(ByteBuf buf) {
		writePos(pos, buf);
		if(master != null) {
			// set flag to say we have a position
			buf.writeBoolean(true);
			writePos(master, buf);
		} else {
			buf.writeBoolean(false);
		}
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		pos = readPos(buf);
		// check if the position flag is set, if so there is more data
		if (buf.readBoolean()) {
			master = readPos(buf);
		}
	}

	@Override
	public void handleClient(NetHandlerPlayClient handler) {
		TileEntity te = Minecraft.getMinecraft().world.getTileEntity(pos);
		if(te instanceof TileBarrelExtension) {
			((TileBarrelExtension) te).setMaster(master);
		}
	}
}

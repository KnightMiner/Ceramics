package knightminer.ceramics.network;

import io.netty.buffer.ByteBuf;
import knightminer.ceramics.tileentity.TileBarrel;
import net.minecraft.client.Minecraft;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class BarrelSizeChangedPacket extends PacketBase {

	public BlockPos pos;
	public int capacity, height;

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

	public static class BarrelCapacityChangedHandler implements IMessageHandler<BarrelSizeChangedPacket, IMessage> {

		@Override
		public IMessage onMessage(final BarrelSizeChangedPacket message, MessageContext ctx) {
			getMainThread(ctx).addScheduledTask(new Runnable() {
				@Override
				public void run() {
					TileEntity te = Minecraft.getMinecraft().world.getTileEntity(message.pos);
					if(te instanceof TileBarrel) {
						((TileBarrel) te).updateSize(message.capacity, message.height);
					}
				}
			});
			return null;
		}

	}
}

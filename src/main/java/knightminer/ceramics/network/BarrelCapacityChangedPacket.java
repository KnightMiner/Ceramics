package knightminer.ceramics.network;

import io.netty.buffer.ByteBuf;
import knightminer.ceramics.tileentity.TileBarrel;
import net.minecraft.client.Minecraft;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class BarrelCapacityChangedPacket extends PacketBase {

	public BlockPos pos;
	public int capacity;

	public BarrelCapacityChangedPacket() {}

	public BarrelCapacityChangedPacket(BlockPos pos, int capacity) {
		this.pos = pos;
		this.capacity = capacity;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		pos = readPos(buf);
		capacity = buf.readInt();
	}

	@Override
	public void toBytes(ByteBuf buf) {
		writePos(pos, buf);
		buf.writeInt(capacity);
	}

	public static class BarrelCapacityChangedHandler implements IMessageHandler<BarrelCapacityChangedPacket, IMessage> {

		@Override
		public IMessage onMessage(final BarrelCapacityChangedPacket message, MessageContext ctx) {
			getMainThread(ctx).addScheduledTask(new Runnable() {
				@Override
				public void run() {
					TileEntity te = Minecraft.getMinecraft().world.getTileEntity(message.pos);
					if(te instanceof TileBarrel) {
						((TileBarrel) te).updateCapacityTo(message.capacity);
					}
				}
			});
			return null;
		}

	}
}

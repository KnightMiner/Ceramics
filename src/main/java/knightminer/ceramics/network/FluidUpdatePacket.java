package knightminer.ceramics.network;

import io.netty.buffer.ByteBuf;
import knightminer.ceramics.library.tank.IFluidUpdateReciever;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.network.ByteBufUtils;

public class FluidUpdatePacket extends PacketBase {
	private BlockPos pos;
	private FluidStack fluid;
	public FluidUpdatePacket() {}
	public FluidUpdatePacket(BlockPos pos, FluidStack fluid) {
		this.pos = pos;
		this.fluid = fluid;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		pos = readPos(buf);
		NBTTagCompound tag = ByteBufUtils.readTag(buf);
		fluid = FluidStack.loadFluidStackFromNBT(tag);
	}

	@Override
	public void toBytes(ByteBuf buf) {
		writePos(pos, buf);
		NBTTagCompound tag = new NBTTagCompound();
		if(fluid != null) {
			fluid.writeToNBT(tag);
		}
		ByteBufUtils.writeTag(buf, tag);
	}

	@Override
	public void handleClient(NetHandlerPlayClient handler) {
		TileEntity te = Minecraft.getMinecraft().world.getTileEntity(pos);
		if(te instanceof IFluidUpdateReciever) {
			((IFluidUpdateReciever) te).updateFluidTo(fluid);
		}
	}
}

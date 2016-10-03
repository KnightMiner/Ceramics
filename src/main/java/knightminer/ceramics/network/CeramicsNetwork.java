package knightminer.ceramics.network;

import knightminer.ceramics.Ceramics;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class CeramicsNetwork {
	public static SimpleNetworkWrapper INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel(Ceramics.modID);

	private static int id = 0;
	public static void registerPackets() {
		INSTANCE.registerMessage(BarrelFluidUpdatePacket.BarrelFluidUpdateHandler.class, BarrelFluidUpdatePacket.class, id++, Side.CLIENT);
		INSTANCE.registerMessage(BarrelCapacityChangedPacket.BarrelCapacityChangedHandler.class, BarrelCapacityChangedPacket.class, id++, Side.CLIENT);
	}
	public static void sendToAllAround(World world, BlockPos pos, PacketBase message) {
		INSTANCE.sendToAllAround(message, new TargetPoint(world.provider.getDimension(), pos.getX(), pos.getY(), pos.getZ(), 64));
	}
}

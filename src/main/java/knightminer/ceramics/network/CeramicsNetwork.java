package knightminer.ceramics.network;

import knightminer.ceramics.Ceramics;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class CeramicsNetwork {
	public static SimpleNetworkWrapper INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel(Ceramics.modID);

	private static int id = 0;
	public static void registerPackets() {
		INSTANCE.registerMessage(FluidUpdatePacket.FluidUpdateHandler.class, FluidUpdatePacket.class, id++, Side.CLIENT);
		INSTANCE.registerMessage(BarrelSizeChangedPacket.BarrelCapacityChangedHandler.class, BarrelSizeChangedPacket.class, id++, Side.CLIENT);
	}

	public static void sendToAllAround(World world, BlockPos pos, PacketBase message) {
		INSTANCE.sendToAllAround(message, new TargetPoint(world.provider.getDimension(), pos.getX(), pos.getY(), pos.getZ(), 64));
	}

	public static void sendToClients(WorldServer world, BlockPos pos, PacketBase packet) {
		Chunk chunk = world.getChunkFromBlockCoords(pos);
		for(EntityPlayer player : world.playerEntities) {
			// only send to relevant players
			if(!(player instanceof EntityPlayerMP)) {
				continue;
			}
			EntityPlayerMP playerMP = (EntityPlayerMP) player;
			if(world.getPlayerChunkMap().isPlayerWatchingChunk(playerMP, chunk.xPosition, chunk.zPosition)) {
				INSTANCE.sendTo(packet, playerMP);
			}
		}
	}
}

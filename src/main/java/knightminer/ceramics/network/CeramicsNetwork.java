package knightminer.ceramics.network;

import knightminer.ceramics.Ceramics;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.network.NetworkDirection;
import slimeknights.mantle.network.NetworkWrapper;

/**
 * Handler for all packets internal to Ceramics
 */
public class CeramicsNetwork extends NetworkWrapper {
  /** Network instance, created in {@link #init()} */
  private static CeramicsNetwork INSTANCE;

  private CeramicsNetwork() {
    super(Ceramics.getResource("network"));
  }

  /**
   * Gets the instance of the network
   * @return  Network instance
   */
  public static CeramicsNetwork getInstance() {
    if (INSTANCE == null) {
      throw new IllegalStateException("Attempt to get network instance before initialization");
    }
    return INSTANCE;
  }

  /**
   * Initializes the network, should be called during mod construct
   */
  public static void init() {
    if (INSTANCE == null) {
      INSTANCE = new CeramicsNetwork();

      INSTANCE.registerPacket(CisternUpdatePacket.class, CisternUpdatePacket::new, NetworkDirection.PLAY_TO_CLIENT);
    }
  }

  /**
   * Sends a packet to all clients around a point, automatically checking the server world cast
   * @param msg    Packet
   * @param world  World instance
   * @param pos    Position
   */
  public void sendToClientsAround(Object msg, World world, BlockPos pos) {
    if (world instanceof ServerWorld) {
      sendToClientsAround(msg, (ServerWorld) world, pos);
    }
  }
}

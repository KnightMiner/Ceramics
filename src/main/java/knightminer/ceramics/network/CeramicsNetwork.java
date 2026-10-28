package knightminer.ceramics.network;

import knightminer.ceramics.Ceramics;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkDirection;
import slimeknights.mantle.network.NetworkWrapper;

import javax.annotation.Nullable;

/**
 * Handler for all packets internal to Ceramics
 */
public class CeramicsNetwork extends NetworkWrapper {
  /**
   * Network instance:
   * <ul>
   * <li>1: Initial 1.20.1 release</li>
   * <li>20.1: 1.8.1 packet changes.</li>
   * </ul>
   */
  private static final String VERSION = "20.1";

  /** Network instance, created in {@link #init()} */
  private static CeramicsNetwork INSTANCE;

  private CeramicsNetwork() {
    super(Ceramics.getResource("network"), VERSION);
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

      INSTANCE.registerPacket(FluidUpdatePacket.class, FluidUpdatePacket::new, NetworkDirection.PLAY_TO_CLIENT);
      INSTANCE.registerPacket(CisternFluidUpdatePacket.class, CisternFluidUpdatePacket::new, NetworkDirection.PLAY_TO_CLIENT);
      INSTANCE.registerPacket(CisternSizeUpdatePacket.class, CisternSizeUpdatePacket::new, NetworkDirection.PLAY_TO_CLIENT);
      INSTANCE.registerPacket(ChannelFlowPacket.class, ChannelFlowPacket::new, NetworkDirection.PLAY_TO_CLIENT);
      INSTANCE.registerPacket(CrackableCrackPacket.class, CrackableCrackPacket::new, NetworkDirection.PLAY_TO_CLIENT);
    }
  }

  /**
   * Sends a packet to all clients around a point, automatically checking the server world cast
   * @param msg    Packet
   * @param world  World instance
   * @param pos    Position
   */
  public void sendToClientsAround(Object msg, @Nullable Level world, BlockPos pos) {
    if (world instanceof ServerLevel) {
      sendToClientsAround(msg, (ServerLevel) world, pos);
    }
  }
}

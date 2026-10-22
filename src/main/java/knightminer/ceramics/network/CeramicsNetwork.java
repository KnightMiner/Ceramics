package knightminer.ceramics.network;

import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.jetbrains.annotations.ApiStatus.Internal;
import slimeknights.mantle.network.ISimplePacket;

/**
 * Handler for all packets internal to Ceramics
 */
public class CeramicsNetwork {
  /**
   * Network instance:
   * <ul>
   * <li>21.0: Initial 1.21.1 release</li>
   * </ul>
   */
  private static final String VERSION = "21.0";

  /**
   * Initializes the network, should be called during mod construct
   */
  @Internal
  public static void register(RegisterPayloadHandlersEvent event) {
    PayloadRegistrar registrar = event.registrar(VERSION);

    registrar.playToClient(FluidUpdatePacket.TYPE, FluidUpdatePacket.CODEC, ISimplePacket.handle());
    registrar.playToClient(ChannelFlowPacket.TYPE, ChannelFlowPacket.CODEC, ISimplePacket.handle());
    registrar.playToClient(CrackableCrackPacket.TYPE, CrackableCrackPacket.CODEC, ISimplePacket.handle());
  }
}

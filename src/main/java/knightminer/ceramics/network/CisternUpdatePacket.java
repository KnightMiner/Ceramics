package knightminer.ceramics.network;

import knightminer.ceramics.tileentity.CisternTileEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.network.NetworkEvent.Context;
import slimeknights.mantle.network.packet.IThreadsafePacket;
import slimeknights.mantle.util.BlockEntityHelper;

/**
 * Packet sent whenever cistern contents change
 */
public class CisternUpdatePacket implements IThreadsafePacket {
  private final BlockPos pos;
  private final FluidStack fluid;
  private final boolean shouldRefreshCapabilities;

  public CisternUpdatePacket(BlockPos pos, FluidStack fluid, boolean shouldRefreshCapabilities) {
    this.pos = pos;
    this.fluid = fluid;
    this.shouldRefreshCapabilities = shouldRefreshCapabilities;
  }

  public CisternUpdatePacket(FriendlyByteBuf buffer) {
    this.pos = buffer.readBlockPos();
    this.fluid = FluidStack.readFromPacket(buffer);
    this.shouldRefreshCapabilities = buffer.readBoolean();
  }

  @Override
  public void encode(FriendlyByteBuf buffer) {
    buffer.writeBlockPos(pos);
    fluid.writeToPacket(buffer);
    buffer.writeBoolean(shouldRefreshCapabilities);
  }

  @Override
  public void handleThreadsafe(Context context) {
    HandleClient.handle(this);
  }

  /** Separate class to prevent unsafe clientside access */
  private static class HandleClient {
    private static void handle(CisternUpdatePacket packet) {
      BlockEntityHelper.get(CisternTileEntity.class, Minecraft.getInstance().level, packet.pos).ifPresent(te -> te.updateFluidTo(packet.fluid, packet.shouldRefreshCapabilities));
    }
  }
}

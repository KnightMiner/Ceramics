package knightminer.ceramics.network;

import knightminer.ceramics.blocks.entity.CisternBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.network.NetworkEvent.Context;
import org.jetbrains.annotations.ApiStatus.Internal;
import slimeknights.mantle.network.packet.BlockEntityPacket;

/** Updates the fluid and extension count in a cistern. */
@Internal
public record CisternFluidUpdatePacket(BlockPos pos, FluidStack fluid, boolean refreshHandler, int extensions) implements BlockEntityPacket<CisternBlockEntity> {
	public CisternFluidUpdatePacket(FriendlyByteBuf buffer) {
		this(buffer.readBlockPos(), buffer.readFluidStack(), buffer.readBoolean(), buffer.readVarInt());
	}

	@Override
	public void encode(FriendlyByteBuf buffer) {
		buffer.writeBlockPos(pos);
		buffer.writeFluidStack(fluid);
		buffer.writeBoolean(refreshHandler);
		buffer.writeVarInt(extensions);
	}

	@Override
	public Class<CisternBlockEntity> type() {
		return CisternBlockEntity.class;
	}

	@Override
	public void handleBlockEntity(Context context, CisternBlockEntity cistern) {
		cistern.updateFluid(fluid, refreshHandler, extensions);
	}
}

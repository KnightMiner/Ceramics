package knightminer.ceramics.network;

import knightminer.ceramics.Ceramics;
import knightminer.ceramics.blocks.entity.CisternBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.ApiStatus.Internal;
import slimeknights.mantle.network.BlockEntityPacket;

/** Base class for fluid updating behavior. */
@Internal
public record CisternFluidUpdatePacket(BlockPos pos, FluidStack fluid, boolean refreshHandler, int extensions) implements BlockEntityPacket<CisternBlockEntity> {
	public static final Type<CisternFluidUpdatePacket> TYPE = new Type<>(Ceramics.getResource("cistern_fluid_update"));
	public static final StreamCodec<RegistryFriendlyByteBuf, CisternFluidUpdatePacket> CODEC = StreamCodec.composite(
		BlockPos.STREAM_CODEC, CisternFluidUpdatePacket::pos,
		FluidStack.OPTIONAL_STREAM_CODEC, CisternFluidUpdatePacket::fluid,
		ByteBufCodecs.BOOL, CisternFluidUpdatePacket::refreshHandler,
		ByteBufCodecs.VAR_INT, CisternFluidUpdatePacket::extensions,
		CisternFluidUpdatePacket::new);

	@Override
	public Type<CisternFluidUpdatePacket> type() {
		return TYPE;
	}

	@Override
	public Class<CisternBlockEntity> blockEntityType() {
		return CisternBlockEntity.class;
	}

	@Override
	public void handleBlockEntity(IPayloadContext context, CisternBlockEntity updater) {
		updater.updateFluid(fluid, refreshHandler, extensions);
	}
}

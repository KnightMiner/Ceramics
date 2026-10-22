package knightminer.ceramics.network;

import knightminer.ceramics.Ceramics;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import slimeknights.mantle.network.BlockEntityPacket;

/** Base class for fluid updating behavior. Works for anything implementing {@link FluidUpdater}. */
public record FluidUpdatePacket(BlockPos pos, FluidStack fluid, boolean data) implements BlockEntityPacket<FluidUpdatePacket.FluidUpdater> {
	public static final Type<FluidUpdatePacket> TYPE = new Type<>(Ceramics.getResource("fluid_update"));
	public static final StreamCodec<RegistryFriendlyByteBuf,FluidUpdatePacket> CODEC = StreamCodec.composite(
		BlockPos.STREAM_CODEC, FluidUpdatePacket::pos,
		FluidStack.OPTIONAL_STREAM_CODEC, FluidUpdatePacket::fluid,
		ByteBufCodecs.BOOL, FluidUpdatePacket::data,
		FluidUpdatePacket::new);

	@Override
	public Type<FluidUpdatePacket> type() {
		return TYPE;
	}

	@Override
	public Class<FluidUpdater> blockEntityType() {
		return FluidUpdater.class;
	}

	@Override
	public void handleBlockEntity(IPayloadContext context, FluidUpdater updater) {
		updater.updateFluid(fluid, data);
	}

	/** Helper for a block entity to update a fluid */
	public interface FluidUpdater {
		/** Updates the fluid from the server */
		void updateFluid(FluidStack fluid, boolean data);
	}
}

package knightminer.ceramics.network;

import io.netty.buffer.ByteBuf;
import knightminer.ceramics.Ceramics;
import knightminer.ceramics.blocks.entity.CisternBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.ApiStatus.Internal;
import slimeknights.mantle.network.BlockEntityPacket;

/** Updates the size of the cistern after an extension is added or removed. */
@Internal
public record CisternSizeUpdatePacket(BlockPos pos, int extensions) implements BlockEntityPacket<CisternBlockEntity> {
	public static final Type<CisternSizeUpdatePacket> TYPE = new Type<>(Ceramics.getResource("cistern_size_update"));
	public static final StreamCodec<ByteBuf, CisternSizeUpdatePacket> CODEC = StreamCodec.composite(
		BlockPos.STREAM_CODEC, CisternSizeUpdatePacket::pos,
		ByteBufCodecs.VAR_INT, CisternSizeUpdatePacket::extensions,
		CisternSizeUpdatePacket::new);

	@Override
	public Type<CisternSizeUpdatePacket> type() {
		return TYPE;
	}

	@Override
	public Class<CisternBlockEntity> blockEntityType() {
		return CisternBlockEntity.class;
	}

	@Override
	public void handleBlockEntity(IPayloadContext context, CisternBlockEntity updater) {
		updater.updateSize(extensions);
	}
}

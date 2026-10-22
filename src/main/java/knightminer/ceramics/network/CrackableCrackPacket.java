package knightminer.ceramics.network;

import io.netty.buffer.ByteBuf;
import knightminer.ceramics.Ceramics;
import knightminer.ceramics.blocks.entity.CrackableBlockEntityHandler.ICrackableBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import slimeknights.mantle.network.BlockEntityPacket;

/** Packet used when a crackable block becomes more cracked. */
public record CrackableCrackPacket(BlockPos pos, int cracks) implements BlockEntityPacket<ICrackableBlockEntity> {
	public static final Type<CrackableCrackPacket> TYPE = new Type<>(Ceramics.getResource("crackable_crack"));
	public static final StreamCodec<ByteBuf,CrackableCrackPacket> CODEC = StreamCodec.composite(
		BlockPos.STREAM_CODEC, CrackableCrackPacket::pos,
		ByteBufCodecs.VAR_INT, CrackableCrackPacket::cracks,
		CrackableCrackPacket::new);

	@Override
	public Type<CrackableCrackPacket> type() {
		return TYPE;
	}

	@Override
	public Class<ICrackableBlockEntity> blockEntityType() {
		return ICrackableBlockEntity.class;
	}

	@Override
	public void handleBlockEntity(IPayloadContext context, ICrackableBlockEntity crackable) {
		crackable.getCracksHandler().setCracks(cracks);
	}
}

package knightminer.ceramics.network;

import knightminer.ceramics.Ceramics;
import knightminer.ceramics.blocks.entity.ChannelBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import slimeknights.mantle.network.BlockEntityPacket;

/** Packet for when the flowing state changes on a channel side */
public record ChannelFlowPacket(BlockPos pos, Direction side, boolean flow) implements BlockEntityPacket<ChannelBlockEntity> {
	public static final Type<ChannelFlowPacket> TYPE = new Type<>(Ceramics.getResource("channel_flow"));
	public static final StreamCodec<RegistryFriendlyByteBuf,ChannelFlowPacket> CODEC = StreamCodec.composite(
		BlockPos.STREAM_CODEC, ChannelFlowPacket::pos,
		Direction.STREAM_CODEC, ChannelFlowPacket::side,
		ByteBufCodecs.BOOL, ChannelFlowPacket::flow,
		ChannelFlowPacket::new);

	@Override
	public Type<ChannelFlowPacket> type() {
		return TYPE;
	}

	@Override
	public Class<ChannelBlockEntity> blockEntityType() {
		return ChannelBlockEntity.class;
	}

	@Override
	public void handleBlockEntity(IPayloadContext context, ChannelBlockEntity channel) {
		channel.setFlow(side, flow);
	}
}
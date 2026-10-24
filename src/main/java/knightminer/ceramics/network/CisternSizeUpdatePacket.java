package knightminer.ceramics.network;

import knightminer.ceramics.blocks.entity.CisternBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent.Context;
import org.jetbrains.annotations.ApiStatus.Internal;
import slimeknights.mantle.network.packet.BlockEntityPacket;

/** Updates the size of the cistern after an extension is added or removed. */
@Internal
public record CisternSizeUpdatePacket(BlockPos pos, int extensions) implements BlockEntityPacket<CisternBlockEntity> {
	public CisternSizeUpdatePacket(FriendlyByteBuf buffer) {
		this(buffer.readBlockPos(), buffer.readVarInt());
	}

	@Override
	public void encode(FriendlyByteBuf buffer) {
		buffer.writeBlockPos(pos);
		buffer.writeVarInt(extensions);
	}

	@Override
	public Class<CisternBlockEntity> type() {
		return CisternBlockEntity.class;
	}

	@Override
	public void handleBlockEntity(Context context, CisternBlockEntity cistern) {
		cistern.updateSize(extensions);
	}
}

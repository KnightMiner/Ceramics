package knightminer.ceramics.blocks;

import knightminer.ceramics.Ceramics;
import knightminer.ceramics.Registration;
import knightminer.ceramics.blocks.entity.ChannelBlockEntity;
import knightminer.ceramics.blocks.entity.CrackableBlockEntityHandler.ICrackableBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import slimeknights.mantle.util.BlockEntityHelper;

import javax.annotation.Nullable;

/**
 * Channel extension that supports moving fluids
 */
public class FlowingChannelBlock extends ChannelBlock implements ICrackableBlock, EntityBlock {
	private final boolean crackable;
	public FlowingChannelBlock(Properties props, boolean crackable) {
		super(props);
		this.crackable = crackable;
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new ChannelBlockEntity(pos, state, crackable);
	}

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
		return BlockEntityHelper.serverTicker(level, type, Registration.CHANNEL_BLOCK_ENTITY.get(), ChannelBlockEntity.SERVER_TICKER);
	}

	/** Gets the direction from the given pair of positions */
	private static Direction fromOffset(BlockPos pos, BlockPos neighbor) {
		BlockPos offset = neighbor.subtract(pos);
		for (Direction direction : Direction.values()) {
			if (direction.getNormal().equals(offset)) {
				return direction;
			}
		}
		Ceramics.LOG.error("Channel found no offset for position pair {} and {} on neighbor changed", pos, neighbor);
		return Direction.DOWN;
	}

	@Override
	protected void activateTileEntity(BlockState state, Level world, BlockPos pos, Direction side) {
		if (world.getBlockEntity(pos) instanceof ChannelBlockEntity channel) {
			channel.refreshNeighbor(state, side);
		}
	}


	/* Cracking */

	@Override
	public boolean isCrackable() {
		return crackable;
	}

	@Override
	protected void randomTick(BlockState state, ServerLevel worldIn, BlockPos pos, RandomSource random) {
		if (isCrackable() && worldIn.getBlockEntity(pos) instanceof ChannelBlockEntity channel) {
			channel.randomTick();
		}
	}

	@Override
	protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
		if (crackable && ICrackableBlock.tryRepair(stack, level, pos, player, hand)) {
			return ItemInteractionResult.SUCCESS;
		}
		return super.useItemOn(stack, state, level, pos, player, hand, hit);
	}
}

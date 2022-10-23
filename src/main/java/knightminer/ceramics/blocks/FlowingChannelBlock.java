package knightminer.ceramics.blocks;

import knightminer.ceramics.Ceramics;
import knightminer.ceramics.Registration;
import knightminer.ceramics.tileentity.ChannelTileEntity;
import knightminer.ceramics.tileentity.CrackableTileEntityHandler.ICrackableBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import slimeknights.mantle.util.BlockEntityHelper;

import javax.annotation.Nullable;
import java.util.Random;

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
		return new ChannelTileEntity(pos, state, crackable);
	}

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
		return BlockEntityHelper.serverTicker(level, type, Registration.CHANNEL_TILE_ENTITY.get(), ChannelTileEntity.SERVER_TICKER);
	}

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

	@SuppressWarnings("deprecation")
	@Override
	@Deprecated
	public void neighborChanged(BlockState state, Level worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
		super.neighborChanged(state, worldIn, pos, blockIn, fromPos, isMoving);
		if (!worldIn.isClientSide()) {
			BlockEntityHelper.get(ChannelTileEntity.class, worldIn, pos)
											 .ifPresent(te -> te.removeCachedNeighbor(fromOffset(pos, fromPos)));
		}
	}

	@Override
	protected void activateTileEntity(BlockState state, Level world, BlockPos pos, Direction side) {
		BlockEntityHelper.get(ChannelTileEntity.class, world, pos).ifPresent(te -> te.refreshNeighbor(state, side));
	}

	/* Cracking */

	@Override
	public boolean isCrackable() {
		return crackable;
	}

	@SuppressWarnings("deprecation")
	@Override
	@Deprecated
	public void randomTick(BlockState state, ServerLevel worldIn, BlockPos pos, Random random) {
		if (isCrackable()) {
			BlockEntityHelper.get(ChannelTileEntity.class, worldIn, pos).ifPresent(ChannelTileEntity::randomTick);
		}
	}

	@Override
	public void setPlacedBy(Level worldIn, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
		if (crackable) {
			ICrackableBlock.onBlockPlacedBy(worldIn, pos, stack);
		}
	}

	@Deprecated
	@Override
	public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
		if (crackable && ICrackableBlock.tryRepair(world, pos, player, hand)) {
			return InteractionResult.SUCCESS;
		}
		return super.use(state, world, pos, player, hand, hit);
	}
}

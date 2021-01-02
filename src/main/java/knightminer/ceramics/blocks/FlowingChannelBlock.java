package knightminer.ceramics.blocks;

import knightminer.ceramics.Ceramics;
import knightminer.ceramics.tileentity.ChannelTileEntity;
import knightminer.ceramics.tileentity.CrackableTileEntityHandler.ICrackableBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import slimeknights.mantle.util.TileEntityHelper;

import java.util.Random;

/**
 * Channel extension that supports moving fluids
 */
public class FlowingChannelBlock extends ChannelBlock implements ICrackableBlock {
	private final boolean crackable;
	public FlowingChannelBlock(Properties props, boolean crackable) {
		super(props);
		this.crackable = crackable;
	}

	@Override
	public boolean hasTileEntity(BlockState state) {
		return true;
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return new ChannelTileEntity(crackable);
	}

	private static Direction fromOffset(BlockPos pos, BlockPos neighbor) {
		BlockPos offset = neighbor.subtract(pos);
		for (Direction direction : Direction.values()) {
			if (direction.getDirectionVec().equals(offset)) {
				return direction;
			}
		}
		Ceramics.LOG.error("Channel found no offset for position pair {} and {} on neighbor changed", pos, neighbor);
		return Direction.DOWN;
	}

	@SuppressWarnings("deprecation")
	@Override
	@Deprecated
	public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
		super.neighborChanged(state, worldIn, pos, blockIn, fromPos, isMoving);
		if (!worldIn.isRemote()) {
			TileEntityHelper.getTile(ChannelTileEntity.class, worldIn, pos)
											.ifPresent(te -> te.removeCachedNeighbor(fromOffset(pos, fromPos)));
		}
	}


	/* Cracking */

	@Override
	public boolean isCrackable() {
		return crackable;
	}

	@SuppressWarnings("deprecation")
	@Override
	@Deprecated
	public void randomTick(BlockState state, ServerWorld worldIn, BlockPos pos, Random random) {
		if (isCrackable()) {
			TileEntityHelper.getTile(ChannelTileEntity.class, worldIn, pos).ifPresent(ChannelTileEntity::randomTick);
		}
	}
}

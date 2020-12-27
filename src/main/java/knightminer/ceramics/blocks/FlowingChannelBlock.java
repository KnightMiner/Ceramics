package knightminer.ceramics.blocks;

import knightminer.ceramics.Ceramics;
import knightminer.ceramics.tileentity.ChannelTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import slimeknights.mantle.util.TileEntityHelper;

/**
 * Channel extension that supports moving fluids
 */
public class FlowingChannelBlock extends ChannelBlock {
	public FlowingChannelBlock(Properties props) {
		super(props);
	}

	@Override
	public boolean hasTileEntity(BlockState state) {
		return true;
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return new ChannelTileEntity();
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
		TileEntityHelper.getTile(ChannelTileEntity.class, worldIn, pos)
										.ifPresent(te -> te.removeCachedNeighbor(fromOffset(pos, fromPos)));
	}
}

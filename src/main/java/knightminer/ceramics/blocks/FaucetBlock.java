package knightminer.ceramics.blocks;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;

import javax.annotation.Nullable;
import java.util.EnumMap;

/**
 * Base faucet block, used for pouring variant and for decorative unfired variant
 */
public class FaucetBlock extends Block {
	public static final DirectionProperty FACING = BlockStateProperties.FACING_EXCEPT_UP;
	private static final EnumMap<Direction,VoxelShape> SHAPES = Maps.newEnumMap(ImmutableMap.of(
		Direction.DOWN,  Block.makeCuboidShape( 4.0D, 10.0D,  4.0D, 12.0D, 16.0D, 12.0D),
		Direction.NORTH, Block.makeCuboidShape( 4.0D,  4.0D, 10.0D, 12.0D, 10.0D, 16.0D),
		Direction.SOUTH, Block.makeCuboidShape( 4.0D,  4.0D,  0.0D, 12.0D, 10.0D,  6.0D),
		Direction.WEST,  Block.makeCuboidShape(10.0D,  4.0D,  4.0D, 16.0D, 10.0D, 12.0D),
		Direction.EAST,  Block.makeCuboidShape( 0.0D,  4.0D,  4.0D,  6.0D, 10.0D, 12.0D)
	));

	public FaucetBlock(Properties properties) {
		super(properties);
		this.setDefaultState(this.stateContainer.getBaseState().with(FACING, Direction.NORTH));
	}

	/* Blockstate */

	@Override
	protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
		builder.add(FACING);
	}

	@Nullable
	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		Direction dir = context.getFace();
		if (dir == Direction.UP) {
			dir = Direction.DOWN;
		}
		return this.getDefaultState().with(FACING, dir);
	}

	@SuppressWarnings("deprecation")
	@Deprecated
	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		return SHAPES.get(state.get(FACING));
	}
}

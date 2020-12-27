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
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;

import javax.annotation.Nullable;
import java.util.EnumMap;

/**
 * Base faucet block, used for pouring variant and for decorative unfired variant
 */
public class FaucetBlock extends Block {
	public static final DirectionProperty FACING = BlockStateProperties.FACING_EXCEPT_UP;
	private static final EnumMap<Direction,VoxelShape> SHAPES = Maps.newEnumMap(ImmutableMap.of(
			Direction.DOWN,  VoxelShapes.combineAndSimplify(makeCuboidShape( 4, 10,  4, 12, 16, 12), makeCuboidShape( 6, 10,  6, 10, 16, 10), IBooleanFunction.ONLY_FIRST),
			Direction.NORTH, VoxelShapes.combineAndSimplify(makeCuboidShape( 4,  4, 10, 12, 10, 16), makeCuboidShape( 6,  6, 10, 10, 10, 16), IBooleanFunction.ONLY_FIRST),
			Direction.SOUTH, VoxelShapes.combineAndSimplify(makeCuboidShape( 4,  4,  0, 12, 10,  6), makeCuboidShape( 6,  6,  0, 10, 10,  6), IBooleanFunction.ONLY_FIRST),
			Direction.WEST,  VoxelShapes.combineAndSimplify(makeCuboidShape(10,  4,  4, 16, 10, 12), makeCuboidShape(10,  6,  6, 16, 10, 10), IBooleanFunction.ONLY_FIRST),
			Direction.EAST,  VoxelShapes.combineAndSimplify(makeCuboidShape( 0,  4,  4,  6, 10, 12), makeCuboidShape( 0,  6,  6,  6, 10, 10), IBooleanFunction.ONLY_FIRST)
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

package knightminer.ceramics.blocks;

import knightminer.ceramics.Ceramics;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;

import javax.annotation.Nullable;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING;

/* Decorative block to place on the side of a cistern, reads fluid value */
public class GaugeBlock extends Block {
  private static final VoxelShape[] BOUNDS = {
      box( 4, 4,  0, 12, 12,  1),
      box(15, 4,  4, 16, 12, 12),
      box( 4, 4, 15, 12, 12, 16),
      box( 0, 4,  4,  1, 12, 12)
  };

  public GaugeBlock(Properties builder) {
    super(builder);
    this.registerDefaultState(this.stateDefinition.any().setValue(HORIZONTAL_FACING, Direction.NORTH));
  }


  /* Behavior */

  @SuppressWarnings("deprecation")
  @Deprecated
  @Override
  public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
    // display adjacent tank contents
    if (!world.isClientSide()) {
      Direction side = state.getValue(HORIZONTAL_FACING);
      BlockEntity te = world.getBlockEntity(pos.relative(side.getOpposite()));
      if (te != null) {
        te.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, side).ifPresent(handler -> {
          FluidStack fluid = handler.getFluidInTank(0);
          if (fluid.isEmpty()) {
            player.displayClientMessage(new TranslatableComponent(Ceramics.lang("block", "gauge.empty")), true);
          } else {
            player.displayClientMessage(new TranslatableComponent(Ceramics.lang("block", "gauge.contents"), fluid.getAmount(), fluid.getDisplayName()), true);
          }
        });
      }
    }

    return InteractionResult.SUCCESS;
  }


  /* Visuals */

  @SuppressWarnings("deprecation")
  @Deprecated
  @Override
  public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
    return BOUNDS[state.getValue(HORIZONTAL_FACING).get2DDataValue()];
  }

  @SuppressWarnings("deprecation")
  @Deprecated
  @Override
  public boolean canSurvive(BlockState state, LevelReader world, BlockPos pos) {
    Direction direction = state.getValue(HORIZONTAL_FACING);
    BlockEntity te = world.getBlockEntity(pos.relative(direction.getOpposite()));
    return te != null && te.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, direction).isPresent();
  }

  @Override
  @Nullable
  public BlockState getStateForPlacement(BlockPlaceContext context) {
    BlockState state = this.defaultBlockState();
    LevelReader world = context.getLevel();
    BlockPos pos = context.getClickedPos();
    Direction[] nearestDir = context.getNearestLookingDirections();
    for (Direction direction : nearestDir) {
      if (direction.getAxis().isHorizontal()) {
        state = state.setValue(HORIZONTAL_FACING, direction.getOpposite());
        if (state.canSurvive(world, pos)) {
          return state;
        }
      }
    }

    return null;
  }

  @SuppressWarnings("deprecation")
  @Deprecated
  @Override
  public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor worldIn, BlockPos currentPos, BlockPos facingPos) {
    return facing.getOpposite() == state.getValue(HORIZONTAL_FACING) && !state.canSurvive(worldIn, currentPos) ? Blocks.AIR.defaultBlockState() : state;
  }

  @Override
  @SuppressWarnings("deprecation")
  @Deprecated
  public BlockState rotate(BlockState state, Rotation rot) {
    return state.setValue(HORIZONTAL_FACING, rot.rotate(state.getValue(HORIZONTAL_FACING)));
  }

  @Override
  @SuppressWarnings("deprecation")
  @Deprecated
  public BlockState mirror(BlockState state, Mirror mirror) {
    return state.rotate(mirror.getRotation(state.getValue(HORIZONTAL_FACING)));
  }

  @Override
  protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
    builder.add(HORIZONTAL_FACING);
  }
}

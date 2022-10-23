package knightminer.ceramics.blocks;

import knightminer.ceramics.Ceramics;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;

import javax.annotation.Nullable;

import static net.minecraft.state.properties.BlockStateProperties.HORIZONTAL_FACING;

import net.minecraft.block.AbstractBlock.Properties;

/**
 * Decorative block to place on the side of a cistern, reads fluid value
 */
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
  public ActionResultType use(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hit) {
    // display adjacent tank contents
    if (!world.isClientSide()) {
      Direction side = state.getValue(HORIZONTAL_FACING);
      TileEntity te = world.getBlockEntity(pos.relative(side.getOpposite()));
      if (te != null) {
        te.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, side).ifPresent(handler -> {
          FluidStack fluid = handler.getFluidInTank(0);
          if (fluid.isEmpty()) {
            player.displayClientMessage(new TranslationTextComponent(Ceramics.lang("block", "gauge.empty")), true);
          } else {
            player.displayClientMessage(new TranslationTextComponent(Ceramics.lang("block", "gauge.contents"), fluid.getAmount(), fluid.getDisplayName()), true);
          }
        });
      }
    }

    return ActionResultType.SUCCESS;
  }


  /* Visuals */

  @SuppressWarnings("deprecation")
  @Deprecated
  @Override
  public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
    return BOUNDS[state.getValue(HORIZONTAL_FACING).get2DDataValue()];
  }

  @SuppressWarnings("deprecation")
  @Deprecated
  @Override
  public boolean canSurvive(BlockState state, IWorldReader world, BlockPos pos) {
    Direction direction = state.getValue(HORIZONTAL_FACING);
    TileEntity te = world.getBlockEntity(pos.relative(direction.getOpposite()));
    return te != null && te.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, direction).isPresent();
  }

  @Override
  @Nullable
  public BlockState getStateForPlacement(BlockItemUseContext context) {
    BlockState state = this.defaultBlockState();
    IWorldReader world = context.getLevel();
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
  public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos) {
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
  protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
    builder.add(HORIZONTAL_FACING);
  }
}

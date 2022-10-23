package knightminer.ceramics.blocks;

import knightminer.ceramics.blocks.ChannelBlock.ChannelConnection;
import knightminer.ceramics.recipe.CeramicsTags;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.pathfinding.PathType;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.AttachFace;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Plane;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;

import java.util.EnumMap;
import java.util.Map;

import static net.minecraft.state.properties.BlockStateProperties.EAST;
import static net.minecraft.state.properties.BlockStateProperties.NORTH;
import static net.minecraft.state.properties.BlockStateProperties.SOUTH;
import static net.minecraft.state.properties.BlockStateProperties.WEST;

import net.minecraft.block.AbstractBlock.Properties;

/**
 * Base block between unfired and fired cisterns. Extended for fluid variant
 */
public class CisternBlock extends Block {
  /** Block state properties for each of the four connection directions */
  public static final Map<Direction,BooleanProperty> CONNECTIONS = Util.make(new EnumMap<>(Direction.class), map -> {
    map.put(Direction.NORTH, NORTH);
    map.put(Direction.SOUTH, SOUTH);
    map.put(Direction.WEST, WEST);
    map.put(Direction.EAST, EAST);
  });
  /** Set when a cistern is an extension */
  public static final BooleanProperty EXTENSION = BooleanProperty.create("extension");

  // cistern bounds
  private static final VoxelShape[] BOUNDS_BASE;
  private static final VoxelShape[] BOUNDS_EXTENSION;
  // bounds for lever placement
  private static final VoxelShape SOLIDNESS_BASE = VoxelShapes.join(
      VoxelShapes.block(),
      box(3, 2, 3, 13, 16, 13),
      IBooleanFunction.ONLY_FIRST);
  private static final VoxelShape SOLIDNESS_EXTENSION = VoxelShapes.join(
      VoxelShapes.block(),
      box(3, 0, 3, 13, 16, 13),
      IBooleanFunction.ONLY_FIRST);

  /**
   * Gets a key for the bounds based on the given booleans
   * @return  Bounds key
   */
  private static int boundsKey(boolean north, boolean south, boolean west, boolean east) {
    return (east ? 0b1000 : 0) | (north ? 0b0100 : 0) | (west ? 0b0010 : 0) | (south ? 0b0001 : 0);
  }

  /**
   * Makes all bounds around the given base shape
   * @param base  Base shape
   * @return  Array of bounds
   */
  private static VoxelShape[] makeBounds(VoxelShape base) {
    // extra edges for bounds of connection
    VoxelShape connectionNorth = box( 4, 4,  0, 12, 12,  1);
    VoxelShape connectionSouth = box( 4, 4, 15, 12, 12, 16);
    VoxelShape connectionWest  = box( 0, 4,  4,  1, 12, 12);
    VoxelShape connectionEast  = box(15, 4,  4, 16, 12, 12);
    // array of booleans for iteration
    boolean[] bools = {false, true};
    // iterate over each combination of booleans
    VoxelShape[] boundList = new VoxelShape[16];
    for (boolean north : bools) {
      for (boolean south : bools) {
        for (boolean west : bools) {
          for (boolean east : bools) {
            // add in the connected sides
            VoxelShape bounds = base;
            if (north) bounds = VoxelShapes.or(bounds, connectionNorth);
            if (south) bounds = VoxelShapes.or(bounds, connectionSouth);
            if (west) bounds = VoxelShapes.or(bounds, connectionWest);
            if (east) bounds = VoxelShapes.or(bounds, connectionEast);

            // simplify the final bounds into the proper key
            boundList[boundsKey(north, south, west, east)] = bounds.optimize();
          }
        }
      }
    }
    return boundList;
  }

  static {
    // base shapes
    BOUNDS_BASE = makeBounds(VoxelShapes.joinUnoptimized(
        VoxelShapes.or(
            box(2, 0, 2, 14,  1, 14),
            box(1, 1, 2, 15, 16, 14),
            box(2, 1, 1, 14, 16, 15)),
        box(3, 2, 3, 13, 16, 13),
        IBooleanFunction.ONLY_FIRST));

    // extension shapes
    BOUNDS_EXTENSION = makeBounds(VoxelShapes.joinUnoptimized(
        VoxelShapes.or(
            box(1, 0, 2, 15, 16, 14),
            box(2, 0, 1, 14, 16, 15)),
        box(3, 0, 3, 13, 16, 13),
        IBooleanFunction.ONLY_FIRST));
  }

  public CisternBlock(Properties properties) {
    super(properties);
  }


  /* Block state properties */

  @Override
  protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
    builder.add(EXTENSION);
    for (Direction side : Plane.HORIZONTAL) {
      builder.add(CONNECTIONS.get(side));
    }
  }

  @SuppressWarnings("deprecation")
  @Deprecated
  @Override
  public boolean isPathfindable(BlockState state, IBlockReader worldIn, BlockPos pos, PathType type) {
    return false;
  }

  @SuppressWarnings("deprecation")
  @Deprecated
  @Override
  public BlockState rotate(BlockState state, Rotation rot) {
    switch(rot) {
      case CLOCKWISE_180:
        return state.setValue(NORTH, state.getValue(SOUTH)).setValue(EAST, state.getValue(WEST)).setValue(SOUTH, state.getValue(NORTH)).setValue(WEST, state.getValue(EAST));
      case COUNTERCLOCKWISE_90:
        return state.setValue(NORTH, state.getValue(EAST)).setValue(EAST, state.getValue(SOUTH)).setValue(SOUTH, state.getValue(WEST)).setValue(WEST, state.getValue(NORTH));
      case CLOCKWISE_90:
        return state.setValue(NORTH, state.getValue(WEST)).setValue(EAST, state.getValue(NORTH)).setValue(SOUTH, state.getValue(EAST)).setValue(WEST, state.getValue(SOUTH));
      default:
        return state;
    }
  }

  @SuppressWarnings("deprecation")
  @Deprecated
  @Override
  public BlockState mirror(BlockState state, Mirror mirrorIn) {
    switch(mirrorIn) {
      case LEFT_RIGHT:
        return state.setValue(NORTH, state.getValue(SOUTH)).setValue(SOUTH, state.getValue(NORTH));
      case FRONT_BACK:
        return state.setValue(EAST, state.getValue(WEST)).setValue(WEST, state.getValue(EAST));
      default:
        return super.mirror(state, mirrorIn);
    }
  }

  private static boolean facingConnected(Direction facing, BlockState state, DirectionProperty property) {
    return !state.hasProperty(property) || state.getValue(property) == facing;
  }

  /**
   * Check if the given block is something the barrel should connect to
   * @param facing       Side to check
   * @param facingState  Block on side
   * @return  True if connected, false otherwise
   */
  protected boolean isConnected(Direction facing, BlockState facingState) {
    // must be in tag
    if (!facingState.is(CeramicsTags.Blocks.CISTERN_CONNECTIONS)) {
      return false;
    }

    // if the block has a side property, use that
    Direction opposite = facing.getOpposite();
    BooleanProperty sideProp = CONNECTIONS.get(opposite);
    if (facingState.hasProperty(sideProp)) {
      return facingState.getValue(sideProp);
    }
    // channel connections
    EnumProperty<ChannelConnection> channelProp = ChannelBlock.DIRECTION_MAP.get(opposite);
    if (facingState.hasProperty(channelProp)) {
      return facingState.getValue(channelProp) == ChannelConnection.OUT;
    }
    // if there is a face property and it is not wall, not connected
    if (facingState.hasProperty(BlockStateProperties.ATTACH_FACE) && facingState.getValue(BlockStateProperties.ATTACH_FACE) != AttachFace.WALL) {
      return false;
    }
    // try relevant facing properties, if any are present must be facing this
    return facingConnected(facing, facingState, BlockStateProperties.HORIZONTAL_FACING)
        && facingConnected(facing, facingState, BlockStateProperties.FACING)
        && facingConnected(facing, facingState, BlockStateProperties.FACING_HOPPER);
  }

  @Override
  public BlockState getStateForPlacement(BlockItemUseContext context) {
    IBlockReader world = context.getLevel();
    BlockPos pos = context.getClickedPos();
    return defaultBlockState().setValue(EXTENSION, world.getBlockState(pos.below()).is(this))
                            .setValue(NORTH, isConnected(Direction.NORTH, world.getBlockState(pos.north())))
                            .setValue(SOUTH, isConnected(Direction.SOUTH, world.getBlockState(pos.south())))
                            .setValue(WEST, isConnected(Direction.WEST, world.getBlockState(pos.west())))
                            .setValue(EAST, isConnected(Direction.EAST, world.getBlockState(pos.east())));
  }

  @SuppressWarnings("deprecation")
  @Override
  @Deprecated
  public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos) {
    if (!facing.getAxis().isVertical()) {
      // barrel connects to
      state = state.setValue(CONNECTIONS.get(facing), isConnected(facing, facingState));
    } else if (facing == Direction.DOWN) {
      // extension if above another of the same block type
      state = state.setValue(EXTENSION, facingState.is(this));
    }
    return state;
  }

  @Override
  @SuppressWarnings("deprecation")
  @Deprecated
  public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context) {
    VoxelShape[] boundList = state.getValue(EXTENSION) ? BOUNDS_EXTENSION : BOUNDS_BASE;
    return boundList[boundsKey(state.getValue(NORTH), state.getValue(SOUTH), state.getValue(WEST), state.getValue(EAST))];
  }

  // used to calculated side solidness for torch/lever placement, not collision
  @Override
  @Deprecated
  public VoxelShape getBlockSupportShape(BlockState state, IBlockReader reader, BlockPos pos) {
    return state.getValue(EXTENSION) ? SOLIDNESS_EXTENSION : SOLIDNESS_BASE;
  }
}

package knightminer.ceramics.client.renderer;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import knightminer.ceramics.blocks.CisternBlock;
import knightminer.ceramics.client.model.CisternModel;
import knightminer.ceramics.tileentity.CisternTileEntity;
import knightminer.ceramics.util.tank.CisternTank;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Plane;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.templates.EmptyFluidHandler;
import slimeknights.mantle.client.model.fluid.FluidCuboid;
import slimeknights.mantle.client.model.util.ModelHelper;
import slimeknights.mantle.client.render.FluidRenderer;

/**
 * Renderer for cistern blocks
 */
public class CisternTileEntityRenderer extends TileEntityRenderer<CisternTileEntity> {
//  /** Map of block state to fluid cuboids for each relevant side */
//  private static final Map<BlockState,Pair<FluidCuboid,List<FluidCuboid>>> CUBOID_MAP = new HashMap<>();
//  /** Start of base fluid cube */
//  private static final Vector3f CUBOID_FROM_BASE = new Vector3f(3, 2, 3);
//  /** Start of extension fluid cubes */
//  private static final Vector3f CUBOID_FROM_EXTENSION = new Vector3f(3, -1, 3);
//  /** End of fluid cube in cisterns */
//  private static final Vector3f CUBOID_TO = new Vector3f(13, 15, 13);
//  /** Fluid cuboids for each side extension */
//  private static final Map<Direction,FluidCuboid> SIDE_CUBOIDS = Util.make(new EnumMap<>(Direction.class), map -> {
//    // logic to make a single side map
//    Function<Direction,Map<Direction,FluidFace>> faceGenerator = dir -> {
//      Map<Direction,FluidFace> faces = new EnumMap<>(Direction.class);
//      faces.put(Direction.UP, FluidFace.NORMAL);
//      faces.put(dir, FluidFace.NORMAL);
//      return faces;
//    };
//    // add four sides to the map
//    map.put(Direction.NORTH, new FluidCuboid(new Vector3f( 5, 6,  0), new Vector3f(11, 11,  3), faceGenerator.apply(Direction.NORTH)));
//    map.put(Direction.SOUTH, new FluidCuboid(new Vector3f( 5, 6, 13), new Vector3f(11, 11, 16), faceGenerator.apply(Direction.SOUTH)));
//    map.put(Direction.WEST,  new FluidCuboid(new Vector3f( 0, 6,  5), new Vector3f( 3, 11, 11), faceGenerator.apply(Direction.WEST)));
//    map.put(Direction.EAST,  new FluidCuboid(new Vector3f(13, 6,  5), new Vector3f(16, 11, 11), faceGenerator.apply(Direction.EAST)));
//  });
//
//  /**
//   * Gets the fluid cuboids for rendering this state
//   * @param state  State to render
//   * @return  Pair of cuboids for center, and cuboid list for sides
//   */
//  private static Pair<FluidCuboid,List<FluidCuboid>> getCuboid(BlockState state) {
//    // determine starting height
//    Vector3f from = state.get(CisternBlock.EXTENSION) ? CUBOID_FROM_EXTENSION : CUBOID_FROM_BASE;
//
//    // determine which side faces to add
//    ImmutableList.Builder<FluidCuboid> sides = ImmutableList.builder();
//    for (Direction direction : Plane.HORIZONTAL) {
//      if (state.get(CisternBlock.CONNECTIONS.get(direction))) {
//        sides.add(SIDE_CUBOIDS.get(direction));
//      }
//    }
//
//    return Pair.of(new FluidCuboid(from, CUBOID_TO, Util.make(new EnumMap<>(Direction.class), map -> map.put(Direction.UP, FluidFace.NORMAL))), sides.build());
//  }
//
//  /**
//   * Gets the cached fluid cuboids for rendering this state
//   * @param state  State to render
//   * @return  Pair of cuboids for center, and cuboid list for sides
//   */
//  private static Pair<FluidCuboid,List<FluidCuboid>> getCachedCuboid(BlockState state) {
//    return CUBOID_MAP.computeIfAbsent(state, CisternTileEntityRenderer::getCuboid);
//  }

  public CisternTileEntityRenderer(TileEntityRendererDispatcher rendererDispatcherIn) {
    super(rendererDispatcherIn);
  }

  @Override
  public void render(CisternTileEntity tileEntity, float partialTicks, MatrixStack matrices, IRenderTypeBuffer buffer, int light, int combinedOverlay) {
    FluidStack fluid = tileEntity.getPublicHandler().orElse(EmptyFluidHandler.INSTANCE).getFluidInTank(0);
    if (!fluid.isEmpty()) {
      int renderIndex = tileEntity.getRenderIndex();
      // capacity for gives us the minimum amount to start rendering in this segement
      // render nothing beyond the base capacity
      int amount = fluid.getAmount() - CisternTank.capacityFor(renderIndex);
      if (amount > 0) {
        // get the model pair, if the capacity is above the capacity per cistern, use the overfull model (no top face)
        BlockState state = tileEntity.getBlockState();
        CisternModel.BakedModel model = ModelHelper.getBakedModel(state, CisternModel.BakedModel.class);
        if (model != null) {
          // fetch textures and attributes
          FluidAttributes attributes = fluid.getFluid().getAttributes();
          TextureAtlasSprite still = FluidRenderer.getBlockSprite(attributes.getStillTexture(fluid));
          TextureAtlasSprite flowing = FluidRenderer.getBlockSprite(attributes.getFlowingTexture(fluid));
          IVertexBuilder builder = buffer.getBuffer(FluidRenderer.RENDER_TYPE);
          int color = attributes.getColor(fluid);

          // if full, just render all full sides
          if (amount > CisternTank.BASE_CAPACITY) {
            for (Direction direction : Plane.HORIZONTAL) {
              // state and model must contain that direction
              FluidCuboid cuboid = model.getFluid(direction);
              if (cuboid != null && state.get(CisternBlock.CONNECTIONS.get(direction))) {
                FluidRenderer.renderCuboid(matrices, builder, cuboid, still, flowing, cuboid.getFromScaled(), cuboid.getToScaled(), color, light, false);
              }
            }
          } else {
            // determine the relevant height of the center
            FluidCuboid center = model.getCenterFluid(state.get(CisternBlock.EXTENSION));
            Vector3f from = center.getFromScaled();
            Vector3f to = center.getToScaled().copy();
            float minY = from.getY();
            to.setY(minY + amount * (to.getY() - minY) / (float)CisternTank.BASE_CAPACITY);
            // render the center using Mantle's logic
            FluidRenderer.renderCuboid(matrices, builder, center, still, still, from, to, color, light, false);

            // scale the sides based on the center
            for (Direction direction : Plane.HORIZONTAL) {
              // state and model must contain that direction
              FluidCuboid cuboid = model.getFluid(direction);
              if (cuboid != null && state.get(CisternBlock.CONNECTIONS.get(direction))) {
                // bottom of the side must be smaller than the height to consider
                Vector3f sFrom = cuboid.getFromScaled();
                if (sFrom.getY() < to.getY()) {
                  // if the side end is larger than the center, clamp it down
                  Vector3f sTo = cuboid.getToScaled();
                  if (sTo.getY() > to.getY()) {
                    sTo = sTo.copy();
                    sTo.setY(to.getY());
                  }
                  FluidRenderer.renderCuboid(matrices, builder, cuboid, still, still, sFrom, sTo, color, light, false);
                }
              }
            }
          }
        }
      }
    }
  }
}

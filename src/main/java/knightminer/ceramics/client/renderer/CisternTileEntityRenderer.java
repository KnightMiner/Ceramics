package knightminer.ceramics.client.renderer;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.datafixers.util.Pair;
import knightminer.ceramics.blocks.CisternBlock;
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
import slimeknights.mantle.client.model.fluid.FluidCuboid.FluidFace;
import slimeknights.mantle.client.render.FluidRenderer;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

/**
 * Renderer for cistern blocks
 */
public class CisternTileEntityRenderer extends TileEntityRenderer<CisternTileEntity> {
  private static final Map<BlockState,Pair<FluidCuboid,FluidCuboid>> CUBOID_MAP = new HashMap<>();
  private static final Vector3f CUBOID_FROM_BASE = new Vector3f(3, 2, 3);
  private static final Vector3f CUBOID_FROM_EXTENSION = new Vector3f(3, -1, 3);
  private static final Vector3f CUBOID_TO = new Vector3f(13, 15, 13);

  /**
   * Gets the fluid cuboids for rendering this state
   * @param state  State to render
   * @return  Pair of cuboids for between 0 and full, and for overfull (fluid rendered in block above)
   */
  private static Pair<FluidCuboid,FluidCuboid> getCuboid(BlockState state) {
    // determine starting height
    Vector3f from = state.get(CisternBlock.EXTENSION) ? CUBOID_FROM_EXTENSION : CUBOID_FROM_BASE;

    // determine which side faces to add
    Map<Direction,FluidFace> faces = new EnumMap<>(Direction.class);
    for (Direction direction : Plane.HORIZONTAL) {
      if (state.get(CisternBlock.CONNECTIONS.get(direction))) {
        faces.put(direction, FluidFace.NORMAL);
      }
    }

    // if we have any sides, add an overfull model
    FluidCuboid overfull = null;
    if (!faces.isEmpty()) {
      overfull = new FluidCuboid(from, CUBOID_TO, new EnumMap<>(faces));
    }

    // add a top face for the regular model
    faces.put(Direction.UP, FluidFace.NORMAL);
    return Pair.of(new FluidCuboid(from, CUBOID_TO, faces), overfull);
  }

  /**
   * Gets the cached fluid cuboids for rendering this state
   * @param state  State to render
   * @return  Pair of cuboids for between 0 and full, and for overfull (fluid rendered in block above)
   */
  private static Pair<FluidCuboid, FluidCuboid> getCachedCuboid(BlockState state) {
    return CUBOID_MAP.computeIfAbsent(state, CisternTileEntityRenderer::getCuboid);
  }

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
        // get the cuboid pair, if the capacity is above the capacity per cistern, use the overfull model (no top face)
        Pair<FluidCuboid,FluidCuboid> pair = getCachedCuboid(tileEntity.getBlockState());
        FluidCuboid cuboid;
        if (amount > CisternTank.BASE_CAPACITY) {
          cuboid = pair.getSecond();
          amount = CisternTank.BASE_CAPACITY;
        } else {
          cuboid = pair.getFirst();
        }
        // skip if no cuboid (means overfull and no side faces)
        if (cuboid != null) {
          FluidAttributes attributes = fluid.getFluid().getAttributes();
          // we are using still regardless, so no point fetching flowing
          TextureAtlasSprite still = FluidRenderer.getBlockSprite(attributes.getStillTexture(fluid));
          // determine the relevant height
          Vector3f from = cuboid.getFromScaled();
          Vector3f to = cuboid.getToScaled().copy();
          float minY = from.getY();
          float maxY = to.getY();
          to.setY(minY + amount * (maxY - minY) / (float)CisternTank.BASE_CAPACITY);
          // render the cuboid using Mantle's logic
          FluidRenderer.renderCuboid(matrices, buffer.getBuffer(FluidRenderer.RENDER_TYPE), cuboid, still, still, from, to, attributes.getColor(fluid), light, false);
        }
      }
    }
  }
}

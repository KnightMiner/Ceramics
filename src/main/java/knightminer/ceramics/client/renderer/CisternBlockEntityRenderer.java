package knightminer.ceramics.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import knightminer.ceramics.blocks.CisternBlock;
import knightminer.ceramics.blocks.entity.CisternBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Plane;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.templates.EmptyFluidHandler;
import org.joml.Vector3f;
import slimeknights.mantle.client.render.FluidCuboid;
import slimeknights.mantle.client.render.FluidRenderer;
import slimeknights.mantle.client.render.MantleRenderTypes;

/**
 * Renderer for cistern blocks
 */
public class CisternBlockEntityRenderer implements BlockEntityRenderer<CisternBlockEntity> {
  public CisternBlockEntityRenderer(BlockEntityRendererProvider.Context context) {}

  @Override
  public void render(CisternBlockEntity tileEntity, float partialTicks, PoseStack matrices, MultiBufferSource buffer, int light, int combinedOverlay) {
    FluidStack fluid = tileEntity.getPublicHandler().orElse(EmptyFluidHandler.INSTANCE).getFluidInTank(0);
    if (!fluid.isEmpty()) {
      int renderIndex = tileEntity.getRenderIndex();
      // capacity for gives us the minimum amount to start rendering in this segement
      // render nothing beyond the base capacity
      int amount = fluid.getAmount() - tileEntity.capacityFor(renderIndex);
      if (amount > 0) {
        // get the model pair, if the capacity is above the capacity per cistern, use the overfull model (no top face)
        BlockState state = tileEntity.getBlockState();
        CisternFluids model = CisternFluids.REGISTRY.get(state.getBlock());
        if (model != null) {
          // fetch textures and attributes
          IClientFluidTypeExtensions client = IClientFluidTypeExtensions.of(fluid.getFluid());
          TextureAtlasSprite still = FluidRenderer.getBlockSprite(client.getStillTexture(fluid));
          TextureAtlasSprite flowing = FluidRenderer.getBlockSprite(client.getFlowingTexture(fluid));
          VertexConsumer builder = buffer.getBuffer(MantleRenderTypes.FLUID);
          int color = client.getTintColor(fluid);
          light = FluidRenderer.withBlockLight(light, fluid.getFluid().getFluidType().getLightLevel(fluid));

          // if full, just render all full sides
          int capacityPerLayer = tileEntity.capacityPerLayer();
          if (amount > capacityPerLayer) {
            for (Direction direction : Plane.HORIZONTAL) {
              // state and model must contain that direction
              FluidCuboid cuboid = model.side(direction);
              if (cuboid != null && state.getValue(CisternBlock.CONNECTIONS.get(direction))) {
                FluidRenderer.renderCuboid(matrices, builder, cuboid, still, flowing, cuboid.getFromScaled(), cuboid.getToScaled(), color, light, false);
              }
            }
          } else {
            // determine the relevant height of the center
            FluidCuboid center = model.base(state.getValue(CisternBlock.EXTENSION));
            Vector3f from = center.getFromScaled();
            Vector3f to = new Vector3f(center.getToScaled());
            float minY = from.y();
            to.y = minY + amount * (to.y() - minY) / (float)capacityPerLayer;
            // render the center using Mantle's logic
            FluidRenderer.renderCuboid(matrices, builder, center, still, still, from, to, color, light, false);

            // scale the sides based on the center
            for (Direction direction : Plane.HORIZONTAL) {
              // state and model must contain that direction
              FluidCuboid cuboid = model.side(direction);
              if (cuboid != null && state.getValue(CisternBlock.CONNECTIONS.get(direction))) {
                // bottom of the side must be smaller than the height to consider
                Vector3f sFrom = cuboid.getFromScaled();
                if (sFrom.y() < to.y()) {
                  // if the side end is larger than the center, clamp it down
                  Vector3f sTo = cuboid.getToScaled();
                  if (sTo.y() > to.y()) {
                    sTo = new Vector3f(sTo);
                    sTo.y = to.y();
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

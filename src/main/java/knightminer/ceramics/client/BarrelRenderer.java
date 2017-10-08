package knightminer.ceramics.client;

import javax.annotation.Nonnull;

import knightminer.ceramics.library.tank.BarrelTank;
import knightminer.ceramics.tileentity.TileBarrel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.model.animation.FastTESR;
import net.minecraftforge.fluids.FluidStack;

public class BarrelRenderer extends FastTESR<TileBarrel> {
	public static Minecraft mc = Minecraft.getMinecraft();

	@Override
	public void renderTileEntityFast(@Nonnull TileBarrel barrel, double x, double y, double z, float partialTicks, int destroyStage, float partial, BufferBuilder renderer) {
		BarrelTank tank = barrel.getTank();

		FluidStack fluid = tank.getFluid();
		if(fluid == null) {
			return;
		}

		BlockPos pos = barrel.getPos();
		int blockHeight = 1 + barrel.height;

		float height = ((float) fluid.amount - tank.renderOffset) / tank.getCapacity() * blockHeight - 0.0625f;

		// clamp the height between the minimim location (little over 1/16) and the maximum (height - 16)
		height = Math.max(0.0675f, Math.min(height, blockHeight - 0.0625f));

		if(tank.renderOffset > 1.2f || tank.renderOffset < -1.2f) {
			tank.renderOffset -= (tank.renderOffset / 12f + 0.1f) * partialTicks;
		}
		else {
			tank.renderOffset = 0;
		}

		renderer.setTranslation(x, y, z);

		TextureAtlasSprite sprite = mc.getTextureMapBlocks().getTextureExtry(fluid.getFluid().getStill(fluid).toString());
		int color = fluid.getFluid().getColor(fluid);
		int brightness = mc.world.getCombinedLight(pos, fluid.getFluid().getLuminosity());
		RenderUtils.putTexturedQuad(renderer, sprite, 0.125, height, 0.125, 0.75, 0, 0.75, EnumFacing.UP, color, brightness, false);
		renderer.setTranslation(0, 0, 0);
	}
}

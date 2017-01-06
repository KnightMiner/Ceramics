package knightminer.ceramics.client;

import javax.annotation.Nonnull;

import org.lwjgl.opengl.GL11;

import knightminer.ceramics.library.BarrelTank;
import knightminer.ceramics.tileentity.TileBarrel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fluids.FluidStack;

public class BarrelRenderer extends TileEntitySpecialRenderer<TileBarrel> {
	public static Minecraft mc = Minecraft.getMinecraft();

	@Override
	public void renderTileEntityAt(@Nonnull TileBarrel barrel, double x, double y, double z, float partialTicks, int destroyStage) {
		BarrelTank tank = barrel.getTank();

		FluidStack fluid = tank.getFluid();
		if(fluid == null) {
			return;
		}

		BlockPos pos = barrel.getPos();
		int blockHeight = 1;
		if(barrel.topPos != null) {
			pos = barrel.topPos;
			blockHeight = 1 + barrel.topPos.getY() - barrel.getPos().getY();
		}

		float height = ((float) fluid.amount - tank.renderOffset) / tank.getCapacity() * blockHeight - 0.0625f;

		// clamp the height between the minimim location (little over 1/16) and the maximum (height - 16)
		height = Math.max(0.0675f, Math.min(height, blockHeight - 0.0625f));

		if(tank.renderOffset > 1.2f || tank.renderOffset < -1.2f) {
			tank.renderOffset -= (tank.renderOffset / 12f + 0.1f) * partialTicks;
		}
		else {
			tank.renderOffset = 0;
		}

		Tessellator tessellator = Tessellator.getInstance();
		VertexBuffer renderer = tessellator.getBuffer();
		renderer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
		mc.renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

		RenderUtils.pre(x, y, z);
		GlStateManager.disableCull();

		TextureAtlasSprite sprite = mc.getTextureMapBlocks().getTextureExtry(fluid.getFluid().getStill(fluid).toString());

		// uv
		double minU = sprite.getInterpolatedU(2);
		double maxU = sprite.getInterpolatedU(14);
		double minV = sprite.getInterpolatedV(2);
		double maxV = sprite.getInterpolatedV(14);

		// color data
		int color = fluid.getFluid().getColor(fluid);
		int a = color >> 24 & 0xFF;
		int r = color >> 16 & 0xFF;
		int g = color >> 8 & 0xFF;
		int b = color & 0xFF;

		// lighting
		int brightness = mc.world.getCombinedLight(pos, fluid.getFluid().getLuminosity());
		int light1 = brightness >> 0x10 & 0xFFFF;
		int light2 = brightness & 0xFFFF;

		renderer.pos(0.125, height, 0.125).color(r, g, b, a).tex(minU, minV).lightmap(light1, light2).endVertex();
		renderer.pos(0.125, height, 0.875).color(r, g, b, a).tex(minU, maxV).lightmap(light1, light2).endVertex();
		renderer.pos(0.875, height, 0.875).color(r, g, b, a).tex(maxU, maxV).lightmap(light1, light2).endVertex();
		renderer.pos(0.875, height, 0.125).color(r, g, b, a).tex(maxU, minV).lightmap(light1, light2).endVertex();

		tessellator.draw();
		GlStateManager.enableCull();

		RenderUtils.post();

	}
}

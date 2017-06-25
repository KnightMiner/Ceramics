package knightminer.ceramics.client;

import org.lwjgl.opengl.GL11;

import knightminer.ceramics.library.BarrelTank;
import knightminer.ceramics.tileentity.TileBarrel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fluids.FluidStack;

public class BarrelRenderer extends TileEntitySpecialRenderer<TileBarrel> {
	public static Minecraft mc = Minecraft.getMinecraft();

	@Override
	public void renderTileEntityAt(TileBarrel barrel, double x, double y, double z, float partialTicks, int destroyStage, float p_192841_10_) {
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

		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder renderer = tessellator.getBuffer();
		renderer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
		mc.renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

		RenderUtils.pre(x, y, z);
		GlStateManager.disableCull();

		TextureAtlasSprite sprite = mc.getTextureMapBlocks().getTextureExtry(fluid.getFluid().getStill(fluid).toString());
		int color = fluid.getFluid().getColor(fluid);
		int brightness = mc.world.getCombinedLight(pos, fluid.getFluid().getLuminosity());
		RenderUtils.putTexturedQuad(renderer, sprite, 0.125, height, 0.125, 0.75, 0, 0.75, EnumFacing.UP, color, brightness, false);

		tessellator.draw();
		GlStateManager.enableCull();

		RenderUtils.post();
	}
}

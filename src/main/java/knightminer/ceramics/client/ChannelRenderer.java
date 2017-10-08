package knightminer.ceramics.client;

import javax.annotation.Nonnull;

import org.lwjgl.opengl.GL11;

import knightminer.ceramics.blocks.BlockChannel;
import knightminer.ceramics.tileentity.TileChannel;
import knightminer.ceramics.tileentity.TileChannel.ChannelConnection;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

public class ChannelRenderer extends TileEntitySpecialRenderer<TileChannel> {
	private static Minecraft mc = Minecraft.getMinecraft();

	@Override
	public void render(@Nonnull TileChannel te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
		FluidStack fluidStack = te.getTank().getFluid();
		if(fluidStack == null) {
			return;
		}
		Fluid fluid = fluidStack.getFluid();
		if(fluid == null) {
			return;
		}

		World world = te.getWorld();
		BlockPos pos = te.getPos();

		// start with the center fluid
		RenderUtils.pre(x, y, z);

		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder renderer = tessellator.getBuffer();
		renderer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
		mc.renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		int color = fluid.getColor(fluidStack);
		int brightness = te.getWorld().getCombinedLight(te.getPos(), fluid.getLuminosity());
		TextureMap map = mc.getTextureMapBlocks();
		TextureAtlasSprite still = map.getTextureExtry(fluid.getStill(fluidStack).toString());
		TextureAtlasSprite flowing = map.getTextureExtry(fluid.getFlowing(fluidStack).toString());

		// sides
		double x1 = 0, z1 = 0, x2 = 0, z2 = 0;
		EnumFacing rotation, oneOutput = null;
		int outputs = 0;
		for(EnumFacing side : EnumFacing.HORIZONTALS) {
			if(te.isFlowing(side)) {
				ChannelConnection connection = te.getConnection(side);
				if(!ChannelConnection.canFlow(connection)) {
					continue;
				}

				// first, get location for side
				// these are the coords for flows going into the channel
				switch(side) {
					case NORTH:
						x1 = 0.375;
						z1 = 0;
						x2 = 0.625;
						z2 = 0.375;
						break;
					case SOUTH:
						x1 = 0.375;
						z1 = 0.625;
						x2 = 0.625;
						z2 = 1;
						break;
					case WEST:
						x1 = 0;
						z1 = 0.375;
						x2 = 0.375;
						z2 = 0.625;
						break;
					case EAST:
						x1 = 0.625;
						z1 = 0.375;
						x2 = 1;
						z2 = 0.625;
						break;
				}

				// next, direction of flow
				// in means we are going the opposite direction
				if(connection == ChannelConnection.IN) {
					rotation = side;
				} else {
					rotation = side.getOpposite();
					outputs++;
					oneOutput = rotation;
				}

				RenderUtils.putRotatedQuad(renderer, flowing, x1, 0.46875, z1, x2-x1, z2-z1, rotation, color, brightness, true);

				// only render the extra piece if no channel on this side
				if(!(world.getBlockState(pos.offset(side)).getBlock() instanceof BlockChannel)) {
					RenderUtils.putTexturedQuad(renderer, flowing, x1, 0.375, z1, x2-x1, 0.09375, z2-z1, side, color, brightness, true);
				}
			} else {
				// sides of main sliver
				RenderUtils.putTexturedQuad(renderer, flowing, 0.375, 0.375, 0.375, 0.25, 0.09375, 0.25, side, color, brightness, true);
			}
		}

		// the stuff in the center
		// if we have just one output, have the center flow towards that
		if(outputs == 1) {
			RenderUtils.putRotatedQuad(renderer, flowing, 0.375, 0.46875, 0.375, 0.25, 0.25, oneOutput, color, brightness, true);
		} else {
			RenderUtils.putTexturedQuad(renderer, still, 0.375, 0.46875, 0.375, 0.25, 0, 0.25, EnumFacing.UP, color, brightness, false);
		}

		// downwards flow
		if(te.isFlowing(EnumFacing.DOWN) && te.isConnectedDown()) {
			// check how far into the 2nd block we want to render
			BlockPos below = pos.down();
			IBlockState state = world.getBlockState(below);
			float yMin = -FaucetRenderer.depth.getDepth(world, below, state);

			double xz1 = 0.375;
			double y1 = 0;
			double wd = 0.25;
			double h = 0.25;
			RenderUtils.putTexturedQuad(renderer, flowing, xz1, y1, xz1, wd, h, wd, EnumFacing.NORTH, color, brightness, true);
			RenderUtils.putTexturedQuad(renderer, flowing, xz1, y1, xz1, wd, h, wd, EnumFacing.EAST,  color, brightness, true);
			RenderUtils.putTexturedQuad(renderer, flowing, xz1, y1, xz1, wd, h, wd, EnumFacing.SOUTH, color, brightness, true);
			RenderUtils.putTexturedQuad(renderer, flowing, xz1, y1, xz1, wd, h, wd, EnumFacing.WEST,  color, brightness, true);

			if(yMin < 0) {
				y1 = yMin;
				h = -yMin;
				RenderUtils.putTexturedQuad(renderer, flowing, xz1, y1, xz1, wd, h, wd, EnumFacing.NORTH, color, brightness, true);
				RenderUtils.putTexturedQuad(renderer, flowing, xz1, y1, xz1, wd, h, wd, EnumFacing.EAST,  color, brightness, true);
				RenderUtils.putTexturedQuad(renderer, flowing, xz1, y1, xz1, wd, h, wd, EnumFacing.SOUTH, color, brightness, true);
				RenderUtils.putTexturedQuad(renderer, flowing, xz1, y1, xz1, wd, h, wd, EnumFacing.WEST,  color, brightness, true);
			}
			// draw at current bottom
			RenderUtils.putTexturedQuad(renderer, flowing, xz1, y1, xz1, wd, h, wd, EnumFacing.DOWN,  color, brightness, true);
		}

		tessellator.draw();
		RenderUtils.post();
	}
}

package knightminer.ceramics.client;

import javax.annotation.Nonnull;

import org.lwjgl.opengl.GL11;

import knightminer.ceramics.blocks.BlockFaucet;
import knightminer.ceramics.library.IFaucetDepthFallback;
import knightminer.ceramics.library.ModIDs;
import knightminer.ceramics.tileentity.TileFaucet;
import net.minecraft.block.state.IBlockState;
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
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Loader;
import slimeknights.tconstruct.library.smeltery.IFaucetDepth;

public class FaucetRenderer extends TileEntitySpecialRenderer<TileFaucet> {

	private static IDepthGetter depth;
	static {
		// if Tinkers Construct is loaded, pipe to its interface
		if(Loader.isModLoaded(ModIDs.TINKERS)) {
			depth = (world, pos, state) -> {
				if(state.getBlock() instanceof IFaucetDepth) {
					return ((IFaucetDepth)state.getBlock()).getFlowDepth(world, pos, state);
				}

				return 15f/16f;
			};
			// otherwise use mine, really just a fallback
		} else {
			depth = (world, pos, state) -> {
				if(state.getBlock() instanceof IFaucetDepthFallback) {
					return ((IFaucetDepthFallback)state.getBlock()).getFlowDepth(world, pos, state);
				}

				return 15f/16f;
			};
		}
	}

	@Override
	public void render(@Nonnull TileFaucet te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
		if(!te.isPouring || te.drained == null) {
			return;
		}

		// check how far into the 2nd block we want to render
		World world = te.getWorld();
		BlockPos pos = te.getPos();
		BlockPos below = pos.down();
		IBlockState belowState = world.getBlockState(below);

		float yMin = -depth.getDepth(world, below, belowState);

		if(te.direction == EnumFacing.UP) {
			RenderUtils.renderFluidCuboid(te.drained, te.getPos(), x, y, z, 0.375, 0, 0.375, 0.625, 1f, 0.625);
			// render in the block beneath
			if(yMin < 0) {
				RenderUtils.renderFluidCuboid(te.drained, te.getPos(), x, y, z, 0.375, yMin, 0.375, 0.625, 0f, 0.625);
			}
		}
		// for horizontal we use custom rendering so we can rotate it and have the flowing texture in the faucet part
		// default direction is north because that makes the fluid flow into the right direction through the UVs
		else if(te.direction.getHorizontalIndex() >= 0) {
			IBlockState state = world.getBlockState(pos);
			boolean connected = false;
			if(state.getBlock() instanceof BlockFaucet) {
				connected = state.getActualState(world, pos).getValue(BlockFaucet.CONNECTED);
			}

			float r = -90f * (2 + te.direction.getHorizontalIndex());
			float o = 0.5f;
			// custom rendering for flowing on top
			RenderUtils.pre(x, y, z);

			Tessellator tessellator = Tessellator.getInstance();
			BufferBuilder renderer = tessellator.getBuffer();
			renderer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
			Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
			int color = te.drained.getFluid().getColor(te.drained);
			int brightness = te.getWorld().getCombinedLight(te.getPos(), te.drained.getFluid().getLuminosity());
			TextureAtlasSprite flowing = Minecraft.getMinecraft().getTextureMapBlocks().getTextureExtry(te.drained.getFluid().getFlowing(te.drained).toString());

			GlStateManager.translate(o, 0, o);
			GlStateManager.rotate(r, 0, 1, 0);
			GlStateManager.translate(-o, 0, -o);

			double x1 = 0.375;
			double x2 = 0.625;
			double y1 = 0.375;
			double y2 = 0.625;
			// push the flow back one pixel if connected to a barrel
			double z1 = connected ? -0.0625 : 0;
			double z2 = 0.375;

			// the stuff in the faucet
			RenderUtils.putTexturedQuad(renderer, flowing, x1, y1, z1, x2-x1, y2-y1, z2-z1, EnumFacing.NORTH, color, brightness, true);
			RenderUtils.putTexturedQuad(renderer, flowing, x1, y1, z1, x2-x1, y2-y1, z2-z1, EnumFacing.UP,    color, brightness, true);

			// the stuff flowing down
			y1 = 0;
			z1 = 0.375;
			z2 = 0.5;
			RenderUtils.putTexturedQuad(renderer, flowing, x1, y1, z1, x2-x1, y2-y1, z2-z1, EnumFacing.NORTH, color, brightness, true);
			RenderUtils.putTexturedQuad(renderer, flowing, x1, y1, z1, x2-x1, y2-y1, z2-z1, EnumFacing.EAST,  color, brightness, true);
			RenderUtils.putTexturedQuad(renderer, flowing, x1, y1, z1, x2-x1, y2-y1, z2-z1, EnumFacing.SOUTH, color, brightness, true);
			RenderUtils.putTexturedQuad(renderer, flowing, x1, y1, z1, x2-x1, y2-y1, z2-z1, EnumFacing.WEST,  color, brightness, true);
			RenderUtils.putTexturedQuad(renderer, flowing, x1, y1, z1, x2-x1, y2-y1, z2-z1, EnumFacing.UP,    color, brightness, true);

			// render in the block beneath
			if(yMin < 0) {
				y1 = yMin;
				y2 = 0;
				RenderUtils.putTexturedQuad(renderer, flowing, x1, y1, z1, x2-x1, y2-y1, z2-z1, EnumFacing.DOWN,  color, brightness, true);
				RenderUtils.putTexturedQuad(renderer, flowing, x1, y1, z1, x2-x1, y2-y1, z2-z1, EnumFacing.NORTH, color, brightness, true);
				RenderUtils.putTexturedQuad(renderer, flowing, x1, y1, z1, x2-x1, y2-y1, z2-z1, EnumFacing.EAST,  color, brightness, true);
				RenderUtils.putTexturedQuad(renderer, flowing, x1, y1, z1, x2-x1, y2-y1, z2-z1, EnumFacing.SOUTH, color, brightness, true);
				RenderUtils.putTexturedQuad(renderer, flowing, x1, y1, z1, x2-x1, y2-y1, z2-z1, EnumFacing.WEST,  color, brightness, true);
			} else {
				// render the bottom of the flow only if needed
				RenderUtils.putTexturedQuad(renderer, flowing, x1, y1, z1, x2-x1, y2-y1, z2-z1, EnumFacing.DOWN,  color, brightness, true);
			}

			tessellator.draw();
			RenderUtils.post();
		}
	}

	private static interface IDepthGetter {
		float getDepth(World world, BlockPos pos, IBlockState state);
	}
}

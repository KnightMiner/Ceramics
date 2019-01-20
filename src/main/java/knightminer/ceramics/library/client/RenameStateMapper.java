package knightminer.ceramics.library.client;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;

@SideOnly(Side.CLIENT)
public class RenameStateMapper extends StateMapperBase {

	private final ResourceLocation location;

	public RenameStateMapper(ResourceLocation location) {
		this.location = location;
	}

	@Nonnull
	@Override
	protected ModelResourceLocation getModelResourceLocation(@Nonnull IBlockState state) {
		return new ModelResourceLocation(location, this.getPropertyString(state.getProperties()));
	}
}

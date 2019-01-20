package knightminer.ceramics.library.client;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.util.ResourceLocation;

public class IgnoreAllStateMapper extends StateMapperBase {

	public static final IgnoreAllStateMapper INSTANCE = new IgnoreAllStateMapper();
	private final ResourceLocation location;

	public IgnoreAllStateMapper() {
		this(null);
	}

	public IgnoreAllStateMapper(ResourceLocation location) {
		this.location = location;
	}

	@Override
	protected ModelResourceLocation getModelResourceLocation(IBlockState state) {
		if (location != null) {
			return new ModelResourceLocation(location, "normal");
		}
		return new ModelResourceLocation(state.getBlock().getRegistryName(), "normal");
	}

}

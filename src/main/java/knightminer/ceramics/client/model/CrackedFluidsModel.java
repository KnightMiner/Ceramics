package knightminer.ceramics.client.model;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.IModelConfiguration;
import net.minecraftforge.client.model.IModelLoader;
import slimeknights.mantle.client.model.fluid.FluidCuboid;
import slimeknights.mantle.client.model.fluid.FluidsModel;
import slimeknights.mantle.client.model.util.SimpleBlockModel;

import java.util.List;
import java.util.function.Function;

/** Variant of {@link FluidsModel} that supports cracking */
public class CrackedFluidsModel extends CrackedModel {
	public static final Loader LOADER = new Loader();

	private final List<FluidCuboid> fluids;
	public CrackedFluidsModel(SimpleBlockModel model, List<FluidCuboid> fluids) {
		super(model);
		this.fluids = fluids;
	}

	@Override
	public BakedModel bake(IModelConfiguration owner, ModelBakery bakery, Function<Material,TextureAtlasSprite> spriteGetter, ModelState transform, ItemOverrides overrides, ResourceLocation location) {
		BakedModel baked = super.bake(owner, bakery, spriteGetter, transform, overrides, location);
		return new FluidsModel.BakedModel(baked, this.fluids);
	}

	/** Loader class */
	private static class Loader implements IModelLoader<CrackedModel> {
		@Override
		public void onResourceManagerReload(ResourceManager resourceManager) {}

		@Override
		public CrackedFluidsModel read(JsonDeserializationContext deserializationContext, JsonObject modelContents) {
			SimpleBlockModel model = SimpleBlockModel.deserialize(deserializationContext, modelContents);
			List<FluidCuboid> fluid = FluidCuboid.listFromJson(modelContents, "fluids");
			return new CrackedFluidsModel(model, fluid);
		}
	}
}

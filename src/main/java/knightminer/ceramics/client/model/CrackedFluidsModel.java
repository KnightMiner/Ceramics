package knightminer.ceramics.client.model;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.IModelTransform;
import net.minecraft.client.renderer.model.ItemOverrideList;
import net.minecraft.client.renderer.model.ModelBakery;
import net.minecraft.client.renderer.model.RenderMaterial;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
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
	public IBakedModel bake(IModelConfiguration owner, ModelBakery bakery, Function<RenderMaterial,TextureAtlasSprite> spriteGetter, IModelTransform transform, ItemOverrideList overrides, ResourceLocation location) {
		IBakedModel baked = super.bake(owner, bakery, spriteGetter, transform, overrides, location);
		return new FluidsModel.BakedModel(baked, this.fluids);
	}

	/** Loader class */
	private static class Loader implements IModelLoader<CrackedModel> {
		@Override
		public void onResourceManagerReload(IResourceManager resourceManager) {}

		@Override
		public CrackedFluidsModel read(JsonDeserializationContext deserializationContext, JsonObject modelContents) {
			SimpleBlockModel model = SimpleBlockModel.deserialize(deserializationContext, modelContents);
			List<FluidCuboid> fluid = FluidCuboid.listFromJson(modelContents, "fluids");
			return new CrackedFluidsModel(model, fluid);
		}
	}
}

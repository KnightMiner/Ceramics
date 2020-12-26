package knightminer.ceramics.client.model;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.IModelTransform;
import net.minecraft.client.renderer.model.IUnbakedModel;
import net.minecraft.client.renderer.model.ItemOverrideList;
import net.minecraft.client.renderer.model.ModelBakery;
import net.minecraft.client.renderer.model.RenderMaterial;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Plane;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.BakedModelWrapper;
import net.minecraftforge.client.model.IModelConfiguration;
import net.minecraftforge.client.model.IModelLoader;
import net.minecraftforge.client.model.geometry.IModelGeometry;
import slimeknights.mantle.client.model.fluid.FluidCuboid;
import slimeknights.mantle.client.model.util.SimpleBlockModel;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * Similar to {@link slimeknights.mantle.client.model.fluid.FluidsModel}, but has a cuboid per connection "side".
 * Used since there is no easy way to handle multipart in the fluid cuboid system.
 */
public class CisternModel implements IModelGeometry<CisternModel> {
	/** Model loader instance */
	public static final Loader LOADER = new Loader();

	/** Base block model */
	private final SimpleBlockModel model;
	/** Map of side to fluid. {@link Direction#UP} represents extension center, {@link Direction#DOWN} base center */
	private final Map<Direction,FluidCuboid> fluids;

	public CisternModel(SimpleBlockModel model, Map<Direction,FluidCuboid> fluids) {
		this.model = model;
		this.fluids = fluids;
	}

	@Override
	public Collection<RenderMaterial> getTextures(IModelConfiguration owner, Function<ResourceLocation,IUnbakedModel> modelGetter, Set<Pair<String,String>> missingTextureErrors) {
		return model.getTextures(owner, modelGetter, missingTextureErrors);
	}

	@Override
	public IBakedModel bake(IModelConfiguration owner, ModelBakery bakery, Function<RenderMaterial,TextureAtlasSprite> spriteGetter, IModelTransform transform, ItemOverrideList overrides, ResourceLocation location) {
		IBakedModel baked = this.model.bakeModel(owner, transform, overrides, spriteGetter, location);
		return new BakedModel(baked, this.fluids);
	}

	/**
	 * Baked model wrapper for cistern models
	 */
	public static class BakedModel extends BakedModelWrapper<IBakedModel> {
		/** Map of side to fluid. {@link Direction#UP} represents extension center, {@link Direction#DOWN} base center */
		private final Map<Direction,FluidCuboid> fluids;
		private BakedModel(IBakedModel originalModel, final Map<Direction,FluidCuboid> fluids) {
			super(originalModel);
			this.fluids = fluids;
		}

		/**
		 * Gets the cuboid for the center
		 * @return  Cuboid for center
		 */
		public FluidCuboid getCenterFluid(boolean extension) {
			return this.fluids.get(extension ? Direction.UP : Direction.DOWN);
		}

		/**
		 * Gets the cuboid for the given side
		 * @param direction  Direction to check
		 * @return  Cuboid
		 */
		@Nullable
		public FluidCuboid getFluid(Direction direction) {
			return this.fluids.get(direction);
		}
	}

	/** Model loader */
	private static class Loader implements IModelLoader<CisternModel> {
		@Override
		public void onResourceManagerReload(IResourceManager resourceManager) {}

		@Override
		public CisternModel read(JsonDeserializationContext deserializationContext, JsonObject modelContents) {
			SimpleBlockModel model = SimpleBlockModel.deserialize(deserializationContext, modelContents);
			// parse fluid cuboid for each side
			JsonObject fluidJson = JSONUtils.getJsonObject(modelContents, "fluids");
			Map<Direction,FluidCuboid> fluids = new EnumMap<>(Direction.class);
			// Y axis reused for base and extension
			fluids.put(Direction.DOWN, FluidCuboid.fromJson(JSONUtils.getJsonObject(fluidJson, "base")));
			fluids.put(Direction.UP, FluidCuboid.fromJson(JSONUtils.getJsonObject(fluidJson, "extension")));
			// sides as themselves
			for (Direction direction : Plane.HORIZONTAL) {
				if (fluidJson.has(direction.getString())) {
					fluids.put(direction, FluidCuboid.fromJson(JSONUtils.getJsonObject(fluidJson, direction.getString())));
				}
			}
			return new CisternModel(model, fluids);
		}
	}
}

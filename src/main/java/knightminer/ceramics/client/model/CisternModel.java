package knightminer.ceramics.client.model;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Plane;
import net.minecraft.util.GsonHelper;
import net.minecraft.resources.ResourceLocation;
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
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Similar to {@link slimeknights.mantle.client.model.fluid.FluidsModel}, but has a cuboid per connection "side".
 * Used since there is no easy way to handle multipart in the fluid cuboid system.
 */
public class CisternModel implements IModelGeometry<CisternModel> {
	/** Model loader instance */
	public static final Loader<CisternModel> LOADER = new Loader<>(CisternModel::new);
	/** Model loader for cracked models */
	public static final Loader<CrackedModel> CRACKED_LOADER = new Loader<>(Cracked::new);

	/** Base block model */
	private final SimpleBlockModel model;
	/** Map of side to fluid. {@link Direction#UP} represents extension center, {@link Direction#DOWN} base center */
	private final Map<Direction,FluidCuboid> fluids;

	public CisternModel(SimpleBlockModel model, Map<Direction,FluidCuboid> fluids) {
		this.model = model;
		this.fluids = fluids;
	}

	@Override
	public Collection<Material> getTextures(IModelConfiguration owner, Function<ResourceLocation,UnbakedModel> modelGetter, Set<Pair<String,String>> missingTextureErrors) {
		return model.getTextures(owner, modelGetter, missingTextureErrors);
	}

	@Override
	public BakedModel bake(IModelConfiguration owner, ModelBakery bakery, Function<Material,TextureAtlasSprite> spriteGetter, ModelState transform, ItemOverrides overrides, ResourceLocation location) {
		BakedModel baked = this.model.bakeModel(owner, transform, overrides, spriteGetter, location);
		return new BakedModel(baked, this.fluids);
	}

	/** Model geometrry for a cracked cistern */
	private static class Cracked extends CrackedModel {
		/** Map of side to fluid. {@link Direction#UP} represents extension center, {@link Direction#DOWN} base center */
		private final Map<Direction,FluidCuboid> fluids;
		public Cracked(SimpleBlockModel model, Map<Direction,FluidCuboid> fluids) {
			super(model);
			this.fluids = fluids;
		}

		@Override
		public BakedModel bake(IModelConfiguration owner, ModelBakery bakery, Function<Material,TextureAtlasSprite> spriteGetter, ModelState modelTransform, ItemOverrides overrides, ResourceLocation modelLocation) {
			BakedModel model = super.bake(owner, bakery, spriteGetter, modelTransform, overrides, modelLocation);
			return new BakedModel(model, fluids);
		}
	}

	/**
	 * Baked model wrapper for cistern models
	 */
	public static class BakedModel extends BakedModelWrapper<BakedModel> {
		/** Map of side to fluid. {@link Direction#UP} represents extension center, {@link Direction#DOWN} base center */
		private final Map<Direction,FluidCuboid> fluids;
		private BakedModel(BakedModel originalModel, Map<Direction,FluidCuboid> fluids) {
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
	private static class Loader<T extends IModelGeometry<T>> implements IModelLoader<T> {
		private final BiFunction<SimpleBlockModel, Map<Direction,FluidCuboid>, T> constructor;
		public Loader(BiFunction<SimpleBlockModel, Map<Direction,FluidCuboid>, T> constructor) {
			this.constructor = constructor;
		}

		@Override
		public void onResourceManagerReload(ResourceManager resourceManager) {}

		@Override
		public T read(JsonDeserializationContext deserializationContext, JsonObject modelContents) {
			SimpleBlockModel model = SimpleBlockModel.deserialize(deserializationContext, modelContents);
			// parse fluid cuboid for each side
			JsonObject fluidJson = GsonHelper.getAsJsonObject(modelContents, "fluids");
			Map<Direction,FluidCuboid> fluids = new EnumMap<>(Direction.class);
			// Y axis reused for base and extension
			fluids.put(Direction.DOWN, FluidCuboid.fromJson(GsonHelper.getAsJsonObject(fluidJson, "base")));
			fluids.put(Direction.UP, FluidCuboid.fromJson(GsonHelper.getAsJsonObject(fluidJson, "extension")));
			// sides as themselves
			for (Direction direction : Plane.HORIZONTAL) {
				if (fluidJson.has(direction.getSerializedName())) {
					fluids.put(direction, FluidCuboid.fromJson(GsonHelper.getAsJsonObject(fluidJson, direction.getSerializedName())));
				}
			}
			return constructor.apply(model, fluids);
		}
	}
}

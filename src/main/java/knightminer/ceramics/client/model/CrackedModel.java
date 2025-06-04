package knightminer.ceramics.client.model;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import knightminer.ceramics.Ceramics;
import knightminer.ceramics.blocks.entity.CrackableBlockEntityHandler;
import knightminer.ceramics.items.CrackableBlockItem;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockElement;
import net.minecraft.client.renderer.block.model.BlockElementFace;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.client.model.geometry.IGeometryBakingContext;
import net.minecraftforge.client.model.geometry.IGeometryLoader;
import net.minecraftforge.client.model.geometry.IUnbakedGeometry;
import slimeknights.mantle.client.model.util.ColoredBlockModel;
import slimeknights.mantle.client.model.util.DynamicBakedWrapper;
import slimeknights.mantle.client.model.util.ExtraTextureContext;
import slimeknights.mantle.client.model.util.SimpleBlockModel;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;

/** Generic cracked model for cracked clay blocks */
public class CrackedModel implements IUnbakedGeometry<CrackedModel> {
	private static final ResourceLocation BAKE_LOCATION = Ceramics.getResource("dynamic_model_baking");
	public static final IGeometryLoader<CrackedModel> LOADER = CrackedModel::deserialize;

	/** Deserializes the model from JSON */
	public static CrackedModel deserialize(JsonObject json, JsonDeserializationContext context) {
		return new CrackedModel(ColoredBlockModel.deserialize(json, context));
	}

	private final SimpleBlockModel model;
	public CrackedModel(SimpleBlockModel model) {
		this.model = model;
	}

	@Override
	public void resolveParents(Function<ResourceLocation, UnbakedModel> modelGetter, IGeometryBakingContext context) {
		model.resolveParents(modelGetter, context);
	}

	@Override
	public BakedModel bake(IGeometryBakingContext owner, ModelBaker bakery, Function<Material,TextureAtlasSprite> spriteGetter, ModelState transform, ItemOverrides overrides, ResourceLocation location) {
		// fetch textures
		Material[] textures = new Material[5];
		for (int i = 0; i < 5; i++) {
			textures[i] = owner.getMaterial("cracks_" + (i + 1));
			// get missing texture errors if missing
			spriteGetter.apply(textures[i]);
		}

		// create extra quads for cracking
		List<BlockElement> elements = model.getElements();
		List<BlockElement> newElements = new ArrayList<>(elements.size() * 2);
		newElements.addAll(elements);
		for (BlockElement element : elements) {
			Map<Direction,BlockElementFace> mapFaces = new HashMap<>();
			for (Entry<Direction, BlockElementFace> entry : element.faces.entrySet()) {
				BlockElementFace face = entry.getValue();
				mapFaces.put(entry.getKey(), new BlockElementFace(face.cullForDirection, -1, "cracks", face.uv));
			}
			newElements.add(new BlockElement(element.from, element.to, mapFaces, element.rotation, element.shade));
		}

		// wrap the original model
		BakedModel original = model.bake(owner, bakery, spriteGetter, transform, overrides, location);
		return new Baked(original, owner, newElements, textures, transform);
	}

	/** Baked model for this */
	private static class Baked extends DynamicBakedWrapper<BakedModel> {
		private final BakedModel[] crackedModels;
		private final IGeometryBakingContext owner;
		private final List<BlockElement> elements;
		private final Material[] textures;
		private final ModelState transform;
		private final ItemOverrides overrides = new Overrides();

		public Baked(BakedModel originalModel, IGeometryBakingContext owner, List<BlockElement> elements, Material[] textures, ModelState transform) {
			super(originalModel);
			this.crackedModels = new BakedModel[textures.length];
			this.owner = owner;
			this.elements = elements;
			this.textures = textures;
			this.transform = transform;
		}

		/**
		 * Gets the cracked model for the given stage
		 * @param cracks  Cracks between 1 and 5
		 * @return  Cracked model
		 */
		public BakedModel getModel(int cracks) {
			int stage = cracks - 1;
			if (crackedModels[stage] == null) {
				// retexture the parts with the texture for this stage
				IGeometryBakingContext retextured = new ExtraTextureContext(owner, ImmutableMap.of("cracks", textures[stage]));
				crackedModels[stage] = SimpleBlockModel.bakeModel(retextured, elements, Material::sprite, transform, ItemOverrides.EMPTY, BAKE_LOCATION);
			}
			return crackedModels[stage];
		}

		@Override
		public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource random, ModelData data, @Nullable RenderType renderType) {
			Integer cracks = data.get(CrackableBlockEntityHandler.CRACKS);
			if (cracks != null && cracks > 0) {
				return getModel(cracks).getQuads(state, side, random, data, renderType);
			}
			return originalModel.getQuads(state, side, random, data, renderType);
		}

		@Override
		public ItemOverrides getOverrides() {
			return overrides;
		}

		/** Handles the cracks on the item model */
		private class Overrides extends ItemOverrides {
			@Override
			public BakedModel resolve(BakedModel model, ItemStack stack, @Nullable ClientLevel world, @Nullable LivingEntity livingEntity, int seed) {
				int cracks = CrackableBlockItem.getCracks(stack);
				if (cracks > 0) {
					return getModel(cracks);
				}
				return model;
			}
		}
	}
}

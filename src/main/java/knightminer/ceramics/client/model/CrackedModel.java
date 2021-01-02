package knightminer.ceramics.client.model;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import knightminer.ceramics.items.CrackableItemBlock;
import knightminer.ceramics.tileentity.CrackableTileEntityHandler;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.BlockPart;
import net.minecraft.client.renderer.model.BlockPartFace;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.IModelTransform;
import net.minecraft.client.renderer.model.IUnbakedModel;
import net.minecraft.client.renderer.model.ItemOverrideList;
import net.minecraft.client.renderer.model.ModelBakery;
import net.minecraft.client.renderer.model.RenderMaterial;
import net.minecraft.client.renderer.texture.MissingTextureSprite;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModelConfiguration;
import net.minecraftforge.client.model.IModelLoader;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.geometry.IModelGeometry;
import slimeknights.mantle.client.model.util.DynamicBakedWrapper;
import slimeknights.mantle.client.model.util.ExtraTextureConfiguration;
import slimeknights.mantle.client.model.util.SimpleBlockModel;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;

/** Generic cracked model for cracked clay blocks */
public class CrackedModel implements IModelGeometry<CrackedModel> {
	/** Item overrides list, note overrides does not work through a model wrapper */
	public static final ItemOverrideList OVERRIDES = new ItemOverrideList() {
		@Override
		public IBakedModel getOverrideModel(IBakedModel model, ItemStack stack, @Nullable ClientWorld world, @Nullable LivingEntity livingEntity) {
			int cracks = CrackableItemBlock.getCracks(stack);
			if (cracks > 0 && model instanceof BakedModel) {
				return ((BakedModel)model).getModel(cracks);
			}
			return model;
		}
	};

	public static final IModelLoader<CrackedModel> LOADER = new Loader();

	private final SimpleBlockModel model;
	public CrackedModel(SimpleBlockModel model) {
		this.model = model;
	}

	@Override
	public Collection<RenderMaterial> getTextures(IModelConfiguration owner, Function<ResourceLocation,IUnbakedModel> modelGetter, Set<Pair<String,String>> missingTextureErrors) {
		Collection<RenderMaterial> textures = model.getTextures(owner, modelGetter, missingTextureErrors);
		for (int i = 1; i <=5; i++) {
			String name = "cracks_" + i;
			RenderMaterial material = owner.resolveTexture(name);
			if (Objects.equals(material.getTextureLocation(), MissingTextureSprite.getLocation())) {
				missingTextureErrors.add(Pair.of(name, owner.getModelName()));
			}
			textures.add(material);
		}
		return textures;
	}

	@Override
	public IBakedModel bake(IModelConfiguration owner, ModelBakery bakery, Function<RenderMaterial,TextureAtlasSprite> spriteGetter, IModelTransform transform, ItemOverrideList overrides, ResourceLocation location) {
		// fetch textures
		RenderMaterial[] textures = new RenderMaterial[5];
		for (int i = 0; i < 5; i++) {
			textures[i] = owner.resolveTexture("cracks_" + (i + 1));
		}

		// create extra quads
		List<BlockPart> elements = model.getElements();
		List<BlockPart> newElements = new ArrayList<>(elements.size() * 2);
		newElements.addAll(elements);
		for (BlockPart element : elements) {
			Map<Direction,BlockPartFace> mapFaces = new HashMap<>();
			for (Entry<Direction, BlockPartFace> entry : element.mapFaces.entrySet()) {
				BlockPartFace face = entry.getValue();
				mapFaces.put(entry.getKey(), new BlockPartFace(face.cullFace, -1, "cracks", face.blockFaceUV));
			}
			newElements.add(new BlockPart(element.positionFrom, element.positionTo, mapFaces, element.partRotation, element.shade));
		}

		// wrap the original model
		IBakedModel original = model.bakeModel(owner, transform, OVERRIDES, spriteGetter, location);
		return new BakedModel(original, owner, newElements, textures, transform);
	}

	/** Baked model for this */
	private static class BakedModel extends DynamicBakedWrapper<IBakedModel> {
		private final IBakedModel[] crackedModels;
		private final IModelConfiguration owner;
		private final List<BlockPart> elements;
		private final RenderMaterial[] textures;
		private final IModelTransform transform;

		public BakedModel(IBakedModel originalModel, IModelConfiguration owner, List<BlockPart> elements, RenderMaterial[] textures, IModelTransform transform) {
			super(originalModel);
			this.crackedModels = new IBakedModel[textures.length];
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
		public IBakedModel getModel(int cracks) {
			int stage = cracks - 1;
			if (crackedModels[stage] == null) {
				// retexture the parts with the texture for this stage
				IModelConfiguration retextured = new ExtraTextureConfiguration(owner, ImmutableMap.of("cracks", textures[stage]));
				crackedModels[stage] = SimpleBlockModel.bakeDynamic(retextured, elements, transform);
			}
			return crackedModels[stage];
		}

		@Override
		public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, Random random, IModelData data) {
			Integer cracks = data.getData(CrackableTileEntityHandler.PROPERTY);
			if (cracks != null && cracks > 0) {
				return getModel(cracks).getQuads(state, side, random, data);
			}
			return originalModel.getQuads(state, side, random, data);
		}
	}

	/** Loader implementation */
	private static class Loader implements IModelLoader<CrackedModel> {
		@Override
		public void onResourceManagerReload(IResourceManager resourceManager) {}

		@Override
		public CrackedModel read(JsonDeserializationContext deserializationContext, JsonObject modelContents) {
			return new CrackedModel(SimpleBlockModel.deserialize(deserializationContext, modelContents));
		}
	}
}

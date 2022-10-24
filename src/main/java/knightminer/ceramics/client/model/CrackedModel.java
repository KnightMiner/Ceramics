package knightminer.ceramics.client.model;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import knightminer.ceramics.items.CrackableBlockItem;
import knightminer.ceramics.blocks.entity.CrackableBlockEntityHandler;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockElement;
import net.minecraft.client.renderer.block.model.BlockElementFace;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
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
	public static final ItemOverrides OVERRIDES = new ItemOverrides() {
		@Override
		public BakedModel resolve(BakedModel model, ItemStack stack, @Nullable ClientLevel world, @Nullable LivingEntity livingEntity, int seed) {
			int cracks = CrackableBlockItem.getCracks(stack);
			if (cracks > 0 && model instanceof Baked baked) {
				return baked.getModel(cracks);
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
	public Collection<Material> getTextures(IModelConfiguration owner, Function<ResourceLocation,UnbakedModel> modelGetter, Set<Pair<String,String>> missingTextureErrors) {
		Collection<Material> textures = model.getTextures(owner, modelGetter, missingTextureErrors);
		for (int i = 1; i <=5; i++) {
			String name = "cracks_" + i;
			Material material = owner.resolveTexture(name);
			if (Objects.equals(material.texture(), MissingTextureAtlasSprite.getLocation())) {
				missingTextureErrors.add(Pair.of(name, owner.getModelName()));
			}
			textures.add(material);
		}
		return textures;
	}

	@Override
	public BakedModel bake(IModelConfiguration owner, ModelBakery bakery, Function<Material,TextureAtlasSprite> spriteGetter, ModelState transform, ItemOverrides overrides, ResourceLocation location) {
		// fetch textures
		Material[] textures = new Material[5];
		for (int i = 0; i < 5; i++) {
			textures[i] = owner.resolveTexture("cracks_" + (i + 1));
		}

		// create extra quads
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
		BakedModel original = model.bakeModel(owner, transform, OVERRIDES, spriteGetter, location);
		return new Baked(original, owner, newElements, textures, transform);
	}

	/** Baked model for this */
	private static class Baked extends DynamicBakedWrapper<BakedModel> {
		private final BakedModel[] crackedModels;
		private final IModelConfiguration owner;
		private final List<BlockElement> elements;
		private final Material[] textures;
		private final ModelState transform;

		public Baked(BakedModel originalModel, IModelConfiguration owner, List<BlockElement> elements, Material[] textures, ModelState transform) {
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
				IModelConfiguration retextured = new ExtraTextureConfiguration(owner, ImmutableMap.of("cracks", textures[stage]));
				crackedModels[stage] = SimpleBlockModel.bakeDynamic(retextured, elements, transform);
			}
			return crackedModels[stage];
		}

		@Override
		public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, Random random, IModelData data) {
			Integer cracks = data.getData(CrackableBlockEntityHandler.PROPERTY);
			if (cracks != null && cracks > 0) {
				return getModel(cracks).getQuads(state, side, random, data);
			}
			return originalModel.getQuads(state, side, random, data);
		}
	}

	/** Loader implementation */
	private static class Loader implements IModelLoader<CrackedModel> {
		@Override
		public void onResourceManagerReload(ResourceManager resourceManager) {}

		@Override
		public CrackedModel read(JsonDeserializationContext deserializationContext, JsonObject modelContents) {
			return new CrackedModel(SimpleBlockModel.deserialize(deserializationContext, modelContents));
		}
	}
}

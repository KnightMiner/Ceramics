/*
 * Minecraft Forge
 * Copyright (c) 2016-2021.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation version 2.1
 * of the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package knightminer.ceramics.client.model;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.math.Transformation;
import knightminer.ceramics.Ceramics;
import knightminer.ceramics.items.SolidClayBucketItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.client.RenderTypeGroup;
import net.neoforged.neoforge.client.model.CompositeModel;
import net.neoforged.neoforge.client.model.DynamicFluidContainerModel;
import net.neoforged.neoforge.client.model.QuadTransformers;
import net.neoforged.neoforge.client.model.SimpleModelState;
import net.neoforged.neoforge.client.model.geometry.IGeometryBakingContext;
import net.neoforged.neoforge.client.model.geometry.IGeometryLoader;
import net.neoforged.neoforge.client.model.geometry.IUnbakedGeometry;
import net.neoforged.neoforge.client.model.geometry.StandaloneGeometryBakingContext;
import net.neoforged.neoforge.client.model.geometry.UnbakedGeometryHelper;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import slimeknights.mantle.client.model.TextureMapManager;
import slimeknights.mantle.client.model.util.MantleItemLayerModel;
import slimeknights.mantle.client.model.util.ModelHelper;
import slimeknights.mantle.data.loadable.Loadables;
import slimeknights.mantle.util.ItemLayerPixels;
import slimeknights.mantle.util.JsonHelper;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Variant of {@link slimeknights.mantle.client.model.FluidContainerModel} using blocks instead of fluids.
 * Leverages {@link slimeknights.mantle.client.model.TextureMapManager} to allow using specific textures instead of the block texture.
 */
public record SolidBucketModel(Block block, @Nullable ResourceLocation textureOverrides) implements IUnbakedGeometry<SolidBucketModel> {
  public static final ResourceLocation ID = Ceramics.getResource("solid_bucket");
  public static final IGeometryLoader<SolidBucketModel> LOADER = SolidBucketModel::deserialize;

  /** Clone of same named field from {@link DynamicFluidContainerModel} */
  public static final Transformation FLUID_TRANSFORM = new Transformation(new Vector3f(), new Quaternionf(), new Vector3f(1, 1, 1.002f), new Quaternionf());

  /** Deserializes this model from JSON */
  public static SolidBucketModel deserialize(JsonObject json, JsonDeserializationContext context) {
    Block block = Loadables.BLOCK.getOrDefault(json, "block", Blocks.AIR);
    return new SolidBucketModel(
      block,
      // no sense using overrides if its a specific block, just use a normal model at that point
      block == Blocks.AIR ? JsonHelper.getResourceLocation(json, "texture_overrides", null) : null
    );
  }

  /** Gets the given sprite, or null if the texture is not present in the model */
  @Nullable
  private static TextureAtlasSprite getSprite(IGeometryBakingContext context, Function<Material,TextureAtlasSprite> spriteGetter, String key) {
    if (context.hasMaterial(key)) {
      return spriteGetter.apply(context.getMaterial(key));
    }
    return null;
  }

  /** Bakes the model with the given fluid */
  private static BakedModel bakeInternal(IGeometryBakingContext context, Function<Material,TextureAtlasSprite> spriteGetter, ModelState modelState, ItemOverrides overrides, Block block, @Nullable TextureAtlasSprite overrideSprite) {
    // get basic sprites
    TextureAtlasSprite baseSprite = getSprite(context, spriteGetter, "base");
    TextureAtlasSprite blockSprite;
    if (block != Blocks.AIR && overrideSprite == null) {
      blockSprite = spriteGetter.apply(new Material(InventoryMenu.BLOCK_ATLAS, ModelHelper.getParticleTexture(block)));
    } else {
      blockSprite = null;
    }

    // determine particle
    TextureAtlasSprite particleSprite = getSprite(context, spriteGetter, "particle");
    if (particleSprite == null) particleSprite = blockSprite;
    if (particleSprite == null) particleSprite = baseSprite;
    if (particleSprite == null) {
      Ceramics.LOG.error("No valid particle sprite for solid bucket model, you should supply either 'base' or 'particle'");
      particleSprite = spriteGetter.apply(new Material(InventoryMenu.BLOCK_ATLAS, MissingTextureAtlasSprite.getLocation()));
    }

    // start building the model
    CompositeModel.Baked.Builder modelBuilder = CompositeModel.Baked.builder(context, particleSprite, overrides, context.getTransforms());
    RenderTypeGroup renderTypes = DynamicFluidContainerModel.getLayerRenderTypes(false);

    // add in block
    ItemLayerPixels pixels = null;
    List<BakedQuad> blockQuads = List.of();
    RenderTypeGroup blockRenderTypes = renderTypes;
    if (overrideSprite != null || blockSprite != null) {
      // get light
      int light = block.defaultBlockState().getLightEmission();
      if (light > 0) {
        blockRenderTypes = DynamicFluidContainerModel.getLayerRenderTypes(true);
      }
      // if given, use the override
      if (overrideSprite != null) {
        // want to prevent z-fighting so using the mantle quad builder
        pixels = new ItemLayerPixels();
        blockQuads = MantleItemLayerModel.getQuadsForSprite(-1, -1, overrideSprite, modelState.getRotation(), light, pixels);
      } else {
        // if absent, build block quads dynamically
        blockQuads = UnbakedGeometryHelper.bakeElements(
          UnbakedGeometryHelper.createUnbakedItemMaskElements(1, spriteGetter.apply(context.getMaterial("block"))),
          $ -> blockSprite,
          new SimpleModelState(modelState.getRotation().compose(FLUID_TRANSFORM), modelState.isUvLocked())
        );
        if (light > 0) {
          QuadTransformers.settingEmissivity(light).processInPlace(blockQuads);
        }
      }
    }

    // add in the base - done second as we need the pixel information from the override sprite
    if (baseSprite != null) {
      modelBuilder.addQuads(renderTypes, MantleItemLayerModel.getQuadsForSprite(-1, -1, baseSprite, modelState.getRotation(), 0, pixels));
    }
    // want block quads added after base quads, but need to process base quads late for handling pixels
    if (!blockQuads.isEmpty()) {
      modelBuilder.addQuads(blockRenderTypes, blockQuads);
    }

    return modelBuilder.build();
  }

  @Override
  public BakedModel bake(IGeometryBakingContext context, ModelBaker bakery, Function<Material,TextureAtlasSprite> spriteGetter, ModelState modelState, ItemOverrides overrides) {
    // We need to disable GUI 3D and block lighting for this to render properly
    context = StandaloneGeometryBakingContext.builder(context).withGui3d(false).withUseBlockLight(false).build(ID);

    // if the model JSON has no block, setup dynamic block
    if (block == Blocks.AIR) {
      // texture overrides give us specific texture overlays for specific variants, preload the cache with them
      Map<Block,BakedModel> cache = new HashMap<>();
      if (textureOverrides != null) {
        Map<String,ResourceLocation> textures = TextureMapManager.INSTANCE.getTextureMap(Minecraft.getInstance().getResourceManager(), textureOverrides);
        for (Map.Entry<String,ResourceLocation> entry : textures.entrySet()) {
          String key = entry.getKey();
          ResourceLocation texture = entry.getValue();
          try {
            Block block = Loadables.BLOCK.parseString(key, textureOverrides.toString());
            cache.put(block, bakeInternal(context, spriteGetter, modelState, overrides, block, spriteGetter.apply(new Material(InventoryMenu.BLOCK_ATLAS, texture))));
          } catch (JsonParseException e) {
            Ceramics.LOG.error("Failed to parse solid bucket texture {} as {} is not a block", textures, key, e);
          }
        }
      }
      overrides = new ContainedBlockOverrideHandler(context, overrides, modelState, cache);
    }
    return bakeInternal(context, spriteGetter, modelState, overrides, block, null);
  }

  /** Handles swapping the model based on the contained fluid */
  private static final class ContainedBlockOverrideHandler extends ItemOverrides {
    // contains all the baked models since they'll never change
    private final Map<Block,BakedModel> cache;
    private final IGeometryBakingContext context;
    private final ItemOverrides nested;
    private final ModelState modelState;

    private ContainedBlockOverrideHandler(IGeometryBakingContext context, ItemOverrides nested, ModelState modelState, Map<Block, BakedModel> cache) {
      this.cache = cache;
      this.context = context;
      this.nested = nested;
      this.modelState = modelState;
    }

    /** Gets the model directly, for creating the cached models */
    private BakedModel getUncahcedModel(Block block) {
      return bakeInternal(context, Material::sprite, modelState, ItemOverrides.EMPTY, block, null);
    }

    /** Function to bake a model for the cache */
    private final Function<Block,BakedModel> BAKER = this::getUncahcedModel;

    @Nullable
    @Override
    public BakedModel resolve(BakedModel originalModel, ItemStack stack, @Nullable ClientLevel world, @Nullable LivingEntity entity, int seed) {
      BakedModel overriden = nested.resolve(originalModel, stack, world, entity, seed);
      if (overriden != originalModel) return overriden;
      Block block = SolidClayBucketItem.getBlock(stack);
      if (block != Blocks.AIR) {
        return cache.computeIfAbsent(block, BAKER);
      }
      return originalModel;
    }
  }
}

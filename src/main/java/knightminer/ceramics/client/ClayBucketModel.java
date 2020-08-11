package knightminer.ceramics.client;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.IModelTransform;
import net.minecraft.client.renderer.model.IUnbakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.model.ItemOverrideList;
import net.minecraft.client.renderer.model.ModelBakery;
import net.minecraft.client.renderer.model.RenderMaterial;
import net.minecraft.client.renderer.texture.MissingTextureSprite;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemStack;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.TransformationMatrix;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.model.BakedItemModel;
import net.minecraftforge.client.model.IModelConfiguration;
import net.minecraftforge.client.model.IModelLoader;
import net.minecraftforge.client.model.ItemLayerModel;
import net.minecraftforge.client.model.ItemTextureQuadConverter;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelTransformComposition;
import net.minecraftforge.client.model.PerspectiveMapWrapper;
import net.minecraftforge.client.model.SimpleModelTransform;
import net.minecraftforge.client.model.geometry.IModelGeometry;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.resource.IResourceType;
import net.minecraftforge.resource.VanillaResourceType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * This is largely based on Forges {@link net.minecraftforge.client.model.DynamicBucketModel}.
 * The main difference is how it handles covers, as inset rather than outset, so transparent fluids render properly
 */
@ParametersAreNonnullByDefault
public final class ClayBucketModel implements IModelGeometry<ClayBucketModel> {
  // offsets that wil place the texture within the 3D item model, but always allow a visible liquid
  private static final float NORTH_Z_INNER = 8.48f / 16f;
  private static final float SOUTH_Z_INNER = 7.52f / 16f;
  private static final float NORTH_Z_FLUID = 7.51f / 16f;
  private static final float SOUTH_Z_FLUID = 8.49f / 16f;

  @Nonnull
  private final Fluid fluid;
  private final boolean flipGas;
  private final boolean tint;

  public ClayBucketModel(Fluid fluid, boolean flipGas, boolean tint) {
    this.fluid = fluid;
    this.flipGas = flipGas;
    this.tint = tint;
  }

  /**
   * Returns a new ModelDynBucket representing the given fluid, but with the same
   * other properties (flipGas, tint).
   */
  public ClayBucketModel withFluid(Fluid newFluid) {
    return new ClayBucketModel(newFluid, flipGas, tint);
  }

  /**
   * Gets the material from the model config for a given name, or null if its not present
   * @param owner  Model configuration
   * @param name   Texture name
   * @return  Material, or null if the material is missing
   */
  private static RenderMaterial getMaterial(IModelConfiguration owner, String name) {
    RenderMaterial location = owner.resolveTexture(name);
    if (MissingTextureSprite.getLocation().equals(location.getTextureLocation())) {
     return null;
    }
    return location;
  }

  @Override
  public IBakedModel bake(IModelConfiguration owner, ModelBakery bakery, Function<RenderMaterial, TextureAtlasSprite> spriteGetter, IModelTransform modelTransform, ItemOverrideList overrides, ResourceLocation modelLocation) {
    RenderMaterial particleLocation = getMaterial(owner, "particle");
    // front texture, full 3D
    RenderMaterial baseLocation = getMaterial(owner, "base");
    // inner (texture in the middle, flat)
    RenderMaterial fluidMaskLocation = getMaterial(owner, "fluid");
    // inner (texture in the back, flat)
    RenderMaterial innerLocation = getMaterial(owner, "inner");

    // determine the transforms to use
    IModelTransform transformsFromModel = owner.getCombinedTransform();
    ImmutableMap<TransformType,TransformationMatrix> transformMap = transformsFromModel != null ?
                                                                    PerspectiveMapWrapper.getTransforms(new ModelTransformComposition(transformsFromModel, modelTransform)) :
                                                                    PerspectiveMapWrapper.getTransforms(modelTransform);
    // particle has fallback if null based on a few other sprites
    TextureAtlasSprite particleSprite = particleLocation != null ? spriteGetter.apply(particleLocation) : null;
    // if the fluid is lighter than air, will manipulate the initial state to be rotated 180 deg to turn it upside down
    if (flipGas && fluid != Fluids.EMPTY && fluid.getAttributes().isLighterThanAir()) {
      modelTransform = new ModelTransformComposition(modelTransform, new SimpleModelTransform(new TransformationMatrix(null, new Quaternion(0, 0, 1, 0), null, null)));
    }

    // start building quads
    TransformationMatrix transform = modelTransform.getRotation();
    ImmutableList.Builder<BakedQuad> builder = ImmutableList.builder();
    if (fluid == Fluids.EMPTY) {
      // if no fluid, just render the inner sprite, looks better
      // use base if no inner sprite
      if (innerLocation == null) innerLocation = baseLocation;
      // this sprite will be used as particle
      if (particleSprite == null) particleSprite = spriteGetter.apply(innerLocation);

      builder.addAll(ItemLayerModel.getQuadsForSprites(ImmutableList.of(innerLocation), transform, spriteGetter));
    } else {
      // base is the outer cover, but is also the only layer in full 3D
      if (baseLocation != null) {
        builder.addAll(ItemLayerModel.getQuadsForSprites(ImmutableList.of(baseLocation), transform, spriteGetter));
      }

      // fluid is next one in
      TextureAtlasSprite fluidSprite = spriteGetter.apply(ForgeHooksClient.getBlockMaterial(fluid.getAttributes().getStillTexture()));
      if (particleSprite == null) particleSprite = fluidSprite;
      if (fluidMaskLocation != null && fluidSprite != null) {
        TextureAtlasSprite templateSprite = spriteGetter.apply(fluidMaskLocation);
        if (templateSprite != null) {
          int color = tint ? fluid.getAttributes().getColor() : 0xFFFFFFFF;
          builder.addAll(ItemTextureQuadConverter.convertTexture(transform, templateSprite, fluidSprite, NORTH_Z_FLUID, Direction.NORTH, color, 1));
          builder.addAll(ItemTextureQuadConverter.convertTexture(transform, templateSprite, fluidSprite, SOUTH_Z_FLUID, Direction.SOUTH, color, 1));
        }
      }
      // inner is at the back of the model behind the fluid
      // needs to be about a pixel back or in hand it gets cut off
      // inventory will not see this regardless
      if (innerLocation != null) {
        // inner (the actual item around the other two)
        TextureAtlasSprite innerSprite = spriteGetter.apply(innerLocation);
        builder.add(ItemTextureQuadConverter.genQuad(transform, 0, 0, 16, 16, NORTH_Z_INNER, innerSprite, Direction.NORTH, 0xFFFFFFFF, 2));
        builder.add(ItemTextureQuadConverter.genQuad(transform, 0, 0, 16, 16, SOUTH_Z_INNER, innerSprite, Direction.SOUTH, 0xFFFFFFFF, 2));
      }
    }

    return new BakedModel(bakery, owner, this, builder.build(), particleSprite, Maps.immutableEnumMap(transformMap), Maps.newHashMap(), transform.isIdentity(), modelTransform, owner.isSideLit());
  }

  @Override
  public Collection<RenderMaterial> getTextures(IModelConfiguration owner, Function<ResourceLocation, IUnbakedModel> modelGetter, Set<Pair<String, String>> missingTextureErrors) {
    Set<RenderMaterial> texs = Sets.newHashSet();
    texs.add(owner.resolveTexture("particle"));
    texs.add(owner.resolveTexture("base"));
    texs.add(owner.resolveTexture("inner"));
    texs.add(owner.resolveTexture("fluid"));
    return texs;
  }

  public enum Loader implements IModelLoader<ClayBucketModel> {
    INSTANCE;

    @Override
    public IResourceType getResourceType()
    {
      return VanillaResourceType.MODELS;
    }

    @Override
    public void onResourceManagerReload(IResourceManager resourceManager) {
      // no need to clear cache since we create a new model instance
    }

    @Override
    public void onResourceManagerReload(IResourceManager resourceManager, Predicate<IResourceType> resourcePredicate) {
      // no need to clear cache since we create a new model instance
    }

    @Override
    public ClayBucketModel read(JsonDeserializationContext deserializationContext, JsonObject modelContents) {
      Fluid fluid = null;
      if (modelContents.has("fluid")) {
        ResourceLocation fluidName = new ResourceLocation(modelContents.get("fluid").getAsString());
        fluid = ForgeRegistries.FLUIDS.getValue(fluidName);
      }
      if (fluid == null) fluid = Fluids.EMPTY;

      // if true, flips gasses in the bucket
      boolean flip = false;
      if (modelContents.has("flipGas")) {
        flip = modelContents.get("flipGas").getAsBoolean();
      }
      // if true, tints the fluid. Not really sure why you would not want this
      boolean tint = true;
      if (modelContents.has("applyTint")) {
        tint = modelContents.get("applyTint").getAsBoolean();
      }
      // create new model with correct liquid
      return new ClayBucketModel(fluid, flip, tint);
    }
  }

  private static final class ContainedFluidOverrideHandler extends ItemOverrideList {
    private static final ResourceLocation REBAKE_LOCATION = new ResourceLocation("ceramics:bucket_override");
    private final ModelBakery bakery;
    private ContainedFluidOverrideHandler(ModelBakery bakery)
    {
      this.bakery = bakery;
    }

    @Override
    public IBakedModel func_239290_a_(IBakedModel originalModel, ItemStack stack, @Nullable ClientWorld world, @Nullable LivingEntity entity) {
      return FluidUtil.getFluidContained(stack)
                      .map(fluidStack -> {
                        BakedModel model = (BakedModel)originalModel;

                        Fluid fluid = fluidStack.getFluid();
                        String name = fluid.getRegistryName().toString();

                        if (!model.cache.containsKey(name)) {
                          ClayBucketModel parent = model.parent.withFluid(fluid);
                          IBakedModel bakedModel = parent.bake(model.owner, bakery, ModelLoader.defaultTextureGetter(), model.originalTransform, model.getOverrides(), REBAKE_LOCATION);
                          model.cache.put(name, bakedModel);
                          return bakedModel;
                        }

                        return model.cache.get(name);
                      })
                      // not a fluid item apparently
                      .orElse(originalModel); // empty bucket
    }
  }

  // the dynamic bucket is based on the empty bucket
  private static final class BakedModel extends BakedItemModel {
    private final IModelConfiguration owner;
    private final ClayBucketModel parent;
    private final Map<String, IBakedModel> cache; // contains all the baked models since they'll never change
    private final IModelTransform originalTransform;

    private BakedModel(ModelBakery bakery,
                       IModelConfiguration owner, ClayBucketModel parent,
                       ImmutableList<BakedQuad> quads,
                       TextureAtlasSprite particle,
                       ImmutableMap<TransformType, TransformationMatrix> transforms,
                       Map<String, IBakedModel> cache,
                       boolean untransformed,
                       IModelTransform originalTransform, boolean isSideLit) {
      super(quads, particle, transforms, new ContainedFluidOverrideHandler(bakery), untransformed, isSideLit);
      this.owner = owner;
      this.parent = parent;
      this.cache = cache;
      this.originalTransform = originalTransform;
    }
  }
}
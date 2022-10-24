package knightminer.ceramics.client.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import com.mojang.math.Quaternion;
import com.mojang.math.Transformation;
import knightminer.ceramics.items.SolidClayBucketItem;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms.TransformType;
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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.model.BakedItemModel;
import net.minecraftforge.client.model.CompositeModelState;
import net.minecraftforge.client.model.ForgeModelBakery;
import net.minecraftforge.client.model.IModelConfiguration;
import net.minecraftforge.client.model.IModelLoader;
import net.minecraftforge.client.model.ItemLayerModel;
import net.minecraftforge.client.model.ItemTextureQuadConverter;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.client.model.PerspectiveMapWrapper;
import net.minecraftforge.client.model.SimpleModelState;
import net.minecraftforge.client.model.geometry.IModelGeometry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.registries.ForgeRegistries;
import slimeknights.mantle.client.model.util.ModelHelper;
import slimeknights.mantle.util.JsonHelper;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

/**
 * This is largely based on Forges {@link net.minecraftforge.client.model.DynamicBucketModel}.
 * The main difference is how it handles covers, as inset rather than outset, so transparent fluids render properly
 */
public abstract class ClayBucketModel<T> implements IModelGeometry<ClayBucketModel<?>> {
  /** Loader instance */
  public static final FluidLoader FLUID_LOADER = new FluidLoader();
  /** Loader instance */
  public static final SolidLoader SOLID_LOADER = new SolidLoader();

  // offsets that wil place the texture within the 3D item model, but always allow a visible liquid
  private static final float NORTH_Z_INNER = 8.48f / 16f;
  private static final float SOUTH_Z_INNER = 7.52f / 16f;
  private static final float NORTH_Z_FLUID = 7.51f / 16f;
  private static final float SOUTH_Z_FLUID = 8.49f / 16f;


  /* Abstract methods */

  /**
   * Returns a new bucket model representing the given contents, but with the same  other properties (flipGas, tint).
   * @param newContents  New contents
   * @return Bucket model instance
   */
  protected abstract ClayBucketModel<T> withContents(T newContents);

  /** Returns true if this bucket should be flipped */
  protected boolean shouldFlip() {
    return false;
  }

  /** Checks if this bucket has any contents, if a parameter is passed checks if the given contents are empty */
  protected abstract boolean isEmpty(@Nullable T contents);

  /** Gets the texture for the contents */
  protected abstract ResourceLocation getTexture();

  /** Gets the tint color for the contents */
  protected int getColor() {
    return -1;
  }

  /** Gets the contained contents */
  protected abstract T getContents(ItemStack stack);

  protected abstract String getContentsName(T contents);


  /* Main logic */

  /**
   * Gets the material from the model config for a given name, or null if its not present
   * @param owner  Model configuration
   * @param name   Texture name
   * @return  Material, or null if the material is missing
   */
  @Nullable
  private static Material getMaterial(IModelConfiguration owner, String name) {
    Material location = owner.resolveTexture(name);
    if (MissingTextureAtlasSprite.getLocation().equals(location.texture())) {
     return null;
    }
    return location;
  }

  @Override
  public BakedModel bake(IModelConfiguration owner, ModelBakery bakery, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelTransform, ItemOverrides overrides, ResourceLocation modelLocation) {
    Material particleLocation = getMaterial(owner, "particle");
    // front texture, full 3D
    Material baseLocation = getMaterial(owner, "base");
    // inner (texture in the middle, flat)
    Material fluidMaskLocation = getMaterial(owner, "fluid");
    // inner (texture in the back, flat)
    Material innerLocation = getMaterial(owner, "inner");

    // determine the transforms to use
    ModelState transformsFromModel = owner.getCombinedTransform();
    ImmutableMap<TransformType,Transformation> transformMap = PerspectiveMapWrapper.getTransforms(new CompositeModelState(transformsFromModel, modelTransform));

    // particle has fallback if null based on a few other sprites
    TextureAtlasSprite particleSprite = particleLocation != null ? spriteGetter.apply(particleLocation) : null;

    // if the fluid is lighter than air, will manipulate the initial state to be rotated 180 deg to turn it upside down
    if (shouldFlip()) {
      modelTransform = new CompositeModelState(modelTransform, new SimpleModelState(new Transformation(null, new Quaternion(0, 0, 1, 0), null, null)));
    }

    // start building quads
    Transformation transform = modelTransform.getRotation();
    ImmutableList.Builder<BakedQuad> builder = ImmutableList.builder();
    if (isEmpty(null)) {
      // if no fluid, just render the inner sprite, looks better
      // use base if no inner sprite
      if (innerLocation == null) innerLocation = baseLocation;

      if (innerLocation != null) {
        // this sprite will be used as particle
        if (particleSprite == null) particleSprite = spriteGetter.apply(innerLocation);
        // add to builder
        builder.addAll(ItemLayerModel.getQuadsForSprites(ImmutableList.of(innerLocation), transform, spriteGetter));
      }
    } else {
      // base is the outer cover, but is also the only layer in full 3D
      if (baseLocation != null) {
        builder.addAll(ItemLayerModel.getQuadsForSprites(ImmutableList.of(baseLocation), transform, spriteGetter));
      }

      // fluid is next one in
      TextureAtlasSprite fluidSprite = spriteGetter.apply(ForgeHooksClient.getBlockMaterial(getTexture()));
      if (particleSprite == null) particleSprite = fluidSprite;
      if (fluidMaskLocation != null && fluidSprite != null) {
        TextureAtlasSprite templateSprite = spriteGetter.apply(fluidMaskLocation);
        if (templateSprite != null) {
          int color = getColor();
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

    if (particleSprite == null) {
      particleSprite = spriteGetter.apply(ModelLoaderRegistry.blockMaterial(MissingTextureAtlasSprite.getLocation()));
    }

    return new BakedBucketModel<>(bakery, owner, this, builder.build(), particleSprite, Maps.immutableEnumMap(transformMap), Maps.newHashMap(), transform.isIdentity(), modelTransform, owner.isSideLit());
  }

  @Override
  public Collection<Material> getTextures(IModelConfiguration owner, Function<ResourceLocation, UnbakedModel> modelGetter, Set<Pair<String, String>> missingTextureErrors) {
    Set<Material> texs = Sets.newHashSet();
    texs.add(owner.resolveTexture("particle"));
    texs.add(owner.resolveTexture("base"));
    texs.add(owner.resolveTexture("inner"));
    texs.add(owner.resolveTexture("fluid"));
    return texs;
  }

  /** Clay bucket model for fluids */
  private static class FluidBucket extends ClayBucketModel<Fluid> {
    private final Fluid contents;
    private final boolean flipGas;
    private final boolean tint;
    private FluidBucket(Fluid contents, boolean flipGas, boolean tint) {
      this.contents = contents;
      this.flipGas = flipGas;
      this.tint = tint;
    }

    @Override
    protected ClayBucketModel<Fluid> withContents(Fluid newContents) {
      return new FluidBucket(newContents, flipGas, tint);
    }

    @Override
    protected boolean shouldFlip() {
      return flipGas && contents != Fluids.EMPTY && contents.getAttributes().isLighterThanAir();
    }

    @Override
    protected boolean isEmpty(@Nullable Fluid contents) {
      if (contents == null) {
        contents = this.contents;
      }
      return contents == Fluids.EMPTY;
    }

    @Override
    protected ResourceLocation getTexture() {
      return contents.getAttributes().getStillTexture();
    }

    @Override
    protected int getColor() {
      return tint ? contents.getAttributes().getColor() : -1;
    }

    @Override
    protected Fluid getContents(ItemStack stack) {
      return  FluidUtil.getFluidContained(stack).orElse(FluidStack.EMPTY).getFluid();
    }

    @Override
    protected String getContentsName(Fluid contents) {
      return Objects.requireNonNull(contents.getRegistryName()).toString();
    }
  }

  /** Clay bucket model for blocks */
  private static class SolidBucket extends ClayBucketModel<Block> {
    private final Block contents;
    private SolidBucket(Block contents) {
      this.contents = contents;
    }

    @Override
    protected ClayBucketModel<Block> withContents(Block newContents) {
      return new SolidBucket(newContents);
    }

    @Override
    protected boolean isEmpty(@Nullable Block contents) {
      if (contents == null) {
        contents = this.contents;
      }
      return contents == Blocks.AIR;
    }

    @Override
    protected ResourceLocation getTexture() {
      return ModelHelper.getParticleTexture(contents);
    }

    @Override
    protected Block getContents(ItemStack stack) {
      return stack.getItem() instanceof SolidClayBucketItem solidBucket ? solidBucket.getBlock(stack) : Blocks.AIR;
    }

    @Override
    protected String getContentsName(Block contents) {
      return Objects.requireNonNull(contents.getRegistryName()).toString();
    }
  }

  /** Model loader logic */
  private static class FluidLoader implements IModelLoader<ClayBucketModel<?>> {
    @Override
    public void onResourceManagerReload(ResourceManager resourceManager) {}

    @Override
    public ClayBucketModel<?> read(JsonDeserializationContext deserializationContext, JsonObject modelContents) {
      Fluid fluid = Fluids.EMPTY;
      if (modelContents.has("fluid")) {
        fluid = JsonHelper.getAsEntry(ForgeRegistries.FLUIDS, modelContents, "fluid");
      }

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
      return new FluidBucket(fluid, flip, tint);
    }
  }

  /** Model loader logic */
  private static class SolidLoader implements IModelLoader<ClayBucketModel<?>> {
    @Override
    public void onResourceManagerReload(ResourceManager resourceManager) {}

    @Override
    public ClayBucketModel<?> read(JsonDeserializationContext deserializationContext, JsonObject modelContents) {
      Block block = Blocks.AIR;
      if (modelContents.has("block")) {
        block = JsonHelper.getAsEntry(ForgeRegistries.BLOCKS, modelContents, "block");
      }
      return new SolidBucket(block);
    }
  }

  /** Handles adding in the model for a specific fluid, replacing the fluidless model */
  private static final class ContainedFluidOverrideHandler extends ItemOverrides {
    static final ContainedFluidOverrideHandler INSTANCE = new ContainedFluidOverrideHandler();

    @Override
    public BakedModel resolve(BakedModel originalModel, ItemStack stack, @Nullable ClientLevel world, @Nullable LivingEntity entity, int seed) {
      return ((BakedBucketModel<?>)originalModel).rebake(stack);
    }
  }

  /** the dynamic bucket is based on the empty bucket */
  private static final class BakedBucketModel<T> extends BakedItemModel {
    private static final ResourceLocation REBAKE_LOCATION = new ResourceLocation("ceramics:bucket_override");

    private final ModelBakery bakery;
    private final IModelConfiguration owner;
    private final ClayBucketModel<T> parent;
    private final Map<String, BakedModel> cache; // contains all the baked models since they'll never change
    private final ModelState originalTransform;

    private BakedBucketModel(ModelBakery bakery, IModelConfiguration owner, ClayBucketModel<T> parent, ImmutableList<BakedQuad> quads,
                       TextureAtlasSprite particle, ImmutableMap<TransformType, Transformation> transforms,
                       Map<String, BakedModel> cache, boolean untransformed, ModelState originalTransform, boolean isSideLit) {
      super(quads, particle, transforms, parent.isEmpty(null) ? ContainedFluidOverrideHandler.INSTANCE : ItemOverrides.EMPTY, untransformed, isSideLit);
      this.bakery = bakery;
      this.owner = owner;
      this.parent = parent;
      this.cache = cache;
      this.originalTransform = originalTransform;
    }

    /** Rebakes the model using the given stack */
    private BakedModel rebake(ItemStack stack) {
      T contents = parent.getContents(stack);
      // empty? return self
      if (parent.isEmpty(contents)) {
        return this;
      }
      // bake contents if not done so yet
      String name = parent.getContentsName(contents);
      if (!cache.containsKey(name)) {
        ClayBucketModel<T> newContents = parent.withContents(contents);
        BakedModel bakedModel = newContents.bake(owner, bakery, ForgeModelBakery.defaultTextureGetter(), originalTransform, ItemOverrides.EMPTY, REBAKE_LOCATION);
        cache.put(name, bakedModel);
        return bakedModel;
      }
      return cache.get(name);
    }
  }
}
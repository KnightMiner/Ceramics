package knightminer.ceramics.client.model;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
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
import net.minecraft.core.Direction.Axis;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.client.RenderTypeGroup;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.client.model.CompositeModel;
import net.minecraftforge.client.model.DynamicFluidContainerModel;
import net.minecraftforge.client.model.QuadTransformers;
import net.minecraftforge.client.model.SimpleModelState;
import net.minecraftforge.client.model.geometry.IGeometryBakingContext;
import net.minecraftforge.client.model.geometry.IGeometryLoader;
import net.minecraftforge.client.model.geometry.IUnbakedGeometry;
import net.minecraftforge.client.model.geometry.UnbakedGeometryHelper;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import slimeknights.mantle.client.model.util.ColoredBlockModel;
import slimeknights.mantle.client.model.util.MantleItemLayerModel;
import slimeknights.mantle.client.model.util.ModelHelper;
import slimeknights.mantle.data.loadable.Loadables;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * This is largely based on Forges {@link net.minecraftforge.client.model.DynamicFluidContainerModel}.
 * The main difference is how it handles covers, as inset rather than outset, so transparent fluids render properly
 */
public abstract class ClayBucketModel<T> implements IUnbakedGeometry<ClayBucketModel<?>> {
  /** Loader instance */
  public static final IGeometryLoader<ClayBucketModel<?>> FLUID_LOADER = ClayBucketModel::deserializeFluid;
  /** Loader instance */
  public static final IGeometryLoader<ClayBucketModel<?>> SOLID_LOADER = ClayBucketModel::deserializeSolid;

  /** Deserializes a fluid model from JSON */
  public static ClayBucketModel<?> deserializeFluid(JsonObject json, JsonDeserializationContext context) {
    Fluid fluid = Loadables.FLUID.getOrDefault(json, "fluid", Fluids.EMPTY);

    // if true, flips gasses in the bucket
    boolean flip = GsonHelper.getAsBoolean(json, "flip_gas", false);
    if (json.has("flipGas")) {
      flip = json.get("flipGas").getAsBoolean();
    }
    // if true, tints the fluid. Not really sure why you would not want this
    boolean tint = GsonHelper.getAsBoolean(json, "apply_tint", true);
    // create new model with correct liquid
    return new FluidBucket(fluid, flip, tint);
  }

  /** Deserializes a block model from JSON */
  public static ClayBucketModel<?> deserializeSolid(JsonObject json, JsonDeserializationContext context) {
    Block block = Loadables.BLOCK.getOrDefault(json, "block", Blocks.AIR);
    return new SolidBucket(block);
  }

  // offsets that wil place the texture within the 3D item model, but always allow a visible liquid
  // fluid is offset slightly away from front
  private static final Transformation FLUID_TRANSFORM = new Transformation(new Vector3f(), new Quaternionf(), new Vector3f(1, 1, 0.998f), new Quaternionf());
  // inner is offset to the center, half a pixel back
  private static final Transformation INNER_TRANSFORM = new Transformation(new Vector3f(0, 0, -0.5f/16f), new Quaternionf(), new Vector3f(1, 1, 1), new Quaternionf());

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
  protected int getLight() {
    return 0;
  }

  /** Gets the tint color for the contents */
  protected int getColor() {
    return -1;
  }

  /** Gets the contained contents */
  protected abstract T getContents(ItemStack stack);


  /* Main logic */

  /** Gets the given sprite, or null if the texture is not present in the model */
  @Nullable
  private static TextureAtlasSprite getSprite(IGeometryBakingContext context, Function<Material,TextureAtlasSprite> spriteGetter, String key) {
    if (context.hasMaterial(key)) {
      return spriteGetter.apply(context.getMaterial(key));
    }
    return null;
  }

  @Override
  public BakedModel bake(IGeometryBakingContext context, ModelBaker bakery, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelState, ItemOverrides overrides, ResourceLocation modelLocation) {
    if (isEmpty(null)) {
      overrides = new BucketFluidOverrides<>(context, this, modelState);
    }
    return bake(context, spriteGetter, modelState, overrides, modelLocation);
  }

  /** Bakes the final model */
  private BakedModel bake(IGeometryBakingContext context, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelState, ItemOverrides overrides, ResourceLocation modelLocation) {
    // front texture, full 3D
    TextureAtlasSprite baseSprite = getSprite(context, spriteGetter, "base");
    // inner (texture in the middle, flat)
    TextureAtlasSprite maskSprite = spriteGetter.apply(context.getMaterial("fluid"));
    // inner (texture in the back, flat)
    TextureAtlasSprite innerSprite = getSprite(context, spriteGetter, "inner");

    // determine particle
    TextureAtlasSprite particleSprite = getSprite(context, spriteGetter, "particle");
    if (particleSprite == null) particleSprite = innerSprite;
    if (particleSprite == null) particleSprite = baseSprite;
    if (particleSprite == null) {
      Ceramics.LOG.error("No valid particle sprite for fluid container model, you should supply either 'base' or 'particle'");
      particleSprite = spriteGetter.apply(new Material(InventoryMenu.BLOCK_ATLAS, MissingTextureAtlasSprite.getLocation()));
    }

    // if the fluid is lighter than air, will manipulate the initial state to be rotated 180 deg to turn it upside down
    if (shouldFlip()) {
      modelState = new SimpleModelState(modelState.getRotation().compose(new Transformation(null, new Quaternionf(0, 0, 1, 0), null, null)));
    }

    // start building the model
    CompositeModel.Baked.Builder modelBuilder = CompositeModel.Baked.builder(context, particleSprite, overrides, context.getTransforms());
    RenderTypeGroup renderTypes = DynamicFluidContainerModel.getLayerRenderTypes(false);

    // start building quads
    if (isEmpty(null)) {
      // if no fluid, just render the inner sprite or base sprite, looks better
      TextureAtlasSprite emptySprite = innerSprite != null ? innerSprite : baseSprite;
      if (innerSprite != null) {
        // add to builder
        modelBuilder.addQuads(renderTypes, UnbakedGeometryHelper.bakeElements(
          UnbakedGeometryHelper.createUnbakedItemElements(0, emptySprite.contents()),
          $ -> emptySprite, modelState, modelLocation
        ));
      }
    } else {
      // base is the outer cover, but is also the only layer in full 3D
      if (baseSprite != null) {
        modelBuilder.addQuads(renderTypes, UnbakedGeometryHelper.bakeElements(
          UnbakedGeometryHelper.createUnbakedItemElements(0, baseSprite.contents()),
          $ -> baseSprite, modelState, modelLocation
        ));
      }

      // inner is at the back of the model behind the fluid
      // needs to be about a pixel back or in hand it gets cut off
      // inventory will not see this regardless
      if (innerSprite != null) {
        modelBuilder.addQuads(renderTypes, MantleItemLayerModel.getQuadForGui(-1, -1, innerSprite, INNER_TRANSFORM, 0));
      }

      // fluid is next one in
      TextureAtlasSprite fluidSprite = spriteGetter.apply(new Material(InventoryMenu.BLOCK_ATLAS, getTexture()));
      if (fluidSprite != null) {
        List<BakedQuad> quads = UnbakedGeometryHelper.bakeElements(
          UnbakedGeometryHelper.createUnbakedItemMaskElements(1, maskSprite.contents()),
          $ -> fluidSprite,
          new SimpleModelState(modelState.getRotation().compose(FLUID_TRANSFORM), modelState.isUvLocked()),
          modelLocation
        );
        quads = quads.stream().filter(quad -> quad.getDirection().getAxis() == Axis.Z).toList();

        // apply light
        RenderTypeGroup fluidRenderTypes = renderTypes;
        int light = getLight();
        if (light > 0) {
          fluidRenderTypes = DynamicFluidContainerModel.getLayerRenderTypes(true);
          QuadTransformers.settingEmissivity(light).processInPlace(quads);
        }
        // apply color
        int color = getColor();
        if (color != -1) {
          ColoredBlockModel.applyColorQuadTransformer(color).processInPlace(quads);
        }
        modelBuilder.addQuads(fluidRenderTypes, quads);
      }
    }
    return modelBuilder.build();
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
      return flipGas && contents != Fluids.EMPTY && contents.getFluidType().isLighterThanAir();
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
      return IClientFluidTypeExtensions.of(contents).getStillTexture();
    }

    @Override
    protected int getLight() {
      return isEmpty(null) ? 0 : contents.getFluidType().getLightLevel();
    }

    @Override
    protected int getColor() {
      return tint ? IClientFluidTypeExtensions.of(contents).getTintColor() : -1;
    }

    @Override
    protected Fluid getContents(ItemStack stack) {
      return FluidUtil.getFluidContained(stack).orElse(FluidStack.EMPTY).getFluid();
    }
  }

  /** Clay bucket model for blocks */
  private static class SolidBucket extends ClayBucketModel<Block> {
    private final Block contents;
    private final int color;
    private SolidBucket(Block contents) {
      this.contents = contents;
      Item item = contents.asItem();
      if (item != Items.AIR) {
        this.color = Minecraft.getInstance().getItemColors().getColor(new ItemStack(item), 0);
      } else {
        this.color = -1;
      }
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

    @SuppressWarnings("deprecation")  // don't have world context
    @Override
    protected int getLight() {
      return contents.defaultBlockState().getLightEmission();
    }

    @Override
    public int getColor() {
      return color;
    }

    @Override
    protected Block getContents(ItemStack stack) {
      return stack.getItem() instanceof SolidClayBucketItem solidBucket ? solidBucket.getBlock(stack) : Blocks.AIR;
    }
  }

  /** Handles dynamically updating the model based on the fluid NBT */
  private static final class BucketFluidOverrides<T> extends ItemOverrides {
    private static final ResourceLocation REBAKE_LOCATION = new ResourceLocation("ceramics:bucket_override");

    private final IGeometryBakingContext context;
    private final ClayBucketModel<T> parent;
    private final ModelState originalTransform;
    private final Map<T, BakedModel> cache = new HashMap<>(); // contains all the baked models since they'll never change

    private BucketFluidOverrides(IGeometryBakingContext context, ClayBucketModel<T> parent, ModelState originalTransform) {
      this.context = context;
      this.parent = parent;
      this.originalTransform = originalTransform;
    }

    @Override
    public BakedModel resolve(BakedModel originalModel, ItemStack stack, @Nullable ClientLevel world, @Nullable LivingEntity entity, int seed) {
      T contents = parent.getContents(stack);
      // empty? return self
      if (parent.isEmpty(contents)) {
        return originalModel;
      }
      // bake contents if not done so yet
      if (!cache.containsKey(contents)) {
        ClayBucketModel<T> newContents = parent.withContents(contents);
        BakedModel bakedModel = newContents.bake(context, Material::sprite, originalTransform, ItemOverrides.EMPTY, REBAKE_LOCATION);
        cache.put(contents, bakedModel);
        return bakedModel;
      }
      return cache.get(contents);
    }
  }
}
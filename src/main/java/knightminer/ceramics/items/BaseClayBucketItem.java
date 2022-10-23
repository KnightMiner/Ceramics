package knightminer.ceramics.items;

import knightminer.ceramics.Registration;
import knightminer.ceramics.recipe.CeramicsTags;
import knightminer.ceramics.util.FluidClayBucketWrapper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.event.entity.player.PlayerDestroyItemEvent;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.Random;

/**
 * Shared logic between the milk and fluid filled clay buckets
 */
@SuppressWarnings("WeakerAccess")
public abstract class BaseClayBucketItem extends Item {
  /** Tag name for fluid in a bucket */
  private static final String TAG_FLUID = "fluid";
  /** Constant used in rendering items breaking */
  private static final float DEGREE_TO_RAD = (float) Math.PI / 180f;

  protected boolean isCracked;
  protected BaseClayBucketItem(boolean isCracked, Properties props) {
    super(props);
    this.isCracked = isCracked;
    if (isCracked) {
      MinecraftForge.EVENT_BUS.addListener(this::onItemDestroyed);
    }
  }


  /* Item methods */

  @Override
  public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
    return new FluidClayBucketWrapper(stack);
  }

  @Override
  @OnlyIn(Dist.CLIENT)
  public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
    if (isCracked) {
      tooltip.add(new TranslatableComponent(this.getDescriptionId() + ".tooltip").withStyle(ChatFormatting.GRAY));
    }
  }

  /**
   * Called when an item is destroyed, as in the stack changes from filled to empty
   * @param event  Event context
   */
  private void onItemDestroyed(PlayerDestroyItemEvent event) {
    ItemStack original = event.getOriginal();
    if(original.getItem() == this) {
      renderBrokenItem(event.getPlayer(), event.getOriginal());
    }
  }

  /**
   * Renders a broken item and plays its sound
   * @param player  Player for which the item breaks
   * @param stack  Item breaking
   */
  protected static void renderBrokenItem(Player player, ItemStack stack) {
    // play sound
    Level world = player.getCommandSenderWorld();
    world.playLocalSound(player.getX(), player.getY(), player.getZ(), SoundEvents.ITEM_BREAK, player.getSoundSource(), 0.8F, 0.8F + world.random.nextFloat() * 0.4F, false);
    // add particles
    Random rand = player.getRandom();
    ItemParticleOption particle = new ItemParticleOption(ParticleTypes.ITEM, stack);
    for(int i = 0; i < 5; ++i) {
      Vec3 offset = new Vec3((rand.nextFloat() - 0.5D) * 0.1D, Math.random() * 0.1D + 0.1D, 0.0D);
      offset = offset.xRot(-player.getXRot() * DEGREE_TO_RAD);
      offset = offset.yRot(-player.getYRot() * DEGREE_TO_RAD);
      Vec3 pos = new Vec3((rand.nextFloat() - 0.5D) * 0.3D, -rand.nextFloat() * 0.6D - 0.3D, 0.6D);
      pos = pos.xRot(-player.getXRot() * DEGREE_TO_RAD);
      pos = pos.yRot(-player.getYRot() * DEGREE_TO_RAD);
      pos = pos.add(player.getX(), player.getY() + player.getEyeHeight(), player.getZ());
      // spawnParticle is no-oped on server, need to use server specific variant
      if (world instanceof ServerLevel) {
        ((ServerLevel)world).sendParticles(particle, pos.x, pos.y, pos.z, 1, offset.x, offset.y + 0.05D, offset.z, 0.0D);
      } else {
        world.addParticle(particle, pos.x, pos.y, pos.z, offset.x, offset.y + 0.05D, offset.z);
      }
    }
  }


  /* Bucket handling */

  @Override
  public boolean hasContainerItem(ItemStack stack) {
    return !isCracked && !hasFluid(stack) && super.hasContainerItem(stack);
  }

  // TODO: perhaps separate filled and empty?
  @Override
  public ItemStack getContainerItem(ItemStack stack) {
    if (isCracked) {
      return ItemStack.EMPTY;
    }
    if (!hasFluid(stack)) {
      return ItemStack.EMPTY;
    }
    return new ItemStack(Registration.CLAY_BUCKET);
  }

  /**
   * Gets the empty bucket, based on the player's abilities
   * @param stack   Filled bucket
   * @param player  Player placer
   * @return  Empty bucket, may be the original stack
   */
  protected static ItemStack emptyBucket(ItemStack stack, Player player) {
    return !player.isCreative() ? stack.getContainerItem() : stack;
  }

  /**
   * Fills a bucket stack with the given fluid
   * @param originalStack  List of empty buckets
   * @param player        Player instance
   * @param newBucket         Filled bucket stack
   * @return  Stack of buckets
   */
  protected static ItemStack updateBucket(ItemStack originalStack, Player player, ItemStack newBucket) {
    // shrink the stack
    if (player.isCreative()) {
      return originalStack;
    }
    originalStack.shrink(1);
    // fill with fluid
    if (originalStack.isEmpty()) {
      return newBucket;
    }
    addItem(player, newBucket);
    return originalStack;
  }

  /**
   * Adds an item to the player inventory, dropping if there is no space
   * @param player  Player instance
   * @param stack   Stack to add
   */
  protected static void addItem(Player player, ItemStack stack) {
    if (!player.getInventory().add(stack)) {
      player.drop(stack, false);
    }
  }


  /* Fluid handling */

  /**
   * Returns true if the fluid cracks the bucket
   * @param fluid  Fluid to test
   * @return true if it cracks the bucket
   */
  public static boolean doesCrack(Fluid fluid) {
    if (fluid == Fluids.EMPTY) {
      return false;
    }
    // if tags are loaded, we can get the most accurate results
    boolean hotTemperature = fluid.getAttributes().getTemperature() >= 450;
    if (CeramicsTags.tagsLoaded()) {
      // if the temperature is hot, ensure its not tagged cool
      if (hotTemperature) {
        return !fluid.is(CeramicsTags.Fluids.COOL_FLUIDS);
      }
      // if the temperature is cool but tagged hot, it cracks
      return fluid.is(CeramicsTags.Fluids.HOT_FLUIDS);
    }

    // no tags, stuck with only temperature
    return hotTemperature;
  }

  /**
   * Returns whether a bucket has fluid. Note the fluid may still be null if
   * true due to milk buckets
   */
  protected boolean hasFluid(ItemStack container) {
    return getFluid(container) != Fluids.EMPTY;
  }

  /**
   * Gets the fluid from the given clay bucket container
   * @param stack  Bucket stack
   * @return  Fluid contained in the container
   */
  public Fluid getFluid(ItemStack stack) {
    CompoundTag tags = stack.getTag();
    if(tags != null) {
      Fluid fluid = ForgeRegistries.FLUIDS.getValue(new ResourceLocation(tags.getString(TAG_FLUID)));
      return fluid == null ? Fluids.EMPTY : fluid;
    }

    return Fluids.EMPTY;
  }

  /**
   * Returns the stack with the specified fluid
   * @param fluid  Fluid for the bucket
   * @return  Clay bucket containing the given fluid
   */
  public ItemStack withFluid(Fluid fluid) {
    // special case milk: returns the metadata version
    if (isMilk(fluid)) {
      return withMilk();
    }

    // return
    return setFluid(new ItemStack(isCracked || doesCrack(fluid) ? Registration.CRACKED_CLAY_BUCKET : Registration.CLAY_BUCKET), fluid);
  }

  /**
   * Checks if the given fluid is milk. May give inaccurate results before tags are loaded.
   * @param fluid  Fluid to check
   * @return  True if the fluid is milk
   */
  protected static boolean isMilk(Fluid fluid) {
    return ForgeMod.MILK.isPresent() && fluid == ForgeMod.MILK.get();
  }

  /**
   * Gets the bucket with milk based on the given cracked status
   * @return  Stack with milk
   */
  protected ItemStack withMilk() {
    return new ItemStack(isCracked ? Registration.CRACKED_MILK_CLAY_BUCKET : Registration.MILK_CLAY_BUCKET);
  }

  /**
   * Sets the fluid for the given stack
   * @param stack  Item stack instance
   * @param fluid  Fluid instance
   * @return  Modified stack
   */
  protected static ItemStack setFluid(ItemStack stack, Fluid fluid) {
    stack.getOrCreateTag().putString(TAG_FLUID, Objects.requireNonNull(fluid.getRegistryName()).toString());
    return stack;
  }

  /**
   * Gets a string variant name for the given stack
   * @param stack  Stack instance to check
   * @return  String variant name
   */
  public static String getSubtype(ItemStack stack) {
    if (stack.hasTag()) {
      assert stack.getTag() != null;
      return stack.getTag().getString(TAG_FLUID);
    }
    return "minecraft:empty";
  }
}

package knightminer.ceramics.items;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.ICapabilityProvider;
import net.neoforged.neoforge.common.EffectCures;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import org.jetbrains.annotations.Nullable;
import slimeknights.mantle.item.ConstantItemFluidHandler;

/**
 * Clay bucket holding milk
 */
public class MilkClayBucketItem extends BaseClayBucketItem {
  public MilkClayBucketItem(boolean isCracked, Properties props) {
    super(isCracked, props);
  }

  @Override
  public InteractionResultHolder<ItemStack> use(Level worldIn, Player player, InteractionHand hand) {
    player.startUsingItem(hand);
    return new InteractionResultHolder<>(InteractionResult.SUCCESS, player.getItemInHand(hand));
  }

  @Override
  public UseAnim getUseAnimation(ItemStack stack) {
    return UseAnim.DRINK;
  }

  @Override
  public int getUseDuration(ItemStack stack, LivingEntity entity) {
    return 32;
  }

  @Override
  public ItemStack finishUsingItem(ItemStack stack, Level worldIn, LivingEntity entity) {
    if (!worldIn.isClientSide()) {
      entity.removeEffectsCuredBy(EffectCures.MILK);
    }
    // update stats
    if (entity instanceof ServerPlayer serverPlayer) {
      CriteriaTriggers.CONSUME_ITEM.trigger(serverPlayer, stack);
      serverPlayer.awardStat(Stats.ITEM_USED.get(this));
    }
    // if a player, empty a bucket
    if (entity instanceof Player player) {
      if (isCracked) {
        renderBrokenItem(player, stack);
      }
      return updateBucket(stack, player, stack.getCraftingRemainingItem());
    }
    return stack;
  }


  /* Fluids */

  /** Capability provider for milk bucket items */
  public static final ICapabilityProvider<ItemStack, Void, IFluidHandlerItem> CAPABILITY_PROVIDER = new ICapabilityProvider<>() {
    @Nullable
    @Override
    public IFluidHandlerItem getCapability(ItemStack stack, @Nullable Void context) {
      if (NeoForgeMod.MILK.isBound()) {
        FluidStack fluid = new FluidStack(NeoForgeMod.MILK, FluidType.BUCKET_VOLUME);
        fluid.applyComponents(stack.getComponentsPatch().forget(FORGET));
        return new ConstantItemFluidHandler(fluid, stack);
      }
      return null;
    }
  };
}

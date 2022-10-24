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
import net.minecraft.world.item.Items;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.ForgeMod;

/**
 * Clay bucket holding milk
 */
public class MilkClayBucketItem extends BaseClayBucketItem {
  private static final ItemStack MILK_STACK = new ItemStack(Items.MILK_BUCKET);
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
  public int getUseDuration(ItemStack stack) {
    return 32;
  }

  @Override
  public ItemStack finishUsingItem(ItemStack stack, Level worldIn, LivingEntity entity) {
    if (!worldIn.isClientSide()) {
      // TODO: this is a hack until I find a better way to make it cure the same as milk
      entity.curePotionEffects(MILK_STACK);
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
      return updateBucket(stack, player, stack.getContainerItem());
    }
    return stack;
  }

  /* Fluids */

  @Override
  public Fluid getFluid(ItemStack stack) {
    if (ForgeMod.MILK.isPresent()) {
      return ForgeMod.MILK.get();
    }
    return Fluids.EMPTY;
  }
}

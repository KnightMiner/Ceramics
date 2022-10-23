package knightminer.ceramics.items;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.UseAction;
import net.minecraft.stats.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeMod;

import net.minecraft.item.Item.Properties;

/**
 * Clay bucket holding milk
 */
public class MilkClayBucketItem extends BaseClayBucketItem {
  private static final ItemStack MILK_STACK = new ItemStack(Items.MILK_BUCKET);
  public MilkClayBucketItem(boolean isCracked, Properties props) {
    super(isCracked, props);
  }

  @Override
  public ActionResult<ItemStack> use(World worldIn, PlayerEntity player, Hand hand) {
    player.startUsingItem(hand);
    return new ActionResult<>(ActionResultType.SUCCESS, player.getItemInHand(hand));
  }

  @Override
  public UseAction getUseAnimation(ItemStack stack) {
    return UseAction.DRINK;
  }

  @Override
  public int getUseDuration(ItemStack stack) {
    return 32;
  }

  @Override
  public ItemStack finishUsingItem(ItemStack stack, World worldIn, LivingEntity entity) {
    if (!worldIn.isClientSide()) {
      // TODO: this is a hack until I find a better way to make it cure the same as milk
      entity.curePotionEffects(MILK_STACK);
    }
    // update stats
    if (entity instanceof ServerPlayerEntity) {
      ServerPlayerEntity serverplayerentity = (ServerPlayerEntity)entity;
      CriteriaTriggers.CONSUME_ITEM.trigger(serverplayerentity, stack);
      serverplayerentity.awardStat(Stats.ITEM_USED.get(this));
    }
    // if a player, empty a bucket
    if (entity instanceof PlayerEntity) {
      PlayerEntity player = (PlayerEntity)entity;
      if (isCracked) {
        renderBrokenItem(player, stack);
      }
      return updateBucket(stack, player, stack.getContainerItem());
    }
    return stack;
  }

  @Override
  public ITextComponent getName(ItemStack stack) {
    return super.getName(stack).plainCopy().withStyle(TextFormatting.RED);
  }

  /* Fluids */

  @Override
  protected boolean hasFluid(ItemStack container) {
    return true;
  }

  @Override
  public Fluid getFluid(ItemStack stack) {
    if (ForgeMod.MILK.isPresent()) {
      return ForgeMod.MILK.get();
    }
    return Fluids.EMPTY;
  }

  @Override
  public void fillItemCategory(ItemGroup tab, NonNullList<ItemStack> subItems) {
    if (/*Config.bucketEnabled && */this.allowdedIn(tab) && !isCracked) {
      subItems.add(new ItemStack(this));
    }
  }
}

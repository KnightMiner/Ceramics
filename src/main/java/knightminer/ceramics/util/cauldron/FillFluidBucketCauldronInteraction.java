package knightminer.ceramics.util.cauldron;

import knightminer.ceramics.items.BaseClayBucketItem;
import knightminer.ceramics.recipe.CeramicsTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.common.SoundActions;
import net.neoforged.neoforge.fluids.CauldronFluidContent;
import net.neoforged.neoforge.fluids.FluidType;

/** Fills a bucket from a cauldron by detecting its fluid. */
public record FillFluidBucketCauldronInteraction(boolean isCracked) implements CauldronInteraction {
  private void fillBucket(Level level, BlockPos pos, BlockState newState, Player player, InteractionHand hand, ItemStack empty, Fluid fluid) {
    Item item = empty.getItem();
    // fill bucket
    player.setItemInHand(hand, ItemUtils.createFilledResult(empty, player, BaseClayBucketItem.withFluid(fluid, isCracked)));
    // grant stats
    player.awardStat(Stats.USE_CAULDRON);
    player.awardStat(Stats.ITEM_USED.get(item));
    // update cauldron
    level.setBlockAndUpdate(pos, newState);
    // effects
    SoundEvent sound = fluid.getFluidType().getSound(SoundActions.BUCKET_FILL);
    if (sound != null) {
      level.playSound(null, pos, sound, SoundSource.BLOCKS, 1.0F, 1.0F);
    }
    level.gameEvent(null, GameEvent.FLUID_PICKUP, pos);
  }

  @Override
  public ItemInteractionResult interact(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, ItemStack stack) {
    CauldronFluidContent cauldronFluid = CauldronFluidContent.getForBlock(state.getBlock());
    // if we have a fluid, it's not blacklisted, and it's not a block entity (don't want entity data), try to pickup
    if (cauldronFluid != null && !cauldronFluid.fluid.is(CeramicsTags.Fluids.BUCKET_BLACKLIST) && level.getBlockEntity(pos) == null) {
      // if we have a level property, figure out how much to drain
      if (cauldronFluid.levelProperty != null) {
        int currentLevel = state.getValue(cauldronFluid.levelProperty);
        // ensure we have a bucket of fluid or more
        if (cauldronFluid.totalAmount * currentLevel / cauldronFluid.maxLevel >= FluidType.BUCKET_VOLUME) {
          // from this point on we are successful, so client need not figure out the rest
          if (!level.isClientSide) {
            // calculate how many levels we need to equal or exceed the fluid
            int toDrain = (FluidType.BUCKET_VOLUME * cauldronFluid.maxLevel + cauldronFluid.totalAmount - 1) / cauldronFluid.totalAmount;
            int newLevel = currentLevel - toDrain;
            BlockState newState = newLevel == 0 ? Blocks.CAULDRON.defaultBlockState() : state.setValue(cauldronFluid.levelProperty, newLevel);
            fillBucket(level, pos, newState, player, hand, stack, cauldronFluid.fluid);
          }
          return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }
        // if it has no level property, just make sure it has a bucket or more, then clear it out
      } else if (cauldronFluid.totalAmount >= FluidType.BUCKET_VOLUME) {
        if (!level.isClientSide) {
          fillBucket(level, pos, Blocks.CAULDRON.defaultBlockState(), player, hand, stack, cauldronFluid.fluid);
        }
        return ItemInteractionResult.sidedSuccess(level.isClientSide);
      }
    }
    return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
  }
}

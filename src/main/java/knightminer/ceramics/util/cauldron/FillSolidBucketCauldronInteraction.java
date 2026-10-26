package knightminer.ceramics.util.cauldron;

import knightminer.ceramics.items.BaseClayBucketItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Predicate;

/** Fills a solid bucket from the given cauldron (which is replaced with {@link net.minecraft.world.level.block.Blocks#CAULDRON}). */
public record FillSolidBucketCauldronInteraction(Predicate<BlockState> cauldron, Block contents, boolean isCracked, SoundEvent sound) implements CauldronInteraction {
  @Override
  public ItemInteractionResult interact(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, ItemStack stack) {
    return CauldronInteraction.fillBucket(state, level, pos, player, hand, stack, BaseClayBucketItem.withBlock(contents, isCracked), cauldron, sound);
  }
}

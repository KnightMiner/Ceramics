package knightminer.ceramics.items;

import com.google.common.collect.ImmutableList;
import knightminer.ceramics.recipe.CeramicsTags;
import net.minecraft.ChatFormatting;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SolidBucketItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.ForgeI18n;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.registries.ForgeRegistries;
import slimeknights.mantle.util.RegistryHelper;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class SolidClayBucketItem extends BaseClayBucketItem {
	/** List of all blocks that can be placed in buckets */
	private static List<Block> BUCKETABLE_BLOCKS = Collections.emptyList();

	/** Loads all bucketable blocks from the item registry */
	public static void loadBucketableBlocks() {
		ImmutableList.Builder<Block> builder = ImmutableList.builder();
		for (Item item : ForgeRegistries.ITEMS) {
			if (item instanceof SolidBucketItem solidBucket) {
				builder.add(solidBucket.getBlock());
			}
		}
		BUCKETABLE_BLOCKS = builder.build();
	}

	/** Tag name for block in a bucket */
	public static final String TAG_BLOCK = "block";

	public SolidClayBucketItem(boolean isCracked, Properties props) {
		super(isCracked, props);
	}

	@Override
	public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
		return null;
	}

	@Override
	public InteractionResult useOn(UseOnContext context) {
		ItemStack stack = context.getItemInHand();
		Block block = getBlock(stack);
		if (block == Blocks.AIR || !(block.asItem() instanceof BlockItem blockItem)) {
			return InteractionResult.FAIL;
		}

		ItemStack resultStack = context.getItemInHand().getContainerItem();
		InteractionResult result = blockItem.useOn(new BlockPlaceContext(context));
		Player player = context.getPlayer();
		if (result.consumesAction() && player != null && !player.isCreative()) {
			player.setItemInHand(context.getHand(), resultStack);
		}
		return result;
	}


	/* Bucket properties */

	/**
	 * Gets the block from the given clay bucket container
	 * @param stack  Bucket stack
	 * @return  Block contained in the container, air if invalid
	 */
	public Block getBlock(ItemStack stack) {
		CompoundTag tags = stack.getTag();
		if(tags != null) {
			Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(tags.getString(TAG_BLOCK)));
			return block == null ? Blocks.AIR : block;
		}

		return Blocks.AIR;
	}

	/**
	 * Sets the fluid for the given stack
	 * @param stack  Item stack instance
	 * @param block  BLock instance
	 * @return  Modified stack
	 */
	protected static ItemStack setBlock(ItemStack stack, Block block) {
		stack.getOrCreateTag().putString(TAG_BLOCK, Objects.requireNonNull(block.getRegistryName()).toString());
		return stack;
	}


	/* Item stack properties */

	@Override
	public int getItemStackLimit(ItemStack stack) {
		return 1;
	}

	@Override
	public Component getName(ItemStack stack) {
		Block block = getBlock(stack);
		MutableComponent component;
		if(block == Blocks.AIR) {
			component = super.getName(stack).plainCopy();
		} else {
			// if the specific block is translatable, use that
			String key = this.getDescriptionId(stack);
			ResourceLocation location = block.getRegistryName();
			assert location != null;
			String blockKey = String.format("%s.%s.%s", key, location.getNamespace(), location.getPath());
			if (ForgeI18n.getPattern(blockKey).equals(blockKey)) {
				component = new TranslatableComponent(key + ".filled", new TranslatableComponent(block.getDescriptionId()));
			} else {
				component = new TranslatableComponent(blockKey);
			}
		}
		// display name in red
		return component.withStyle(ChatFormatting.RED);
	}

	@Override
	public void fillItemCategory(CreativeModeTab tab, NonNullList<ItemStack> subItems) {
		if (this.allowdedIn(tab)) {
			for (Block block : BUCKETABLE_BLOCKS) {
				if (isCracked == RegistryHelper.contains(CeramicsTags.Blocks.BUCKET_CRACKING_BLOCKS, block)) {
					subItems.add(setBlock(new ItemStack(this), block));
				}
			}
		}
	}
}

package knightminer.ceramics.items;

import com.google.common.collect.ImmutableList;
import knightminer.ceramics.Ceramics;
import knightminer.ceramics.recipe.CeramicsTags;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SolidBucketItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.ForgeI18n;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.registries.ForgeRegistries;
import slimeknights.mantle.data.loadable.Loadables;
import slimeknights.mantle.item.ConstantFluidContainerWrapper;
import slimeknights.mantle.recipe.helper.TagPreference;
import slimeknights.mantle.util.RegistryHelper;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

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

	@Nullable
	@Override
	public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
		// TODO: can this be extracted to JSON?
		if (getBlock(stack) == Blocks.POWDER_SNOW) {
			Optional<Fluid> optional = TagPreference.getPreference(CeramicsTags.Fluids.POWDERED_SNOW);
			if (optional.isPresent()) {
				return new ConstantFluidContainerWrapper(new FluidStack(optional.get(), FluidType.BUCKET_VOLUME), stack);
			}
		}
		return null;
	}

	@Override
	public InteractionResult useOn(UseOnContext context) {
		ItemStack stack = context.getItemInHand();
		Block block = getBlock(stack);
		if (block == Blocks.AIR || !(block.asItem() instanceof BlockItem blockItem)) {
			return InteractionResult.FAIL;
		}

		ItemStack resultStack = context.getItemInHand().getCraftingRemainingItem();
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
		stack.getOrCreateTag().putString(TAG_BLOCK, Loadables.BLOCK.getString(block));
		return stack;
	}


	/* Item stack properties */

	@Override
	public int getMaxStackSize(ItemStack stack) {
		// TODO: can we allow larger stack sizes for weird block stuff here?
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
			ResourceLocation location = Loadables.BLOCK.getKey(block);
			String blockKey = String.format("%s.%s.%s", key, location.getNamespace(), location.getPath());
			if (ForgeI18n.getPattern(blockKey).equals(blockKey)) {
				component = Component.translatable(key + ".filled", Component.translatable(block.getDescriptionId()));
			} else {
				component = Component.translatable(blockKey);
			}
		}
		// display name in red
		return component.withStyle(ChatFormatting.RED);
	}

	@Override
	public void addVariants(Consumer<ItemStack> consumer) {
		for (Block block : BUCKETABLE_BLOCKS) {
			if (isCracked == RegistryHelper.contains(CeramicsTags.Blocks.BUCKET_CRACKING_BLOCKS, block)) {
				consumer.accept(setBlock(new ItemStack(this), block));
			}
		}
	}

	@Override
	public String getCreatorModId(ItemStack stack) {
		Block block = getBlock(stack);
		if (block != Blocks.AIR) {
			String namespace = Loadables.BLOCK.getKey(block).getNamespace();
			if (!"minecraft".equals(namespace)) {
				return namespace;
			}
		}
		return Ceramics.MOD_ID;
	}
}

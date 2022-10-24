package knightminer.ceramics.items;

import knightminer.ceramics.Ceramics;
import knightminer.ceramics.util.FluidClayBucketWrapper;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.SolidBucketItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.event.ForgeEventFactory;

import javax.annotation.Nullable;
import java.util.List;

public class EmptyClayBucketItem extends BaseClayBucketItem {
	public EmptyClayBucketItem(boolean isCracked, Properties props) {
		super(isCracked, props);
	}


	/* Bucket behavior */

	@Override
	public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		BlockHitResult trace = getPlayerPOVHitResult(world, player, ClipContext.Fluid.SOURCE_ONLY);

		// fire Forge event for bucket use
		InteractionResultHolder<ItemStack> ret = ForgeEventFactory.onBucketUse(player, world, stack, trace);
		if (ret != null) {
			return ret;
		}

		// if we missed, do nothing
		if (trace.getType() != Type.BLOCK) {
			return InteractionResultHolder.pass(stack);
		}

		// normal fluid logic
		BlockPos pos = trace.getBlockPos();
		Direction direction = trace.getDirection();
		BlockPos offset = pos.relative(direction);

		// ensure we can place a fluid there
		if (world.mayInteract(player, pos) && player.mayUseItemAt(offset, direction, stack)) {
			BlockState state = world.getBlockState(pos);
			Block block = state.getBlock();
			if (block instanceof BucketPickup pickup) {
				ItemStack pickupStack = pickup.pickupBlock(world, pos, state);
				ItemStack newStack;
				if (pickupStack.getItem() instanceof BucketItem bucket) {
					ItemStack result = withFluid(bucket.getFluid(), isCracked);
					CompoundTag tag = pickupStack.getTag();
					if (tag != null && !tag.contains(FluidClayBucketItem.TAG_FLUID)) {
						result.getOrCreateTag().merge(tag);
					}
					newStack = ItemUtils.createFilledResult(stack, player, result);
				} else if (pickupStack.getItem() instanceof SolidBucketItem bucket) {
					world.gameEvent(player, GameEvent.FLUID_PICKUP, pos);
					ItemStack result = withBlock(bucket.getBlock(), isCracked);
					CompoundTag tag = pickupStack.getTag();
					if (tag != null && !tag.contains(SolidClayBucketItem.TAG_BLOCK)) {
						result.getOrCreateTag().merge(tag);
					}
					newStack = ItemUtils.createFilledResult(stack, player, result);
				} else {
					// unfortunately, nothing we can do in this case, can logically only handle blocks and fluids
					Ceramics.LOG.warn("Received an unexpected bucket {} on pickup, attempting to replace original state {}", pickupStack, state);
					world.setBlock(pos, state, 0);
					return InteractionResultHolder.fail(stack);
				}

				// stats and sounds
				player.awardStat(Stats.ITEM_USED.get(this));
				pickup.getPickupSound(state).ifPresent((sound) -> player.playSound(sound, 1.0F, 1.0F));
				if (!world.isClientSide) {
					CriteriaTriggers.FILLED_BUCKET.trigger((ServerPlayer)player, stack);
				}
				// success
				return InteractionResultHolder.sidedSuccess(newStack, world.isClientSide());
			}
		}
		return InteractionResultHolder.fail(stack);
	}

	@Override
	public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
		// only work if the bucket is empty and right clicking a cow
		if(!player.isCreative() && target instanceof Cow && !target.isBaby()) {
			// sound
			player.playSound(SoundEvents.COW_MILK, 1.0F, 1.0F);
			if (!player.getCommandSenderWorld().isClientSide()) {
				addItem(player, withMilk(isCracked));
				stack.shrink(1);
			}
			return InteractionResult.SUCCESS;
		}
		return InteractionResult.PASS;
	}

	@Override
	public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
		return new FluidClayBucketWrapper(stack);
	}


	/* Stack properties */

	@Override
	public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
		tooltip.add(new TranslatableComponent(this.getDescriptionId() + ".tooltip").withStyle(ChatFormatting.GRAY));
	}
}

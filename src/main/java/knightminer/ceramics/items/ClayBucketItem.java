package knightminer.ceramics.items;

import knightminer.ceramics.Ceramics;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ForgeI18n;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Clay bucket that holds arbitrary fluids
 */
public class ClayBucketItem extends BaseClayBucketItem {
	public ClayBucketItem(boolean isCracked, Properties props) {
		super(isCracked, props);
	}


	/* Bucket behavior */

	@Override
	public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		Fluid fluid = this.getFluid(stack);
		BlockHitResult trace = getPlayerPOVHitResult(world, player, fluid == Fluids.EMPTY ? ClipContext.Fluid.SOURCE_ONLY : ClipContext.Fluid.NONE);

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
			if (fluid == Fluids.EMPTY) {
				if (block instanceof BucketPickup pickup) {

					ItemStack pickupStack = pickup.pickupBlock(world, pos, state);
					if (pickupStack.getItem() instanceof BucketItem bucket) {
						Fluid newFluid = bucket.getFluid();
						if (newFluid != Fluids.EMPTY) {
							player.awardStat(Stats.ITEM_USED.get(this));

							// play sound effect
							SoundEvent sound = newFluid.getAttributes().getFillSound();
							if (sound == null) {
								sound = newFluid.is(FluidTags.LAVA) ? SoundEvents.BUCKET_FILL_LAVA : SoundEvents.BUCKET_FILL;
							}
							player.playSound(sound, 1.0F, 1.0F);
							ItemStack newStack = updateBucket(stack, player, withFluid(newFluid));
							if (!world.isClientSide()) {
								CriteriaTriggers.FILLED_BUCKET.trigger((ServerPlayer)player, newStack.copy());
							}

							return InteractionResultHolder.success(newStack);
						}
					} else {
						// TODO: doomed
						Ceramics.LOG.error("Picked up invalid fluid, this will get fixed later");
					}
				}
			} else {
				BlockPos fluidPos = state.getBlock() instanceof LiquidBlockContainer && fluid == Fluids.WATER ? pos : offset;
				if (this.tryPlaceContainedLiquid(player, world, fluidPos, stack, trace)) {
					onLiquidPlaced(player, fluid, world, stack, fluidPos);
					if (player instanceof ServerPlayer) {
						CriteriaTriggers.PLACED_BLOCK.trigger((ServerPlayer)player, fluidPos, stack);
					}

					player.awardStat(Stats.ITEM_USED.get(this));
					return InteractionResultHolder.success(emptyBucket(stack, player));
				}
			}
		}
		return InteractionResultHolder.fail(stack);
	}

	/**
	 * Called when a liquid is placed in world
	 * @param player Player placing the liquid
	 * @param fluid  Fluid to place
	 * @param world  World instance
	 * @param stack  Stack instance
	 * @param pos    Position to place the world
	 */
	private static void onLiquidPlaced(Player player, Fluid fluid, Level world, ItemStack stack, BlockPos pos) {
		// TODO: is this bad?
		Item item = fluid.getBucket();
		if (item instanceof BucketItem) {
			((BucketItem)item).checkExtraContent(player, world, stack, pos);
		}
	}

	// TODO: possibly migrate to the Forge method
	@SuppressWarnings("deprecation")
	private boolean tryPlaceContainedLiquid(@Nullable Player player, Level world, BlockPos pos, ItemStack stack, @Nullable BlockHitResult trace) {
		Fluid fluid = this.getFluid(stack);
		if (!(fluid instanceof FlowingFluid)) {
			return false;
		}

		BlockState state = world.getBlockState(pos);
		Block block = state.getBlock();
		boolean replaceable = state.canBeReplaced(fluid);
		if (state.isAir() || replaceable || block instanceof LiquidBlockContainer && ((LiquidBlockContainer)block).canPlaceLiquid(world, pos, state, fluid)) {
			if (world.dimensionType().ultraWarm() && fluid.is(FluidTags.WATER)) {
				world.playSound(player, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.5F, 2.6F + (world.random.nextFloat() - world.random.nextFloat()) * 0.8F);

				for(int l = 0; l < 8; ++l) {
					world.addParticle(ParticleTypes.LARGE_SMOKE, pos.getX() + Math.random(), pos.getY() + Math.random(), pos.getZ() + Math.random(), 0.0D, 0.0D, 0.0D);
				}
			} else if (block instanceof LiquidBlockContainer && fluid == Fluids.WATER) {
				if (((LiquidBlockContainer)block).placeLiquid(world, pos, state, ((FlowingFluid)fluid).getSource(false))) {
					this.playEmptySound(fluid, player, world, pos);
				}
			} else {
				if (!world.isClientSide() && replaceable && !state.getMaterial().isLiquid()) {
					world.destroyBlock(pos, true);
				}

				this.playEmptySound(fluid, player, world, pos);
				world.setBlock(pos, fluid.defaultFluidState().createLegacyBlock(), 11);
			}

			return true;
		}
		if (trace == null) {
			return false;
		}
		return this.tryPlaceContainedLiquid(player, world, trace.getBlockPos().relative(trace.getDirection()), stack, null);
	}

	/**
	 * Plays the sound on emptying the bucket
	 * @param fluid   Fluid placed
	 * @param player  Player accessing the bucket
	 * @param world   World instance
	 * @param pos     Position of sound
	 */
	private void playEmptySound(Fluid fluid, @Nullable Player player, LevelAccessor world, BlockPos pos) {
		SoundEvent sound = fluid.getAttributes().getEmptySound();
		if (sound == null) {
			sound = fluid.is(FluidTags.LAVA) ? SoundEvents.BUCKET_EMPTY_LAVA : SoundEvents.BUCKET_EMPTY;
		}
		world.playSound(player, pos, sound, SoundSource.BLOCKS, 1.0F, 1.0F);
	}

	@Override
	public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
		// only work if the bucket is empty and right clicking a cow
		if(!player.isCreative() && !hasFluid(stack) && target instanceof Cow && !target.isBaby()) {
			// sound
			player.playSound(SoundEvents.COW_MILK, 1.0F, 1.0F);
			if (!player.getCommandSenderWorld().isClientSide()) {
				addItem(player, withMilk());
				stack.shrink(1);
			}
			return InteractionResult.SUCCESS;
		}
		return InteractionResult.PASS;
	}


	/* Item stack properties */

	// TODO: perhaps separate filled and empty?
	@Override
	public int getItemStackLimit(ItemStack stack) {
		// empty stacks to 16
		return hasFluid(stack) ? 1 : 16;
	}

	@Override
	public int getBurnTime(ItemStack stack, @Nullable RecipeType<?> recipeType) {
		return getFluid(stack) == Fluids.LAVA ? 20000 : 0;
	}

	@Override
	public Component getName(ItemStack stack) {
		Fluid fluid = getFluid(stack);
		Component component;
		if(fluid == Fluids.EMPTY) {
			component = super.getName(stack);
		} else {
			// if the specific fluid is translatable, use that
			String key = this.getDescriptionId(stack);
			ResourceLocation location = fluid.getRegistryName();
			assert location != null;
			String fluidKey = String.format("%s.%s.%s", key, location.getNamespace(), location.getPath());
			if (ForgeI18n.getPattern(fluidKey).equals(fluidKey)) {
				component = new TranslatableComponent(key + ".filled", new FluidStack(fluid, FluidAttributes.BUCKET_VOLUME).getDisplayName());
			} else {
				component = new TranslatableComponent(fluidKey);
			}
		}
		// display name in red
		return component.plainCopy().withStyle(ChatFormatting.RED);
	}

	@Override
	public void fillItemCategory(CreativeModeTab tab, NonNullList<ItemStack> subItems) {
		if (/*Config.bucketEnabled && */this.allowdedIn(tab)) {
			// empty only for non-cracked, keep cracked hidden
			if (!isCracked) {
				subItems.add(new ItemStack(this));
			}
			// add all fluids that the bucket can be filled with
			for(Fluid fluid : ForgeRegistries.FLUIDS.getValues()) {
				// skip flowing fluids (we have still) and milks
				// include cracked if cracked, non-cracked if not cracked
				if (isVisible(fluid) && isCracked == doesCrack(fluid)) {
					subItems.add(setFluid(new ItemStack(this), fluid));
				}
			}
		}
	}


	@Override
	@OnlyIn(Dist.CLIENT)
	public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
		if (isCracked || getFluid(stack) == Fluids.EMPTY) {
			tooltip.add(new TranslatableComponent(this.getDescriptionId() + ".tooltip").withStyle(ChatFormatting.GRAY));
		}
	}

	/**
	 * Checks if the given fluid is visible in creative
	 * @param fluid  Fluid to check
	 * @return  True if its visible
	 */
	private static boolean isVisible(Fluid fluid) {
		// hide empty and milk (milk shows in its own bucket
		if (fluid == Fluids.EMPTY || isMilk(fluid)) {
			return false;
		}
		return fluid.defaultFluidState().isSource();
	}
}

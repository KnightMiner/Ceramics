package knightminer.ceramics.items;

import knightminer.ceramics.util.FluidClayBucketWrapper;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
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
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraftforge.common.ForgeI18n;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

/**
 * Clay bucket that holds arbitrary fluids
 */
public class FluidClayBucketItem extends BaseClayBucketItem {
	/** Tag name for fluid in a bucket */
	public static final String TAG_FLUID = "fluid";

	public FluidClayBucketItem(boolean isCracked, Properties props) {
		super(isCracked, props);
	}


	/* Bucket behavior */

	@Override
	public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		Fluid fluid = this.getFluid(stack);
		if (fluid == Fluids.EMPTY) {
			return InteractionResultHolder.fail(stack);
		}
		BlockHitResult trace = getPlayerPOVHitResult(world, player, ClipContext.Fluid.NONE);

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
			BlockPos fluidPos = state.getBlock() instanceof LiquidBlockContainer container && container.canPlaceLiquid(world, pos, state, fluid) ? pos : offset;
			if (this.tryPlaceContainedLiquid(player, world, fluidPos, stack, trace)) {
				onLiquidPlaced(player, fluid, world, stack, fluidPos);
				if (player instanceof ServerPlayer server) {
					CriteriaTriggers.PLACED_BLOCK.trigger(server, fluidPos, stack);
				}

				player.awardStat(Stats.ITEM_USED.get(this));
				return InteractionResultHolder.success(emptyBucket(stack, player));
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
		if (!(fluid instanceof FlowingFluid flowingFluid)) {
			return false;
		}

		BlockState state = world.getBlockState(pos);
		Block block = state.getBlock();
		boolean replaceable = state.canBeReplaced(fluid);
		if (state.isAir() || replaceable || block instanceof LiquidBlockContainer container && container.canPlaceLiquid(world, pos, state, fluid)) {
			if (world.dimensionType().ultraWarm() && fluid.is(FluidTags.WATER)) {
				world.playSound(player, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.5F, 2.6F + (world.random.nextFloat() - world.random.nextFloat()) * 0.8F);

				for(int l = 0; l < 8; ++l) {
					world.addParticle(ParticleTypes.LARGE_SMOKE, pos.getX() + Math.random(), pos.getY() + Math.random(), pos.getZ() + Math.random(), 0.0D, 0.0D, 0.0D);
				}
				return true;
			} else if (block instanceof LiquidBlockContainer container && container.canPlaceLiquid(world, pos, state, fluid)) {
				if (container.placeLiquid(world, pos, state, flowingFluid.getSource(false))) {
					playEmptySound(fluid, player, world, pos);
				}
				return true;
			} else {
				if (!world.isClientSide() && replaceable && !state.getMaterial().isLiquid()) {
					world.destroyBlock(pos, true);
				}

				if (!world.setBlock(pos, fluid.defaultFluidState().createLegacyBlock(), 11) && !state.getFluidState().isSource()) {
					return false;
				}

				playEmptySound(fluid, player, world, pos);
				return true;
			}
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
	private static void playEmptySound(Fluid fluid, @Nullable Player player, LevelAccessor world, BlockPos pos) {
		SoundEvent sound = fluid.getAttributes().getEmptySound();
		if (sound == null) {
			sound = fluid.is(FluidTags.LAVA) ? SoundEvents.BUCKET_EMPTY_LAVA : SoundEvents.BUCKET_EMPTY;
		}
		world.playSound(player, pos, sound, SoundSource.BLOCKS, 1.0F, 1.0F);
	}

	@Override
	public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
		return new FluidClayBucketWrapper(stack);
	}


	/* Bucket properties */

	@Override
	public Fluid getFluid(ItemStack stack) {
		CompoundTag tags = stack.getTag();
		if(tags != null) {
			Fluid fluid = ForgeRegistries.FLUIDS.getValue(new ResourceLocation(tags.getString(TAG_FLUID)));
			return fluid == null ? Fluids.EMPTY : fluid;
		}

		return Fluids.EMPTY;
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


	/* Item stack properties */

	@Override
	public int getBurnTime(ItemStack stack, @Nullable RecipeType<?> recipeType) {
		return getFluid(stack) == Fluids.LAVA ? 20000 : 0;
	}

	@Override
	public Component getName(ItemStack stack) {
		Fluid fluid = getFluid(stack);
		if(fluid == Fluids.EMPTY) {
			return super.getName(stack);
		}
		// if the specific fluid is translatable, use that
		String key = this.getDescriptionId(stack);
		ResourceLocation location = fluid.getRegistryName();
		assert location != null;
		String fluidKey = String.format("%s.%s.%s", key, location.getNamespace(), location.getPath());
		MutableComponent component;
		if (ForgeI18n.getPattern(fluidKey).equals(fluidKey)) {
			component = new TranslatableComponent(key + ".filled", new FluidStack(fluid, FluidAttributes.BUCKET_VOLUME).getDisplayName());
		} else {
			component = new TranslatableComponent(fluidKey);
		}
		// display name in red
		return component.withStyle(ChatFormatting.RED);
	}

	@Override
	public void fillItemCategory(CreativeModeTab tab, NonNullList<ItemStack> subItems) {
		if (this.allowdedIn(tab)) {
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
	public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
		if (isCracked) {
			tooltip.add(new TranslatableComponent(this.getDescriptionId() + ".tooltip").withStyle(ChatFormatting.GRAY));
		}
	}


	/* Helpers */

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

package knightminer.ceramics.items;

import java.util.Locale;

import javax.annotation.Nullable;

import knightminer.ceramics.Ceramics;
import knightminer.ceramics.library.Config;
import knightminer.ceramics.library.DispenseClayBucket;
import knightminer.ceramics.library.FluidClayBucketWrapper;
import knightminer.ceramics.library.Util;
import net.minecraft.block.BlockCauldron;
import net.minecraft.block.BlockDispenser;
import net.minecraft.block.BlockSand;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityCow;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.stats.StatList;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.player.FillBucketEvent;
import net.minecraftforge.event.entity.player.PlayerDestroyItemEvent;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidActionResult;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.items.ItemHandlerHelper;

@SuppressWarnings("deprecation")
public class ItemClayBucket extends Item {

	public static final String TAG_FLUIDS = "fluids";
	public static ItemStack MILK_BUCKET = new ItemStack(Items.MILK_BUCKET);
	public static ItemStack BRICK = new ItemStack(Items.BRICK);

	public ItemClayBucket() {
		this.setCreativeTab(Ceramics.tab);
		this.hasSubtypes = true;

		BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(this, DispenseClayBucket.INSTANCE);
	}


	/* Bucket behavior */

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
		ItemStack stack = player.getHeldItem(hand);

		// milk we set active and return success, drinking code is done elsewhere
		if(getSpecialFluid(stack) == SpecialFluid.MILK) {
			player.setActiveHand(hand);
			return new ActionResult<>(EnumActionResult.SUCCESS, stack);
		}

		// just call the event for all logic
		ActionResult<ItemStack> ret = ForgeEventFactory.onBucketUse(player, world, stack, this.rayTrace(world, player, !hasFluid(stack)));
		if(ret != null) {
			return ret;
		}

		return ActionResult.newResult(EnumActionResult.PASS, stack);
	}

	@SubscribeEvent(priority = EventPriority.LOW)
	public void onBucketEvent(FillBucketEvent event) {
		if(event.getResult() != Event.Result.DEFAULT) {
			// event was already handled
			return;
		}

		// ensure its our item
		ItemStack stack = event.getEmptyBucket();
		if(stack == null || !stack.getItem().equals(this)) {
			return;
		}

		// validate ray trace
		RayTraceResult target = event.getTarget();
		if(target == null || target.typeOfHit != RayTraceResult.Type.BLOCK) {
			return;
		}

		// make sure we have permission
		World world = event.getWorld();
		BlockPos pos = target.getBlockPos();
		EntityPlayer player = event.getEntityPlayer();
		if(!world.isBlockModifiable(player, pos)) {
			event.setCanceled(true);
			return;
		}

		// if we clicked a cauldron, try that first
		IBlockState state = world.getBlockState(pos);
		ItemStack result = null;
		if(state.getBlock() == Blocks.CAULDRON && (player == null || !player.isSneaking())) {
			result = interactWithCauldron(event, player, world, pos, state, stack);

			// deny means cauldron is not right state to fill
			if(event.getResult() == Result.DENY) {
				return;
			}
		}

		// if the cauldron passed or there was no cauldron, try placing normal fluids
		if (result == null) {
			if (hasFluid(stack)) {
				// check permissions
				if (!player.canPlayerEdit(pos, target.sideHit, stack)) {
					event.setCanceled(true);
					return;
				}

				BlockPos targetPos = pos.offset(target.sideHit);
				if(hasSpecialFluid(stack)) {
					result = tryPlaceSpecialFluid(stack, player, world, targetPos);
				} else {
					result = tryPlaceFluid(stack, player, world, targetPos);
				}
			} else {
				result = tryFillBucket(stack, player, world, pos, state, target.sideHit);
			}
		}

		if(result != null) {
			event.setResult(Result.ALLOW);
			event.setFilledBucket(result);
		} else {
			event.setResult(Result.DENY);
		}
	}

	/** Attempts to full an empty bucket */
	private ItemStack tryFillBucket(ItemStack stack, EntityPlayer player, World world, BlockPos pos, IBlockState state, EnumFacing side) {
		// first, try filling using fluid logic
		ItemStack single = stack.copy();
		single.setCount(1);
		FluidActionResult result = FluidUtil.tryPickUpFluid(single, player, world, pos, side);

		// if it worked, return that
		if(result.isSuccess()) {
			return result.getResult();
		}

		// if bucket sand is disabled, skip
		if(!Config.bucketSand) {
			return null;
		}

		// otherwise try gravel and sand
		SpecialFluid fluid = SpecialFluid.fromState(state);
		if(fluid != null) {
			// play sound
			world.playSound(player, pos, state.getBlock().getSoundType(state, world, pos, player).getBreakSound(), SoundCategory.BLOCKS, 1.0F, 0.8F);

			// set air
			if(!world.isRemote) {
				world.setBlockToAir(pos);
			}

			// and result
			return new ItemStack(this, 1, fluid.getMeta());
		}

		return null;
	}

	/** Attempts to place fluid from a filled bucket */
	private ItemStack tryPlaceFluid(ItemStack stack, EntityPlayer player, World world, BlockPos pos) {
		stack = stack.copy();
		FluidStack fluidStack = getFluid(stack);
		FluidActionResult result = FluidUtil.tryPlaceFluid(player, player.getEntityWorld(), pos, stack, fluidStack);
		if(result.isSuccess()) {
			// water and lava place non-flowing for some reason
			if(fluidStack.getFluid() == FluidRegistry.WATER || fluidStack.getFluid() == FluidRegistry.LAVA) {
				IBlockState state = world.getBlockState(pos);
				world.neighborChanged(pos, state.getBlock(), pos);
			}

			return result.getResult();
		}

		return null;
	}

	/** Attempts to place a special fluid from a filled bucket */
	private ItemStack tryPlaceSpecialFluid(ItemStack stack, EntityPlayer player, World world, BlockPos pos) {
		IBlockState state = getSpecialFluid(stack).getState();

		IBlockState currentState = world.getBlockState(pos);
		if(currentState.getBlock().isReplaceable(world, pos)) {
			// place block
			if(!world.isRemote) {
				world.setBlockState(pos, state);
			}

			// sound effect
			world.playSound(player, pos, state.getBlock().getSoundType(state, world, pos, player).getPlaceSound(), SoundCategory.BLOCKS, 1.0F, 0.8F);
			if(player != null) {
				player.addStat(StatList.getObjectUseStats(this));
			}

			return new ItemStack(this);
		}

		return null;
	}

	/** Interacts with a cauldron in world */
	private ItemStack interactWithCauldron(FillBucketEvent event, EntityPlayer player, World world, BlockPos pos, IBlockState state, ItemStack stack) {
		int level = state.getValue(BlockCauldron.LEVEL);

		// if we have a fluid, try filling
		if (!hasFluid(stack)) {
			// if empty, try emptying
			if(level == 3) {
				// empty cauldron logic
				if(player != null) {
					player.addStat(StatList.CAULDRON_USED);
				}
				if(!world.isRemote) {
					Blocks.CAULDRON.setWaterLevel(world, pos, state, 0);
				}
				world.playSound(null, pos, SoundEvents.ITEM_BUCKET_FILL, SoundCategory.BLOCKS, 1.0F, 1.0F);

				return withFluid(FluidRegistry.WATER);
			}

			// deny so it stops here
			event.setResult(Result.DENY);
		} else if(getFluid(stack).getFluid() == FluidRegistry.WATER) {
			if(level < 3) {
				// fill cauldron logic
				if(player != null) {
					player.addStat(StatList.CAULDRON_FILLED);
				}
				if(!world.isRemote) {
					Blocks.CAULDRON.setWaterLevel(world, pos, state, 3);
				}
				world.playSound(null, pos, SoundEvents.ITEM_BUCKET_EMPTY, SoundCategory.BLOCKS, 1.0F, 1.0F);

				// return empty bucket
				return new ItemStack(this);
			}

			// deny so it stops here
			event.setResult(Result.DENY);
		}

		return null;
	}


	/* Milk bucket logic */

	@Override
	public EnumAction getItemUseAction(ItemStack stack) {
		// milk has drinking animation
		return getSpecialFluid(stack) == SpecialFluid.MILK ? EnumAction.DRINK : EnumAction.NONE;
	}

	@Override
	public int getMaxItemUseDuration(ItemStack stack) {
		// milk requires drinking time
		return getSpecialFluid(stack) == SpecialFluid.MILK ? 32 : 0;
	}

	@Override
	@Nullable
	public ItemStack onItemUseFinish(ItemStack stack, World worldIn, EntityLivingBase entityLiving) {
		// must be milk
		if(getSpecialFluid(stack) != SpecialFluid.MILK) {
			return stack;
		}

		if(entityLiving instanceof EntityPlayer && !((EntityPlayer) entityLiving).capabilities.isCreativeMode) {
			stack = new ItemStack(this);
		}

		if(!worldIn.isRemote) {
			entityLiving.curePotionEffects(MILK_BUCKET);
		}

		if(entityLiving instanceof EntityPlayer) {
			((EntityPlayer) entityLiving).addStat(StatList.getObjectUseStats(this));
		}

		return stack;
	}

	@Override
	public boolean itemInteractionForEntity(ItemStack stack, EntityPlayer player, EntityLivingBase target, EnumHand hand) {
		// only work if the bucket is empty and right clicking a cow
		if(!hasFluid(stack) && target instanceof EntityCow && !player.capabilities.isCreativeMode) {
			// sound
			player.playSound(SoundEvents.ENTITY_COW_MILK, 1.0F, 1.0F);

			// modify items
			// because the action expects mutating the item stack
			if(stack.getCount() == 1) {
				stack.setItemDamage(SpecialFluid.MILK.getMeta());
			} else {
				stack.shrink(1);
				ItemHandlerHelper.giveItemToPlayer(player, withSpecialFluid(SpecialFluid.MILK));
			}

			return true;
		}
		return false;
	}


	/* Item stack properties */

	@Override
	public int getItemStackLimit(ItemStack stack) {
		// empty stacks to 16
		return hasFluid(stack) ? 1 : 16;
	}

	@Override
	public int getItemBurnTime(ItemStack stack) {
		FluidStack fluid = getFluid(stack);
		if(fluid != null && fluid.getFluid() == FluidRegistry.LAVA) {
			return 20000;
		}
		return 0;
	}

	@Override
	public ItemStack getContainerItem(ItemStack stack) {
		if (doesBreak(stack)) {
			return ItemStack.EMPTY;
		}
		return new ItemStack(this);
	}

	@Override
	public boolean hasContainerItem(ItemStack stack) {
		return !doesBreak(stack);
	}

	@SubscribeEvent
	public void onItemDestroyed(PlayerDestroyItemEvent event) {
		ItemStack original = event.getOriginal();
		if(original.getItem() == this) {
			event.getEntityPlayer().renderBrokenItemStack(BRICK);
		}
	}

	@Override
	public String getItemStackDisplayName(ItemStack stack) {
		if(hasSpecialFluid(stack)) {
			String specialFluid = getSpecialFluid(stack).getName();
			return I18n.translateToLocal("item." + Util.prefix("clay_bucket." + specialFluid) + ".name");
		}
		FluidStack fluidStack = getFluid(stack);
		if(fluidStack == null) {
			return I18n.translateToLocal("item." + Util.prefix("clay_bucket.empty") + ".name");
		}

		String unloc = this.getUnlocalizedNameInefficiently(stack);
		if(I18n.canTranslate(unloc + "." + fluidStack.getFluid().getName())) {
			return I18n.translateToLocal(unloc + "." + fluidStack.getFluid().getName());
		}

		return I18n.translateToLocalFormatted(unloc + ".name", fluidStack.getLocalizedName());
	}

	@Override
	public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> subItems) {
		if (Config.bucketEnabled && this.isInCreativeTab(tab)) {
			// empty
			subItems.add(new ItemStack(this));

			// add all fluids that the bucket can be filled with
			for(Fluid fluid : FluidRegistry.getRegisteredFluids().values()) {
				// skip milk if registered since we add it manually whether it is a fluid or not
				// also skip hot fluids if hot pickup is disabled
				if(!fluid.getName().equals("milk") && (Config.bucketHotFluids || !doesBreak(fluid))) {
					subItems.add(withFluid(fluid));
				}
			}
			// special fluids
			for(SpecialFluid fluid : SpecialFluid.values()) {
				if(fluid.show()) {
					subItems.add(new ItemStack(this, 1, fluid.getMeta()));
				}
			}
		}
	}

	@Override
	public ICapabilityProvider initCapabilities(ItemStack stack, NBTTagCompound nbt) {
		return new FluidClayBucketWrapper(stack);
	}


	/* Utils */

	/**
	 * Gets the contained fluid in the bucket
	 * @param container
	 * @return
	 */
	public FluidStack getFluid(ItemStack container) {
		// milk logic, if milk is registered we use that basically
		if(getSpecialFluid(container) == SpecialFluid.MILK) {
			return FluidRegistry.getFluidStack("milk", Fluid.BUCKET_VOLUME);
		}
		NBTTagCompound tags = container.getTagCompound();
		if(tags != null) {
			return FluidStack.loadFluidStackFromNBT(tags.getCompoundTag(TAG_FLUIDS));
		}

		return null;
	}

	/**
	 * Returns whether a bucket has fluid. Note the fluid may still be null if
	 * true due to milk buckets
	 */
	public boolean hasFluid(ItemStack container) {
		if(hasSpecialFluid(container)) {
			return true;
		}

		return getFluid(container) != null;
	}

	/**
	 * Checks if the stack is not a regular dynamic bucket
	 * Used for sand, gravel, and milk
	 * @param stack  Stack to check
	 * @return true if the bucket contains a special fluid
	 */
	protected boolean hasSpecialFluid(ItemStack stack) {
		return stack.getItemDamage() != 0;
	}

	/**
	 * Gets the special fluid type for the bucket
	 * @param stack  Stack to check
	 * @return the special fluid type
	 */
	public SpecialFluid getSpecialFluid(ItemStack stack) {
		return SpecialFluid.fromMeta(stack.getItemDamage());
	}

	/**
	 * Returns the stack with the specified fluid
	 * @param fluid  Fluid for the bucket
	 * @return  Clay bucket containing the given fluid
	 */
	public ItemStack withFluid(Fluid fluid) {
		// special case milk: returns the metadata version
		if ("milk".equals(fluid.getName())) {
			return new ItemStack(this, 1, SpecialFluid.MILK.getMeta());
		}
		ItemStack stack = new ItemStack(this, 1, 0);

		// add fluid to NBT
		NBTTagCompound tag = new NBTTagCompound();
		tag.setTag(TAG_FLUIDS, new FluidStack(fluid, Fluid.BUCKET_VOLUME).writeToNBT(new NBTTagCompound()));
		stack.setTagCompound(tag);

		// return
		return stack;
	}

	/**
	 * Returns the stack with the specified special fluid
	 * @param fluid  Fluid for the bucket
	 * @return  Clay bucket containing the given special fluid
	 */
	public ItemStack withSpecialFluid(SpecialFluid fluid) {
		return new ItemStack(this, 1, fluid.getMeta());
	}

	/**
	 * Returns true if the specified fluid breaks
	 * @param stack  Bucket to check
	 * @return  True if it breaks
	 */
	public boolean doesBreak(ItemStack stack) {
		// special fluids never breaks
		return !hasSpecialFluid(stack) && doesBreak(getFluid(stack));
	}

	/**
	 * Returns true if the fluid breaks the bucket
	 * @param fluid  Fluid to test
	 * @return true if it breaks the bucket
	 */
	public boolean doesBreak(FluidStack fluid) {
		return fluid != null && doesBreak(fluid.getFluid());
	}

	/**
	 * Returns true if the fluid breaks the bucket
	 * @param fluid  Fluid to test
	 * @return true if it breaks the bucket
	 */
	protected boolean doesBreak(Fluid fluid) {
		// other fluids break if hot
		if(fluid != null && fluid.getTemperature() >= 450) {
			return true;
		}

		return false;
	}

	/**
	 * Special fluid types
	 */
	public enum SpecialFluid {
		EMPTY,
		MILK,
		SAND(Blocks.SAND.getDefaultState().withProperty(BlockSand.VARIANT, BlockSand.EnumType.SAND)),
		RED_SAND(Blocks.SAND.getDefaultState().withProperty(BlockSand.VARIANT, BlockSand.EnumType.RED_SAND)),
		GRAVEL(Blocks.GRAVEL.getDefaultState());

		// store the meta to access faster
		private int meta;
		private IBlockState state;

		SpecialFluid() {
			this.meta = ordinal();
			this.state = null;
		}

		SpecialFluid(IBlockState state) {
			this.meta = ordinal();
			this.state = state;
		}

		/**
		 * Gets the name for this fluid type, used for model locations
		 * @return fluid type metadata
		 */
		public String getName() {
			return this.toString().toLowerCase(Locale.US);
		}

		/**
		 * Gets the metadata for this fluid type
		 * @return fluid type metadata
		 */
		public int getMeta() {
			return meta;
		}

		/**
		 * Gets a type from metadata
		 * @param meta  metadata input
		 * @return value for the specifed metadata
		 */
		public static SpecialFluid fromMeta(int meta) {
			if(meta < 0 || meta > values().length) {
				meta = 0;
			}

			return values()[meta];
		}

		/**
		 * Determines if the special fluid is a block type
		 */
		public boolean show() {
			return this != EMPTY && state == null;
		}

		/**
		 * Gets the block for the special fluid
		 */
		public IBlockState getState() {
			return state;
		}

		/**
		 * Gets the special fluid from the specified state
		 * @param state
		 * @return The fluid for the state, or null if none exists
		 */
		@Nullable
		public static SpecialFluid fromState(IBlockState state) {
			// feature disabled
			if(!Config.bucketSand && state == null) {
				return null;
			}

			// try all listed blocks
			for(SpecialFluid fluid : values()) {
				if(state == fluid.getState()) {
					return fluid;
				}
			}

			// none? return null
			return null;
		}
	}
}

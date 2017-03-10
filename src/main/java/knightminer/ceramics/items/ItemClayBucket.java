package knightminer.ceramics.items;

import java.util.Locale;

import javax.annotation.Nullable;

import knightminer.ceramics.Ceramics;
import knightminer.ceramics.library.Config;
import knightminer.ceramics.library.DispenseClayBucket;
import knightminer.ceramics.library.FluidClayBucketWrapper;
import knightminer.ceramics.library.Util;
import net.minecraft.block.BlockDispenser;
import net.minecraft.block.BlockSand;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityCow;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.stats.StatList;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
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
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.ItemHandlerHelper;

@SuppressWarnings("deprecation")
public class ItemClayBucket extends Item {

	public static final String TAG_FLUIDS = "fluids";
	public static ItemStack MILK_BUCKET = new ItemStack(Items.MILK_BUCKET);
	public static ItemStack BRICK = new ItemStack(Items.BRICK);

	public ItemClayBucket() {
		this.setCreativeTab(Ceramics.tab);
		this.hasSubtypes = true;

		BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(this, DispenseClayBucket.getInstance());
	}

	// allow empty to stack to 16
	@Override
	public int getItemStackLimit(ItemStack stack) {
		if(!hasFluid(stack)) {
			return 16;
		}

		return 1;
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
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
		ItemStack stack = player.getHeldItem(hand);

		// milk we set active and return success, drinking code is done elsewhere
		if(getSpecialFluid(stack) == SpecialFluid.MILK) {
			player.setActiveHand(hand);
			return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, stack);
		}

		// empty bucket logic is just an event :)
		if(!hasFluid(stack)) {
			ActionResult<ItemStack> ret = ForgeEventFactory.onBucketUse(player, world, stack,
					this.rayTrace(world, player, true));
			if(ret != null) {
				return ret;
			}

			return ActionResult.newResult(EnumActionResult.PASS, stack);
		}

		// clicked on a block?
		RayTraceResult mop = this.rayTrace(world, player, false);
		if(mop == null || mop.typeOfHit != RayTraceResult.Type.BLOCK) {
			return ActionResult.newResult(EnumActionResult.PASS, stack);
		}

		BlockPos clickPos = mop.getBlockPos();
		// can we place liquid there?
		if(world.isBlockModifiable(player, clickPos)) {
			BlockPos targetPos = clickPos.offset(mop.sideHit);

			// can the player place there?
			if(player.canPlayerEdit(targetPos, mop.sideHit, stack)) {
				// first, try placing special fluids
				FluidStack fluidStack = getFluid(stack);
				if(hasSpecialFluid(stack)) {
					// get the state from the fluid
					IBlockState state = getSpecialFluid(stack).getState();

					// if we got a block state (not milk basically)
					if(state != null) {
						IBlockState currentState = world.getBlockState(targetPos);
						if(currentState.getBlock().isReplaceable(world, targetPos)) {
							// place block
							if(!world.isRemote) {
								world.setBlockState(targetPos, state);
							}

							// sound effect
							world.playSound(player, targetPos,
									state.getBlock().getSoundType(state, world, targetPos, player).getPlaceSound(),
									SoundCategory.BLOCKS, 1.0F, 0.8F);

							// only empty if not creative
							if(!player.capabilities.isCreativeMode) {
								player.addStat(StatList.getObjectUseStats(this));

								setSpecialFluid(stack, SpecialFluid.EMPTY);
							}

							return ActionResult.newResult(EnumActionResult.SUCCESS, stack);
						}
					}
				}
				// try placing liquid
				else {
					FluidActionResult result = FluidUtil.tryPlaceFluid(player, player.getEntityWorld(), targetPos, stack, fluidStack);

					if(result.isSuccess()) {
						// water and lava place non-flowing for some reason
						if(fluidStack.getFluid() == FluidRegistry.WATER || fluidStack.getFluid() == FluidRegistry.LAVA) {
							IBlockState state = world.getBlockState(targetPos);
							world.neighborChanged(targetPos, state.getBlock(), targetPos);
						}


						ItemStack returnStack = stack.copy();
						if(!player.capabilities.isCreativeMode) {
							returnStack.shrink(1);
							ItemStack drained = result.getResult().copy();

							// if the stack is empty, relace it with the empty bucket
							if(returnStack.isEmpty()) {
								returnStack = drained;
							}
							else if(!drained.isEmpty()) {
								// otheriwise add empty bucket to player inventory
								ItemHandlerHelper.giveItemToPlayer(player, drained);
							}
						}

						return ActionResult.newResult(EnumActionResult.SUCCESS, returnStack);
					}
				}
			}
		}

		// couldn't place liquid there
		return ActionResult.newResult(EnumActionResult.FAIL, stack);
	}

	@SubscribeEvent(priority = EventPriority.LOW)
	public void onFillBucket(FillBucketEvent event) {
		if(event.getResult() != Event.Result.DEFAULT) {
			// event was already handled
			return;
		}

		// not for us to handle
		ItemStack emptyBucket = event.getEmptyBucket();
		if(emptyBucket == null || !emptyBucket.getItem().equals(this)) {
			return;
		}

		// needs to target a block or entity

		ItemStack singleBucket = emptyBucket.copy();
		singleBucket.setCount(1);

		RayTraceResult target = event.getTarget();
		if(target == null || target.typeOfHit != RayTraceResult.Type.BLOCK) {
			return;
		}

		World world = event.getWorld();
		BlockPos pos = target.getBlockPos();

		FluidActionResult result = FluidUtil.tryPickUpFluid(singleBucket, event.getEntityPlayer(), world, pos, target.sideHit);

		// if we have a bucket from the fluid, use that
		if(result.isSuccess()) {
			event.setResult(Event.Result.ALLOW);
			event.setFilledBucket(result.getResult());
		}
		// otherwise, try gravel/sand
		else {
			IBlockState state = world.getBlockState(pos);
			SpecialFluid fluid = SpecialFluid.fromState(state);
			if(fluid != null) {
				// fill the bucket
				event.setFilledBucket(setSpecialFluid(singleBucket, fluid));

				// play sound
				EntityPlayer player = event.getEntityPlayer();
				world.playSound(player, pos,
						state.getBlock().getSoundType(state, world, pos, player).getBreakSound(),
						SoundCategory.BLOCKS, 1.0F, 0.8F);

				// set air
				if(!world.isRemote) {
					world.setBlockToAir(pos);
				}

				// and allow
				event.setResult(Event.Result.ALLOW);
			}
			else {
				// cancel event, otherwise the vanilla minecraft ItemBucket would
				// convert it into a water/lava bucket depending on the blocks
				// material
				event.setCanceled(true);
			}
		}
	}

	@SubscribeEvent
	public void onItemDestroyed(PlayerDestroyItemEvent event) {
		ItemStack original = event.getOriginal();
		if(original.getItem() == this) {
			event.getEntityPlayer().renderBrokenItemStack(BRICK);
		}
	}

	// container items

	@Override
	public ItemStack getContainerItem(ItemStack stack) {
		if (doesBreak(stack)) {
			return null;
		}
		return new ItemStack(this);
	}

	@Override
	public boolean hasContainerItem(ItemStack stack) {
		return !doesBreak(stack);
	}

	@Override
	public boolean itemInteractionForEntity(ItemStack stack, EntityPlayer player, EntityLivingBase target, EnumHand hand) {
		// only work if the bucket is empty and right clicking a cow
		if(!hasFluid(stack) && target instanceof EntityCow && !player.capabilities.isCreativeMode) {
			// if we have multiple buckets in the stack, move to a new slot
			if(stack.getCount() > 1) {
				stack.shrink(1);
				ItemHandlerHelper.giveItemToPlayer(player, setSpecialFluid(new ItemStack(this), SpecialFluid.MILK));
			}
			else {
				setSpecialFluid(stack, SpecialFluid.MILK);
			}

			return true;
		}
		return false;
	}

	public boolean doesBreak(ItemStack stack) {
		// special fluids never breaks
		if(hasSpecialFluid(stack)) {
			return false;
		}

		// other fluids break if hot
		FluidStack fluid = getFluid(stack);
		if(fluid != null && fluid.getFluid().getTemperature() >= 450) {
			return true;
		}

		return false;
	}

	/**
	 * Checks if the stack is not a regular dynamic bucket
	 * Used for sand, gravel, and milk
	 * @param stack  Stack to check
	 * @return true if the bucket contains a special fluid
	 */
	public boolean hasSpecialFluid(ItemStack stack) {
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

	// in case I change it later
	public ItemStack setSpecialFluid(ItemStack stack, SpecialFluid fluid) {
		stack.setItemDamage(fluid.getMeta());
		return stack;
	}

	/* Fluids */

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

	public int getCapacity() {
		return Fluid.BUCKET_VOLUME;
	}

	public int fill(ItemStack container, FluidStack resource, boolean doFill) {
		// has to be exactly 1, must be handled from the caller
		if(container.getCount() != 1) {
			return 0;
		}

		// can only fill exact capacity
		if(resource == null || resource.amount < getCapacity()) {
			return 0;
		}

		// already contains fluid?
		if(hasFluid(container)) {
			return 0;
		}

		// milk is handled separatelly since there is not always a fluid for it
		// registered
		if(resource.getFluid().getName().equals("milk")) {
			if(doFill) {
				setSpecialFluid(container, SpecialFluid.MILK);
			}
			return getCapacity();
		}
		// registered in the registry?
		// we manually add water and lava since they by default are not
		// registered (as vanilla adds them)
		else if(FluidRegistry.getBucketFluids().contains(resource.getFluid())
				|| resource.getFluid() == FluidRegistry.WATER || resource.getFluid() == FluidRegistry.LAVA) {
			// fill the container
			if(doFill) {
				NBTTagCompound tag = container.getTagCompound();
				if(tag == null) {
					tag = new NBTTagCompound();
				}
				tag.setTag(TAG_FLUIDS, resource.writeToNBT(new NBTTagCompound()));
				container.setTagCompound(tag);
			}
			return getCapacity();
		}

		return 0;
	}

	public FluidStack drain(ItemStack container, int maxDrain, boolean doDrain) {
		// has to be exactly 1, must be handled from the caller
		if(container.getCount() != 1) {
			return null;
		}

		// can only drain everything at once
		if(maxDrain < getCapacity()) {
			return null;
		}

		FluidStack fluidStack = getFluid(container);
		if(doDrain && hasFluid(container)) {
			if(doesBreak(container)) {
				container.setCount(0);
			}
			else {
				// milk simply requires a metadata change
				if(getSpecialFluid(container) == SpecialFluid.MILK) {
					setSpecialFluid(container, SpecialFluid.EMPTY);
				}
				// don't run for non-fluids
				else if(!hasSpecialFluid(container)) {
					NBTTagCompound tag = container.getTagCompound();
					if(tag != null) {
						tag.removeTag(TAG_FLUIDS);
					}
					// remove the compound if nothing else exists, for the sake
					// of stacking
					if(tag.hasNoTags()) {
						container.setTagCompound(null);
					}
				}
			}
		}

		return fluidStack;
	}

	/* Milk bucket logic */
	/**
	 * Called when the player finishes using this Item (E.g. finishes eating.).
	 * Not called when the player stops using the Item before the action is
	 * complete.
	 */
	@Override
	@Nullable
	public ItemStack onItemUseFinish(ItemStack stack, World worldIn, EntityLivingBase entityLiving) {
		// must be milk
		if(getSpecialFluid(stack) != SpecialFluid.MILK) {
			return stack;
		}

		if(entityLiving instanceof EntityPlayer && !((EntityPlayer) entityLiving).capabilities.isCreativeMode) {
			setSpecialFluid(stack, SpecialFluid.EMPTY);
		}

		if(!worldIn.isRemote) {
			entityLiving.curePotionEffects(MILK_BUCKET);
		}

		if(entityLiving instanceof EntityPlayer) {
			((EntityPlayer) entityLiving).addStat(StatList.getObjectUseStats(this));
		}

		return stack;
	}

	/**
	 * How long it takes to use or consume an item
	 */
	@Override
	public int getMaxItemUseDuration(ItemStack stack) {
		// milk requires drinking time
		return getSpecialFluid(stack) == SpecialFluid.MILK ? 32 : 0;
	}

	/**
	 * returns the action that specifies what animation to play when the items
	 * is being used
	 */
	@Override
	public EnumAction getItemUseAction(ItemStack stack) {
		// milk has drinking animation
		return getSpecialFluid(stack) == SpecialFluid.MILK ? EnumAction.DRINK : EnumAction.NONE;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void getSubItems(Item itemIn, CreativeTabs tab, NonNullList<ItemStack> subItems) {
		// empty
		subItems.add(new ItemStack(this));

		// add all fluids that the bucket can be filled with
		for(Fluid fluid : FluidRegistry.getRegisteredFluids().values()) {
			// skip milk if registered since we add it manually whether it is a
			// fluid or not
			if(!fluid.getName().equals("milk")) {
				FluidStack fs = new FluidStack(fluid, getCapacity());
				ItemStack stack = new ItemStack(this);
				if(fill(stack, fs, true) == fs.amount) {
					subItems.add(stack);
				}
			}
		}
		// special fluids
		for(SpecialFluid fluid : SpecialFluid.values()) {
			if(fluid.show()) {
				subItems.add(new ItemStack(this, 1, fluid.getMeta()));
			}
		}
	}

	@Override
	public ICapabilityProvider initCapabilities(ItemStack stack, NBTTagCompound nbt) {
		return new FluidClayBucketWrapper(stack);
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
			if(isBlock()) {
				return false;
			}
			return this != EMPTY;
		}

		/**
		 * Determines if the special fluid is a block type
		 */
		public boolean isBlock() {
			return state != null;
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
			if(!Config.bucketSand) {
				return null;
			}

			// try all listed blocks
			for(SpecialFluid fluid : values()) {
				if(fluid.isBlock()) {
					if(state == fluid.getState()) {
						return fluid;
					}
				}
			}

			// none? return null
			return null;
		}
	}
}

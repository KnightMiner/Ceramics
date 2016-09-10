package knightminer.ceramics.items;

import java.util.List;

import javax.annotation.Nullable;

import knightminer.ceramics.Ceramics;
import knightminer.ceramics.library.FluidClayBucketWrapper;
import knightminer.ceramics.library.Util;
import net.minecraft.block.BlockDispenser;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityCow;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.stats.StatList;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.player.FillBucketEvent;
import net.minecraftforge.event.entity.player.PlayerDestroyItemEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.EntityInteract;
import net.minecraftforge.fluids.DispenseFluidContainer;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.IFluidContainerItem;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.ItemHandlerHelper;

@SuppressWarnings("deprecation")
public class ItemClayBucket extends Item implements IFluidContainerItem {

	public static final String TAG_FLUIDS = "fluids";
	public static ItemStack MILK_BUCKET;
	public static ItemStack BRICK;

	public ItemClayBucket() {
		this.setCreativeTab(Ceramics.tab);
		this.hasSubtypes = true;

		MILK_BUCKET = new ItemStack(Items.MILK_BUCKET);
		BRICK = new ItemStack(Items.BRICK);

		BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(this, DispenseFluidContainer.getInstance());
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
		if(isMilk(stack)) {
			return I18n.translateToLocal("item." + Util.prefix("clay_bucket.milk") + ".name");
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
	public ActionResult<ItemStack> onItemRightClick(ItemStack itemstack, World world, EntityPlayer player,
			EnumHand hand) {

		if(isMilk(itemstack)) {
			player.setActiveHand(hand);
			return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, itemstack);
		}

		FluidStack fluidStack = getFluid(itemstack);

		// empty bucket logic is just an event :)
		if(fluidStack == null) {
			ActionResult<ItemStack> ret = ForgeEventFactory.onBucketUse(player, world, itemstack,
					this.rayTrace(world, player, true));
			if(ret != null) {
				return ret;
			}

			return ActionResult.newResult(EnumActionResult.PASS, itemstack);
		}

		// clicked on a block?
		RayTraceResult mop = this.rayTrace(world, player, false);
		if(mop == null || mop.typeOfHit != RayTraceResult.Type.BLOCK) {
			return ActionResult.newResult(EnumActionResult.PASS, itemstack);
		}

		BlockPos clickPos = mop.getBlockPos();
		// can we place liquid there?
		if(world.isBlockModifiable(player, clickPos)) {
			BlockPos targetPos = clickPos.offset(mop.sideHit);

			// can the player place there?
			if(player.canPlayerEdit(targetPos, mop.sideHit, itemstack)) {
				// try placing liquid
				if(FluidUtil.tryPlaceFluid(player, player.getEntityWorld(), fluidStack, targetPos)) {
					// success!

					// water and lava use the non-flowing form for the fluid, so
					// give it a block update to make it flow
					if(fluidStack.getFluid() == FluidRegistry.WATER || fluidStack.getFluid() == FluidRegistry.LAVA) {
						world.notifyBlockOfStateChange(targetPos, world.getBlockState(targetPos).getBlock());
					}

					if(!player.capabilities.isCreativeMode) {
						player.addStat(StatList.getObjectUseStats(this));

						drain(itemstack, Fluid.BUCKET_VOLUME, true);
					}

					return ActionResult.newResult(EnumActionResult.SUCCESS, itemstack);
				}
			}
		}

		// couldn't place liquid there
		return ActionResult.newResult(EnumActionResult.FAIL, itemstack);
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
		singleBucket.stackSize = 1;

		RayTraceResult target = event.getTarget();
		if(target == null || target.typeOfHit != RayTraceResult.Type.BLOCK) {
			return;
		}

		World world = event.getWorld();
		BlockPos pos = target.getBlockPos();

		ItemStack filledBucket = FluidUtil.tryPickUpFluid(singleBucket, event.getEntityPlayer(), world, pos,
				target.sideHit);
		if(filledBucket != null) {
			event.setResult(Event.Result.ALLOW);
			event.setFilledBucket(filledBucket);
		}
		else {
			// cancel event, otherwise the vanilla minecraft ItemBucket would
			// convert it into a water/lava bucket depending on the blocks
			// material
			event.setCanceled(true);
		}
	}

	@SubscribeEvent
	public void onItemDestroyed(PlayerDestroyItemEvent event) {
		if(event.getOriginal() != null && event.getOriginal().getItem() == this) {
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

	// milking cows
	@SubscribeEvent
	public void onInteractWithEntity(EntityInteract event) {
		ItemStack stack = event.getItemStack();
		EntityPlayer player = event.getEntityPlayer();
		if(stack != null && stack.getItem() == this && !hasFluid(stack) && event.getTarget() instanceof EntityCow
				&& !player.capabilities.isCreativeMode) {
			if(stack.stackSize > 1) {
				stack.stackSize -= 1;
				ItemHandlerHelper.giveItemToPlayer(player, setMilk(new ItemStack(this), true));
			}
			else {
				setMilk(stack, true);
			}

			event.setCanceled(true);
		}
	}

	public boolean doesBreak(ItemStack stack) {
		// milk never breaks
		if(isMilk(stack)) {
			return false;
		}

		// other fluids break if hot
		FluidStack fluid = getFluid(stack);
		if(fluid != null && fluid.getFluid().getTemperature() >= 450) {
			return true;
		}

		return false;
	}

	// in case I change it later
	public boolean isMilk(ItemStack stack) {
		return stack.getItemDamage() == 1;
	}

	// in case I change it later
	public ItemStack setMilk(ItemStack stack, boolean milk) {
		stack.setItemDamage(milk ? 1 : 0);
		return stack;
	}

	/* Fluids */

	@Override
	public FluidStack getFluid(ItemStack container) {
		// milk logic, if milk is registered we use that basically
		if(isMilk(container)) {
			Fluid milk = FluidRegistry.getFluid("milk");
			if(milk != null) {
				return new FluidStack(milk, 1000);
			}

			return null;
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
		if(isMilk(container)) {
			return true;
		}

		return getFluid(container) != null;
	}

	@Override
	public int getCapacity(ItemStack container) {
		return getCapacity();
	}

	public int getCapacity() {
		return Fluid.BUCKET_VOLUME;
	}

	@Override
	public int fill(ItemStack container, FluidStack resource, boolean doFill) {
		// has to be exactly 1, must be handled from the caller
		if(container.stackSize != 1) {
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
				setMilk(container, true);
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

	@Override
	public FluidStack drain(ItemStack container, int maxDrain, boolean doDrain) {
		if(maxDrain == Integer.MAX_VALUE) {
			Ceramics.log.info("Loading model thingy");
		}

		// has to be exactly 1, must be handled from the caller
		if(container.stackSize != 1) {
			return null;
		}

		// can only drain everything at once
		if(maxDrain < getCapacity(container)) {
			return null;
		}

		FluidStack fluidStack = getFluid(container);
		if(doDrain && hasFluid(container)) {
			if(doesBreak(container)) {
				container.stackSize = 0;
			}
			else {
				// milk simply requires a metadata change
				if(isMilk(container)) {
					setMilk(container, false);
				}
				else {
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
		// milk check
		if(!isMilk(stack)) {
			return stack;
		}

		if(entityLiving instanceof EntityPlayer && !((EntityPlayer) entityLiving).capabilities.isCreativeMode) {
			setMilk(stack, false);
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
		return isMilk(stack) ? 32 : 0;
	}

	/**
	 * returns the action that specifies what animation to play when the items
	 * is being used
	 */
	@Override
	public EnumAction getItemUseAction(ItemStack stack) {
		return isMilk(stack) ? EnumAction.DRINK : EnumAction.NONE;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void getSubItems(Item itemIn, CreativeTabs tab, List<ItemStack> subItems) {
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
		// milk
		subItems.add(new ItemStack(this, 1, 1));
	}

	@Override
	public ICapabilityProvider initCapabilities(ItemStack stack, NBTTagCompound nbt) {
		return new FluidClayBucketWrapper(stack);
	}
}

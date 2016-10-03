package knightminer.ceramics.items;

import knightminer.ceramics.Ceramics;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemShears;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.world.BlockEvent.HarvestDropsEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ItemClayShears extends ItemShears {
	public ItemClayShears() {
		this.setMaxStackSize(1);
		this.setMaxDamage(100);
		this.setCreativeTab(Ceramics.tab);
	}


	// slightly faster than normal shears since clay is known to be sharper
	@Override
	public float getStrVsBlock(ItemStack stack, IBlockState state) {
		Block block = state.getBlock();
		if(block == Blocks.WEB || state.getMaterial() == Material.LEAVES) {
			return 18.0f;
		}
		if(block == Blocks.WOOL) {
			return 6f;
		}

		return super.getStrVsBlock(stack, state);
	}

	// webs require shears to drop
	@SubscribeEvent(priority = EventPriority.HIGH) // run before someone else does special stuff with string
	public void onBlockDrops(HarvestDropsEvent event) {
		EntityPlayer harvester = event.getHarvester();
		ItemStack shears = harvester != null ? harvester.getHeldItemMainhand() : null;
		if(shears != null && shears.getItem() == this && event.getState().getBlock() == Blocks.WEB) {
			event.getDrops().clear();
			event.getDrops().add(new ItemStack(Blocks.WEB));
		}
	}

}

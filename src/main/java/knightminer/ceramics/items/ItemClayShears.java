package knightminer.ceramics.items;

import com.google.common.eventbus.Subscribe;

import knightminer.ceramics.Ceramics;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemShears;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.world.BlockEvent.HarvestDropsEvent;

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
	@Subscribe
	public void onBlockDrops(HarvestDropsEvent event) {
		ItemStack shears = event.getHarvester().getActiveItemStack();
		if(shears != null && shears.getItem() == this) {
			for(ItemStack stack : event.getDrops()) {
				if(stack != null && stack.getItem() == Items.STRING) {
					event.getDrops().remove(stack);
					event.getDrops().add(new ItemStack(Blocks.WEB));
				}
			}
		}
	}

}

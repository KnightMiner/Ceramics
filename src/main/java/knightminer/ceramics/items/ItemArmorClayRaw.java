package knightminer.ceramics.items;

import knightminer.ceramics.Ceramics;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemArmorClayRaw extends ItemArmor {

	public ItemArmorClayRaw(EntityEquipmentSlot slot) {
		super(Ceramics.clayArmorRaw, 0, slot);
		this.setMaxDamage(1); // unfired clay armor doesn't hold together
		this.setCreativeTab(Ceramics.tab);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public String getArmorTexture(ItemStack stack, Entity entity, EntityEquipmentSlot slot, String type) {
		if(slot == EntityEquipmentSlot.LEGS) {
			return "ceramics:textures/models/armor/clay_raw_leggings.png";
		}

		return "ceramics:textures/models/armor/clay_raw.png";
	}


	/**
	 * returns a list of items with the same ID, but different meta (eg: dye returns 16 items)
	 */
	@Override
	@SideOnly(Side.CLIENT)
	public void getSubItems(Item itemIn, CreativeTabs tab, NonNullList<ItemStack> subItems) {
		// shh, its an easter egg now :)
	}

}

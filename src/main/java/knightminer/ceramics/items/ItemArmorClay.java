package knightminer.ceramics.items;

import knightminer.ceramics.Ceramics;
import net.minecraft.entity.Entity;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemArmorClay extends ItemArmor {

	public ItemArmorClay(EntityEquipmentSlot slot) {
		super(Ceramics.clayArmor, 0, slot);
		this.setCreativeTab(Ceramics.tab);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public String getArmorTexture(ItemStack stack, Entity entity, EntityEquipmentSlot slot, String type) {
		if(slot == EntityEquipmentSlot.LEGS) {
			return "ceramics:textures/models/armor/clay_leggings.png";
		}

		return "ceramics:textures/models/armor/clay.png";
	}

}

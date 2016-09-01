package knightminer.ceramics.items;

import knightminer.ceramics.Ceramics;
import net.minecraft.entity.Entity;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemArmorClayRaw extends ItemArmor {

	public ItemArmorClayRaw(EntityEquipmentSlot slot) {
		super(Ceramics.clayArmorRaw, 0, slot);
		this.setMaxDamage(1); // unfired clay armor doesn't hold together.
								// really, cook it.
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

}

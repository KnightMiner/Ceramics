package knightminer.ceramics.library;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class CreativeTab extends CreativeTabs {

	private ItemStack icon;

	public CreativeTab(String label, ItemStack icon) {
		super(label);
		this.icon = icon;
	}

	public void setIcon(ItemStack icon) {
		if(icon != null && icon.getItem() != null) {
			this.icon = icon;
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public ItemStack getIconItemStack() {
		return icon;
	}

	@Override
	public ItemStack getTabIconItem() {
		return icon;
	}

}

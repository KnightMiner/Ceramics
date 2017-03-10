package knightminer.ceramics.items;

import java.util.Locale;

import knightminer.ceramics.Ceramics;
import knightminer.ceramics.library.Config;
import knightminer.ceramics.library.Util;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemClayUnfired extends Item {

	public ItemClayUnfired() {
		this.setCreativeTab(Ceramics.tab);
		this.setHasSubtypes(true);
		this.setNoRepair();
	}

	/**
	 * returns a list of items with the same ID, but different meta (eg: dye returns 16 items)
	 */
	@Override
	@SideOnly(Side.CLIENT)
	public void getSubItems(Item itemIn, CreativeTabs tab, NonNullList<ItemStack> subItems) {
		for(UnfiredType type : UnfiredType.values()) {
			if(type.shouldDisplay()) {
				subItems.add(new ItemStack(itemIn, 1, type.getMeta()));
			}
		}
	}

	/**
	 * Gets the maximum number of items that this stack should be able to hold.
	 * This is a ItemStack (and thus NBT) sensitive version of Item.getItemStackLimit()
	 *
	 * @param stack The ItemStack
	 * @return The maximum number this item can be stacked to
	 */
	@Override
	public int getItemStackLimit(ItemStack stack) {
		return UnfiredType.fromMeta(stack.getItemDamage()).getStackSize();
	}

	/**
	 * Returns the unlocalized name of this item. This version accepts an ItemStack so different stacks can have
	 * different names based on their damage or NBT.
	 */
	@Override
	public String getUnlocalizedName(ItemStack stack) {
		return "item." + Util.prefix("unfired." + UnfiredType.fromMeta(stack.getItemDamage()).getName());
	}

	public enum UnfiredType {
		BUCKET(16),
		SHEARS(1),
		BARREL,
		BARREL_EXTENSION,
		PORCELAIN,
		PORCELAIN_BRICK,
		BARREL_PORCELAIN,
		BARREL_PORCELAIN_EXTENSION;

		private int meta;
		private int stackSize;

		UnfiredType() {
			meta = this.ordinal();
			this.stackSize = 64;
		}

		UnfiredType(int stackSize) {
			meta = this.ordinal();
			this.stackSize = stackSize;
		}

		public int getMeta() {
			return meta;
		}

		/**
		 * Determines if the unfired item is enabled
		 */
		public boolean shouldDisplay() {
			switch(this) {
				case BUCKET:
					return Config.bucketEnabled;

				case SHEARS:
					return Config.shearsEnabled;

				case BARREL:
				case BARREL_EXTENSION:
					return Config.barrelEnabled;

				case PORCELAIN:
				case PORCELAIN_BRICK:
					return Config.porcelainEnabled;

				case BARREL_PORCELAIN:
				case BARREL_PORCELAIN_EXTENSION:
					return Config.barrelEnabled && Config.porcelainEnabled;
			}
			return true;
		}

		public int getStackSize() {
			return stackSize;
		}

		public static UnfiredType fromMeta(int meta) {
			if(meta < 0 || meta >= values().length) {
				meta = 0;
			}

			return values()[meta];
		}

		public String getName() {
			return this.name().toLowerCase(Locale.US);
		}
	}

}

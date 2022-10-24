package knightminer.ceramics.items;

import knightminer.ceramics.Ceramics;
import knightminer.ceramics.tileentity.CrackableTileEntityHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import javax.annotation.Nullable;
import java.util.List;

/** BlockItem for crackable blocks to show cracked amount */
public class CrackableBlockItem extends FixedTooltipBlockItem {
	private static final String TOOLTIP_KEY = Ceramics.lang("tooltip", "cracked");
	public CrackableBlockItem(Block blockIn, Properties builder, String tooltipSuffix) {
		super(blockIn, builder, tooltipSuffix);
	}

	/**
	 * Gets the cracks value for the given stack
	 * @param stack  Stack
	 * @return  Cracks
	 */
	public static int getCracks(ItemStack stack) {
		CompoundTag nbt = stack.getTag();
		if (nbt != null && nbt.contains(CrackableTileEntityHandler.TAG_CRACKS, Tag.TAG_ANY_NUMERIC)) {
			return nbt.getInt(CrackableTileEntityHandler.TAG_CRACKS);
		}
		return 0;
	}

	/**
	 * Sets the number of cracks for the item
	 * @param stack  Stack to set
	 * @param cracks  New cracks value
	 */
	public static ItemStack setCracks(ItemStack stack, int cracks) {
		if (cracks == 0) {
			CompoundTag nbt = stack.getTag();
			if (nbt != null) {
				nbt.remove(CrackableTileEntityHandler.TAG_CRACKS);
				if (nbt.isEmpty()) {
					stack.setTag(null);
				}
			}
		} else {
			stack.getOrCreateTag().putInt(CrackableTileEntityHandler.TAG_CRACKS, cracks);
		}
		return stack;
	}

	@Override
	public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flag) {
		super.appendHoverText(stack, worldIn, tooltip, flag);
		int cracks = getCracks(stack);
		if (cracks > 0) {
			tooltip.add(new TranslatableComponent(TOOLTIP_KEY, 6 - cracks, 6));
		}
	}

	@Override
	public boolean isBarVisible(ItemStack stack) {
		return stack.getCount() == 1 && getCracks(stack) > 0;
	}

	@Override
	public int getBarWidth(ItemStack stack) {
		return 13 - getCracks(stack) * 2;
	}
}

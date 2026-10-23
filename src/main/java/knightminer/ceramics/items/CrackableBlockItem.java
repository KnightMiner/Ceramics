package knightminer.ceramics.items;

import knightminer.ceramics.Ceramics;
import knightminer.ceramics.Registration;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;

import java.util.List;

/** BlockItem for crackable blocks to show cracked amount */
public class CrackableBlockItem extends FixedTooltipBlockItem {
	private static final Component CRACKABLE = Ceramics.component("tooltip", "crackable").withStyle(ChatFormatting.GRAY);
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
		return stack.getOrDefault(Registration.CRACKS, (byte)0);
	}

	/**
	 * Sets the number of cracks for the item
	 * @param stack  Stack to set
	 * @param cracks  New cracks value
	 */
	public static ItemStack setCracks(ItemStack stack, int cracks) {
		if (cracks <= 0) {
			stack.remove(Registration.CRACKS);
		} else {
			stack.set(Registration.CRACKS, (byte) Math.min(cracks, 5));
		}
		return stack;
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
		int cracks = getCracks(stack);
		if (cracks > 0) {
			tooltip.add(Component.translatable(TOOLTIP_KEY, 6 - cracks, 6));
		}
		super.appendHoverText(stack, context, tooltip, flag);
		tooltip.add(CRACKABLE);
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

package knightminer.ceramics.items;

import knightminer.ceramics.Ceramics;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;

import java.util.List;

/**
 * Adds a specific tooltip to a block, instead of basing it off the translation key as {@link slimeknights.mantle.item.BlockTooltipItem} does
 */
public class FixedTooltipBlockItem extends BlockItem {
	private final Component tooltipComponent;
	public FixedTooltipBlockItem(Block blockIn, Properties builder, String tooltipSuffix) {
		super(blockIn, builder);
		tooltipComponent = Component.translatable(Ceramics.lang("block", tooltipSuffix)).withStyle(ChatFormatting.GRAY);
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
		tooltip.add(tooltipComponent);
		super.appendHoverText(stack, context, tooltip, flag);
	}
}

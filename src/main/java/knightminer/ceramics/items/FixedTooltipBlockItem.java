package knightminer.ceramics.items;

import knightminer.ceramics.Ceramics;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.List;

import net.minecraft.world.item.Item.Properties;

/**
 * Adds a specific tooltip to a block, instead of basing it off the translation key as {@link slimeknights.mantle.item.BlockTooltipItem} does
 */
public class FixedTooltipBlockItem extends BlockItem {
	private final Component tooltipComponent;
	public FixedTooltipBlockItem(Block blockIn, Properties builder, String tooltipSuffix) {
		super(blockIn, builder);
		tooltipComponent = new TranslatableComponent(Ceramics.lang("block", tooltipSuffix)).withStyle(ChatFormatting.GRAY);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
		tooltip.add(tooltipComponent);
		super.appendHoverText(stack, worldIn, tooltip, flagIn);
	}
}

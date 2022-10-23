package knightminer.ceramics.items;

import knightminer.ceramics.Ceramics;
import net.minecraft.block.Block;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.List;

import net.minecraft.item.Item.Properties;

/**
 * Adds a specific tooltip to a block, instead of basing it off the translation key as {@link slimeknights.mantle.item.BlockTooltipItem} does
 */
public class FixedTooltipBlockItem extends BlockItem {
	private final ITextComponent tooltipComponent;
	public FixedTooltipBlockItem(Block blockIn, Properties builder, String tooltipSuffix) {
		super(blockIn, builder);
		tooltipComponent = new TranslationTextComponent(Ceramics.lang("block", tooltipSuffix)).withStyle(TextFormatting.GRAY);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void appendHoverText(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
		tooltip.add(tooltipComponent);
		super.appendHoverText(stack, worldIn, tooltip, flagIn);
	}
}

package knightminer.ceramics.items;

import knightminer.ceramics.Ceramics;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;

import java.util.List;

/** Block item adding an extra tooltip for heat-resistant variants */
public class HeatResistantBlockItem extends FixedTooltipBlockItem {
  private static final Component HEAT_RESISTANT = Ceramics.component("tooltip", "heat_resistant").withStyle(ChatFormatting.GRAY);
  public HeatResistantBlockItem(Block blockIn, Properties builder, String tooltipSuffix) {
    super(blockIn, builder, tooltipSuffix);
  }

  @Override
  public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
    super.appendHoverText(stack, context, tooltip, flag);
    tooltip.add(HEAT_RESISTANT);
  }
}

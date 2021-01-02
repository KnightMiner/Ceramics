package knightminer.ceramics.items;

import knightminer.ceramics.Ceramics;
import knightminer.ceramics.tileentity.CrackableTileEntityHandler;
import net.minecraft.block.Block;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.Constants.NBT;

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
		CompoundNBT nbt = stack.getTag();
		if (nbt != null && nbt.contains(CrackableTileEntityHandler.TAG_CRACKS, NBT.TAG_ANY_NUMERIC)) {
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
			CompoundNBT nbt = stack.getTag();
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
	@OnlyIn(Dist.CLIENT)
	public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flag) {
		super.addInformation(stack, worldIn, tooltip, flag);
		int cracks = getCracks(stack);
		if (cracks > 0) {
			tooltip.add(new TranslationTextComponent(TOOLTIP_KEY, 6 - cracks, 6));
		}
	}

	@Override
	public boolean showDurabilityBar(ItemStack stack) {
		return stack.getCount() == 1 && getCracks(stack) > 0;
	}

	@Override
	public double getDurabilityForDisplay(ItemStack stack) {
		return (double) getCracks(stack) / 6.0;
	}
}

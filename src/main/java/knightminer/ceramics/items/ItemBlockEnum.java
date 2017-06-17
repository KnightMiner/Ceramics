package knightminer.ceramics.items;

import java.util.List;

import javax.annotation.Nonnull;

import knightminer.ceramics.blocks.IBlockEnum;
import net.minecraft.block.Block;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemColored;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.translation.I18n;

@SuppressWarnings("deprecation")
public class ItemBlockEnum extends ItemColored {

	private PropertyEnum<?> prop;

	public <T extends Block & IBlockEnum<?>> ItemBlockEnum(T block) {
		super(block, true);
		this.prop = block.getMappingProperty();
	}

	@Override
	public void addInformation(@Nonnull ItemStack stack, @Nonnull EntityPlayer playerIn, @Nonnull List<String> tooltip, boolean advanced) {
		if(I18n.canTranslate(this.getUnlocalizedName(stack) + ".tooltip")) {
			tooltip.add(TextFormatting.GRAY.toString() + I18n.translateToLocal(this.getUnlocalizedName(stack) + ".tooltip"));
		}
		else if(I18n.canTranslate(super.getUnlocalizedName(stack) + ".tooltip")) {
			tooltip.add(TextFormatting.GRAY.toString() + I18n.translateToLocal(super.getUnlocalizedName(stack) + ".tooltip"));
		}
		super.addInformation(stack, playerIn, tooltip, advanced);
	}

	@Nonnull
	@Override
	public String getUnlocalizedName(@Nonnull ItemStack stack) {
		if(prop == null) {
			return super.getUnlocalizedName(stack);
		}

		IBlockState state = block.getStateFromMeta(stack.getMetadata());
		String name = state.getValue(prop).getName();
		return super.getUnlocalizedName(stack) + "." + name;
	}
}

package knightminer.ceramics.items;

import javax.annotation.Nonnull;

import knightminer.ceramics.blocks.BlockEnumBase;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemColored;
import net.minecraft.item.ItemStack;

public class ItemBlockEnum extends ItemColored {

	private PropertyEnum<?> prop;

	public ItemBlockEnum(BlockEnumBase<?> block) {
		super(block, true);
		this.prop = block.getMappingProperty();
	}

	@SuppressWarnings("deprecation")
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

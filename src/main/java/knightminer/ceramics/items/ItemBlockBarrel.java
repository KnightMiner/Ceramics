package knightminer.ceramics.items;

import net.minecraft.block.Block;
import net.minecraft.item.ItemMultiTexture;

public class ItemBlockBarrel extends ItemMultiTexture {

	public ItemBlockBarrel(Block block, String[] names) {
		super(block, block, names);
	}
}

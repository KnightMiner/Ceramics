package knightminer.ceramics.items;

import net.minecraft.block.Block;
import net.minecraft.item.ItemMultiTexture;

public class ItemBlockBarrel extends ItemMultiTexture {

	public ItemBlockBarrel(Block block) {
		super(block, block, new String[] {"barrel", "barrel_extension"});
		// TODO Auto-generated constructor stub
	}
}

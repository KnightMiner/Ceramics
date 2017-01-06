package knightminer.ceramics.blocks;

import knightminer.ceramics.Ceramics;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.state.IBlockState;

public class BlockStairsBase extends BlockStairs {

	public BlockStairsBase(IBlockState modelState) {
		super(modelState);
		this.useNeighborBrightness = true;
		this.setCreativeTab(Ceramics.tab);
	}

}

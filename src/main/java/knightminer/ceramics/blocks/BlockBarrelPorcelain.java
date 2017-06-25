package knightminer.ceramics.blocks;

import knightminer.ceramics.Ceramics;
import knightminer.ceramics.tileentity.TileBarrel;
import knightminer.ceramics.tileentity.TileBarrelExtension;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;

public class BlockBarrelPorcelain extends BlockBarrelStained {

	public BlockBarrelPorcelain(boolean extension) {
		super(extension);
	}

	// porcelain barrels have a larger capacity
	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		if(isExtension(getStateFromMeta(meta))) {
			return new TileBarrelExtension();
		}
		return new TileBarrel(Fluid.BUCKET_VOLUME * 6);
	}

	/**
	 * Used by the tile entity to determine if this block is a valid extension for the barrel
	 * @param state  State to test
	 * @return  True if the state is a valid extension for this barrel type
	 */
	@Override
	public boolean isValidExtension(IBlockState state) {
		return state.getBlock() == Ceramics.porcelainBarrelExtension;
	}
}

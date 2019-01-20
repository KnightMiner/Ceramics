package knightminer.ceramics.blocks;

import knightminer.ceramics.Ceramics;
import knightminer.ceramics.library.Config;
import knightminer.ceramics.tileentity.TileBarrel;
import knightminer.ceramics.tileentity.TileBarrelExtension;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.NonNullList;
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
		return new TileBarrel(Fluid.BUCKET_VOLUME * Config.barrelPorcelainCapacity);
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

	/**
	 * returns a list of blocks with the same ID, but different meta (eg: wood returns 4 blocks)
	 */
	@Override
	public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> list) {
		if(Config.porcelainEnabled && Config.barrelEnabled) {
			for (EnumDyeColor enumdyecolor : EnumDyeColor.values()) {
				list.add(new ItemStack(this, 1, enumdyecolor.getMetadata()));
			}
		}
	}
}

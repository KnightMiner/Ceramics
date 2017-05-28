package knightminer.ceramics.plugin.tconstruct;

import javax.annotation.Nonnull;

import knightminer.ceramics.Ceramics;
import knightminer.ceramics.blocks.BlockBarrel;
import net.minecraft.block.Block;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

/**
 * This is our own copy of the faucet from Tinkers. It still uses the old TE (and thus TESR),
 * but has some special visual compatibility with barrels
 */
public class BlockFaucet extends slimeknights.tconstruct.smeltery.block.BlockFaucet {

	public static final PropertyBool CONNECTED = PropertyBool.create("connected");

	public BlockFaucet() {
		super();

		this.setCreativeTab(Ceramics.tab);
		this.setDefaultState(this.getDefaultState().withProperty(CONNECTED, false));
	}

	@Nonnull
	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, FACING, CONNECTED);
	}

	/**
	 * Get the actual Block state of this Block at the given position. This applies properties not visible in the
	 * metadata, such as fence connections.
	 */
	@Override
	@Deprecated
	public IBlockState getActualState(IBlockState stateIn, IBlockAccess world, BlockPos pos) {
		IBlockState state = super.getActualState(stateIn, world, pos);

		// if a barrel is behind this, enable the connected state
		EnumFacing facing = state.getValue(FACING);
		if(facing != EnumFacing.UP) {
			Block back = world.getBlockState(pos.offset(facing)).getBlock();
			if(back instanceof BlockBarrel) {
				state = state.withProperty(CONNECTED, true);
			}
		}

		return state;
	}
}

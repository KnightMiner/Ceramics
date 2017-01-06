package knightminer.ceramics.blocks;

import knightminer.ceramics.Ceramics;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;

public class BlockClaySlab extends BlockEnumSlabBase<BlockClayHard.ClayTypeHard> {

	public BlockClaySlab() {
		super(Material.ROCK, BlockClayHard.TYPE);
		this.setCreativeTab(Ceramics.tab);
		this.setHarvestLevel("pickaxe", 0);
		this.setHardness(1.25f);
		this.setResistance(30);
		this.setSoundType(SoundType.STONE);
	}

	@Override
	public IBlockState getFullBlock(IBlockState state) {
		return Ceramics.clayHard.getDefaultState().withProperty(BlockClayHard.TYPE, state.getValue(BlockClayHard.TYPE));
	}

}

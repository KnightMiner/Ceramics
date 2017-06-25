package knightminer.ceramics.library;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface IFaucetDepthFallback {
	float getFlowDepth(World world, BlockPos pos, IBlockState state);
}

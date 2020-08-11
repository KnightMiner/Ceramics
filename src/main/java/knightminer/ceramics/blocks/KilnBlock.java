package knightminer.ceramics.blocks;

import knightminer.ceramics.tileentity.KilnTileEntity;
import net.minecraft.block.AbstractFurnaceBlock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.Random;

public class KilnBlock extends AbstractFurnaceBlock {

  public KilnBlock(Properties props) {
    super(props);
  }

  /** @deprecated  Only added to fully implement interface in {@link net.minecraft.block.AbstractFurnaceBlock} */
  @Deprecated
  @Nullable
  @Override
  public TileEntity createNewTileEntity(IBlockReader world) {
    return new KilnTileEntity();
  }

  @Override
  @Nullable
  public TileEntity createTileEntity(BlockState state, IBlockReader world) {
    return new KilnTileEntity();
  }

  @Override
  protected void interactWith(World world, BlockPos pos, PlayerEntity player) {
    TileEntity tile = world.getTileEntity(pos);
    if (tile instanceof KilnTileEntity) {
      player.openContainer((KilnTileEntity)tile);
    }
  }

  @Override
  @OnlyIn(Dist.CLIENT)
  public void animateTick(BlockState state, World world, BlockPos pos, Random random) {
    if (state.get(LIT)) {
      double x = pos.getX() + 0.5D;
      double y = pos.getY();
      double z = pos.getZ() + 0.5D;
      if (random.nextDouble() < 0.1D) {
        world.playSound(x, y, z, SoundEvents.BLOCK_FURNACE_FIRE_CRACKLE, SoundCategory.BLOCKS, 1.0F, 1.0F, false);
      }

      Direction facing = state.get(FACING);
      Axis axis = facing.getAxis();
      double axisOffset = random.nextDouble() * 0.6D - 0.3D;
      double xOffset = axis == Axis.X ? (double)facing.getXOffset() * 0.52D : axisOffset;
      double yOffset = random.nextDouble() * 7.0D / 16.0D;
      double zOffset = axis == Axis.Z ? (double)facing.getZOffset() * 0.52D : axisOffset;
      world.addParticle(ParticleTypes.SMOKE, x + xOffset, y + yOffset, z + zOffset, 0.0D, 0.0D, 0.0D);
    }
  }
}

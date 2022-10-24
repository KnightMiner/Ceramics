package knightminer.ceramics.blocks;

import knightminer.ceramics.Registration;
import knightminer.ceramics.tileentity.KilnTileEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.Random;

public class KilnBlock extends AbstractFurnaceBlock {

  public KilnBlock(Properties props) {
    super(props);
  }

  @Nullable
  @Override
  public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
    return new KilnTileEntity(pos, state);
  }

  @Override
  @Nullable
  public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType) {
    return createFurnaceTicker(pLevel, pBlockEntityType, Registration.KILN_TILE_ENTITY.get());
  }
  @Override
  protected void openContainer(Level world, BlockPos pos, Player player) {
    BlockEntity tile = world.getBlockEntity(pos);
    if (tile instanceof KilnTileEntity) {
      player.openMenu((KilnTileEntity)tile);
    }
  }

  @Override
  public void animateTick(BlockState state, Level world, BlockPos pos, Random random) {
    if (state.getValue(LIT)) {
      double x = pos.getX() + 0.5D;
      double y = pos.getY();
      double z = pos.getZ() + 0.5D;
      if (random.nextDouble() < 0.1D) {
        world.playLocalSound(x, y, z, SoundEvents.FURNACE_FIRE_CRACKLE, SoundSource.BLOCKS, 1.0F, 1.0F, false);
      }

      Direction facing = state.getValue(FACING);
      Axis axis = facing.getAxis();
      double axisOffset = random.nextDouble() * 0.6D - 0.3D;
      double xOffset = axis == Axis.X ? (double)facing.getStepX() * 0.52D : axisOffset;
      double yOffset = random.nextDouble() * 7.0D / 16.0D;
      double zOffset = axis == Axis.Z ? (double)facing.getStepZ() * 0.52D : axisOffset;
      world.addParticle(ParticleTypes.SMOKE, x + xOffset, y + yOffset, z + zOffset, 0.0D, 0.0D, 0.0D);
    }
  }
}

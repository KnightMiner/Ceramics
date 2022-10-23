package knightminer.ceramics.blocks;

import knightminer.ceramics.tileentity.CrackableTileEntityHandler.ICrackableBlock;
import knightminer.ceramics.tileentity.FaucetTileEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.InteractionResult;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import slimeknights.mantle.util.TileEntityHelper;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.Random;

import net.minecraft.world.level.block.state.BlockBehaviour.Properties;

/**
 * Pouring variant of the faucet block
 */
public class PouringFaucetBlock extends FaucetBlock implements ICrackableBlock {
  private final boolean crackable;
  public PouringFaucetBlock(Properties builder, boolean crackable) {
    super(builder);
    this.crackable = crackable;
  }


  /* Tile entity */

  @Override
  public boolean hasTileEntity(BlockState state) {
    return true;
  }

  @Override
  public BlockEntity createTileEntity(BlockState state, BlockGetter world) {
    return new FaucetTileEntity(crackable);
  }

  @SuppressWarnings("deprecation")
  @Deprecated
  @Override
  public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn, BlockHitResult hit) {
    if (isCrackable() && ICrackableBlock.tryRepair(worldIn, pos, player, handIn)) {
      return InteractionResult.SUCCESS;
    }
    if (player.isShiftKeyDown()) {
      return InteractionResult.PASS;
    }
    getFaucet(worldIn, pos).ifPresent(FaucetTileEntity::activate);
    return InteractionResult.SUCCESS;
  }

  @SuppressWarnings("deprecation")
  @Deprecated
  @Override
  public void neighborChanged(BlockState state, Level worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
    if (worldIn.isClientSide()) {
      return;
    }
    getFaucet(worldIn, pos).ifPresent(faucet -> {
      faucet.neighborChanged(fromPos);
      faucet.handleRedstone(worldIn.hasNeighborSignal(pos));
    });
  }

  @SuppressWarnings("deprecation")
  @Deprecated
  @Override
  public void tick(BlockState state, ServerLevel worldIn, BlockPos pos, Random rand) {
    getFaucet(worldIn, pos).ifPresent(FaucetTileEntity::activate);
  }

  /**
   * Gets the facuet tile entity at the given position
   * @param world  World instance
   * @param pos    Faucet position
   * @return  Optional of faucet, empty if missing or wrong type
   */
  private Optional<FaucetTileEntity> getFaucet(Level world, BlockPos pos) {
    return TileEntityHelper.getTile(FaucetTileEntity.class, world, pos);
  }

  /* Display */

  /**
   * Adds particles to the faucet
   * @param state    Faucet state
   * @param worldIn  World instance
   * @param pos      Faucet position
   */
  private static void addParticles(BlockState state, LevelAccessor worldIn, BlockPos pos) {
    Direction direction = state.getValue(FACING);
    double x = (double)pos.getX() + 0.5D - 0.3D * (double)direction.getStepX();
    double y = (double)pos.getY() + 0.5D - 0.3D * (double)direction.getStepY();
    double z = (double)pos.getZ() + 0.5D - 0.3D * (double)direction.getStepZ();
    worldIn.addParticle(new DustParticleOptions(1.0F, 0.0F, 0.0F, 0.5f), x, y, z, 0.0D, 0.0D, 0.0D);
  }

  @Override
  @OnlyIn(Dist.CLIENT)
  public void animateTick(BlockState stateIn, Level worldIn, BlockPos pos, Random rand) {
    getFaucet(worldIn, pos).ifPresent(faucet -> {
      if (faucet.isPouring() && faucet.getRenderFluid().isEmpty() && rand.nextFloat() < 0.25F) {
        addParticles(stateIn, worldIn, pos);
      }
    });
  }


  /* Cracking */

  @Override
  public boolean isCrackable() {
    return crackable;
  }

  @SuppressWarnings("deprecation")
  @Override
  @Deprecated
  public void randomTick(BlockState state, ServerLevel worldIn, BlockPos pos, Random random) {
    if (isCrackable()) {
      TileEntityHelper.getTile(FaucetTileEntity.class, worldIn, pos).ifPresent(FaucetTileEntity::randomTick);
    }
  }

  @Override
  public void setPlacedBy(Level worldIn, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
    if (isCrackable()) {
      ICrackableBlock.onBlockPlacedBy(worldIn, pos, stack);
    }
  }
}

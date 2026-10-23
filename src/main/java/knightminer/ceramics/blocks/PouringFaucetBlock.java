package knightminer.ceramics.blocks;

import knightminer.ceramics.Registration;
import knightminer.ceramics.blocks.entity.CrackableBlockEntityHandler.ICrackableBlock;
import knightminer.ceramics.blocks.entity.FaucetBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import slimeknights.mantle.util.BlockEntityHelper;

import javax.annotation.Nullable;

/**
 * Pouring variant of the faucet block
 */
public class PouringFaucetBlock extends FaucetBlock implements ICrackableBlock, EntityBlock {
  private final boolean crackable;
  public PouringFaucetBlock(Properties builder, boolean crackable) {
    super(builder);
    this.crackable = crackable;
  }


  /* Tile entity */

  @Nullable
  @Override
  public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
    return new FaucetBlockEntity(pos, state, crackable);
  }

  @Nullable
  @Override
  public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
    return BlockEntityHelper.serverTicker(level, type, Registration.FAUCET_BLOCK_ENTITY.get(), FaucetBlockEntity.SERVER_TICKER);
  }

  @Override
  protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
    if (isCrackable() && ICrackableBlock.tryRepair(stack, level, pos, player, hand)) {
      return ItemInteractionResult.SUCCESS;
    }
    return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
  }

  @Override
  protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
    if (player.isShiftKeyDown()) {
      return InteractionResult.PASS;
    }
    if (level.getBlockEntity(pos) instanceof FaucetBlockEntity faucet) {
      faucet.activate();
    }
    return InteractionResult.SUCCESS;
  }

  @Override
  @Deprecated
  protected void neighborChanged(BlockState state, Level worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
    if (worldIn.isClientSide()) {
      return;
    }
    if (worldIn.getBlockEntity(pos) instanceof FaucetBlockEntity faucet) {
      faucet.handleRedstone(worldIn.hasNeighborSignal(pos));
    }
  }

  @Override
  protected void tick(BlockState state, ServerLevel worldIn, BlockPos pos, RandomSource rand) {
    if (worldIn.getBlockEntity(pos) instanceof FaucetBlockEntity faucet) {
      faucet.activate();
    }
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
    worldIn.addParticle(new DustParticleOptions(DustParticleOptions.REDSTONE_PARTICLE_COLOR, 0.5f), x, y, z, 0.0D, 0.0D, 0.0D);
  }

  @Override
  public void animateTick(BlockState stateIn, Level worldIn, BlockPos pos, RandomSource rand) {
    if (worldIn.getBlockEntity(pos) instanceof FaucetBlockEntity faucet) {
      if (faucet.isPouring() && faucet.getRenderFluid().isEmpty() && rand.nextFloat() < 0.25F) {
        addParticles(stateIn, worldIn, pos);
      }
    }
  }


  /* Cracking */

  @Override
  public boolean isCrackable() {
    return crackable;
  }

  @Override
  protected void randomTick(BlockState state, ServerLevel worldIn, BlockPos pos, RandomSource random) {
    if (isCrackable() && worldIn.getBlockEntity(pos) instanceof FaucetBlockEntity faucet) {
      faucet.randomTick();
    }
  }
}

package knightminer.ceramics.blocks;

import knightminer.ceramics.tileentity.CrackableTileEntityHandler.ICrackableBlock;
import knightminer.ceramics.tileentity.FaucetTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.RedstoneParticleData;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import slimeknights.mantle.util.TileEntityHelper;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.Random;

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
  public TileEntity createTileEntity(BlockState state, IBlockReader world) {
    return new FaucetTileEntity(crackable);
  }

  @SuppressWarnings("deprecation")
  @Deprecated
  @Override
  public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
    if (player.isSneaking()) {
      return ActionResultType.PASS;
    }
    getFaucet(worldIn, pos).ifPresent(FaucetTileEntity::activate);
    return ActionResultType.SUCCESS;
  }

  @Override
  public boolean canConnectRedstone(BlockState state, IBlockReader world, BlockPos pos, @Nullable Direction side) {
    // TODO: keep?
    return true;
  }

  @SuppressWarnings("deprecation")
  @Deprecated
  @Override
  public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
    if (worldIn.isRemote()) {
      return;
    }
    getFaucet(worldIn, pos).ifPresent(faucet -> {
      faucet.neighborChanged(fromPos);
      faucet.handleRedstone(worldIn.isBlockPowered(pos));
    });
  }

  @SuppressWarnings("deprecation")
  @Deprecated
  @Override
  public void tick(BlockState state, ServerWorld worldIn, BlockPos pos, Random rand) {
    getFaucet(worldIn, pos).ifPresent(FaucetTileEntity::activate);
  }

  /**
   * Gets the facuet tile entity at the given position
   * @param world  World instance
   * @param pos    Faucet position
   * @return  Optional of faucet, empty if missing or wrong type
   */
  private Optional<FaucetTileEntity> getFaucet(World world, BlockPos pos) {
    return TileEntityHelper.getTile(FaucetTileEntity.class, world, pos);
  }

  /* Display */

  /**
   * Adds particles to the faucet
   * @param state    Faucet state
   * @param worldIn  World instance
   * @param pos      Faucet position
   */
  private static void addParticles(BlockState state, IWorld worldIn, BlockPos pos) {
    Direction direction = state.get(FACING);
    double x = (double)pos.getX() + 0.5D - 0.3D * (double)direction.getXOffset();
    double y = (double)pos.getY() + 0.5D - 0.3D * (double)direction.getYOffset();
    double z = (double)pos.getZ() + 0.5D - 0.3D * (double)direction.getZOffset();
    worldIn.addParticle(new RedstoneParticleData(1.0F, 0.0F, 0.0F, 0.5f), x, y, z, 0.0D, 0.0D, 0.0D);
  }

  @Override
  @OnlyIn(Dist.CLIENT)
  public void animateTick(BlockState stateIn, World worldIn, BlockPos pos, Random rand) {
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
  public void randomTick(BlockState state, ServerWorld worldIn, BlockPos pos, Random random) {
    if (isCrackable()) {
      TileEntityHelper.getTile(FaucetTileEntity.class, worldIn, pos).ifPresent(FaucetTileEntity::randomTick);
    }
  }

  @Override
  public void onBlockPlacedBy(World worldIn, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
    if (crackable) {
      ICrackableBlock.onBlockPlacedBy(worldIn, pos, stack);
    }
  }
}

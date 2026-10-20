package knightminer.ceramics.blocks;

import knightminer.ceramics.blocks.entity.CisternBlockEntity;
import knightminer.ceramics.blocks.entity.CrackableBlockEntityHandler.ICrackableBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import slimeknights.mantle.fluid.FluidTransferHelper;

import javax.annotation.Nullable;

/**
 * Fired cistern block that can store fluids
 */
public class FluidCisternBlock extends CisternBlock implements ICrackableBlock, EntityBlock {
  private final boolean crackable;
  public FluidCisternBlock(Properties properties, boolean crackable) {
    super(properties);
    this.crackable = crackable;
  }


  /* Block entity */

  @Override
  public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
    return new CisternBlockEntity(pos, state, crackable);
  }

  
  /* Interaction */

  @Override
  protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
    if (crackable && ICrackableBlock.tryRepair(stack, level, pos, player, hand)) {
      return ItemInteractionResult.SUCCESS;
    }
    if (FluidTransferHelper.interactWithTank(level, pos, player, hand, hit)) {
      return ItemInteractionResult.SUCCESS;
    }
    return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
  }


  /* Structure behavior */

  /**
   * Finds the base TE for this extension
   * @param world  World instance
   * @param pos    Cistern extension position
   * @return  Optional containing base TE, or empty optional if base cannot be found
   */
  @Nullable
  private CisternBlockEntity findBase(Level world, BlockPos pos) {
    BlockPos base = pos;
    BlockState checkState;
    do {
      base = base.below();
      checkState = world.getBlockState(base);
    } while (checkState.is(this) && checkState.getValue(CisternBlock.EXTENSION));

    // if the position is a cistern, it means we found a base, return that position
    if (checkState.is(this) && world.getBlockEntity(base) instanceof CisternBlockEntity cistern) {
      return cistern;
    }
    // not found, return nothing
    return null;
  }

  @Override
  public void setPlacedBy(Level world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
    if (state.getValue(CisternBlock.EXTENSION)) {
      // try to find a base cistern below if an extension
      CisternBlockEntity base = findBase(world, pos);
      if (base != null) {
        base.addExtension(pos);
      }
    } else if (world.getBlockEntity(pos) instanceof CisternBlockEntity cistern) {
      cistern.tryMerge(pos.above());
    }
  }

  @Override
  protected void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean isMoving) {
    if (state.hasBlockEntity() && (!state.is(newState.getBlock()) || !newState.hasBlockEntity())) {
      if (state.getValue(EXTENSION)) {
        CisternBlockEntity base = findBase(world, pos);
        if (base != null) {
          base.removeExtension(pos);
        }
      } else if (world.getBlockEntity(pos) instanceof CisternBlockEntity cistern) {
        cistern.onBroken(this);
      }
      world.removeBlockEntity(pos);
    }
  }

  @Override
  protected BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor worldIn, BlockPos currentPos, BlockPos facingPos) {
    // overridden to remove down connection, we handle that conditionally in the TE
    if (!facing.getAxis().isVertical()) {
      // barrel connects to
      state = state.setValue(CONNECTIONS.get(facing), isConnected(facing, facingState));
    }
    return state;
  }


  /* Cracking */

  @Override
  public boolean isCrackable() {
    return crackable;
  }

  @Override
  protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
    if (isCrackable() && random.nextInt(5) == 0 && level.getBlockEntity(pos) instanceof CisternBlockEntity cistern) {
      cistern.randomTick();
    }
  }
}

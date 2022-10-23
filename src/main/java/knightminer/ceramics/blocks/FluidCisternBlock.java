package knightminer.ceramics.blocks;

import knightminer.ceramics.tileentity.CisternTileEntity;
import knightminer.ceramics.tileentity.CrackableTileEntityHandler.ICrackableBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import slimeknights.mantle.util.BlockEntityHelper;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.Random;

/**
 * Fired cistern block that can store fluids
 */
public class FluidCisternBlock extends CisternBlock implements ICrackableBlock, EntityBlock {
  private final boolean crackable;
  public FluidCisternBlock(Properties properties, boolean crackable) {
    super(properties);
    this.crackable = crackable;
  }


  /* Tile entity */

  @Override
  public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
    return new CisternTileEntity(pos, state, crackable);
  }

  /* Interaction */

  @SuppressWarnings("deprecation")
  @Deprecated
  @Override
  public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
    if (crackable && ICrackableBlock.tryRepair(world, pos, player, hand)) {
      return InteractionResult.SUCCESS;
    }
    // success if the item is a fluid handler, regardless of if fluid moved
    if (FluidUtil.getFluidHandler(player.getItemInHand(hand)).isPresent()) {
      // only server needs to do anything
      if (!world.isClientSide()) {
        BlockEntity te = world.getBlockEntity(pos);
        if (te != null) {
          // simply update the fluid handler capability
            te.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, hit.getDirection())
              .ifPresent(handler -> FluidUtil.interactWithFluidHandler(player, hand, handler));
        }
      }
      return InteractionResult.SUCCESS;
    }
    return InteractionResult.PASS;
  }


  /* Structure behavior */

  /**
   * Finds the base TE for this extension
   * @param world  World instance
   * @param pos    Cistern extension position
   * @return  Optional containing base TE, or empty optional if base cannot be found
   */
  private Optional<CisternTileEntity> findBase(Level world, BlockPos pos) {
    BlockPos base = pos;
    BlockState checkState;
    do {
      base = base.below();
      checkState = world.getBlockState(base);
    } while (checkState.is(this) && checkState.getValue(CisternBlock.EXTENSION));

    // if the position is a cistern, it means we found a base, return that position
    if (checkState.is(this)) {
      return BlockEntityHelper.get(CisternTileEntity.class, world, base);
    }
    // not found, return nothing
    return Optional.empty();
  }

  @Override
  public void setPlacedBy(Level world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
    if (state.getValue(CisternBlock.EXTENSION)) {
      // try to find a base cistern below if an extension
      findBase(world, pos).ifPresent(te -> te.addExtension(pos));
      // crackable handling
      if (crackable) {
        ICrackableBlock.onBlockPlacedBy(world, pos, stack);
      }
    } else {
      BlockEntityHelper.get(CisternTileEntity.class, world, pos).ifPresent(te -> {
        te.tryMerge(pos.above());
        // crackable handling
        if (crackable) {
          te.getCracksHandler().setCracks(stack);
        }
      });
    }
  }

  @SuppressWarnings("deprecation")
  @Override
  @Deprecated
  public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean isMoving) {
    if (state.hasBlockEntity() && (!state.is(newState.getBlock()) || !newState.hasBlockEntity())) {
      if (state.getValue(EXTENSION)) {
        findBase(world, pos).ifPresent(te -> te.removeExtension(pos));
      } else {
        BlockEntityHelper.get(CisternTileEntity.class, world, pos).ifPresent(te -> te.onBroken(this));
      }
      world.removeBlockEntity(pos);
    }
  }

  @SuppressWarnings("deprecation")
  @Override
  @Deprecated
  public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor worldIn, BlockPos currentPos, BlockPos facingPos) {
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

  @SuppressWarnings("deprecation")
  @Override
  @Deprecated
  public void randomTick(BlockState state, ServerLevel worldIn, BlockPos pos, Random random) {
    if (isCrackable() && random.nextInt(5) == 0) {
      BlockEntityHelper.get(CisternTileEntity.class, worldIn, pos).ifPresent(CisternTileEntity::randomTick);
    }
  }
}

package knightminer.ceramics.blocks;

import knightminer.ceramics.tileentity.CisternTileEntity;
import knightminer.ceramics.tileentity.CrackableTileEntityHandler.ICrackableBlock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
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
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import slimeknights.mantle.util.TileEntityHelper;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.Random;

import net.minecraft.block.AbstractBlock.Properties;

/**
 * Fired cistern block that can store fluids
 */
public class FluidCisternBlock extends CisternBlock implements ICrackableBlock {
  private final boolean crackable;
  public FluidCisternBlock(Properties properties, boolean crackable) {
    super(properties);
    this.crackable = crackable;
  }


  /* Tile entity */

  @Override
  public boolean hasTileEntity(BlockState state) {
    return true;
  }

  @Override
  @Nullable
  public TileEntity createTileEntity(BlockState state, IBlockReader world) {
    return new CisternTileEntity(crackable);
  }


  /* Interaction */

  @SuppressWarnings("deprecation")
  @Deprecated
  @Override
  public ActionResultType use(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hit) {
    if (crackable && ICrackableBlock.tryRepair(world, pos, player, hand)) {
      return ActionResultType.SUCCESS;
    }
    // success if the item is a fluid handler, regardless of if fluid moved
    if (FluidUtil.getFluidHandler(player.getItemInHand(hand)).isPresent()) {
      // only server needs to do anything
      if (!world.isClientSide()) {
        TileEntity te = world.getBlockEntity(pos);
        if (te != null) {
          // simply update the fluid handler capability
            te.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, hit.getDirection())
              .ifPresent(handler -> FluidUtil.interactWithFluidHandler(player, hand, handler));
        }
      }
      return ActionResultType.SUCCESS;
    }
    return ActionResultType.PASS;
  }


  /* Structure behavior */

  /**
   * Finds the base TE for this extension
   * @param world  World instance
   * @param pos    Cistern extension position
   * @return  Optional containing base TE, or empty optional if base cannot be found
   */
  private Optional<CisternTileEntity> findBase(World world, BlockPos pos) {
    BlockPos base = pos;
    BlockState checkState;
    do {
      base = base.below();
      checkState = world.getBlockState(base);
    } while (checkState.is(this) && checkState.getValue(CisternBlock.EXTENSION));

    // if the position is a cistern, it means we found a base, return that position
    if (checkState.is(this)) {
      return TileEntityHelper.getTile(CisternTileEntity.class, world, base);
    }
    // not found, return nothing
    return Optional.empty();
  }

  @Override
  public void setPlacedBy(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
    if (state.getValue(CisternBlock.EXTENSION)) {
      // try to find a base cistern below if an extension
      findBase(world, pos).ifPresent(te -> te.addExtension(pos));
      // crackable handling
      if (crackable) {
        ICrackableBlock.onBlockPlacedBy(world, pos, stack);
      }
    } else {
      TileEntityHelper.getTile(CisternTileEntity.class, world, pos).ifPresent(te -> {
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
  public void onRemove(BlockState state, World world, BlockPos pos, BlockState newState, boolean isMoving) {
    if (state.hasTileEntity() && (!state.is(newState.getBlock()) || !newState.hasTileEntity())) {
      if (state.getValue(EXTENSION)) {
        findBase(world, pos).ifPresent(te -> te.removeExtension(pos));
      } else {
        TileEntityHelper.getTile(CisternTileEntity.class, world, pos).ifPresent(te -> te.onBroken(this));
      }
      world.removeBlockEntity(pos);
    }
  }

  @SuppressWarnings("deprecation")
  @Override
  @Deprecated
  public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos) {
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
  public void randomTick(BlockState state, ServerWorld worldIn, BlockPos pos, Random random) {
    if (isCrackable() && random.nextInt(5) == 0) {
      TileEntityHelper.getTile(CisternTileEntity.class, worldIn, pos).ifPresent(CisternTileEntity::randomTick);
    }
  }
}

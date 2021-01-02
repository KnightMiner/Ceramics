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
  public ActionResultType onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hit) {
    boolean success = false;
    if (!world.isRemote()) {
      // simply update the fluid handler capability
      TileEntity te = world.getTileEntity(pos);
      if (te != null) {
        success = te.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, hit.getFace())
                    .filter(handler -> FluidUtil.interactWithFluidHandler(player, hand, handler))
                    .isPresent();
      }
    }
    // return success for any fluid handlers
    if (success || FluidUtil.getFluidHandler(player.getHeldItem(hand)).isPresent()) {
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
      base = base.down();
      checkState = world.getBlockState(base);
    } while (checkState.isIn(this) && checkState.get(CisternBlock.EXTENSION));

    // if the position is a cistern, it means we found a base, return that position
    if (checkState.isIn(this)) {
      return TileEntityHelper.getTile(CisternTileEntity.class, world, base);
    }
    // not found, return nothing
    return Optional.empty();
  }

  @Override
  public void onBlockPlacedBy(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
    if (state.get(CisternBlock.EXTENSION)) {
      // try to find a base cistern below if an extension
      findBase(world, pos).ifPresent(te -> te.addExtension(pos));
      // crackable handling
      if (crackable) {
        ICrackableBlock.onBlockPlacedBy(world, pos, stack);
      }
    } else {
      TileEntityHelper.getTile(CisternTileEntity.class, world, pos).ifPresent(te -> {
        te.tryMerge(pos.up());
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
  public void onReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean isMoving) {
    if (state.hasTileEntity() && (!state.isIn(newState.getBlock()) || !newState.hasTileEntity())) {
      if (state.get(EXTENSION)) {
        findBase(world, pos).ifPresent(te -> te.removeExtension(pos));
      } else {
        TileEntityHelper.getTile(CisternTileEntity.class, world, pos).ifPresent(te -> te.onBroken(this));
      }
      world.removeTileEntity(pos);
    }
  }

  @SuppressWarnings("deprecation")
  @Override
  @Deprecated
  public BlockState updatePostPlacement(BlockState state, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos) {
    // overridden to remove down connection, we handle that conditionally in the TE
    if (!facing.getAxis().isVertical()) {
      // barrel connects to
      state = state.with(CONNECTIONS.get(facing), isConnected(facing, facingState));
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

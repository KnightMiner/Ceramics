package knightminer.ceramics.blocks.entity;

import knightminer.ceramics.blocks.entity.CrackableBlockEntityHandler.ICrackableBlock;
import knightminer.ceramics.blocks.entity.CrackableBlockEntityHandler.ICrackableBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;
import slimeknights.mantle.block.entity.MantleBlockEntity;

import javax.annotation.Nonnull;

/** Base logic for a block entity with cracking behavior. */
public abstract class CrackableBlockEntity extends MantleBlockEntity implements ICrackableBlockEntity {
  /** Crackable handling */
  private final CrackableBlockEntityHandler cracksHandler;

  /** Gets the crackable status for the given block state. */
  public static boolean isCrackable(BlockState state) {
    return state.getBlock() instanceof ICrackableBlock crackable && crackable.isCrackable();
  }

  protected CrackableBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, boolean crackable) {
    super(type, pos, state);
    cracksHandler = new CrackableBlockEntityHandler(this, crackable);
  }

  protected CrackableBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
    this(type, pos, state, isCrackable(state));
  }

  @Override
  public CrackableBlockEntityHandler getCracksHandler() {
    return cracksHandler;
  }

  @Nonnull
  @Override
  public ModelData getModelData() {
    return cracksHandler.getModelData();
  }


  /* Components */

  @Override
  protected void applyImplicitComponents(DataComponentInput componentInput) {
    cracksHandler.applyImplicitComponents(componentInput);
  }

  @Override
  protected void collectImplicitComponents(DataComponentMap.Builder components) {
    cracksHandler.collectImplicitComponents(components);
  }

  @SuppressWarnings("deprecation")
  @Override
  public void removeComponentsFromTag(CompoundTag tag) {
    cracksHandler.removeComponentsFromTag(tag);
  }


  /* NBT */

  @Override
  protected void saveSynced(CompoundTag nbt, HolderLookup.Provider registries) {
    super.saveSynced(nbt, registries);
    cracksHandler.writeNBT(nbt);
  }

  @Override
  protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
    super.loadAdditional(tag, registries);
    cracksHandler.readNBT(getBlockState(), tag);
  }
}

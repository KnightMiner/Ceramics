package knightminer.ceramics.datagen;

import knightminer.ceramics.Ceramics;
import knightminer.ceramics.Registration;
import knightminer.ceramics.blocks.RainbowPorcelain;
import knightminer.ceramics.registration.BuildingBlockObject;
import knightminer.ceramics.registration.EnumBlockObject;
import net.minecraft.block.Block;
import net.minecraft.item.DyeColor;
import net.minecraft.world.storage.loot.ConstantRange;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import java.util.stream.Collectors;

public class BlockLootTables extends net.minecraft.data.loot.BlockLootTables {
  @Nonnull
  @Override
  protected Iterable<Block> getKnownBlocks() {
    return ForgeRegistries.BLOCKS.getValues().stream()
                                 .filter((block) -> Ceramics.MOD_ID.equals(block.getRegistryName().getNamespace()))
                                 .collect(Collectors.toList());
  }

  @Override
  protected void addTables() {
    // unfired porcelain drops 4 of the item form
    this.registerLootTable(Registration.UNFIRED_PORCELAIN_BLOCK.get(), (block) -> {
      return droppingWithSilkTouchOrRandomly(block, Registration.UNFIRED_PORCELAIN, ConstantRange.of(4));
    });
    registerEnumLootTables(Registration.PORCELAIN_BLOCK, DyeColor.values());
    registerEnumLootTables(Registration.RAINBOW_PORCELAIN, RainbowPorcelain.values());

    // bricks - clay
    registerBuildingLootTable(Registration.DARK_BRICKS);
    registerBuildingLootTable(Registration.LAVA_BRICKS);
    registerBuildingLootTable(Registration.DRAGON_BRICKS);
    // bricks - porcelain
    registerBuildingLootTable(Registration.PORCELAIN_BRICKS);
    registerBuildingLootTable(Registration.MONOCHROME_BRICKS);
    registerBuildingLootTable(Registration.GOLDEN_BRICKS);
    registerBuildingLootTable(Registration.MARINE_BRICKS);
    registerBuildingLootTable(Registration.RAINBOW_BRICKS);
  }

                           /**
                            * Registers self drops for an enum block
                            * @param enumBlock  Block instance
                            * @param values     Values list
                            * @param <T>        Block value type
                            */
  private <T extends Enum<T>> void registerEnumLootTables(EnumBlockObject<T, ? extends Block> enumBlock, T[] values) {
    for (T value : values) {
      registerDropSelfLootTable(enumBlock.getBlock(value));
    }
  }

  /**
   * Registers self loot tables for block, slab, stairs, and wall
   * @param building  Building block object
   */
  private void registerBuildingLootTable(BuildingBlockObject building) {
    registerDropSelfLootTable(building.get());
    registerDropSelfLootTable(building.getSlab());
    registerDropSelfLootTable(building.getStairs());
    registerDropSelfLootTable(building.getWall());
  }
}

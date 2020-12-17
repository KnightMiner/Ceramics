package knightminer.ceramics.datagen;

import knightminer.ceramics.Ceramics;
import knightminer.ceramics.Registration;
import net.minecraft.block.Block;
import net.minecraft.loot.ConstantRange;
import net.minecraftforge.registries.ForgeRegistries;
import slimeknights.mantle.registration.object.WallBuildingBlockObject;

import java.util.Objects;
import java.util.stream.Collectors;

public class BlockLootTables extends net.minecraft.data.loot.BlockLootTables {
  @Override
  protected Iterable<Block> getKnownBlocks() {
    return ForgeRegistries.BLOCKS.getValues().stream()
                                 .filter((block) -> Ceramics.MOD_ID.equals(Objects.requireNonNull(block.getRegistryName()).getNamespace()))
                                 .collect(Collectors.toList());
  }

  @Override
  protected void addTables() {
    // unfired porcelain drops 4 of the item form
    this.registerLootTable(Registration.UNFIRED_PORCELAIN_BLOCK.get(), (block) ->
        droppingWithSilkTouchOrRandomly(block, Registration.UNFIRED_PORCELAIN, ConstantRange.of(4)));
    Registration.PORCELAIN_BLOCK.forEach(this::registerDropSelfLootTable);
    Registration.RAINBOW_PORCELAIN.forEach(this::registerDropSelfLootTable);

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
    // kiln
    registerDropSelfLootTable(Registration.KILN.get());
    // cistern
    registerDropSelfLootTable(Registration.TERRACOTTA_CISTERN.get());
    registerDropSelfLootTable(Registration.UNFIRED_CISTERN.get());
    registerDropSelfLootTable(Registration.GAUGE.get());
    Registration.COLORED_CISTERN.forEach(this::registerDropSelfLootTable);
  }

  /**
   * Registers self loot tables for block, slab, stairs, and wall
   * @param building  Building block object
   */
  private void registerBuildingLootTable(WallBuildingBlockObject building) {
    registerDropSelfLootTable(building.get());
    registerLootTable(building.getSlab(), BlockLootTables::droppingSlab);
    registerDropSelfLootTable(building.getStairs());
    registerDropSelfLootTable(building.getWall());
  }
}

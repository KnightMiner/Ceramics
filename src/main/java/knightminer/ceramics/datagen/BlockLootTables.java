package knightminer.ceramics.datagen;

import knightminer.ceramics.Ceramics;
import knightminer.ceramics.Registration;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootTable.Builder;
import net.minecraft.world.level.storage.loot.functions.CopyComponentsFunction;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import slimeknights.mantle.registration.object.WallBuildingBlockObject;

import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import java.util.function.IntFunction;

public class BlockLootTables extends BlockLootSubProvider {
  protected BlockLootTables(HolderLookup.Provider registries) {
    super(Set.of(), FeatureFlags.REGISTRY.allFlags(), registries);
  }

  @Override
  protected Iterable<Block> getKnownBlocks() {
    return BuiltInRegistries.BLOCK.entrySet().stream()
      .filter(entry -> Ceramics.MOD_ID.equals(entry.getKey().location().getNamespace()))
      .map(Entry::getValue)
      .toList();
  }

  @Override
  protected void generate() {
    IntFunction<Function<Block,Builder>> dropClay = count -> block -> createSingleItemTableWithSilkTouch(block, Items.CLAY_BALL, ConstantValue.exactly(count));
    IntFunction<Function<Block,Builder>> dropPorcelain = count -> block -> createSingleItemTableWithSilkTouch(block, Registration.UNFIRED_PORCELAIN, ConstantValue.exactly(count));
    Function<Block,Builder> dropSelfWithCracks = block -> createSingleItemTable(block).apply(CopyComponentsFunction.copyComponents(CopyComponentsFunction.Source.BLOCK_ENTITY).include(Registration.CRACKS.get()));

    // unfired porcelain drops 4 of the item form
    add(Registration.UNFIRED_PORCELAIN_BLOCK.get(), dropPorcelain.apply(4));
    Registration.PORCELAIN_BLOCK.forEach(this::dropSelf);
    Registration.RAINBOW_PORCELAIN.forEach(this::dropSelf);

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
    dropSelf(Registration.KILN.get());
    // gauge
    dropSelf(Registration.TERRACOTTA_GAUGE.get());
    dropSelf(Registration.PORCELAIN_GAUGE.get());
    // cistern
    add(Registration.CLAY_CISTERN.get(), dropClay.apply(3));
    add(Registration.UNFIRED_CISTERN.get(), dropPorcelain.apply(3));
    add(Registration.TERRACOTTA_CISTERN.get(), dropSelfWithCracks);
    Registration.COLORED_CISTERN.forEach(cistern -> add(cistern, dropSelfWithCracks));
    Registration.PORCELAIN_CISTERN.forEach(this::dropSelf);
    // faucet
    add(Registration.CLAY_FAUCET.get(), dropClay.apply(2));
    add(Registration.UNFIRED_FAUCET.get(), dropPorcelain.apply(2));
    add(Registration.TERRACOTTA_FAUCET.get(), dropSelfWithCracks);
    dropSelf(Registration.PORCELAIN_FAUCET.get());
    // channel
    add(Registration.CLAY_CHANNEL.get(), dropClay.apply(2));
    add(Registration.UNFIRED_CHANNEL.get(), dropPorcelain.apply(2));
    add(Registration.TERRACOTTA_CHANNEL.get(), dropSelfWithCracks);
    dropSelf(Registration.PORCELAIN_CHANNEL.get());
  }

  /**
   * Registers self loot tables for block, slab, stairs, and wall
   * @param building  Building block object
   */
  private void registerBuildingLootTable(WallBuildingBlockObject building) {
    dropSelf(building.get());
    add(building.getSlab(), this::createSlabItemTable);
    dropSelf(building.getStairs());
    dropSelf(building.getWall());
  }
}

package knightminer.ceramics.datagen;

import knightminer.ceramics.Ceramics;
import knightminer.ceramics.Registration;
import net.minecraft.data.loot.BlockLoot;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootTable.Builder;
import net.minecraft.world.level.storage.loot.functions.CopyNbtFunction;
import net.minecraft.world.level.storage.loot.functions.CopyNbtFunction.MergeStrategy;
import net.minecraft.world.level.storage.loot.providers.nbt.ContextNbtProvider;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraftforge.registries.ForgeRegistries;
import slimeknights.mantle.registration.object.WallBuildingBlockObject;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.stream.Collectors;

import static knightminer.ceramics.tileentity.CrackableTileEntityHandler.TAG_CRACKS;

public class BlockLootTables extends BlockLoot {
  @Override
  protected Iterable<Block> getKnownBlocks() {
    return ForgeRegistries.BLOCKS.getValues().stream()
                                 .filter((block) -> Ceramics.MOD_ID.equals(Objects.requireNonNull(block.getRegistryName()).getNamespace()))
                                 .collect(Collectors.toList());
  }

  @Override
  protected void addTables() {
    IntFunction<Function<Block,Builder>> dropClay = count -> block -> createSingleItemTableWithSilkTouch(block, Items.CLAY_BALL, ConstantValue.exactly(count));
    IntFunction<Function<Block,Builder>> dropPorcelain = count -> block -> createSingleItemTableWithSilkTouch(block, Registration.UNFIRED_PORCELAIN, ConstantValue.exactly(count));
    Function<Block,Builder> dropSelfWithCracks = block -> createSingleItemTable(block).apply(CopyNbtFunction.copyData(ContextNbtProvider.BLOCK_ENTITY).copy(TAG_CRACKS, TAG_CRACKS, MergeStrategy.REPLACE));

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
    add(building.getSlab(), BlockLootTables::createSlabItemTable);
    dropSelf(building.getStairs());
    dropSelf(building.getWall());
  }
}

package knightminer.ceramics.datagen;

import knightminer.ceramics.Ceramics;
import knightminer.ceramics.Registration;
import net.minecraft.block.Block;
import net.minecraft.item.Items;
import net.minecraft.loot.ConstantRange;
import net.minecraft.loot.LootTable.Builder;
import net.minecraft.loot.functions.CopyNbt;
import net.minecraft.loot.functions.CopyNbt.Action;
import net.minecraft.loot.functions.CopyNbt.Source;
import net.minecraftforge.registries.ForgeRegistries;
import slimeknights.mantle.registration.object.WallBuildingBlockObject;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.stream.Collectors;

import static knightminer.ceramics.tileentity.CrackableTileEntityHandler.TAG_CRACKS;

public class BlockLootTables extends net.minecraft.data.loot.BlockLootTables {
  @Override
  protected Iterable<Block> getKnownBlocks() {
    return ForgeRegistries.BLOCKS.getValues().stream()
                                 .filter((block) -> Ceramics.MOD_ID.equals(Objects.requireNonNull(block.getRegistryName()).getNamespace()))
                                 .collect(Collectors.toList());
  }

  @Override
  protected void addTables() {
    IntFunction<Function<Block,Builder>> dropClay = count -> block -> droppingWithSilkTouchOrRandomly(block, Items.CLAY_BALL, ConstantRange.of(count));
    IntFunction<Function<Block,Builder>> dropPorcelain = count -> block -> droppingWithSilkTouchOrRandomly(block, Registration.UNFIRED_PORCELAIN, ConstantRange.of(count));
    Function<Block,Builder> dropSelfWithCracks = block -> dropping(block).acceptFunction(CopyNbt.builder(Source.BLOCK_ENTITY).addOperation(TAG_CRACKS, TAG_CRACKS, Action.REPLACE));

    // unfired porcelain drops 4 of the item form
    registerLootTable(Registration.UNFIRED_PORCELAIN_BLOCK.get(), dropPorcelain.apply(4));
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
    // gauge
    registerDropSelfLootTable(Registration.TERRACOTTA_GAUGE.get());
    registerDropSelfLootTable(Registration.PORCELAIN_GAUGE.get());
    // cistern
    registerLootTable(Registration.CLAY_CISTERN.get(), dropClay.apply(3));
    registerLootTable(Registration.UNFIRED_CISTERN.get(), dropPorcelain.apply(3));
    registerLootTable(Registration.TERRACOTTA_CISTERN.get(), dropSelfWithCracks);
    Registration.COLORED_CISTERN.forEach(cistern -> registerLootTable(cistern, dropSelfWithCracks));
    Registration.PORCELAIN_CISTERN.forEach(this::registerDropSelfLootTable);
    // faucet
    registerLootTable(Registration.CLAY_FAUCET.get(), dropClay.apply(2));
    registerLootTable(Registration.UNFIRED_FAUCET.get(), dropPorcelain.apply(2));
    registerLootTable(Registration.TERRACOTTA_FAUCET.get(), dropSelfWithCracks);
    registerDropSelfLootTable(Registration.PORCELAIN_FAUCET.get());
    // channel
    registerLootTable(Registration.CLAY_CHANNEL.get(), dropClay.apply(2));
    registerLootTable(Registration.UNFIRED_CHANNEL.get(), dropPorcelain.apply(2));
    registerLootTable(Registration.TERRACOTTA_CHANNEL.get(), dropSelfWithCracks);
    registerDropSelfLootTable(Registration.PORCELAIN_CHANNEL.get());
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

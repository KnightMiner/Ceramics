package knightminer.ceramics.datagen;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import knightminer.ceramics.Ceramics;
import net.minecraft.data.DataGenerator;
import net.minecraft.loot.LootParameterSet;
import net.minecraft.loot.LootParameterSets;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.LootTableManager;
import net.minecraft.loot.ValidationTracker;
import net.minecraft.util.ResourceLocation;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class LootTableProvider extends net.minecraft.data.LootTableProvider {

  public LootTableProvider(DataGenerator gen) {
    super(gen);
  }

  @Override
  public String getName() {
    return "Ceramics Loot Tables";
  }

  @Override
  protected List<Pair<Supplier<Consumer<BiConsumer<ResourceLocation, LootTable.Builder>>>,LootParameterSet>> getTables() {
    return ImmutableList.of(Pair.of(BlockLootTables::new, LootParameterSets.BLOCK));
  }
  
  @Override
  protected void validate(Map<ResourceLocation,LootTable> map, ValidationTracker validationtracker) {
    map.forEach((loc, table) -> LootTableManager.validate(validationtracker, loc, table));
    // Remove vanilla's tables, which we also loaded so we can redirect stuff to them.
    // This ensures the remaining generator logic doesn't write those to files.
    map.keySet().removeIf((loc) -> !loc.getNamespace().equals(Ceramics.MOD_ID));
  }
}

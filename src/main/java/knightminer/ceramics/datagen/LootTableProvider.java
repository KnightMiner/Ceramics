package knightminer.ceramics.datagen;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import knightminer.ceramics.Ceramics;
import net.minecraft.data.DataGenerator;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.LootTables;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class LootTableProvider extends net.minecraft.data.loot.LootTableProvider {

  public LootTableProvider(DataGenerator gen) {
    super(gen);
  }

  @Override
  public String getName() {
    return "Ceramics Loot Tables";
  }

  @Override
  protected List<Pair<Supplier<Consumer<BiConsumer<ResourceLocation, LootTable.Builder>>>,LootContextParamSet>> getTables() {
    return ImmutableList.of(Pair.of(BlockLootTables::new, LootContextParamSets.BLOCK));
  }
  
  @Override
  protected void validate(Map<ResourceLocation,LootTable> map, ValidationContext validationtracker) {
    map.forEach((loc, table) -> LootTables.validate(validationtracker, loc, table));
    // Remove vanilla's tables, which we also loaded so we can redirect stuff to them.
    // This ensures the remaining generator logic doesn't write those to files.
    map.keySet().removeIf((loc) -> !loc.getNamespace().equals(Ceramics.MOD_ID));
  }
}

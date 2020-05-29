package knightminer.ceramics.datagen;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import knightminer.ceramics.Ceramics;
import net.minecraft.data.DataGenerator;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.LootParameterSet;
import net.minecraft.world.storage.loot.LootParameterSets;
import net.minecraft.world.storage.loot.LootTable;
import net.minecraft.world.storage.loot.LootTable.Builder;
import net.minecraft.world.storage.loot.LootTableManager;
import net.minecraft.world.storage.loot.ValidationTracker;

import javax.annotation.Nonnull;
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

  @Nonnull
  @Override
  protected List<Pair<Supplier<Consumer<BiConsumer<ResourceLocation, Builder>>>,LootParameterSet>> getTables() {
    return ImmutableList.of(Pair.of(BlockLootTables::new, LootParameterSets.BLOCK));
  }
  
  @Override
  protected void validate(Map<ResourceLocation,LootTable> map, ValidationTracker validationtracker) {
    map.forEach((loc, table) -> LootTableManager.func_227508_a_(validationtracker, loc, table));
    // Remove vanilla's tables, which we also loaded so we can redirect stuff to them.
    // This ensures the remaining generator logic doesn't write those to files.
    map.keySet().removeIf((loc) -> !loc.getNamespace().equals(Ceramics.MOD_ID));
  }
}

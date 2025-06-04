package knightminer.ceramics.datagen;

import net.minecraft.data.PackOutput;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

import java.util.List;
import java.util.Set;

public class LootTableProvider extends net.minecraft.data.loot.LootTableProvider {
  public LootTableProvider(PackOutput packOutput) {
    super(packOutput, Set.of(), List.of(
      new SubProviderEntry(BlockLootTables::new, LootContextParamSets.BLOCK)
    ));
  }
}

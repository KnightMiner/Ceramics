package knightminer.ceramics.client.renderer;

import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import slimeknights.mantle.client.render.FluidCuboid;
import slimeknights.mantle.data.datamap.RegistryDataMapLoader;
import slimeknights.mantle.data.loadable.primitive.EnumLoadable;
import slimeknights.mantle.data.loadable.record.RecordLoadable;

import javax.annotation.Nullable;
import java.util.Map;

/** JSON element representing fluids in a cistern */
public record CisternFluids(FluidCuboid center, FluidCuboid extension, Map<Direction,FluidCuboid> sides) {
  public static final RecordLoadable<CisternFluids> LOADABLE = RecordLoadable.create(
    FluidCuboid.LOADABLE.requiredField("center", CisternFluids::center),
    FluidCuboid.LOADABLE.requiredField("extension", CisternFluids::extension),
    EnumLoadable.of(Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST).mapWithValues(FluidCuboid.LOADABLE).requiredField("sides", CisternFluids::sides),
    CisternFluids::new);
  @SuppressWarnings("deprecation")
  public static final RegistryDataMapLoader<Block, CisternFluids> REGISTRY = new RegistryDataMapLoader<>("Channel fluids", "ceramics/cistern_fluids", BuiltInRegistries.BLOCK, LOADABLE);

  /** Gets a side fluid */
  @Nullable
  public FluidCuboid side(Direction direction) {
    if (direction.getAxis().isVertical()) {
      throw new IllegalStateException("Direction must be horizontal");
    }
    return sides.get(direction);
  }

  /** Gets the center for either extension or non-extension */
  public FluidCuboid base(boolean extension) {
    return extension ? this.extension : this.center;
  }
}

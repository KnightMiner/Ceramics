package knightminer.ceramics.datagen.client;

import knightminer.ceramics.Ceramics;
import knightminer.ceramics.Registration;
import knightminer.ceramics.client.renderer.CisternFluids;
import net.minecraft.core.Direction;
import net.minecraft.data.PackOutput;
import net.minecraft.data.PackOutput.Target;
import net.minecraft.world.level.block.Block;
import slimeknights.mantle.client.render.FluidCuboid;
import slimeknights.mantle.data.datamap.RegistryDataMapProvider;

import java.util.Map;

/** Adds the locations of fluids in the cistern model */
public class CisternFluidProvider extends RegistryDataMapProvider<Block,CisternFluids> {
  public CisternFluidProvider(PackOutput output) {
    super(output, Target.RESOURCE_PACK, CisternFluids.REGISTRY, Ceramics.MOD_ID);
  }

  @Override
  protected void addEntries() {
    String cistern = "templates/cistern";
    entry(cistern, new CisternFluids(
      FluidCuboid.builder().from(3,  2, 3).to(13, 15, 13).face(Direction.UP).build(),
      FluidCuboid.builder().from(3, -1, 3).to(13, 15, 13).face(Direction.UP).build(),
      Map.of(
        Direction.NORTH, FluidCuboid.builder().from( 5, 6,  0).to(11, 11,  3).face(Direction.UP, Direction.NORTH).build(),
        Direction.SOUTH, FluidCuboid.builder().from( 5, 6, 13).to(11, 11, 16).face(Direction.UP, Direction.SOUTH).build(),
        Direction.WEST,  FluidCuboid.builder().from( 0, 6,  5).to( 3, 11, 11).face(Direction.UP, Direction.WEST).build(),
        Direction.EAST,  FluidCuboid.builder().from(13, 6,  5).to(16, 11, 11).face(Direction.UP, Direction.EAST).build()
      )
    ));
    redirect(Registration.TERRACOTTA_CISTERN, cistern);
    Registration.COLORED_CISTERN.forEach(block -> redirect(block, cistern));
    Registration.PORCELAIN_CISTERN.forEach(block -> redirect(block, cistern));
  }

  @Override
  public String getName() {
    return "Ceramics cistern render fluid provider";
  }
}

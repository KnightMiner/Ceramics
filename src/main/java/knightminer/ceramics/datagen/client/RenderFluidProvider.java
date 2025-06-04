package knightminer.ceramics.datagen.client;

import knightminer.ceramics.Ceramics;
import knightminer.ceramics.Registration;
import knightminer.ceramics.blocks.FaucetBlock;
import net.minecraft.core.Direction;
import net.minecraft.data.PackOutput;
import net.minecraft.data.PackOutput.Target;
import slimeknights.mantle.client.render.FluidCuboid;
import slimeknights.mantle.data.datamap.BlockStateDataMapProvider;

import java.util.List;

/** Provides fluid cuboids for block entity renderers */
public class RenderFluidProvider extends BlockStateDataMapProvider<List<FluidCuboid>> {
  public RenderFluidProvider(PackOutput output) {
    super(output, Target.RESOURCE_PACK, FluidCuboid.REGISTRY, Ceramics.MOD_ID);
  }

  @Override
  protected void addEntries() {
    Direction[] horizontal = { Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST };
    String faucet = "templates/faucet";
    String faucetUp = "templates/faucet_up";
    entry(faucet, List.of(
      FluidCuboid.builder()
        .from(6, 6, 0)
        .to(10, 9, 6)
        .face(true, 0, Direction.UP).face(Direction.NORTH).build(),
      FluidCuboid.builder()
        .from(6, 0, 6)
        .to(10, 9, 8)
        .face(true, 0, Direction.UP, horizontal).build()
    ));
    entry(faucetUp, List.of(
      FluidCuboid.builder()
        .from(6, 0, 6)
        .to(10, 16, 10)
        .face(Direction.UP)
        .face(true, 0, Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST).build()
    ));
    block(Registration.TERRACOTTA_FAUCET)
      .variant(faucet).end()
      .variant(faucetUp).when(FaucetBlock.FACING, Direction.DOWN);
    block(Registration.PORCELAIN_FAUCET)
      .variant(faucet).end()
      .variant(faucetUp).when(FaucetBlock.FACING, Direction.DOWN);
  }

  @Override
  public String getName() {
    return "Ceramics render fluid provider";
  }
}

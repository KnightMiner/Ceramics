package knightminer.ceramics.datagen.client;

import knightminer.ceramics.Ceramics;
import knightminer.ceramics.Registration;
import knightminer.ceramics.blocks.CisternBlock;
import knightminer.ceramics.datagen.client.FaucetFluidProvider.SimpleFaucetFluid;
import net.minecraft.data.PackOutput;
import net.minecraft.data.PackOutput.Target;
import net.minecraft.world.level.block.Block;
import slimeknights.mantle.client.render.FaucetFluid;
import slimeknights.mantle.data.datamap.BlockStateDataMapProvider;
import slimeknights.mantle.data.loadable.primitive.BooleanLoadable;
import slimeknights.mantle.data.loadable.primitive.IntLoadable;
import slimeknights.mantle.data.loadable.record.RecordLoadable;

import java.util.function.Consumer;

/** Adds locations for faucets to flow in for the faucet model */
public class FaucetFluidProvider extends BlockStateDataMapProvider<SimpleFaucetFluid> {
  /** Simpler faucet fluid that uses the compact form */
  record SimpleFaucetFluid(int bottom, boolean isContinued) {
    public SimpleFaucetFluid(int bottom) {
      this(bottom, false);
    }

    private static final RecordLoadable<SimpleFaucetFluid> LOADABLE = RecordLoadable.create(
      IntLoadable.FROM_ZERO.requiredField("bottom", SimpleFaucetFluid::bottom),
      BooleanLoadable.INSTANCE.defaultField("continue", false, false, SimpleFaucetFluid::isContinued),
      SimpleFaucetFluid::new);
  }

  public FaucetFluidProvider(PackOutput output) {
    super(output, Target.RESOURCE_PACK, FaucetFluid.REGISTRY.getFolder(), SimpleFaucetFluid.LOADABLE, Ceramics.MOD_ID);
  }

  @Override
  protected void addEntries() {
    // cisterns
    String cistern = "templates/cistern";
    String extension = "templates/cistern_extension";
    entry(cistern, new SimpleFaucetFluid(2));
    entry(extension, new SimpleFaucetFluid(0, true));
    Consumer<Block> addCistern = block -> {
      block(block)
        .variant(cistern).when(CisternBlock.EXTENSION, false).end()
        .variant(extension).when(CisternBlock.EXTENSION, true).end();
    };
    addCistern.accept(Registration.TERRACOTTA_CISTERN.get());
    Registration.COLORED_CISTERN.forEach(addCistern);
    Registration.PORCELAIN_CISTERN.forEach(addCistern);

    // channels
    String channel = "templates/channel";
    entry(channel, new SimpleFaucetFluid(8));
    block(Registration.TERRACOTTA_CHANNEL).variant(channel);
    block(Registration.PORCELAIN_CHANNEL).variant(channel);
  }

  @Override
  public String getName() {
    return "Ceramics faucet fluid provider";
  }
}

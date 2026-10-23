package knightminer.ceramics;

import knightminer.ceramics.client.model.ClayBucketModel;
import knightminer.ceramics.client.model.CrackedModel;
import knightminer.ceramics.client.renderer.ChannelBlockEntityRenderer;
import knightminer.ceramics.client.renderer.CisternBlockEntityRenderer;
import knightminer.ceramics.client.renderer.CisternFluids;
import knightminer.ceramics.client.renderer.FaucetBlockEntityRenderer;
import knightminer.ceramics.client.screen.KilnScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ModelEvent.RegisterGeometryLoaders;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import slimeknights.mantle.client.render.ChannelFluids;
import slimeknights.mantle.client.render.FaucetFluid;

@EventBusSubscriber(modid=Ceramics.MOD_ID, value= Dist.CLIENT)
public class ClientEvents {
  @SubscribeEvent
  static void registerReloadListeners(RegisterClientReloadListenersEvent event) {
    FaucetFluid.initialize(event);
    ChannelFluids.initialize(event);
    event.registerReloadListener(CisternFluids.REGISTRY);
  }

  @SubscribeEvent
  static void registerScreens(RegisterMenuScreensEvent event) {
    event.register(Registration.KILN_MENU.get(), KilnScreen::new);
  }

  @SubscribeEvent
  static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
    event.registerBlockEntityRenderer(Registration.CISTERN_BLOCK_ENTITY.get(), CisternBlockEntityRenderer::new);
    event.registerBlockEntityRenderer(Registration.FAUCET_BLOCK_ENTITY.get(), FaucetBlockEntityRenderer::new);
    event.registerBlockEntityRenderer(Registration.CHANNEL_BLOCK_ENTITY.get(), ChannelBlockEntityRenderer::new);
  }

  @SubscribeEvent
  static void registerModels(RegisterGeometryLoaders event) {
    event.register(Ceramics.getResource("fluid_bucket"), ClayBucketModel.FLUID_LOADER);
    event.register(Ceramics.getResource("solid_bucket"), ClayBucketModel.SOLID_LOADER);
    event.register(Ceramics.getResource("cracked"), CrackedModel.LOADER);
  }
}

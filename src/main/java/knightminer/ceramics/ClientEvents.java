package knightminer.ceramics;

import knightminer.ceramics.client.model.ClayBucketModel;
import knightminer.ceramics.client.model.CrackedModel;
import knightminer.ceramics.client.renderer.ChannelBlockEntityRenderer;
import knightminer.ceramics.client.renderer.CisternBlockEntityRenderer;
import knightminer.ceramics.client.renderer.CisternFluids;
import knightminer.ceramics.client.renderer.FaucetBlockEntityRenderer;
import knightminer.ceramics.client.screen.KilnScreen;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.ModelEvent.RegisterGeometryLoaders;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import slimeknights.mantle.client.render.ChannelFluids;
import slimeknights.mantle.client.render.FaucetFluid;

@SuppressWarnings("unused")
@Mod.EventBusSubscriber(modid=Ceramics.MOD_ID, bus=Bus.MOD, value=Dist.CLIENT)
public class ClientEvents {
  @SubscribeEvent
  static void registerReloadListeners(RegisterClientReloadListenersEvent event) {
    FaucetFluid.initialize(event);
    ChannelFluids.initialize(event);
    event.registerReloadListener(CisternFluids.REGISTRY);
  }

  @SubscribeEvent
  static void setupClient(FMLClientSetupEvent event) {
    RenderType cutout = RenderType.cutout();
    ItemBlockRenderTypes.setRenderLayer(Registration.TERRACOTTA_GAUGE.get(), cutout);
    ItemBlockRenderTypes.setRenderLayer(Registration.PORCELAIN_GAUGE.get(), cutout);
    ItemBlockRenderTypes.setRenderLayer(Registration.TERRACOTTA_CISTERN.get(), cutout);
    Registration.COLORED_CISTERN.forEach(cistern -> ItemBlockRenderTypes.setRenderLayer(cistern, cutout));
    ItemBlockRenderTypes.setRenderLayer(Registration.TERRACOTTA_FAUCET.get(), cutout);
    ItemBlockRenderTypes.setRenderLayer(Registration.TERRACOTTA_CHANNEL.get(), cutout);

    MenuScreens.register(Registration.KILN_MENU.get(), KilnScreen::new);
  }

  @SubscribeEvent
  static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
    event.registerBlockEntityRenderer(Registration.CISTERN_BLOCK_ENTITY.get(), CisternBlockEntityRenderer::new);
    event.registerBlockEntityRenderer(Registration.FAUCET_BLOCK_ENTITY.get(), FaucetBlockEntityRenderer::new);
    event.registerBlockEntityRenderer(Registration.CHANNEL_BLOCK_ENTITY.get(), ChannelBlockEntityRenderer::new);
  }

  @SubscribeEvent
  static void registerModels(RegisterGeometryLoaders event) {
    event.register("fluid_bucket", ClayBucketModel.FLUID_LOADER);
    event.register("solid_bucket", ClayBucketModel.SOLID_LOADER);
    event.register("cracked", CrackedModel.LOADER);
  }
}

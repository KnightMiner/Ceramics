package knightminer.ceramics;

import knightminer.ceramics.client.model.ChannelModel;
import knightminer.ceramics.client.model.CisternModel;
import knightminer.ceramics.client.model.ClayBucketModel;
import knightminer.ceramics.client.model.CrackedFluidsModel;
import knightminer.ceramics.client.model.CrackedModel;
import knightminer.ceramics.client.renderer.ChannelBlockEntityRenderer;
import knightminer.ceramics.client.renderer.CisternBlockEntityRenderer;
import knightminer.ceramics.client.renderer.FaucetBlockEntityRenderer;
import knightminer.ceramics.client.screen.KilnScreen;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import slimeknights.mantle.client.model.FaucetFluidLoader;

@SuppressWarnings("unused")
@Mod.EventBusSubscriber(modid=Ceramics.MOD_ID, bus=Bus.MOD, value=Dist.CLIENT)
public class ClientEvents {
  @SubscribeEvent
  static void registerReloadListeners(RegisterClientReloadListenersEvent event) {
    FaucetFluidLoader.initialize(event);
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
  static void registerModels(ModelRegistryEvent event) {
    ModelLoaderRegistry.registerLoader(Ceramics.getResource("fluid_bucket"), ClayBucketModel.FLUID_LOADER);
    ModelLoaderRegistry.registerLoader(Ceramics.getResource("solid_bucket"), ClayBucketModel.SOLID_LOADER);
    ModelLoaderRegistry.registerLoader(Ceramics.getResource("cistern"), CisternModel.LOADER);
    ModelLoaderRegistry.registerLoader(Ceramics.getResource("channel"), ChannelModel.LOADER);
    // cracked models
    ModelLoaderRegistry.registerLoader(Ceramics.getResource("cracked"), CrackedModel.LOADER);
    ModelLoaderRegistry.registerLoader(Ceramics.getResource("cracked_fluids"), CrackedFluidsModel.LOADER);
    ModelLoaderRegistry.registerLoader(Ceramics.getResource("cracked_cistern"), CisternModel.CRACKED_LOADER);
    ModelLoaderRegistry.registerLoader(Ceramics.getResource("cracked_channel"), ChannelModel.CRACKED_LOADER);
  }
}

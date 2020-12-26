package knightminer.ceramics;

import knightminer.ceramics.client.gui.KilnScreen;
import knightminer.ceramics.client.model.ClayBucketModel;
import knightminer.ceramics.client.model.FaucetFluidLoader;
import knightminer.ceramics.client.renderer.CisternTileEntityRenderer;
import knightminer.ceramics.client.renderer.FaucetTileEntityRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.resources.IReloadableResourceManager;
import net.minecraft.resources.IResourceManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@SuppressWarnings("unused")
@Mod.EventBusSubscriber(modid=Ceramics.MOD_ID, bus=Bus.MOD, value=Dist.CLIENT)
public class ClientEvents {
  @SuppressWarnings("ConstantConditions")
  public static void onConstructor() {
    Minecraft mc = Minecraft.getInstance();
    if (mc != null) {
      IResourceManager manager = mc.getResourceManager();
      if (manager instanceof IReloadableResourceManager) {
        ((IReloadableResourceManager) manager).addReloadListener(FaucetFluidLoader.INSTANCE);
      }
    }
  }

  @SubscribeEvent
  static void setupClient(FMLClientSetupEvent event) {
    RenderTypeLookup.setRenderLayer(Registration.GAUGE.get(), RenderType.getCutout());

    ScreenManager.registerFactory(Registration.KILN_CONTAINER.get(), KilnScreen::new);
    ClientRegistry.bindTileEntityRenderer(Registration.CISTERN_TILE_ENTITY.get(), CisternTileEntityRenderer::new);
    ClientRegistry.bindTileEntityRenderer(Registration.FAUCET_TILE_ENTITY.get(), FaucetTileEntityRenderer::new);
  }

  @SubscribeEvent
  static void registerModels(ModelRegistryEvent event) {
    ModelLoaderRegistry.registerLoader(Ceramics.getResource("bucket"), ClayBucketModel.LOADER);
  }
}

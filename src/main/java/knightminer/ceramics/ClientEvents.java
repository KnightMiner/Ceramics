package knightminer.ceramics;

import knightminer.ceramics.client.ClayBucketModel;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@Mod.EventBusSubscriber(modid=Ceramics.MOD_ID, bus=Bus.MOD, value=Dist.CLIENT)
public class ClientEvents {
  @SubscribeEvent
  public static void registerModels(ModelRegistryEvent event) {
    ModelLoaderRegistry.registerLoader(new ResourceLocation("ceramics", "bucket"), ClayBucketModel.Loader.INSTANCE);
  }
}

package knightminer.ceramics;

import com.google.common.collect.ImmutableList;
import knightminer.ceramics.client.model.ClayBucketModel;
import knightminer.ceramics.client.model.CrackedModel;
import knightminer.ceramics.client.renderer.ChannelBlockEntityRenderer;
import knightminer.ceramics.client.renderer.CisternBlockEntityRenderer;
import knightminer.ceramics.client.renderer.CisternFluids;
import knightminer.ceramics.client.renderer.FaucetBlockEntityRenderer;
import knightminer.ceramics.client.screen.KilnScreen;
import knightminer.ceramics.menu.KilnMenu;
import net.minecraft.client.RecipeBookCategories;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ModelEvent.RegisterGeometryLoaders;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.RegisterRecipeBookCategoriesEvent;
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

  @SubscribeEvent
  static void registerRecipeBookCategories(RegisterRecipeBookCategoriesEvent event) {
    RecipeBookCategories search = EnumExtensions.KILN_SEARCH.getValue();
    RecipeBookCategories blocks = EnumExtensions.KILN_BLOCKS.getValue();
    RecipeBookCategories misc = EnumExtensions.KILN_MISC.getValue();
    event.registerAggregateCategory(search, ImmutableList.of(blocks, misc));
    event.registerBookCategories(KilnMenu.KILN_RECIPE_BOOK, ImmutableList.of(search, blocks, misc));
    // if it's not blocks, its misc
    event.registerRecipeCategoryFinder(Registration.KILN_RECIPE.get(), holder ->
      holder.value() instanceof AbstractCookingRecipe recipe && recipe.category() == CookingBookCategory.BLOCKS ? blocks : misc);
  }
}

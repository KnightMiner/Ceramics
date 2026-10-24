package knightminer.ceramics;

import com.google.common.collect.ImmutableList;
import knightminer.ceramics.client.model.ClayBucketModel;
import knightminer.ceramics.client.model.CrackedModel;
import knightminer.ceramics.client.renderer.ChannelBlockEntityRenderer;
import knightminer.ceramics.client.renderer.CisternBlockEntityRenderer;
import knightminer.ceramics.client.renderer.CisternFluids;
import knightminer.ceramics.client.renderer.FaucetBlockEntityRenderer;
import knightminer.ceramics.client.screen.KilnScreen;
import net.minecraft.client.RecipeBookCategories;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.ModelEvent.RegisterGeometryLoaders;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.client.event.RegisterRecipeBookCategoriesEvent;
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

  /** Recipe book categories added by Ceramics. In a subclass to avoid class loading too early. */
  private static class CeramicsCategories {
    public static final RecipeBookCategories KILN_SEARCH = RecipeBookCategories.create("CERAMICS_KILN_SEARCH", new ItemStack(Items.COMPASS));
    public static final RecipeBookCategories KILN_BLOCKS = RecipeBookCategories.create("CERAMICS_KILN_BLOCKS", new ItemStack(Registration.PORCELAIN_BRICKS));
    public static final RecipeBookCategories KILN_MISC = RecipeBookCategories.create("CERAMICS_KILN_MISC", new ItemStack(Registration.EMPTY_CLAY_BUCKET), new ItemStack(Registration.PORCELAIN_BRICK));
  }


  @SubscribeEvent
  static void registerRecipeBookCategories(RegisterRecipeBookCategoriesEvent event) {
    RecipeBookCategories search = CeramicsCategories.KILN_SEARCH;
    RecipeBookCategories blocks = CeramicsCategories.KILN_BLOCKS;
    RecipeBookCategories misc = CeramicsCategories.KILN_MISC;
    event.registerAggregateCategory(search, ImmutableList.of(blocks, misc));
    event.registerBookCategories(Registration.KILN_RECIPE_BOOK, ImmutableList.of(search, blocks, misc));
    // if it's not blocks, its misc
    event.registerRecipeCategoryFinder(Registration.KILN_RECIPE.get(), recipe ->
      recipe instanceof AbstractCookingRecipe cooking && cooking.category() == CookingBookCategory.BLOCKS ? blocks : misc);
  }
}

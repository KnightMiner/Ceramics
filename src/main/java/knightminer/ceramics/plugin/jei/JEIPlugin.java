package knightminer.ceramics.plugin.jei;

import knightminer.ceramics.Ceramics;
import knightminer.ceramics.Registration;
import knightminer.ceramics.client.gui.KilnScreen;
import knightminer.ceramics.container.KilnContainer;
import knightminer.ceramics.items.BaseClayBucketItem;
import knightminer.ceramics.items.CrackableBlockItem;
import knightminer.ceramics.recipe.CeramicsTags;
import knightminer.ceramics.recipe.KilnRecipe;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.ingredients.subtypes.IIngredientSubtypeInterpreter;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import mezz.jei.api.registration.ISubtypeRegistration;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import slimeknights.mantle.util.RegistryHelper;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
@JeiPlugin
public class JEIPlugin implements IModPlugin {
  @Override
  public ResourceLocation getPluginUid() {
    return new ResourceLocation(Ceramics.MOD_ID, "jei_plugin");
  }

  @Override
  public void registerItemSubtypes(ISubtypeRegistration registration) {
    IIngredientSubtypeInterpreter<ItemStack> fluidInterpreter = (stack, context) -> BaseClayBucketItem.getSubtype(stack);
    registration.registerSubtypeInterpreter(VanillaTypes.ITEM_STACK, Registration.FLUID_CLAY_BUCKET.get(), fluidInterpreter);
    registration.registerSubtypeInterpreter(VanillaTypes.ITEM_STACK, Registration.CRACKED_FLUID_CLAY_BUCKET.get(), fluidInterpreter);

    // separate different states of crackable clay
    IIngredientSubtypeInterpreter<ItemStack> crackableClay = (stack, context) -> CrackableBlockItem.getCracks(stack) > 0 ? "cracked" : "";
    registration.registerSubtypeInterpreter(VanillaTypes.ITEM_STACK, Registration.TERRACOTTA_CISTERN.asItem(), crackableClay);
    Registration.COLORED_CISTERN.forEach(block -> registration.registerSubtypeInterpreter(VanillaTypes.ITEM_STACK, block.asItem(), crackableClay));
    registration.registerSubtypeInterpreter(VanillaTypes.ITEM_STACK, Registration.TERRACOTTA_FAUCET.asItem(), crackableClay);
    registration.registerSubtypeInterpreter(VanillaTypes.ITEM_STACK, Registration.TERRACOTTA_CHANNEL.asItem(), crackableClay);
  }

  @Override
  public void registerCategories(IRecipeCategoryRegistration registration) {
    registration.addRecipeCategories(new KilnCategory(registration.getJeiHelpers().getGuiHelper()));
  }

  @Override
  public void registerRecipes(IRecipeRegistration registration) {
    ClientLevel world = Minecraft.getInstance().level;
    assert world != null;
    RecipeManager recipeManager = world.getRecipeManager();
    List<KilnRecipe> results = new ArrayList<>();
    for (Recipe<Container> recipe : recipeManager.byType(Registration.KILN_RECIPE.get()).values()) {
      // ignore dynamic
      if (recipe.isSpecial()) {
        continue;
      }

      // validate output
      ItemStack output = recipe.getResultItem();
      if (output.isEmpty()) {
        Ceramics.LOG.error("Invalid kiln recipe {}, no output", recipe.getId());
        continue;
      }
      List<Ingredient> ingredients = recipe.getIngredients();
      if (ingredients.size() != 1) {
        Ceramics.LOG.error("Invalid kiln recipe {}, wrong number of inputs", recipe.getId());
        continue;
      }

      // recipe type
      if (!(recipe instanceof KilnRecipe kilnRecipe)) {
        Ceramics.LOG.error("Invalid kiln recipe {}, wrong class", recipe.getId());
        continue;
      }
      // add to list
      results.add(kilnRecipe);
    }
    registration.addRecipes(KilnCategory.TYPE, results);

    // clay repair info
    List<ItemStack> clayRepair = RegistryHelper.getTagValueStream(Registry.ITEM, CeramicsTags.Items.TERRACOTTA_CRACK_REPAIR).map(ItemStack::new).toList();
    registration.addIngredientInfo(clayRepair, VanillaTypes.ITEM_STACK, Ceramics.component("jei", "clay_repair"));
  }

  @Override
  public void registerGuiHandlers(IGuiHandlerRegistration registration) {
    registration.addRecipeClickArea(KilnScreen.class, 78, 32, 28, 23, KilnCategory.TYPE, RecipeTypes.FUELING);
  }

  @Override
  public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
    registration.addRecipeTransferHandler(KilnContainer.class, KilnCategory.TYPE,   KilnCategory.INPUT_SLOT, 1, 3, 36);
    registration.addRecipeTransferHandler(KilnContainer.class, RecipeTypes.FUELING, KilnCategory.FUEL_SLOT,  1, 3, 36);
  }

  @Override
  public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
    registration.addRecipeCatalyst(new ItemStack(Registration.KILN), KilnCategory.TYPE, RecipeTypes.FUELING);
  }

  @Override
  public void onRuntimeAvailable(IJeiRuntime runtime) {
    // add buckets to the ingredient list since JEI fills that list too soon
    NonNullList<ItemStack> buckets = NonNullList.create();
    Registration.FLUID_CLAY_BUCKET.get().fillItemCategory(CreativeModeTab.TAB_SEARCH, buckets);
    Registration.CRACKED_FLUID_CLAY_BUCKET.get().fillItemCategory(CreativeModeTab.TAB_SEARCH, buckets);
    runtime.getIngredientManager().addIngredientsAtRuntime(VanillaTypes.ITEM_STACK, buckets);
  }
}

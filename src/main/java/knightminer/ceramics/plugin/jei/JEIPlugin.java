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
import mezz.jei.api.constants.VanillaRecipeCategoryUid;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.ingredients.subtypes.ISubtypeInterpreter;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import mezz.jei.api.registration.ISubtypeRegistration;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.RecipeManager;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
@JeiPlugin
public class JEIPlugin implements IModPlugin {
  @Override
  public ResourceLocation getPluginUid() {
    return new ResourceLocation(Ceramics.MOD_ID, "jei_plugin");
  }

  @Override
  public void registerItemSubtypes(ISubtypeRegistration registration) {
    ISubtypeInterpreter bucketInterpreter = BaseClayBucketItem::getSubtype;
    registration.registerSubtypeInterpreter(Registration.CLAY_BUCKET.get(), bucketInterpreter);
    registration.registerSubtypeInterpreter(Registration.CRACKED_CLAY_BUCKET.get(), bucketInterpreter);

    // separate different states of crackable clay
    ISubtypeInterpreter crackableClay = stack -> CrackableBlockItem.getCracks(stack) > 0 ? "cracked" : "";
    registration.registerSubtypeInterpreter(Registration.TERRACOTTA_CISTERN.asItem(), crackableClay);
    Registration.COLORED_CISTERN.forEach(block -> registration.registerSubtypeInterpreter(block.asItem(), crackableClay));
    registration.registerSubtypeInterpreter(Registration.TERRACOTTA_FAUCET.asItem(), crackableClay);
    registration.registerSubtypeInterpreter(Registration.TERRACOTTA_CHANNEL.asItem(), crackableClay);
  }

  @Override
  public void registerCategories(IRecipeCategoryRegistration registration) {
    registration.addRecipeCategories(new KilnCategory(registration.getJeiHelpers().getGuiHelper()));
  }

  @Override
  public void registerRecipes(IRecipeRegistration registration) {
    ClientWorld world = Minecraft.getInstance().world;
    assert world != null;
    RecipeManager recipeManager = world.getRecipeManager();
    List<KilnRecipe> results = new ArrayList<>();
    for (IRecipe<IInventory> recipe : recipeManager.getRecipes(Registration.KILN_RECIPE).values()) {
      // ignore dynamic
      if (recipe.isDynamic()) {
        continue;
      }

      // validate output
      ItemStack output = recipe.getRecipeOutput();
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
      if (!(recipe instanceof KilnRecipe)) {
        Ceramics.LOG.error("Invalid kiln recipe {}, wrong class", recipe.getId());
        continue;
      }
      // add to list
      results.add((KilnRecipe)recipe);
    }
    registration.addRecipes(results, KilnCategory.UID);

    // clay repair info
    List<ItemStack> clayRepair = CeramicsTags.Items.TERRACOTTA_CRACK_REPAIR.getAllElements().stream().map(ItemStack::new).collect(Collectors.toList());
    registration.addIngredientInfo(clayRepair, VanillaTypes.ITEM, Ceramics.lang("jei", "clay_repair"));
  }

  @Override
  public void registerGuiHandlers(IGuiHandlerRegistration registration) {
    registration.addRecipeClickArea(KilnScreen.class, 78, 32, 28, 23, KilnCategory.UID, VanillaRecipeCategoryUid.FUEL);
  }

  @Override
  public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
    registration.addRecipeTransferHandler(KilnContainer.class, KilnCategory.UID,              KilnCategory.INPUT_SLOT, 1, 3, 36);
    registration.addRecipeTransferHandler(KilnContainer.class, VanillaRecipeCategoryUid.FUEL, KilnCategory.FUEL_SLOT,  1, 3, 36);
  }

  @Override
  public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
    registration.addRecipeCatalyst(new ItemStack(Registration.KILN), KilnCategory.UID, VanillaRecipeCategoryUid.FUEL);
  }

  @Override
  public void onRuntimeAvailable(IJeiRuntime runtime) {
    // add buckets to the ingredient list since JEI fills that list too soon
    NonNullList<ItemStack> buckets = NonNullList.create();
    Registration.CLAY_BUCKET.get().fillItemGroup(ItemGroup.SEARCH, buckets);
    Registration.CRACKED_CLAY_BUCKET.get().fillItemGroup(ItemGroup.SEARCH, buckets);
    runtime.getIngredientManager().addIngredientsAtRuntime(VanillaTypes.ITEM, buckets);
  }
}

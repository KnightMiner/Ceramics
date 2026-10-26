package knightminer.ceramics.plugin.jei;

import knightminer.ceramics.Ceramics;
import knightminer.ceramics.Registration;
import knightminer.ceramics.client.screen.KilnScreen;
import knightminer.ceramics.items.CrackableBlockItem;
import knightminer.ceramics.items.FluidClayBucketItem;
import knightminer.ceramics.items.SolidClayBucketItem;
import knightminer.ceramics.menu.KilnMenu;
import knightminer.ceramics.recipe.CeramicsTags;
import knightminer.ceramics.recipe.KilnRecipe;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.ingredients.subtypes.ISubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import mezz.jei.api.registration.ISubtypeRegistration;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import slimeknights.mantle.client.SafeClientAccess;
import slimeknights.mantle.data.loadable.Loadables;
import slimeknights.mantle.plugin.jei.MantleJEIConstants;
import slimeknights.mantle.recipe.MultiNamedRecipe;
import slimeknights.mantle.recipe.NamedRecipe;
import slimeknights.mantle.util.RegistryHelper;

import java.util.List;
import java.util.stream.Stream;

@SuppressWarnings("unused")
@JeiPlugin
public class JEIPlugin implements IModPlugin {
  public JEIPlugin() {
    MantleJEIConstants.enableTransferTab();
  }

  @Override
  public ResourceLocation getPluginUid() {
    return Ceramics.getResource("jei_plugin");
  }

  @Override
  public void registerItemSubtypes(ISubtypeRegistration registration) {
    ISubtypeInterpreter<ItemStack> fluidInterpreter = new ISubtypeInterpreter<>() {
      @Override
      public Fluid getSubtypeData(ItemStack stack, UidContext context) {
        return FluidClayBucketItem.getFluid(stack);
      }

      @Override
      public String getLegacyStringSubtypeInfo(ItemStack stack, UidContext context) {
        return Loadables.FLUID.getString(getSubtypeData(stack, context));
      }
    };
    registration.registerSubtypeInterpreter(VanillaTypes.ITEM_STACK, Registration.FLUID_CLAY_BUCKET.get(), fluidInterpreter);
    registration.registerSubtypeInterpreter(VanillaTypes.ITEM_STACK, Registration.CRACKED_FLUID_CLAY_BUCKET.get(), fluidInterpreter);
    ISubtypeInterpreter<ItemStack> blockInterpreter = new ISubtypeInterpreter<>() {
      @Override
      public Block getSubtypeData(ItemStack stack, UidContext context) {
        return SolidClayBucketItem.getBlock(stack);
      }

      @Override
      public String getLegacyStringSubtypeInfo(ItemStack ingredient, UidContext context) {
        return Loadables.BLOCK.getString(getSubtypeData(ingredient, context));
      }
    };
    registration.registerSubtypeInterpreter(VanillaTypes.ITEM_STACK, Registration.SOLID_CLAY_BUCKET.get(), blockInterpreter);
    registration.registerSubtypeInterpreter(VanillaTypes.ITEM_STACK, Registration.CRACKED_SOLID_CLAY_BUCKET.get(), blockInterpreter);

    // separate different states of crackable clay
    ISubtypeInterpreter<ItemStack> crackableClay = new ISubtypeInterpreter<>() {
      @Override
      public Integer getSubtypeData(ItemStack stack, UidContext context) {
        return CrackableBlockItem.getCracks(stack);
      }

      @Override
      public String getLegacyStringSubtypeInfo(ItemStack stack, UidContext context) {
        return Integer.toString(getSubtypeData(stack, context));
      }
    };
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
    RegistryAccess access = SafeClientAccess.getRegistryAccess();
    List<NamedRecipe<KilnRecipe>> results = MultiNamedRecipe.streamNamedRecipes(world.registryAccess(), recipeManager, Registration.KILN_RECIPE.get(), KilnRecipe.class)
      .filter(holder -> {
        KilnRecipe recipe = holder.recipe();
        // skip dynamic recipes
        if (recipe.isSpecial()) {
          return false;
        }

        // validate output
        ItemStack output = recipe.getResultItem(access);
        if (output.isEmpty()) {
          Ceramics.LOG.error("Invalid kiln recipe {}, no output", holder.id());
          return false;
        }
        List<Ingredient> ingredients = recipe.getIngredients();
        if (ingredients.size() != 1) {
          Ceramics.LOG.error("Invalid kiln recipe {}, wrong number of inputs", holder.id());
          return false;
        }
        return true;
      }).toList();
    registration.addRecipes(KilnCategory.TYPE, results);

    // clay repair info
    List<ItemStack> clayRepair = RegistryHelper.getTagValueStream(BuiltInRegistries.ITEM, CeramicsTags.Items.TERRACOTTA_CRACK_REPAIR).map(ItemStack::new).toList();
    registration.addIngredientInfo(clayRepair, VanillaTypes.ITEM_STACK, Ceramics.component("jei", "clay_repair"));
  }

  @Override
  public void registerGuiHandlers(IGuiHandlerRegistration registration) {
    registration.addRecipeClickArea(KilnScreen.class, 78, 32, 28, 23, KilnCategory.TYPE, RecipeTypes.FUELING);
  }

  @Override
  public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
    registration.addRecipeTransferHandler(KilnMenu.class, Registration.KILN_MENU.get(), KilnCategory.TYPE, KilnCategory.INPUT_SLOT, 1, 3, 36);
    registration.addRecipeTransferHandler(KilnMenu.class, Registration.KILN_MENU.get(), RecipeTypes.FUELING, KilnCategory.FUEL_SLOT, 1, 3, 36);
  }

  @Override
  public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
    registration.addRecipeCatalyst(new ItemStack(Registration.KILN), KilnCategory.TYPE, RecipeTypes.FUELING);
    // add cisterns as two groups: clay and porcelain
    registration.addRecipeCatalyst(MantleJEIConstants.FLUID_TRANSFER, consumer ->
      consumer.addItemStacks(Stream.concat(Stream.of(Registration.TERRACOTTA_CISTERN.get()), Registration.COLORED_CISTERN.stream()).map(ItemStack::new).toList()));
    registration.addRecipeCatalyst(MantleJEIConstants.FLUID_TRANSFER, consumer ->
      consumer.addItemStacks(Registration.PORCELAIN_CISTERN.stream().map(ItemStack::new).toList()));
  }

  @Override
  public void onRuntimeAvailable(IJeiRuntime runtime) {
    // fetch the JEI fuel list to show in the kiln category fuel slot
    KilnCategory.FUELS = runtime.getRecipeManager().createRecipeLookup(RecipeTypes.FUELING).includeHidden().get().flatMap(r -> r.getInputs().stream()).toList();
  }
}

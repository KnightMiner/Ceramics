package knightminer.ceramics.plugin.jei;

import knightminer.ceramics.Ceramics;
import knightminer.ceramics.Registration;
import knightminer.ceramics.items.BaseClayBucketItem;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.ingredients.subtypes.ISubtypeInterpreter;
import mezz.jei.api.registration.ISubtypeRegistration;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;

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

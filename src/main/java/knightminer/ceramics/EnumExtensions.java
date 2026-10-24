package knightminer.ceramics;

import net.minecraft.client.RecipeBookCategories;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.fml.common.asm.enumextension.EnumProxy;

import java.util.List;
import java.util.function.Supplier;

/**
 * Handles any enum extensions added by Ceramics. Notably includes recipe book categories.
 * @see knightminer.ceramics.menu.KilnMenu#KILN_RECIPE_BOOK
 */
public class EnumExtensions {
  /** Search tab in the kiln recipe book */
  public static final EnumProxy<RecipeBookCategories> KILN_SEARCH = new EnumProxy<>(RecipeBookCategories.class,
    (Supplier<List<ItemStack>>) () -> List.of(new ItemStack(Items.COMPASS)));
  /** Search tab in the kiln recipe book */
  public static final EnumProxy<RecipeBookCategories> KILN_BLOCKS = new EnumProxy<>(RecipeBookCategories.class,
    (Supplier<List<ItemStack>>) () -> List.of(new ItemStack(Registration.PORCELAIN_BRICKS)));
  /** Search tab in the kiln recipe book */
  public static final EnumProxy<RecipeBookCategories> KILN_MISC = new EnumProxy<>(RecipeBookCategories.class,
    (Supplier<List<ItemStack>>) () -> List.of(Registration.EMPTY_CLAY_BUCKET.toStack(), Registration.PORCELAIN_BRICK.toStack()));
}

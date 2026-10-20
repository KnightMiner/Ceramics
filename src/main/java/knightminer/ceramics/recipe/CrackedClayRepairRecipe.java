package knightminer.ceramics.recipe;

import knightminer.ceramics.Ceramics;
import knightminer.ceramics.Registration;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import slimeknights.mantle.data.loadable.Loadables;
import slimeknights.mantle.data.loadable.record.RecordLoadable;

import static knightminer.ceramics.items.CrackableBlockItem.getCracks;
import static knightminer.ceramics.items.CrackableBlockItem.setCracks;

public class CrackedClayRepairRecipe extends ShapelessRecipe {
	public static final RecordLoadable<CrackedClayRepairRecipe> LOADABLE = RecordLoadable.create(
		Loadables.ITEM.requiredField("item", r -> r.item),
		Loadables.ITEM_INGREDIENT_NONEMPTY.requiredField("ingredient", r -> r.repairIngredient),
		CrackedClayRepairRecipe::new);

	private final Item item;
	private final Ingredient repairIngredient;
	public CrackedClayRepairRecipe(ItemLike item, Ingredient repairIngredient) {
		super(Ceramics.locationName("clay_repair"), CraftingBookCategory.MISC, new ItemStack(item), NonNullList.of(Ingredient.EMPTY, Ingredient.of(setCracks(new ItemStack(item), 3)), repairIngredient));
		this.item = item.asItem();
		this.repairIngredient = repairIngredient;
	}

	@Override
	public boolean matches(CraftingInput input, Level worldIn) {
		if (!super.matches(input, worldIn)) {
			return false;
		}
		for (int i = 0; i < input.size(); i++) {
			ItemStack stack = input.getItem(i);
			if (stack.getItem() == item) {
				return getCracks(stack) > 0;
			}
		}
		return false;
	}

	@Override
	public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
		for (int i = 0; i < input.size(); i++) {
			ItemStack stack = input.getItem(i);
			if (stack.getItem() == item) {
				stack = stack.copy();
				stack.setCount(1);
				return setCracks(stack, Math.max(0, getCracks(stack) - 3));
			}
		}
		return super.assemble(input, registries);
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return Registration.CLAY_REPAIR_RECIPE_SERIALIZER.get();
	}
}

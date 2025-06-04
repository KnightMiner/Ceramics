package knightminer.ceramics.recipe;

import com.google.gson.JsonObject;
import knightminer.ceramics.Ceramics;
import knightminer.ceramics.Registration;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.advancements.RequirementsStrategy;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import slimeknights.mantle.data.loadable.Loadables;
import slimeknights.mantle.util.JsonHelper;

import javax.annotation.Nullable;

import static knightminer.ceramics.items.CrackableBlockItem.getCracks;
import static knightminer.ceramics.items.CrackableBlockItem.setCracks;

public class CrackedClayRepairRecipe extends ShapelessRecipe {
	private final Item item;
	private final Ingredient repairIngredient;
	public CrackedClayRepairRecipe(ResourceLocation id, ItemLike item, Ingredient repairIngredient) {
		super(id, Ceramics.locationName("clay_repair"), CraftingBookCategory.MISC, new ItemStack(item), NonNullList.of(Ingredient.EMPTY, Ingredient.of(setCracks(new ItemStack(item), 3)), repairIngredient));
		this.item = item.asItem();
		this.repairIngredient = repairIngredient;
	}

	@Override
	public boolean matches(CraftingContainer inv, Level worldIn) {
		if (!super.matches(inv, worldIn)) {
			return false;
		}
		for (int i = 0; i < inv.getContainerSize(); i++) {
			ItemStack stack = inv.getItem(i);
			if (stack.getItem() == item) {
				return getCracks(stack) > 0;
			}
		}
		return false;
	}

	@Override
	public ItemStack assemble(CraftingContainer inv, RegistryAccess access) {
		for (int i = 0; i < inv.getContainerSize(); i++) {
			ItemStack stack = inv.getItem(i);
			if (stack.getItem() == item) {
				stack = stack.copy();
				stack.setCount(1);
				return setCracks(stack, Math.max(0, getCracks(stack) - 3));
			}
		}
		return super.assemble(inv, access);
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return Registration.CLAY_REPAIR_RECIPE_SERIALIZER.get();
	}

	/** Serializer class for this recipe */
	public static class Serializer implements RecipeSerializer<CrackedClayRepairRecipe> {
		@Override
		public CrackedClayRepairRecipe fromJson(ResourceLocation id, JsonObject json) {
			Item item = Loadables.ITEM.getIfPresent(json, "item");
			Ingredient ingredient = Ingredient.fromJson(JsonHelper.getElement(json, "ingredient"), false);
			return new CrackedClayRepairRecipe(id, item, ingredient);
		}

		@Override
		public CrackedClayRepairRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) {
			Item item = Loadables.ITEM.decode(buffer);
			Ingredient ingredient = Ingredient.fromNetwork(buffer);
			return new CrackedClayRepairRecipe(id, item, ingredient);
		}

		@Override
		public void toNetwork(FriendlyByteBuf buffer, CrackedClayRepairRecipe recipe) {
			Loadables.ITEM.encode(buffer, recipe.item);
			recipe.repairIngredient.toNetwork(buffer);
		}
	}

	/**
	 * Finished recipe for datagens
	 */
	public static class Finished implements FinishedRecipe {
		private final ResourceLocation id;
		private final Item item;
		private final Ingredient ingredient;
		@Nullable
		private final Advancement.Builder advancementBuilder;
		@Nullable
		private final ResourceLocation advancementId;
		public Finished(ResourceLocation id, ItemLike item, Ingredient ingredient, @Nullable CriterionTriggerInstance criteria) {
			this.id = id;
			this.item = item.asItem();
			this.ingredient = ingredient;
			if (criteria != null) {
				advancementBuilder = Advancement.Builder.advancement()
																								.addCriterion("has_item", criteria)
																								.parent(new ResourceLocation("recipes/root"))
																								.addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(id))
																								.rewards(AdvancementRewards.Builder.recipe(id))
																								.requirements(RequirementsStrategy.OR);
				advancementId = new ResourceLocation(id.getNamespace(), "recipes/clay_repair/" + id.getPath());
			} else {
				advancementBuilder = null;
				advancementId = null;
			}
		}

		@Override
		public ResourceLocation getId() {
			return id;
		}

		@Override
		public void serializeRecipeData(JsonObject json) {
			json.addProperty("item", Loadables.ITEM.getString(item));
			json.add("ingredient", ingredient.toJson());
		}

		@Override
		public RecipeSerializer<?> getType() {
			return Registration.CLAY_REPAIR_RECIPE_SERIALIZER.get();
		}

		@Nullable
		@Override
		public JsonObject serializeAdvancement() {
			if (advancementBuilder != null) {
				return advancementBuilder.serializeToJson();
			}
			return null;
		}

		@Nullable
		@Override
		public ResourceLocation getAdvancementId() {
			return advancementId;
		}
	}
}

package knightminer.ceramics.recipe;

import com.google.gson.JsonObject;
import knightminer.ceramics.Ceramics;
import knightminer.ceramics.Registration;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.ICriterionInstance;
import net.minecraft.advancements.IRequirementsStrategy;
import net.minecraft.advancements.criterion.RecipeUnlockedTrigger;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapelessRecipe;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.registries.ForgeRegistryEntry;
import slimeknights.mantle.recipe.RecipeHelper;
import slimeknights.mantle.util.JsonHelper;

import javax.annotation.Nullable;
import java.util.Objects;

import static knightminer.ceramics.items.CrackableBlockItem.getCracks;
import static knightminer.ceramics.items.CrackableBlockItem.setCracks;

public class CrackedClayRepairRecipe extends ShapelessRecipe {
	private final Item item;
	private final Ingredient repairIngredient;
	public CrackedClayRepairRecipe(ResourceLocation id, IItemProvider item, Ingredient repairIngredient) {
		super(id, Ceramics.locationName("clay_repair"), new ItemStack(item), NonNullList.of(Ingredient.EMPTY, Ingredient.of(setCracks(new ItemStack(item), 3)), repairIngredient));
		this.item = item.asItem();
		this.repairIngredient = repairIngredient;
	}

	@Override
	public boolean matches(CraftingInventory inv, World worldIn) {
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
	public ItemStack assemble(CraftingInventory inv) {
		for (int i = 0; i < inv.getContainerSize(); i++) {
			ItemStack stack = inv.getItem(i);
			if (stack.getItem() == item) {
				stack = stack.copy();
				stack.setCount(1);
				return setCracks(stack, Math.max(0, getCracks(stack) - 3));
			}
		}
		return super.assemble(inv);
	}

	@Override
	public IRecipeSerializer<?> getSerializer() {
		return Registration.CLAY_REPAIR_RECIPE_SERIALIZER.get();
	}

	/** Serializer class for this recipe */
	public static class Serializer extends ForgeRegistryEntry<IRecipeSerializer<?>> implements IRecipeSerializer<CrackedClayRepairRecipe> {
		@Override
		public CrackedClayRepairRecipe fromJson(ResourceLocation id, JsonObject json) {
			Item item = RecipeHelper.deserializeItem(JSONUtils.getAsString(json, "item"), "item", Item.class);
			Ingredient ingredient = Ingredient.fromJson(JsonHelper.getElement(json, "ingredient"));
			return new CrackedClayRepairRecipe(id, item, ingredient);
		}

		@Override
		public CrackedClayRepairRecipe fromNetwork(ResourceLocation id, PacketBuffer buffer) {
			Item item = RecipeHelper.readItem(buffer);
			Ingredient ingredient = Ingredient.fromNetwork(buffer);
			return new CrackedClayRepairRecipe(id, item, ingredient);
		}

		@Override
		public void toNetwork(PacketBuffer buffer, CrackedClayRepairRecipe recipe) {
			RecipeHelper.writeItem(buffer, recipe.item);
			recipe.repairIngredient.toNetwork(buffer);
		}
	}

	/**
	 * Finished recipe for datagens
	 */
	public static class FinishedRecipe implements IFinishedRecipe {
		private final ResourceLocation id;
		private final Item item;
		private final Ingredient ingredient;
		@Nullable
		private final Advancement.Builder advancementBuilder;
		@Nullable
		private final ResourceLocation advancementId;
		public FinishedRecipe(ResourceLocation id, IItemProvider item, Ingredient ingredient, @Nullable ICriterionInstance criteria) {
			this.id = id;
			this.item = item.asItem();
			this.ingredient = ingredient;
			if (criteria != null) {
				advancementBuilder = Advancement.Builder.advancement()
																								.addCriterion("has_item", criteria)
																								.parent(new ResourceLocation("recipes/root"))
																								.addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(id))
																								.rewards(AdvancementRewards.Builder.recipe(id))
																								.requirements(IRequirementsStrategy.OR);
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
			json.addProperty("item", Objects.requireNonNull(item.getRegistryName()).toString());
			json.add("ingredient", ingredient.toJson());
		}

		@Override
		public IRecipeSerializer<?> getType() {
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

package knightminer.ceramics.recipe;

import com.google.gson.JsonObject;
import knightminer.ceramics.Registration;
import knightminer.ceramics.items.CrackableBlockItem;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.ShapedRecipe;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import javax.annotation.Nullable;

/**
 * Recipe that prevents using a cracked block as any component
 */
public class NoCrackedShapedRecipe extends ShapedRecipe {
	public NoCrackedShapedRecipe(ShapedRecipe base) {
		super(base.getId(), base.getGroup(), base.getRecipeWidth(), base.getRecipeHeight(), base.getIngredients(), base.getRecipeOutput());
	}

	@Override
	public boolean matches(CraftingInventory inv, World worldIn) {
		if (!super.matches(inv, worldIn)) {
			return false;
		}
		for (int i = 0; i < inv.getSizeInventory(); i++) {
			ItemStack stack = inv.getStackInSlot(i);
			if (!stack.isEmpty() && CrackableBlockItem.getCracks(stack) > 0) {
				return false;
			}
		}
		return true;
	}

	@Override
	public IRecipeSerializer<?> getSerializer() {
		return Registration.NO_CRACKED_SHAPED_RECIPE.get();
	}

	/** Serializer logic */
	public static class Serializer extends ShapedRecipe.Serializer {
		@Override
		public NoCrackedShapedRecipe read(ResourceLocation id, JsonObject json) {
			return new NoCrackedShapedRecipe(super.read(id, json));
		}

		@Override
		@Nullable
		public NoCrackedShapedRecipe read(ResourceLocation id, PacketBuffer buffer) {
			ShapedRecipe base = super.read(id, buffer);
			if (base != null) {
				return new NoCrackedShapedRecipe(base);
			}
			return null;
		}
	}
}

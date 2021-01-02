package knightminer.ceramics.recipe;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.IIngredientSerializer;
import net.minecraftforge.common.crafting.VanillaIngredientSerializer;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Ingredient extension that only matches if the stack has no NBT
 */
public class NoNBTIngredient extends Ingredient {
	public static final Serializer SERIALIZER = new Serializer();

	public NoNBTIngredient(Ingredient ingredient) {
		this(Arrays.stream(ingredient.acceptedItems));
	}

	protected NoNBTIngredient(Stream<? extends IItemList> itemLists) {
		super(itemLists);
	}

	@Override
	public boolean test(@Nullable ItemStack stack) {
		return stack != null && !stack.hasTag() && super.test(stack);
	}

	@Override
	public IIngredientSerializer<? extends Ingredient> getSerializer() {
		return SERIALIZER;
	}

	@Override
	public JsonElement serialize() {
		JsonElement element = super.serialize();
		JsonObject object;
		if (element.isJsonObject()) {
			object = element.getAsJsonObject();
		} else {
			throw new IllegalStateException("Invalid NoNBTIngredient");
		}
		object.addProperty("type", Objects.requireNonNull(CraftingHelper.getID(SERIALIZER)).toString());
		return object;
	}

	/** Serializer logic is basically vanilla */
	private static class Serializer extends VanillaIngredientSerializer {
		@Override
		public NoNBTIngredient parse(PacketBuffer buffer) {
			return new NoNBTIngredient(super.parse(buffer));
		}

		@Override
		public Ingredient parse(JsonObject json) {
			return new NoNBTIngredient(super.parse(json));
		}
	}
}

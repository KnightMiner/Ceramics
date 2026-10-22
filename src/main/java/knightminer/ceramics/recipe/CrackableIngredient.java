package knightminer.ceramics.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import knightminer.ceramics.Registration;
import knightminer.ceramics.items.CrackableBlockItem;
import net.minecraft.core.HolderSet;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;
import slimeknights.mantle.data.codec.MantleCodecs;
import slimeknights.mantle.network.codec.MantleStreamCodecs;
import slimeknights.mantle.recipe.ingredient.IngredientHelper;

import java.util.stream.IntStream;
import java.util.stream.Stream;

/** Matches crackable items with the given number of cracks. See {@link CrackableBlockItem#getCracks(ItemStack)} */
public record CrackableIngredient(HolderSet<Item> item, int min, int max) implements ICustomIngredient {
  private static final Codec<Integer> VALID_INTEGER = Codec.intRange(0, 5);
  public static final MapCodec<CrackableIngredient> CODEC = RecordCodecBuilder.<CrackableIngredient>mapCodec(instance -> instance.group(
    MantleCodecs.ITEM_SET.fieldOf("item").forGetter(CrackableIngredient::item),
    VALID_INTEGER.optionalFieldOf("min", 0).forGetter(CrackableIngredient::min),
    VALID_INTEGER.optionalFieldOf("max", 5).forGetter(CrackableIngredient::max)
  ).apply(instance, CrackableIngredient::new))
    .validate(ingredient -> ingredient.min <= ingredient.max ? DataResult.success(ingredient) : DataResult.error(() -> "min must be less than or equal to max"));
  public static final StreamCodec<RegistryFriendlyByteBuf,CrackableIngredient> STREAM_CODEC = StreamCodec.composite(
    MantleStreamCodecs.ITEM_SET, CrackableIngredient::item,
    ByteBufCodecs.VAR_INT, CrackableIngredient::min,
    ByteBufCodecs.VAR_INT, CrackableIngredient::max,
    CrackableIngredient::new);

  /** Creates a new ingredient instance */
  public static Ingredient of(HolderSet<Item> item, int min, int max) {
    return new CrackableIngredient(item, min, max).toVanilla();
  }

  /** Creates a new ingredient instance for an uncracked ingredient. */
  public static Ingredient uncracked(HolderSet<Item> item) {
    return of(item, 0, 0);
  }

  /** Creates a new ingredient instance for an uncracked tag. */
  public static Ingredient uncracked(TagKey<Item> tag) {
    return uncracked(IngredientHelper.itemSet(tag));
  }


  @Override
  public boolean test(ItemStack stack) {
    if (item.contains(stack.getItemHolder())) {
      int cracks = CrackableBlockItem.getCracks(stack);
      return min <= cracks && cracks <= max;
    }
    return false;
  }

  @Override
  public Stream<ItemStack> getItems() {
    // create an ingredient for each cracks amount for each item
    return item.stream().flatMap(item ->
      IntStream.rangeClosed(min, max).mapToObj(cracks ->
        CrackableBlockItem.setCracks(new ItemStack(item), cracks)));
  }

  @Override
  public boolean isSimple() {
    return false;
  }

  @Override
  public IngredientType<CrackableIngredient> getType() {
    return Registration.CRACKABLE_INGREDIENT.get();
  }
}

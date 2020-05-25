package knightminer.ceramics.registration;

import net.minecraft.block.Block;
import net.minecraft.item.Item;

import java.util.Map;
import java.util.function.Supplier;

public class EnumBlockObject<T extends Enum<T>, B extends Block> {
  private Map<T,Supplier<B>> map;

  public EnumBlockObject(Map<T,Supplier<B>> map) {
    this.map = map;
  }

  /**
   * Gets a block supplier for the given value
   * @param value  Value to get
   * @return  BlockItemObject
   */
  public Supplier<? extends B> get(T value) {
    return map.get(value);
  }

  /**
   * Gets the block for the given value
   * @param value  Value to get
   * @return  Block
   */
  public B getBlock(T value) {
    if (!map.containsKey(value)) {
      return null;
    }
    return get(value).get();
  }

  /**
   * Gets the item for the given value
   * @param value  Value to get
   * @return  Item
   */
  public Item asItem(T value) {
    if (!map.containsKey(value)) {
      return null;
    }
    return getBlock(value).asItem();
  }
}

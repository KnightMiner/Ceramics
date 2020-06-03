package knightminer.ceramics.registration.object;

import net.minecraftforge.registries.IForgeRegistryEntry;

import java.util.Map;
import java.util.function.Supplier;

public class EnumObject<T extends Enum<T>, I extends IForgeRegistryEntry<? super I>> {
  private Map<T,Supplier<? extends I>> map;

  public EnumObject(Map<T,Supplier<? extends I>> map) {
    this.map = map;
  }

  /**
   * Gets a block supplier for the given value
   * @param value  Value to get
   * @return  BlockItemObject
   */
  public Supplier<? extends I> getSupplier(T value) {
    return map.get(value);
  }

  /**
   * Gets the value for the given enum
   * @param value  Value to get
   * @return  Block
   */
  public I get(T value) {
    if (!map.containsKey(value)) {
      return null;
    }
    return getSupplier(value).get();
  }
}
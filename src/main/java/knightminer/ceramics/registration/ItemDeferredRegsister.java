package knightminer.ceramics.registration;

import net.minecraft.item.Item;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Supplier;

public class ItemDeferredRegsister {

  private final DeferredRegister<Item> itemRegistry;
  public ItemDeferredRegsister(String modID) {
    this.itemRegistry = new DeferredRegister<>(ForgeRegistries.ITEMS, modID);
  }

  /**
   * Initializes this registry wrapper. Needs to be called during mod construction
   */
  public void init() {
    itemRegistry.register(FMLJavaModLoadingContext.get().getModEventBus());
  }

  /**
   * Adds a new supplier to the list to be registered, using the given supplier
   * @param name   Item name
   * @param sup    Supplier returning an item
   * @return  Item registry object
   */
  public <I extends Item> ItemObject<I> register(final String name, final Supplier<? extends I> sup) {
    return new ItemObject(itemRegistry.register(name, sup));
  }

  /**
   * Adds a new supplier to the list to be registered, based on the given item properties
   * @param name   Item name
   * @param props  Item properties
   * @return  Item registry object
   */
  public ItemObject<Item> register(final String name, Item.Properties props) {
    return register(name, () -> new Item(props));
  }
}

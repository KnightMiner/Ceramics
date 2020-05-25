package knightminer.ceramics.registration;

import net.minecraft.block.Block;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.StairsBlock;
import net.minecraft.block.WallBlock;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.EnumMap;
import java.util.function.Function;
import java.util.function.Supplier;

public class BlockDeferredRegister {

  private final DeferredRegister<Block> blockRegistry;
  private final DeferredRegister<Item> itemRegistry;
  public BlockDeferredRegister(String modID) {
    this.blockRegistry = new DeferredRegister<>(ForgeRegistries.BLOCKS, modID);
    this.itemRegistry = new DeferredRegister<>(ForgeRegistries.ITEMS, modID);
  }

  /**
   * Initializes this registry wrapper. Needs to be called during mod construction
   */
  public void init() {
    IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
    blockRegistry.register(bus);
    itemRegistry.register(bus);
  }

  /* Blocks with no items */

  /**
   * Registers a block with the block registry
   * @param name   Block ID
   * @param block  Block supplier
   * @param <B>    Block class
   * @return  Block registry object
   */
  public <B extends Block> RegistryObject<B> register(final String name, final Supplier<? extends B> block) {
    return blockRegistry.register(name, block);
  }

  /**
   * Registers a block with the block registry
   * @param name   Block ID
   * @param props  Block properties
   * @return  Block registry object
   */
  public RegistryObject<Block> register(final String name, final Block.Properties props) {
    return register(name, () -> new Block(props));
  }


  /* Block item pairs */

  /**
   * Registers a block with the block registry, using the function for the BlockItem
   * @param name   Block ID
   * @param block  Block supplier
   * @param item   Function to create a BlockItem from a Block
   * @param <B>    Block class
   * @param <I>    Item class
   * @return  Block item registry object pair
   */
  public <B extends Block, I extends BlockItem> BlockItemObject<B,I> register(final String name, final Supplier<? extends B> block, final Function<? super B, ? extends I> item) {
    RegistryObject<B> blockObj = register(name, block);
    return new BlockItemObject<>(blockObj, itemRegistry.register(name, () -> item.apply(blockObj.get())));
  }

  /**
   * Registers a block with the block registry, using the given item properties
   * @param name   Block ID
   * @param block  Block supplier
   * @param props  Item properties
   * @param <B>    Block class
   * @return  Block item registry object pair
   */
  public <B extends Block> BlockItemObject<B,BlockItem> register(final String name, final Supplier<? extends B> block, final Item.Properties props) {
    return register(name, block, (b) -> new BlockItem(b, props));
  }

  /**
   * Registers a block with the block registry, using the given item properties
   * @param name        Block ID
   * @param blockProps  Block properties
   * @param itemProps   Item properties
   * @return  Block item registry object pair
   */
  public BlockItemObject<Block,BlockItem> register(final String name, final Block.Properties blockProps, final Item.Properties itemProps) {
    return register(name, () -> new Block(blockProps), itemProps);
  }


  /* Specialty */

  /**
   * Registers a block with slab, stairs, and walls
   * @param name      Name of the block
   * @param props     Block properties
   * @param itemProps Item properties
   * @return  BuildingBlockObject class that returns different block types
   */
  public BuildingBlockObject registerBuilding(final String name, Block.Properties props, Item.Properties itemProps) {
    BlockItemObject<Block, BlockItem> blockObj = register(name, props, itemProps);
    return new BuildingBlockObject(blockObj,
                                   register(name + "_slab", () -> new SlabBlock(props), itemProps),
                                   register(name + "_stairs", () -> new StairsBlock(() -> blockObj.get().getDefaultState(), props), itemProps),
                                   register(name + "_wall", () -> new WallBlock(props), itemProps)
    );
  }

  /**
   * Registers a block with slab, stairs, and walls
   * @param name      Name of the block
   * @param supplier  Function to get a block for the given dye color
   * @param itemProps Item properties
   * @return  BuildingBlockObject class that returns different block types
   */
  public <T extends Enum<T>, B extends Block> EnumBlockObject<T,B> registerEnum(final T[] values, final String name, Function<T,? extends B> supplier, Item.Properties itemProps) {
    if (values.length == 0) {
      throw new IllegalArgumentException("Must have at least one value");
    }
    // note this cast only works because you cannot extend an enum
    EnumMap<T, Supplier<B>> map = new EnumMap<>((Class<T>)values[0].getClass());
    for (T value : values) {
      map.put(value, register(value.toString() + "_" + name, () -> supplier.apply(value), itemProps));
    }
    return new EnumBlockObject<T, B>(map);
  }
}

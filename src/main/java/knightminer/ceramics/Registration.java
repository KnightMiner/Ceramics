package knightminer.ceramics;

import knightminer.ceramics.blocks.KilnBlock;
import knightminer.ceramics.blocks.RainbowPorcelain;
import knightminer.ceramics.blocks.TooltipBlock;
import knightminer.ceramics.container.KilnContainer;
import knightminer.ceramics.items.ArmorMaterials;
import knightminer.ceramics.items.ClayBucketItem;
import knightminer.ceramics.items.MilkClayBucketItem;
import knightminer.ceramics.recipe.KilnRecipe;
import knightminer.ceramics.registration.BlockDeferredRegister;
import knightminer.ceramics.registration.BlockItemObject;
import knightminer.ceramics.registration.BuildingBlockObject;
import knightminer.ceramics.registration.EnumBlockObject;
import knightminer.ceramics.registration.ItemDeferredRegsister;
import knightminer.ceramics.registration.ItemObject;
import knightminer.ceramics.tileentity.KilnTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.BlockItem;
import net.minecraft.item.DyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CookingRecipeSerializer;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.EnumMap;
import java.util.function.Supplier;

@Mod.EventBusSubscriber(modid=Ceramics.MOD_ID, bus=Bus.FORGE)
public class Registration {

  /** Registers blocks */
  private static final BlockDeferredRegister BLOCK_REGISTRY = new BlockDeferredRegister(Ceramics.MOD_ID);
  private static final ItemDeferredRegsister ITEM_REGISTRY = new ItemDeferredRegsister(Ceramics.MOD_ID);
  private static final DeferredRegister<ContainerType<?>> CONTAINER_REGISTRY = new DeferredRegister<>(ForgeRegistries.CONTAINERS, Ceramics.MOD_ID);
  private static final DeferredRegister<TileEntityType<?>> TILE_ENTITY_REGISTER = new DeferredRegister<>(ForgeRegistries.TILE_ENTITIES, Ceramics.MOD_ID);
  private static final DeferredRegister<IRecipeSerializer<?>> SERIALIZER_REGISTER = new DeferredRegister<>(ForgeRegistries.RECIPE_SERIALIZERS, Ceramics.MOD_ID);

  public static void init() {
    IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
    BLOCK_REGISTRY.register(bus);
    ITEM_REGISTRY.register(bus);
    CONTAINER_REGISTRY.register(bus);
    TILE_ENTITY_REGISTER.register(bus);
    SERIALIZER_REGISTER.register(bus);
  }

  /** Creative tab for all of Ceramics */
  private static final ItemGroup GROUP = new ItemGroup(Ceramics.MOD_ID) {
    @Override
    @OnlyIn(Dist.CLIENT)
    public ItemStack createIcon() {
      return new ItemStack(PORCELAIN_BRICK);
    }
  };
  private static final Item.Properties GROUP_PROPS = new Item.Properties().group(GROUP);
  private static final Item.Properties UNSTACKABLE_PROPS = new Item.Properties().maxStackSize(1).group(GROUP);

  /** Mapping for terracotta to make registration easier */
  public static final EnumBlockObject<DyeColor,Block> TERRACOTTA;
  static {
    EnumMap<DyeColor,Supplier<Block>> map = new EnumMap<>(DyeColor.class);
    map.put(DyeColor.WHITE,      Blocks.WHITE_TERRACOTTA.delegate);
    map.put(DyeColor.ORANGE,     Blocks.ORANGE_TERRACOTTA.delegate);
    map.put(DyeColor.MAGENTA,    Blocks.MAGENTA_TERRACOTTA.delegate);
    map.put(DyeColor.LIGHT_BLUE, Blocks.LIGHT_BLUE_TERRACOTTA.delegate);
    map.put(DyeColor.YELLOW,     Blocks.YELLOW_TERRACOTTA.delegate);
    map.put(DyeColor.LIME,       Blocks.LIME_TERRACOTTA.delegate);
    map.put(DyeColor.PINK,       Blocks.PINK_TERRACOTTA.delegate);
    map.put(DyeColor.GRAY,       Blocks.GRAY_TERRACOTTA.delegate);
    map.put(DyeColor.LIGHT_GRAY, Blocks.LIGHT_GRAY_TERRACOTTA.delegate);
    map.put(DyeColor.CYAN,       Blocks.CYAN_TERRACOTTA.delegate);
    map.put(DyeColor.PURPLE,     Blocks.PURPLE_TERRACOTTA.delegate);
    map.put(DyeColor.BLUE,       Blocks.BLUE_TERRACOTTA.delegate);
    map.put(DyeColor.BROWN,      Blocks.BROWN_TERRACOTTA.delegate);
    map.put(DyeColor.GREEN,      Blocks.GREEN_TERRACOTTA.delegate);
    map.put(DyeColor.RED,        Blocks.RED_TERRACOTTA.delegate);
    map.put(DyeColor.BLACK,      Blocks.BLACK_TERRACOTTA.delegate);
    TERRACOTTA = new EnumBlockObject<>(map);
  }

  /* Building blocks */
  public static final BlockItemObject<Block,BlockItem> UNFIRED_PORCELAIN_BLOCK = BLOCK_REGISTRY.register("unfired_porcelain_block", Block.Properties.create(Material.CLAY).hardnessAndResistance(0.6F).sound(SoundType.GROUND), GROUP_PROPS);

  // porcelain
  public static final EnumBlockObject<DyeColor,Block> PORCELAIN_BLOCK = BLOCK_REGISTRY.registerEnum(DyeColor.values(), "porcelain", (color) -> new Block(Block.Properties.from(TERRACOTTA.getBlock(color))), GROUP_PROPS);
  public static final EnumBlockObject<RainbowPorcelain,Block> RAINBOW_PORCELAIN = BLOCK_REGISTRY.registerEnum(
      RainbowPorcelain.values(), "rainbow_porcelain",
      (color) -> new TooltipBlock(Block.Properties.create(Material.ROCK, color.getColor()).hardnessAndResistance(2.0F, 6.0F)),
      GROUP_PROPS);

  // clay bricks
  public static final BuildingBlockObject DARK_BRICKS   = BLOCK_REGISTRY.registerBuilding("dark_bricks", Block.Properties.create(Material.ROCK, MaterialColor.RED).hardnessAndResistance(2.0F, 6.0F), GROUP_PROPS);
  public static final BuildingBlockObject LAVA_BRICKS   = BLOCK_REGISTRY.registerBuilding("lava_bricks", Block.Properties.create(Material.ROCK, MaterialColor.ADOBE).hardnessAndResistance(2.0F, 6.0F), GROUP_PROPS);
  public static final BuildingBlockObject DRAGON_BRICKS = BLOCK_REGISTRY.registerBuilding("dragon_bricks", Block.Properties.create(Material.ROCK, MaterialColor.OBSIDIAN).hardnessAndResistance(2.0F, 6.0F), GROUP_PROPS);

  // porcelain bricks
  public static final BuildingBlockObject PORCELAIN_BRICKS  = BLOCK_REGISTRY.registerBuilding("porcelain_bricks", Block.Properties.create(Material.ROCK, MaterialColor.SNOW).hardnessAndResistance(2.0F, 6.0F), GROUP_PROPS);
  public static final BuildingBlockObject MONOCHROME_BRICKS = BLOCK_REGISTRY.registerBuilding("monochrome_bricks", Block.Properties.create(Material.ROCK, MaterialColor.STONE).hardnessAndResistance(2.0F, 6.0F), GROUP_PROPS);
  public static final BuildingBlockObject GOLDEN_BRICKS     = BLOCK_REGISTRY.registerBuilding("golden_bricks", Block.Properties.create(Material.ROCK, MaterialColor.YELLOW).hardnessAndResistance(2.0F, 6.0F), GROUP_PROPS);
  public static final BuildingBlockObject MARINE_BRICKS     = BLOCK_REGISTRY.registerBuilding("marine_bricks", Block.Properties.create(Material.ROCK, MaterialColor.LIGHT_BLUE).hardnessAndResistance(2.0F, 6.0F), GROUP_PROPS);
  public static final BuildingBlockObject RAINBOW_BRICKS    = BLOCK_REGISTRY.registerBuilding("rainbow_bricks", Block.Properties.create(Material.ROCK, MaterialColor.GREEN).hardnessAndResistance(2.0F, 6.0F), GROUP_PROPS);


  /* items */
  public static final ItemObject<Item> UNFIRED_PORCELAIN = ITEM_REGISTRY.register("unfired_porcelain", GROUP_PROPS);
  public static final ItemObject<Item> PORCELAIN_BRICK = ITEM_REGISTRY.register("porcelain_brick", GROUP_PROPS);

  // tools
  public static final ItemObject<Item> UNFIRED_CLAY_BUCKET           = ITEM_REGISTRY.register("unfired_clay_bucket", new Item.Properties().maxStackSize(16).group(GROUP));
  public static final ItemObject<ClayBucketItem> CLAY_BUCKET         = ITEM_REGISTRY.register("clay_bucket", () -> new ClayBucketItem(false, GROUP_PROPS));
  public static final ItemObject<ClayBucketItem> CRACKED_CLAY_BUCKET = ITEM_REGISTRY.register("cracked_clay_bucket", () -> new ClayBucketItem(true, GROUP_PROPS));
  public static final ItemObject<Item> MILK_CLAY_BUCKET              = ITEM_REGISTRY.register("milk_clay_bucket", () -> new MilkClayBucketItem(false, UNSTACKABLE_PROPS));
  public static final ItemObject<Item> CRACKED_MILK_CLAY_BUCKET      = ITEM_REGISTRY.register("cracked_milk_clay_bucket", () -> new MilkClayBucketItem(true, UNSTACKABLE_PROPS));

  // armor
  public static final ItemObject<Item> UNFIRED_CLAY_PLATE = ITEM_REGISTRY.register("unfired_clay_plate", GROUP_PROPS);
  public static final ItemObject<Item> CLAY_PLATE = ITEM_REGISTRY.register("clay_plate", GROUP_PROPS);
  public static final ItemObject<ArmorItem> CLAY_HELMET     = ITEM_REGISTRY.register("clay_helmet",     () -> new ArmorItem(ArmorMaterials.CLAY, EquipmentSlotType.HEAD,  UNSTACKABLE_PROPS));
  public static final ItemObject<ArmorItem> CLAY_CHESTPLATE = ITEM_REGISTRY.register("clay_chestplate", () -> new ArmorItem(ArmorMaterials.CLAY, EquipmentSlotType.CHEST, UNSTACKABLE_PROPS));
  public static final ItemObject<ArmorItem> CLAY_LEGGINGS   = ITEM_REGISTRY.register("clay_leggings",   () -> new ArmorItem(ArmorMaterials.CLAY, EquipmentSlotType.LEGS,  UNSTACKABLE_PROPS));
  public static final ItemObject<ArmorItem> CLAY_BOOTS      = ITEM_REGISTRY.register("clay_boots",      () -> new ArmorItem(ArmorMaterials.CLAY, EquipmentSlotType.FEET,  UNSTACKABLE_PROPS));

  // kiln block
  public static final BlockItemObject<KilnBlock, BlockItem> KILN = BLOCK_REGISTRY.register("kiln", () -> new KilnBlock(Block.Properties.create(Material.ROCK).hardnessAndResistance(3.5F).lightValue(13)), GROUP_PROPS);
  public static final RegistryObject<ContainerType<KilnContainer>> KILN_CONTAINER = CONTAINER_REGISTRY.register("kiln", () -> new ContainerType<>(KilnContainer::new));
  // TODO: data fixer?
  public static final RegistryObject<TileEntityType<KilnTileEntity>> KILN_TILE_ENTITY = TILE_ENTITY_REGISTER.register("kiln", () -> TileEntityType.Builder.create(KilnTileEntity::new, KILN.get()).build(null));
  // kiln recipes
  public static final IRecipeType<KilnRecipe> KILN_RECIPE = IRecipeType.register("ceramics:kiln");
  public static final RegistryObject<CookingRecipeSerializer<KilnRecipe>> KILN_SERIALIZER = SERIALIZER_REGISTER.register("kiln", () -> new CookingRecipeSerializer<>(KilnRecipe::new, 100));
}

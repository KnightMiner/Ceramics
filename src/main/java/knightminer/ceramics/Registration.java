package knightminer.ceramics;

import knightminer.ceramics.blocks.ChannelBlock;
import knightminer.ceramics.blocks.CisternBlock;
import knightminer.ceramics.blocks.FaucetBlock;
import knightminer.ceramics.blocks.FlowingChannelBlock;
import knightminer.ceramics.blocks.FluidCisternBlock;
import knightminer.ceramics.blocks.GaugeBlock;
import knightminer.ceramics.blocks.KilnBlock;
import knightminer.ceramics.blocks.PouringFaucetBlock;
import knightminer.ceramics.blocks.RainbowPorcelain;
import knightminer.ceramics.container.KilnContainer;
import knightminer.ceramics.items.ArmorMaterials;
import knightminer.ceramics.items.ClayBucketItem;
import knightminer.ceramics.items.CrackableItemBlock;
import knightminer.ceramics.items.FixedTooltipBlockItem;
import knightminer.ceramics.items.MilkClayBucketItem;
import knightminer.ceramics.recipe.KilnRecipe;
import knightminer.ceramics.tileentity.ChannelTileEntity;
import knightminer.ceramics.tileentity.CisternTileEntity;
import knightminer.ceramics.tileentity.FaucetTileEntity;
import knightminer.ceramics.tileentity.KilnTileEntity;
import net.minecraft.block.AbstractBlock;
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
import net.minecraftforge.common.ToolType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import slimeknights.mantle.item.BlockTooltipItem;
import slimeknights.mantle.registration.deferred.BlockDeferredRegister;
import slimeknights.mantle.registration.deferred.ContainerTypeDeferredRegister;
import slimeknights.mantle.registration.deferred.ItemDeferredRegister;
import slimeknights.mantle.registration.deferred.TileEntityTypeDeferredRegister;
import slimeknights.mantle.registration.object.EnumObject;
import slimeknights.mantle.registration.object.ItemObject;
import slimeknights.mantle.registration.object.WallBuildingBlockObject;

import java.util.function.Function;

public class Registration {
  private static final BlockDeferredRegister BLOCKS = new BlockDeferredRegister(Ceramics.MOD_ID);
  private static final ItemDeferredRegister ITEMS = new ItemDeferredRegister(Ceramics.MOD_ID);
  private static final ContainerTypeDeferredRegister CONTAINERS = new ContainerTypeDeferredRegister(Ceramics.MOD_ID);
  private static final TileEntityTypeDeferredRegister TILE_ENTIITES = new TileEntityTypeDeferredRegister(Ceramics.MOD_ID);
  private static final DeferredRegister<IRecipeSerializer<?>> SERIALIZERS = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, Ceramics.MOD_ID);

  /** Initializes the registries with the forge mod bus */
  static void init() {
    IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
    BLOCKS.register(bus);
    ITEMS.register(bus);
    CONTAINERS.register(bus);
    TILE_ENTIITES.register(bus);
    SERIALIZERS.register(bus);
  }

  /** Creative tab for all of Ceramics */
  private static final ItemGroup GROUP = new ItemGroup(Ceramics.MOD_ID) {
    @Override
    @OnlyIn(Dist.CLIENT)
    public ItemStack createIcon() {
      return new ItemStack(PORCELAIN_BRICK);
    }
  };
  /** Item properties with the group set */
  private static final Item.Properties GROUP_PROPS = new Item.Properties().group(GROUP);
  /** Item properties with the group set and a stack size of 1 */
  private static final Item.Properties UNSTACKABLE_PROPS = new Item.Properties().maxStackSize(1).group(GROUP);
  /** Item block function that sets the group props */
  private static final Function<Block,BlockItem> DEFAULT_BLOCK_ITEM = (block) -> new BlockItem(block, GROUP_PROPS);
  /** Item block function using {@link BlockTooltipItem} */
  private static final Function<Block,BlockItem> TOOLTIP_BLOCK_ITEM = (block) -> new BlockTooltipItem(block, GROUP_PROPS);
  /** Item block function using {@link FixedTooltipBlockItem} */
  private static final Function<String, Function<Block,BlockItem>> FIXED_TOOLTIP = name -> block -> new FixedTooltipBlockItem(block, GROUP_PROPS, name);

  /** Mapping for terracotta to make registration easier */
  public static final EnumObject<DyeColor,Block> TERRACOTTA = new EnumObject.Builder<DyeColor,Block>(DyeColor.class)
    .put(DyeColor.WHITE,      Blocks.WHITE_TERRACOTTA.delegate)
    .put(DyeColor.ORANGE,     Blocks.ORANGE_TERRACOTTA.delegate)
    .put(DyeColor.MAGENTA,    Blocks.MAGENTA_TERRACOTTA.delegate)
    .put(DyeColor.LIGHT_BLUE, Blocks.LIGHT_BLUE_TERRACOTTA.delegate)
    .put(DyeColor.YELLOW,     Blocks.YELLOW_TERRACOTTA.delegate)
    .put(DyeColor.LIME,       Blocks.LIME_TERRACOTTA.delegate)
    .put(DyeColor.PINK,       Blocks.PINK_TERRACOTTA.delegate)
    .put(DyeColor.GRAY,       Blocks.GRAY_TERRACOTTA.delegate)
    .put(DyeColor.LIGHT_GRAY, Blocks.LIGHT_GRAY_TERRACOTTA.delegate)
    .put(DyeColor.CYAN,       Blocks.CYAN_TERRACOTTA.delegate)
    .put(DyeColor.PURPLE,     Blocks.PURPLE_TERRACOTTA.delegate)
    .put(DyeColor.BLUE,       Blocks.BLUE_TERRACOTTA.delegate)
    .put(DyeColor.BROWN,      Blocks.BROWN_TERRACOTTA.delegate)
    .put(DyeColor.GREEN,      Blocks.GREEN_TERRACOTTA.delegate)
    .put(DyeColor.RED,        Blocks.RED_TERRACOTTA.delegate)
    .put(DyeColor.BLACK,      Blocks.BLACK_TERRACOTTA.delegate)
    .build();

  /* Building blocks */
  public static final ItemObject<Block> UNFIRED_PORCELAIN_BLOCK = BLOCKS.register("unfired_porcelain_block", Block.Properties.create(Material.CLAY).hardnessAndResistance(0.6F).sound(SoundType.GROUND), DEFAULT_BLOCK_ITEM);

  // porcelain
  public static final EnumObject<DyeColor,Block> PORCELAIN_BLOCK = BLOCKS.registerEnum(DyeColor.values(), "porcelain", (color) -> new Block(Block.Properties.from(TERRACOTTA.get(color))), DEFAULT_BLOCK_ITEM);
  public static final EnumObject<RainbowPorcelain,Block> RAINBOW_PORCELAIN = BLOCKS.registerEnum(RainbowPorcelain.values(), "rainbow_porcelain",
      (color) -> new Block(Block.Properties.create(Material.ROCK, color.getColor()).hardnessAndResistance(2.0F, 6.0F)), TOOLTIP_BLOCK_ITEM);

  // clay bricks
  public static final WallBuildingBlockObject DARK_BRICKS   = BLOCKS.registerWallBuilding("dark_bricks", Block.Properties.create(Material.ROCK, MaterialColor.RED).hardnessAndResistance(2.0F, 6.0F), DEFAULT_BLOCK_ITEM);
  public static final WallBuildingBlockObject LAVA_BRICKS   = BLOCKS.registerWallBuilding("lava_bricks", Block.Properties.create(Material.ROCK, MaterialColor.ADOBE).hardnessAndResistance(2.0F, 6.0F), DEFAULT_BLOCK_ITEM);
  public static final WallBuildingBlockObject DRAGON_BRICKS = BLOCKS.registerWallBuilding("dragon_bricks", Block.Properties.create(Material.ROCK, MaterialColor.OBSIDIAN).hardnessAndResistance(2.0F, 6.0F), DEFAULT_BLOCK_ITEM);

  // porcelain bricks
  public static final WallBuildingBlockObject PORCELAIN_BRICKS  = BLOCKS.registerWallBuilding("porcelain_bricks", Block.Properties.create(Material.ROCK, MaterialColor.SNOW).hardnessAndResistance(2.0F, 6.0F), DEFAULT_BLOCK_ITEM);
  public static final WallBuildingBlockObject MONOCHROME_BRICKS = BLOCKS.registerWallBuilding("monochrome_bricks", Block.Properties.create(Material.ROCK, MaterialColor.STONE).hardnessAndResistance(2.0F, 6.0F), DEFAULT_BLOCK_ITEM);
  public static final WallBuildingBlockObject GOLDEN_BRICKS     = BLOCKS.registerWallBuilding("golden_bricks", Block.Properties.create(Material.ROCK, MaterialColor.YELLOW).hardnessAndResistance(2.0F, 6.0F), DEFAULT_BLOCK_ITEM);
  public static final WallBuildingBlockObject MARINE_BRICKS     = BLOCKS.registerWallBuilding("marine_bricks", Block.Properties.create(Material.ROCK, MaterialColor.LIGHT_BLUE).hardnessAndResistance(2.0F, 6.0F), DEFAULT_BLOCK_ITEM);
  public static final WallBuildingBlockObject RAINBOW_BRICKS    = BLOCKS.registerWallBuilding("rainbow_bricks", Block.Properties.create(Material.ROCK, MaterialColor.GREEN).hardnessAndResistance(2.0F, 6.0F), DEFAULT_BLOCK_ITEM);


  /* items */
  public static final ItemObject<Item> UNFIRED_PORCELAIN = ITEMS.register("unfired_porcelain", GROUP_PROPS);
  public static final ItemObject<Item> PORCELAIN_BRICK = ITEMS.register("porcelain_brick", GROUP_PROPS);

  // tools
  public static final ItemObject<Item> UNFIRED_CLAY_BUCKET           = ITEMS.register("unfired_clay_bucket", new Item.Properties().maxStackSize(16).group(GROUP));
  public static final ItemObject<ClayBucketItem> CLAY_BUCKET         = ITEMS.register("clay_bucket", () -> new ClayBucketItem(false, GROUP_PROPS));
  public static final ItemObject<ClayBucketItem> CRACKED_CLAY_BUCKET = ITEMS.register("cracked_clay_bucket", () -> new ClayBucketItem(true, GROUP_PROPS));
  public static final ItemObject<Item> MILK_CLAY_BUCKET              = ITEMS.register("milk_clay_bucket", () -> new MilkClayBucketItem(false, UNSTACKABLE_PROPS));
  public static final ItemObject<Item> CRACKED_MILK_CLAY_BUCKET      = ITEMS.register("cracked_milk_clay_bucket", () -> new MilkClayBucketItem(true, UNSTACKABLE_PROPS));

  // armor
  public static final ItemObject<Item> UNFIRED_CLAY_PLATE = ITEMS.register("unfired_clay_plate", GROUP_PROPS);
  public static final ItemObject<Item> CLAY_PLATE = ITEMS.register("clay_plate", GROUP_PROPS);
  public static final ItemObject<ArmorItem> CLAY_HELMET     = ITEMS.register("clay_helmet", () -> new ArmorItem(ArmorMaterials.CLAY, EquipmentSlotType.HEAD, UNSTACKABLE_PROPS));
  public static final ItemObject<ArmorItem> CLAY_CHESTPLATE = ITEMS.register("clay_chestplate", () -> new ArmorItem(ArmorMaterials.CLAY, EquipmentSlotType.CHEST, UNSTACKABLE_PROPS));
  public static final ItemObject<ArmorItem> CLAY_LEGGINGS   = ITEMS.register("clay_leggings", () -> new ArmorItem(ArmorMaterials.CLAY, EquipmentSlotType.LEGS, UNSTACKABLE_PROPS));
  public static final ItemObject<ArmorItem> CLAY_BOOTS      = ITEMS.register("clay_boots", () -> new ArmorItem(ArmorMaterials.CLAY, EquipmentSlotType.FEET, UNSTACKABLE_PROPS));

  // kiln block
  public static final ItemObject<KilnBlock> KILN = BLOCKS.register("kiln", () -> new KilnBlock(terracottaProps(MaterialColor.ADOBE).setLightLevel(s -> s.get(KilnBlock.LIT) ? 13 : 0)), DEFAULT_BLOCK_ITEM);
  public static final RegistryObject<ContainerType<KilnContainer>> KILN_CONTAINER = CONTAINERS.register("kiln", KilnContainer::new);
  public static final RegistryObject<TileEntityType<KilnTileEntity>> KILN_TILE_ENTITY = TILE_ENTIITES.register("kiln", KilnTileEntity::new, KILN);
  // kiln recipes
  public static final IRecipeType<KilnRecipe> KILN_RECIPE = IRecipeType.register("ceramics:kiln");
  public static final RegistryObject<CookingRecipeSerializer<KilnRecipe>> KILN_SERIALIZER = SERIALIZERS.register("kiln", () -> new CookingRecipeSerializer<>(KilnRecipe::new, 100));

  /*
   * fluid handling
   */
  private static final AbstractBlock.Properties CLAY_PROPERTIES = AbstractBlock.Properties.create(Material.CLAY).harvestTool(ToolType.SHOVEL).hardnessAndResistance(0.6F).sound(SoundType.GROUND).notSolid();
  private static final Function<Block,BlockItem> GAUGE_BLOCK_ITEM = FIXED_TOOLTIP.apply("gauge.tooltip");
  public static final ItemObject<GaugeBlock> TERRACOTTA_GAUGE = BLOCKS.register("terracotta_gauge", () -> new GaugeBlock(AbstractBlock.Properties.create(Material.MISCELLANEOUS, MaterialColor.ADOBE).harvestTool(ToolType.PICKAXE).doesNotBlockMovement().hardnessAndResistance(0.5F).notSolid()), GAUGE_BLOCK_ITEM);
  public static final ItemObject<GaugeBlock> PORCELAIN_GAUGE = BLOCKS.register("porcelain_gauge", () -> new GaugeBlock(AbstractBlock.Properties.create(Material.MISCELLANEOUS, MaterialColor.WHITE_TERRACOTTA).harvestTool(ToolType.PICKAXE).doesNotBlockMovement().hardnessAndResistance(0.5F).notSolid()), GAUGE_BLOCK_ITEM);

  // cistern
  private static final Function<String,Function<Block,BlockItem>> CRACKABLE_BLOCK_ITEM = tooltip -> block -> new CrackableItemBlock(block, GROUP_PROPS, tooltip);
  private static final Function<Block,BlockItem> TERRACOTTA_CISTERN_BLOCK_ITEM = CRACKABLE_BLOCK_ITEM.apply("terracotta_cistern.tooltip");
  private static final Function<Block,BlockItem> PORCELAIN_CISTERN_BLOCK_ITEM = FIXED_TOOLTIP.apply("porcelain_cistern.tooltip");
  public static final ItemObject<CisternBlock> CLAY_CISTERN = BLOCKS.register("clay_cistern", () -> new CisternBlock(CLAY_PROPERTIES), DEFAULT_BLOCK_ITEM);
  public static final ItemObject<FluidCisternBlock> TERRACOTTA_CISTERN = BLOCKS.register("terracotta_cistern", () -> new FluidCisternBlock(terracottaProps(MaterialColor.ADOBE).notSolid().tickRandomly(), true), TERRACOTTA_CISTERN_BLOCK_ITEM);
  public static final EnumObject<DyeColor, FluidCisternBlock> COLORED_CISTERN = BLOCKS.registerEnum(DyeColor.values(), "terracotta_cistern", (color) -> new FluidCisternBlock(terracottaProps(getTerracottaColor(color)).notSolid().tickRandomly(), true), TERRACOTTA_CISTERN_BLOCK_ITEM);
  public static final ItemObject<CisternBlock> UNFIRED_CISTERN = BLOCKS.register("unfired_cistern", () -> new CisternBlock(CLAY_PROPERTIES), DEFAULT_BLOCK_ITEM);
  public static final EnumObject<DyeColor, FluidCisternBlock> PORCELAIN_CISTERN = BLOCKS.registerEnum(DyeColor.values(), "porcelain_cistern", (color) -> new FluidCisternBlock(terracottaProps(getTerracottaColor(color)).notSolid(), false), PORCELAIN_CISTERN_BLOCK_ITEM);
  public static final RegistryObject<TileEntityType<CisternTileEntity>> CISTERN_TILE_ENTITY = TILE_ENTIITES.register("cistern", CisternTileEntity::new, builder -> {
    builder.add(TERRACOTTA_CISTERN.get());
    builder.addAll(COLORED_CISTERN.values());
    builder.addAll(PORCELAIN_CISTERN.values());
  });

  // faucet
  public static final ItemObject<FaucetBlock> CLAY_FAUCET = BLOCKS.register("clay_faucet", () -> new FaucetBlock(CLAY_PROPERTIES), DEFAULT_BLOCK_ITEM);
  public static final ItemObject<PouringFaucetBlock> TERRACOTTA_FAUCET = BLOCKS.register("terracotta_faucet", () -> new PouringFaucetBlock(terracottaProps(MaterialColor.ADOBE).notSolid().tickRandomly(), true), CRACKABLE_BLOCK_ITEM.apply("terracotta_faucet.tooltip"));
  public static final ItemObject<FaucetBlock> UNFIRED_FAUCET = BLOCKS.register("unfired_faucet", () -> new FaucetBlock(CLAY_PROPERTIES), DEFAULT_BLOCK_ITEM);
  public static final ItemObject<PouringFaucetBlock> PORCELAIN_FAUCET = BLOCKS.register("porcelain_faucet", () -> new PouringFaucetBlock(terracottaProps(MaterialColor.WHITE_TERRACOTTA).notSolid(), false), TOOLTIP_BLOCK_ITEM);
  public static final RegistryObject<TileEntityType<FaucetTileEntity>> FAUCET_TILE_ENTITY = TILE_ENTIITES.register("faucet", FaucetTileEntity::new, builder -> builder.add(TERRACOTTA_FAUCET.get(), PORCELAIN_FAUCET.get()));

  // channel
  public static final ItemObject<ChannelBlock> CLAY_CHANNEL = BLOCKS.register("clay_channel", () -> new ChannelBlock(CLAY_PROPERTIES), DEFAULT_BLOCK_ITEM);
  public static final ItemObject<FlowingChannelBlock> TERRACOTTA_CHANNEL = BLOCKS.register("terracotta_channel", () -> new FlowingChannelBlock(terracottaProps(MaterialColor.ADOBE).notSolid().tickRandomly(), true), CRACKABLE_BLOCK_ITEM.apply("terracotta_channel.tooltip"));
  public static final ItemObject<ChannelBlock> UNFIRED_CHANNEL = BLOCKS.register("unfired_channel", () -> new ChannelBlock(CLAY_PROPERTIES), DEFAULT_BLOCK_ITEM);
  public static final ItemObject<FlowingChannelBlock> PORCELAIN_CHANNEL = BLOCKS.register("porcelain_channel", () -> new FlowingChannelBlock(terracottaProps(MaterialColor.WHITE_TERRACOTTA).notSolid(), false), TOOLTIP_BLOCK_ITEM);
  public static final RegistryObject<TileEntityType<ChannelTileEntity>> CHANNEL_TILE_ENTITY = TILE_ENTIITES.register("channel", ChannelTileEntity::new, builder -> builder.add(TERRACOTTA_CHANNEL.get(), PORCELAIN_CHANNEL.get()));


  /**
   * Standard hardened clay properties
   * @param color  Map color of block
   * @return  Block properties
   */
  private static AbstractBlock.Properties terracottaProps(MaterialColor color) {
    return AbstractBlock.Properties.create(Material.ROCK, color).harvestTool(ToolType.PICKAXE).setRequiresTool().hardnessAndResistance(1.25F, 4.2F);
  }

  /**
   * Gets the terracotta map color for the given dye color
   * @param color  Dye color
   * @return  Material color
   */
  private static MaterialColor getTerracottaColor(DyeColor color) {
    switch (color) {
      default:
      case WHITE:      return MaterialColor.WHITE_TERRACOTTA;
      case ORANGE:     return MaterialColor.ORANGE_TERRACOTTA;
      case MAGENTA:    return MaterialColor.MAGENTA_TERRACOTTA;
      case LIGHT_BLUE: return MaterialColor.LIGHT_BLUE_TERRACOTTA;
      case YELLOW:     return MaterialColor.YELLOW_TERRACOTTA;
      case LIME:       return MaterialColor.LIME_TERRACOTTA;
      case PINK:       return MaterialColor.PINK_TERRACOTTA;
      case GRAY:       return MaterialColor.GRAY_TERRACOTTA;
      case LIGHT_GRAY: return MaterialColor.LIGHT_GRAY_TERRACOTTA;
      case CYAN:       return MaterialColor.CYAN_TERRACOTTA;
      case PURPLE:     return MaterialColor.PURPLE_TERRACOTTA;
      case BLUE:       return MaterialColor.BLUE_TERRACOTTA;
      case BROWN:      return MaterialColor.BROWN_TERRACOTTA;
      case GREEN:      return MaterialColor.GREEN_TERRACOTTA;
      case RED:        return MaterialColor.RED_TERRACOTTA;
      case BLACK:      return MaterialColor.BLACK_TERRACOTTA;
    }
  }
}

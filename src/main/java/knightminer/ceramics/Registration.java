package knightminer.ceramics;

import com.mojang.serialization.Codec;
import knightminer.ceramics.blocks.ChannelBlock;
import knightminer.ceramics.blocks.CisternBlock;
import knightminer.ceramics.blocks.FaucetBlock;
import knightminer.ceramics.blocks.FlowingChannelBlock;
import knightminer.ceramics.blocks.FluidCisternBlock;
import knightminer.ceramics.blocks.KilnBlock;
import knightminer.ceramics.blocks.PouringFaucetBlock;
import knightminer.ceramics.blocks.RainbowPorcelain;
import knightminer.ceramics.blocks.entity.ChannelBlockEntity;
import knightminer.ceramics.blocks.entity.CisternBlockEntity;
import knightminer.ceramics.blocks.entity.FaucetBlockEntity;
import knightminer.ceramics.blocks.entity.KilnBlockEntity;
import knightminer.ceramics.items.BaseClayBucketItem;
import knightminer.ceramics.items.CrackableBlockItem;
import knightminer.ceramics.items.EmptyClayBucketItem;
import knightminer.ceramics.items.FixedTooltipBlockItem;
import knightminer.ceramics.items.FluidClayBucketItem;
import knightminer.ceramics.items.HeatResistantBlockItem;
import knightminer.ceramics.items.MilkClayBucketItem;
import knightminer.ceramics.items.SolidClayBucketItem;
import knightminer.ceramics.menu.KilnMenu;
import knightminer.ceramics.recipe.CrackableIngredient;
import knightminer.ceramics.recipe.CrackedClayRepairRecipe;
import knightminer.ceramics.recipe.KilnRecipe;
import knightminer.ceramics.util.EmptyFluidBucketCauldronInteraction;
import knightminer.ceramics.util.EmptySolidBucketCauldronInteraction;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTab.ItemDisplayParameters;
import net.minecraft.world.item.CreativeModeTab.Output;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.crafting.IngredientType;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import slimeknights.mantle.block.GaugeBlock;
import slimeknights.mantle.item.tooltip.BlockTooltipItem;
import slimeknights.mantle.recipe.helper.LoadableRecipeSerializer;
import slimeknights.mantle.registration.deferred.BlockDeferredRegister;
import slimeknights.mantle.registration.deferred.BlockEntityTypeDeferredRegister;
import slimeknights.mantle.registration.deferred.ItemDeferredRegister;
import slimeknights.mantle.registration.deferred.MenuTypeDeferredRegister;
import slimeknights.mantle.registration.object.EnumObject;
import slimeknights.mantle.registration.object.MultiObject;
import slimeknights.mantle.registration.object.WallBuildingBlockObject;

import java.util.EnumMap;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class Registration {
  private static final BlockDeferredRegister BLOCKS = new BlockDeferredRegister(Ceramics.MOD_ID);
  private static final ItemDeferredRegister ITEMS = new ItemDeferredRegister(Ceramics.MOD_ID);
  private static final MenuTypeDeferredRegister MENUS = new MenuTypeDeferredRegister(Ceramics.MOD_ID);
  private static final BlockEntityTypeDeferredRegister BLOCK_ENTITIES = new BlockEntityTypeDeferredRegister(Ceramics.MOD_ID);
  private static final DeferredRegister<ArmorMaterial> ARMOR_MATERIALS = DeferredRegister.create(Registries.ARMOR_MATERIAL, Ceramics.MOD_ID);
  private static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(Registries.RECIPE_SERIALIZER, Ceramics.MOD_ID);
  private static final DeferredRegister<RecipeType<?>> RECIPE_TYPES = DeferredRegister.create(Registries.RECIPE_TYPE, Ceramics.MOD_ID);
  private static final DeferredRegister<IngredientType<?>> INGREDIENTS = DeferredRegister.create(NeoForgeRegistries.Keys.INGREDIENT_TYPES, Ceramics.MOD_ID);
  private static final DeferredRegister<CreativeModeTab> CREATIVE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Ceramics.MOD_ID);
  private static final DeferredRegister<DataComponentType<?>> DATA_COMPONENTS = DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, Ceramics.MOD_ID);

  /**
   * Initializes the registries with the forge mod bus
   */
  static void init(IEventBus bus) {
    BLOCKS.register(bus);
    ITEMS.register(bus);
    MENUS.register(bus);
    BLOCK_ENTITIES.register(bus);
    ARMOR_MATERIALS.register(bus);
    RECIPE_SERIALIZERS.register(bus);
    RECIPE_TYPES.register(bus);
    INGREDIENTS.register(bus);
    CREATIVE_TABS.register(bus);
    DATA_COMPONENTS.register(bus);
    bus.register(Registration.class);
  }

  /* Creative tab */
  static {
    CREATIVE_TABS.register(Ceramics.MOD_ID, () ->
      CreativeModeTab.builder()
        .title(Component.translatable("creative_tab.ceramics"))
        .icon(() -> new ItemStack(Registration.KILN))
        .displayItems(Registration::addTabItems)
        .build());
  }

  /**
   * Item block function that sets the group props
   */
  private static final Function<Block, BlockItem> DEFAULT_BLOCK_ITEM = (block) -> new BlockItem(block, new Item.Properties());
  /**
   * Item block function using {@link FixedTooltipBlockItem}
   */
  private static final Function<String, Function<Block, BlockItem>> HEAT_RESISTANT = name -> block -> new HeatResistantBlockItem(block, new Item.Properties(), name);

  /**
   * Mapping for terracotta to make registration easier
   */
  @SuppressWarnings("deprecation")
  public static final EnumObject<DyeColor, Block> TERRACOTTA = new EnumObject.Builder<DyeColor, Block>(DyeColor.class)
    .put(DyeColor.WHITE, Blocks.WHITE_TERRACOTTA.builtInRegistryHolder())
    .put(DyeColor.ORANGE, Blocks.ORANGE_TERRACOTTA.builtInRegistryHolder())
    .put(DyeColor.MAGENTA, Blocks.MAGENTA_TERRACOTTA.builtInRegistryHolder())
    .put(DyeColor.LIGHT_BLUE, Blocks.LIGHT_BLUE_TERRACOTTA.builtInRegistryHolder())
    .put(DyeColor.YELLOW, Blocks.YELLOW_TERRACOTTA.builtInRegistryHolder())
    .put(DyeColor.LIME, Blocks.LIME_TERRACOTTA.builtInRegistryHolder())
    .put(DyeColor.PINK, Blocks.PINK_TERRACOTTA.builtInRegistryHolder())
    .put(DyeColor.GRAY, Blocks.GRAY_TERRACOTTA.builtInRegistryHolder())
    .put(DyeColor.LIGHT_GRAY, Blocks.LIGHT_GRAY_TERRACOTTA.builtInRegistryHolder())
    .put(DyeColor.CYAN, Blocks.CYAN_TERRACOTTA.builtInRegistryHolder())
    .put(DyeColor.PURPLE, Blocks.PURPLE_TERRACOTTA.builtInRegistryHolder())
    .put(DyeColor.BLUE, Blocks.BLUE_TERRACOTTA.builtInRegistryHolder())
    .put(DyeColor.BROWN, Blocks.BROWN_TERRACOTTA.builtInRegistryHolder())
    .put(DyeColor.GREEN, Blocks.GREEN_TERRACOTTA.builtInRegistryHolder())
    .put(DyeColor.RED, Blocks.RED_TERRACOTTA.builtInRegistryHolder())
    .put(DyeColor.BLACK, Blocks.BLACK_TERRACOTTA.builtInRegistryHolder())
    .build();

  /* Building blocks */
  public static final DeferredBlock<Block> UNFIRED_PORCELAIN_BLOCK = BLOCKS.register("unfired_porcelain_block", Block.Properties.of().mapColor(MapColor.CLAY).strength(0.6F).sound(SoundType.GRAVEL));

  // porcelain
  public static final EnumObject<DyeColor, Block> PORCELAIN_BLOCK = BLOCKS.registerEnum(DyeColor.values(), "porcelain", color -> new Block(Block.Properties.ofLegacyCopy(TERRACOTTA.get(color))));
  public static final EnumObject<RainbowPorcelain, Block> RAINBOW_PORCELAIN = BLOCKS.registerEnum(RainbowPorcelain.values(), "rainbow_porcelain",
    color -> new Block(Block.Properties.of().mapColor(color.getColor()).instrument(NoteBlockInstrument.BASEDRUM).strength(2.0F, 6.0F).requiresCorrectToolForDrops()), block -> new BlockTooltipItem(block, new Item.Properties()));

  // clay bricks
  public static final WallBuildingBlockObject DARK_BRICKS = BLOCKS.registerBuilding("dark_bricks", Block.Properties.of().mapColor(MapColor.COLOR_RED).instrument(NoteBlockInstrument.BASEDRUM).strength(2.0F, 6.0F).requiresCorrectToolForDrops()).wall();
  public static final WallBuildingBlockObject LAVA_BRICKS = BLOCKS.registerBuilding("lava_bricks", Block.Properties.of().mapColor(MapColor.COLOR_ORANGE).instrument(NoteBlockInstrument.BASEDRUM).strength(2.0F, 6.0F).requiresCorrectToolForDrops()).wall();
  public static final WallBuildingBlockObject DRAGON_BRICKS = BLOCKS.registerBuilding("dragon_bricks", Block.Properties.of().mapColor(MapColor.PODZOL).instrument(NoteBlockInstrument.BASEDRUM).strength(2.0F, 6.0F).requiresCorrectToolForDrops()).wall();

  // porcelain bricks
  public static final WallBuildingBlockObject PORCELAIN_BRICKS = BLOCKS.registerBuilding("porcelain_bricks", Block.Properties.of().mapColor(MapColor.SNOW).instrument(NoteBlockInstrument.BASEDRUM).strength(2.0F, 6.0F).requiresCorrectToolForDrops()).wall();
  public static final WallBuildingBlockObject MONOCHROME_BRICKS = BLOCKS.registerBuilding("monochrome_bricks", Block.Properties.of().mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).strength(2.0F, 6.0F).requiresCorrectToolForDrops()).wall();
  public static final WallBuildingBlockObject GOLDEN_BRICKS = BLOCKS.registerBuilding("golden_bricks", Block.Properties.of().mapColor(MapColor.COLOR_YELLOW).instrument(NoteBlockInstrument.BASEDRUM).strength(2.0F, 6.0F).requiresCorrectToolForDrops()).wall();
  public static final WallBuildingBlockObject MARINE_BRICKS = BLOCKS.registerBuilding("marine_bricks", Block.Properties.of().mapColor(MapColor.COLOR_LIGHT_BLUE).instrument(NoteBlockInstrument.BASEDRUM).strength(2.0F, 6.0F).requiresCorrectToolForDrops()).wall();
  public static final WallBuildingBlockObject RAINBOW_BRICKS = BLOCKS.registerBuilding("rainbow_bricks", Block.Properties.of().mapColor(MapColor.COLOR_GREEN).instrument(NoteBlockInstrument.BASEDRUM).strength(2.0F, 6.0F).requiresCorrectToolForDrops()).wall();


  /* items */
  public static final DeferredItem<Item> UNFIRED_PORCELAIN = ITEMS.register("unfired_porcelain");
  public static final DeferredItem<Item> PORCELAIN_BRICK = ITEMS.register("porcelain_brick");

  // tools
  public static final DeferredItem<Item> UNFIRED_CLAY_BUCKET;
  public static final DeferredItem<EmptyClayBucketItem> EMPTY_CLAY_BUCKET, CRACKED_EMPTY_CLAY_BUCKET;
  public static final DeferredItem<FluidClayBucketItem> FLUID_CLAY_BUCKET, CRACKED_FLUID_CLAY_BUCKET;
  public static final DeferredItem<MilkClayBucketItem> MILK_CLAY_BUCKET, CRACKED_MILK_CLAY_BUCKET;
  public static final DeferredItem<SolidClayBucketItem> SOLID_CLAY_BUCKET, CRACKED_SOLID_CLAY_BUCKET;
  static {
    // empty
    Item.Properties emptyProps = new Item.Properties().stacksTo(16);
    UNFIRED_CLAY_BUCKET = ITEMS.register("unfired_clay_bucket", emptyProps);
    EMPTY_CLAY_BUCKET = ITEMS.register("empty_clay_bucket", () -> new EmptyClayBucketItem(false, emptyProps));
    CRACKED_EMPTY_CLAY_BUCKET = ITEMS.register("cracked_empty_clay_bucket", () -> new EmptyClayBucketItem(true, emptyProps));
    // uncracked filled
    FLUID_CLAY_BUCKET = ITEMS.register("fluid_clay_bucket", () -> new FluidClayBucketItem(false, new Item.Properties().stacksTo(1).craftRemainder(EMPTY_CLAY_BUCKET.get())));
    MILK_CLAY_BUCKET = ITEMS.register("milk_clay_bucket", () -> new MilkClayBucketItem(false, new Item.Properties().stacksTo(1).craftRemainder(EMPTY_CLAY_BUCKET.get())));
    SOLID_CLAY_BUCKET = ITEMS.register("solid_clay_bucket", () -> new SolidClayBucketItem(false, new Item.Properties().stacksTo(1).craftRemainder(EMPTY_CLAY_BUCKET.get())));
    // cracked filled
    Item.Properties crackedProps = new Item.Properties().stacksTo(1);
    CRACKED_FLUID_CLAY_BUCKET = ITEMS.register("cracked_fluid_clay_bucket", () -> new FluidClayBucketItem(true, crackedProps));
    CRACKED_MILK_CLAY_BUCKET = ITEMS.register("cracked_milk_clay_bucket", () -> new MilkClayBucketItem(true, crackedProps));
    CRACKED_SOLID_CLAY_BUCKET = ITEMS.register("cracked_solid_clay_bucket", () -> new SolidClayBucketItem(true, crackedProps));
  }

  // armor
  public static final DeferredItem<Item> UNFIRED_CLAY_PLATE = ITEMS.register("unfired_clay_plate");
  public static final DeferredItem<Item> CLAY_PLATE = ITEMS.register("clay_plate");
  public static final DeferredHolder<ArmorMaterial,ArmorMaterial> CLAY_MATERIAL = ARMOR_MATERIALS.register("clay", id -> new ArmorMaterial(Util.make(new EnumMap<>(ArmorItem.Type.class), map -> {
    map.put(ArmorItem.Type.BOOTS, 1);
    map.put(ArmorItem.Type.LEGGINGS, 2);
    map.put(ArmorItem.Type.CHESTPLATE, 3);
    map.put(ArmorItem.Type.HELMET, 1);
    map.put(ArmorItem.Type.BODY, 2);
  }), 7, SoundEvents.ARMOR_EQUIP_GENERIC, () -> Ingredient.of(CLAY_PLATE), List.of(new ArmorMaterial.Layer(id)), 0, 0));
 public static final DeferredItem<ArmorItem> CLAY_HELMET, CLAY_CHESTPLATE, CLAY_LEGGINGS, CLAY_BOOTS;
  static {
    Item.Properties armorProps = new Item.Properties().stacksTo(1);
    CLAY_HELMET     = ITEMS.register("clay_helmet",     () -> new ArmorItem(CLAY_MATERIAL, ArmorItem.Type.HELMET, armorProps));
    CLAY_CHESTPLATE = ITEMS.register("clay_chestplate", () -> new ArmorItem(CLAY_MATERIAL, ArmorItem.Type.CHESTPLATE, armorProps));
    CLAY_LEGGINGS   = ITEMS.register("clay_leggings",   () -> new ArmorItem(CLAY_MATERIAL, ArmorItem.Type.LEGGINGS, armorProps));
    CLAY_BOOTS      = ITEMS.register("clay_boots",      () -> new ArmorItem(CLAY_MATERIAL, ArmorItem.Type.BOOTS, armorProps));
  }

  // kiln block
  public static final DeferredBlock<KilnBlock> KILN = BLOCKS.register("kiln", () -> new KilnBlock(terracottaProps(MapColor.COLOR_ORANGE).lightLevel(s -> s.getValue(KilnBlock.LIT) ? 13 : 0)), DEFAULT_BLOCK_ITEM);
  public static final DeferredHolder<MenuType<?>,MenuType<KilnMenu>> KILN_MENU = MENUS.register("kiln", KilnMenu::new);
  public static final DeferredHolder<BlockEntityType<?>,BlockEntityType<KilnBlockEntity>> KILN_BLOCK_ENTITY = BLOCK_ENTITIES.register("kiln", KilnBlockEntity::new, KILN);
  // kiln recipes
  public static final DeferredHolder<RecipeType<?>,RecipeType<KilnRecipe>> KILN_RECIPE = RECIPE_TYPES.register("kiln", id -> new RecipeType<>() {
    @Override
    public String toString() {
      return id.toString();
    }
  });
  public static final DeferredHolder<RecipeSerializer<?>,RecipeSerializer<KilnRecipe>> KILN_SERIALIZER = RECIPE_SERIALIZERS.register("kiln", () -> LoadableRecipeSerializer.of(KilnRecipe.LOADABLE));

  /*
   * fluid handling
   */
  public static final DeferredHolder<DataComponentType<?>,DataComponentType<Byte>> CRACKS = DATA_COMPONENTS.register("cracks", () -> DataComponentType.<Byte>builder()
    .persistent(Codec.BYTE.validate(Codec.checkRange((byte)1, (byte)5)))
    .networkSynchronized(ByteBufCodecs.BYTE)
    .build());
  // gauge
  public static final DeferredBlock<GaugeBlock> TERRACOTTA_GAUGE, PORCELAIN_GAUGE;

  static {
    Function<Block, BlockItem> GAUGE_BLOCK_ITEM = block -> new FixedTooltipBlockItem(block, new Item.Properties(), "gauge.tooltip");
    BlockBehaviour.Properties GAUGE_PROPERTIES = BlockBehaviour.Properties.of().mapColor(MapColor.NONE).pushReaction(PushReaction.DESTROY).noCollission().strength(0.5F).noOcclusion().requiresCorrectToolForDrops();
    TERRACOTTA_GAUGE = BLOCKS.register("terracotta_gauge", () -> new GaugeBlock(GAUGE_PROPERTIES), GAUGE_BLOCK_ITEM);
    PORCELAIN_GAUGE = BLOCKS.register("porcelain_gauge", () -> new GaugeBlock(GAUGE_PROPERTIES), GAUGE_BLOCK_ITEM);
  }

  // raw clay blocks
  public static final DeferredBlock<CisternBlock> CLAY_CISTERN, UNFIRED_CISTERN;
  public static final DeferredBlock<FaucetBlock> CLAY_FAUCET, UNFIRED_FAUCET;
  public static final DeferredBlock<ChannelBlock> CLAY_CHANNEL, UNFIRED_CHANNEL;
  static {
    BlockBehaviour.Properties CLAY_PROPERTIES = BlockBehaviour.Properties.of().mapColor(MapColor.CLAY).strength(0.6F).sound(SoundType.GRAVEL).noOcclusion();
    CLAY_CISTERN = BLOCKS.register("clay_cistern", () -> new CisternBlock(CLAY_PROPERTIES), DEFAULT_BLOCK_ITEM);
    UNFIRED_CISTERN = BLOCKS.register("unfired_cistern", () -> new CisternBlock(CLAY_PROPERTIES), DEFAULT_BLOCK_ITEM);
    CLAY_FAUCET = BLOCKS.register("clay_faucet", () -> new FaucetBlock(CLAY_PROPERTIES), DEFAULT_BLOCK_ITEM);
    UNFIRED_FAUCET = BLOCKS.register("unfired_faucet", () -> new FaucetBlock(CLAY_PROPERTIES), DEFAULT_BLOCK_ITEM);
    CLAY_CHANNEL = BLOCKS.register("clay_channel", () -> new ChannelBlock(CLAY_PROPERTIES), DEFAULT_BLOCK_ITEM);
    UNFIRED_CHANNEL = BLOCKS.register("unfired_channel", () -> new ChannelBlock(CLAY_PROPERTIES), DEFAULT_BLOCK_ITEM);
  }

  // cistern
  private static final Function<String,Function<Block,BlockItem>> CRACKABLE_BLOCK_ITEM = tooltip -> block -> new CrackableBlockItem(block, new Item.Properties(), tooltip);
  private static final Function<Block,BlockItem> TERRACOTTA_CISTERN_BLOCK_ITEM = CRACKABLE_BLOCK_ITEM.apply("cistern.tooltip");
  private static final Function<Block,BlockItem> PORCELAIN_CISTERN_BLOCK_ITEM = HEAT_RESISTANT.apply("cistern.tooltip");
  public static final DeferredBlock<FluidCisternBlock> TERRACOTTA_CISTERN = BLOCKS.register("terracotta_cistern", () -> new FluidCisternBlock(terracottaProps(MapColor.COLOR_ORANGE).noOcclusion().randomTicks(), true), TERRACOTTA_CISTERN_BLOCK_ITEM);
  public static final EnumObject<DyeColor, Block> COLORED_CISTERN = BLOCKS.registerEnum(DyeColor.values(), "terracotta_cistern", (color) -> new FluidCisternBlock(terracottaProps(getTerracottaColor(color)).noOcclusion().randomTicks(), true), TERRACOTTA_CISTERN_BLOCK_ITEM);
  public static final EnumObject<DyeColor, Block> PORCELAIN_CISTERN = BLOCKS.registerEnum(DyeColor.values(), "porcelain_cistern", (color) -> new FluidCisternBlock(terracottaProps(getTerracottaColor(color)).noOcclusion(), false), PORCELAIN_CISTERN_BLOCK_ITEM);
  public static final DeferredHolder<BlockEntityType<?>,BlockEntityType<CisternBlockEntity>> CISTERN_BLOCK_ENTITY = BLOCK_ENTITIES.register("cistern", CisternBlockEntity::new, builder -> {
    builder.add(TERRACOTTA_CISTERN.get());
    builder.addAll(COLORED_CISTERN.values());
    builder.addAll(PORCELAIN_CISTERN.values());
  });

  // faucet
  public static final DeferredBlock<PouringFaucetBlock> TERRACOTTA_FAUCET = BLOCKS.register("terracotta_faucet", () -> new PouringFaucetBlock(terracottaProps(MapColor.COLOR_ORANGE).noOcclusion().randomTicks(), true), CRACKABLE_BLOCK_ITEM.apply("faucet.tooltip"));
  public static final DeferredBlock<PouringFaucetBlock> PORCELAIN_FAUCET = BLOCKS.register("porcelain_faucet", () -> new PouringFaucetBlock(terracottaProps(MapColor.TERRACOTTA_WHITE).noOcclusion(), false), HEAT_RESISTANT.apply("faucet.tooltip"));
  public static final DeferredHolder<BlockEntityType<?>,BlockEntityType<FaucetBlockEntity>> FAUCET_BLOCK_ENTITY = BLOCK_ENTITIES.register("faucet", FaucetBlockEntity::new, builder -> builder.add(TERRACOTTA_FAUCET.get(), PORCELAIN_FAUCET.get()));

  // channel
  public static final DeferredBlock<FlowingChannelBlock> TERRACOTTA_CHANNEL = BLOCKS.register("terracotta_channel", () -> new FlowingChannelBlock(terracottaProps(MapColor.COLOR_ORANGE).noOcclusion().randomTicks(), true), CRACKABLE_BLOCK_ITEM.apply("channel.tooltip"));
  public static final DeferredBlock<FlowingChannelBlock> PORCELAIN_CHANNEL = BLOCKS.register("porcelain_channel", () -> new FlowingChannelBlock(terracottaProps(MapColor.TERRACOTTA_WHITE).noOcclusion(), false), HEAT_RESISTANT.apply("channel.tooltip"));
  public static final DeferredHolder<BlockEntityType<?>,BlockEntityType<ChannelBlockEntity>> CHANNEL_BLOCK_ENTITY = BLOCK_ENTITIES.register("channel", ChannelBlockEntity::new, builder -> builder.add(TERRACOTTA_CHANNEL.get(), PORCELAIN_CHANNEL.get()));

  // clay repair
  public static final DeferredHolder<RecipeSerializer<?>,RecipeSerializer<CrackedClayRepairRecipe>> CLAY_REPAIR_RECIPE_SERIALIZER = RECIPE_SERIALIZERS.register("cracked_clay_repair", () -> LoadableRecipeSerializer.of(CrackedClayRepairRecipe.LOADABLE));

  // ingredients
  public static final DeferredHolder<IngredientType<?>,IngredientType<CrackableIngredient>> CRACKABLE_INGREDIENT = INGREDIENTS.register("crackable", () -> new IngredientType<>(CrackableIngredient.CODEC, CrackableIngredient.STREAM_CODEC));

  @SubscribeEvent
  static void commonSetup(FMLCommonSetupEvent event) {
    SolidClayBucketItem.loadBucketableBlocks();
    event.enqueueWork(() -> {
      record FillClayBucketCauldronInteraction(Predicate<BlockState> cauldron, ItemStack filledBucket, SoundEvent sound) implements CauldronInteraction {
        @Override
        public ItemInteractionResult interact(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, ItemStack stack) {
          return CauldronInteraction.fillBucket(state, level, pos, player, hand, stack, filledBucket.copy(), cauldron, sound);
        }
      }

      // filling buckets - not needed on empty cauldron
      // TODO: this is pretty hardcoded, can this be generalized in any way?
      // neo has a new event for this
      Predicate<BlockState> full = state -> state.getValue(LayeredCauldronBlock.LEVEL) == 3;
      CauldronInteraction.WATER.map().put(EMPTY_CLAY_BUCKET.get(),         new FillClayBucketCauldronInteraction(full, BaseClayBucketItem.withFluid(Fluids.WATER, false), SoundEvents.BUCKET_FILL));
      CauldronInteraction.WATER.map().put(CRACKED_EMPTY_CLAY_BUCKET.get(), new FillClayBucketCauldronInteraction(full, BaseClayBucketItem.withFluid(Fluids.WATER, true),  SoundEvents.BUCKET_FILL));
      CauldronInteraction.POWDER_SNOW.map().put(EMPTY_CLAY_BUCKET.get(),         new FillClayBucketCauldronInteraction(full, BaseClayBucketItem.withBlock(Blocks.POWDER_SNOW, false), SoundEvents.BUCKET_FILL_POWDER_SNOW));
      CauldronInteraction.POWDER_SNOW.map().put(CRACKED_EMPTY_CLAY_BUCKET.get(), new FillClayBucketCauldronInteraction(full, BaseClayBucketItem.withBlock(Blocks.POWDER_SNOW, true),  SoundEvents.BUCKET_FILL_POWDER_SNOW));
      CauldronInteraction fillLava = new FillClayBucketCauldronInteraction(state -> true, BaseClayBucketItem.withFluid(Fluids.LAVA, true), SoundEvents.BUCKET_FILL_LAVA);
      CauldronInteraction.LAVA.map().put(EMPTY_CLAY_BUCKET.get(),         fillLava);
      CauldronInteraction.LAVA.map().put(CRACKED_EMPTY_CLAY_BUCKET.get(), fillLava);

      // emptying buckets - needed on all as fluid can replace existing
      BiConsumer<ItemLike, CauldronInteraction> addAll = (item, interaction) -> {
        CauldronInteraction.EMPTY.map().put(item.asItem(), interaction);
        CauldronInteraction.WATER.map().put(item.asItem(), interaction);
        CauldronInteraction.LAVA.map().put(item.asItem(), interaction);
        CauldronInteraction.POWDER_SNOW.map().put(item.asItem(), interaction);
      };
      addAll.accept(FLUID_CLAY_BUCKET,         new EmptyFluidBucketCauldronInteraction(FLUID_CLAY_BUCKET.get()));
      addAll.accept(CRACKED_FLUID_CLAY_BUCKET, new EmptyFluidBucketCauldronInteraction(CRACKED_FLUID_CLAY_BUCKET.get()));
      addAll.accept(SOLID_CLAY_BUCKET,         new EmptySolidBucketCauldronInteraction(SOLID_CLAY_BUCKET.get()));
      addAll.accept(CRACKED_SOLID_CLAY_BUCKET, new EmptySolidBucketCauldronInteraction(CRACKED_SOLID_CLAY_BUCKET.get()));
    });
  }

  /** Adds relevant items to the creative tab */
  private static void addTabItems(ItemDisplayParameters parameters, Output output) {
    output.accept(KILN);

    // clay bricks
    accept(output, DARK_BRICKS);
    accept(output, LAVA_BRICKS);
    accept(output, DRAGON_BRICKS);

    // basic porcelain
    output.accept(UNFIRED_PORCELAIN);
    output.accept(UNFIRED_PORCELAIN_BLOCK);
    accept(output, PORCELAIN_BLOCK);
    accept(output, RAINBOW_PORCELAIN);

    // porcelain bricks
    output.accept(PORCELAIN_BRICK);
    accept(output, PORCELAIN_BRICKS);
    accept(output, MONOCHROME_BRICKS);
    accept(output, GOLDEN_BRICKS);
    accept(output, MARINE_BRICKS);
    accept(output, RAINBOW_BRICKS);

    // fluid handling - terracotta
    output.accept(TERRACOTTA_GAUGE);
    output.accept(TERRACOTTA_FAUCET);
    output.accept(TERRACOTTA_CHANNEL);
    output.accept(TERRACOTTA_CISTERN);
    // fluid handling - porcelain
    output.accept(PORCELAIN_GAUGE);
    accept(output, COLORED_CISTERN);
    output.accept(PORCELAIN_FAUCET);
    output.accept(PORCELAIN_CHANNEL);
    accept(output, PORCELAIN_CISTERN);
    // fluid handling - unfired
    output.accept(CLAY_FAUCET);
    output.accept(CLAY_CHANNEL);
    output.accept(CLAY_CISTERN);
    output.accept(UNFIRED_FAUCET);
    output.accept(UNFIRED_CHANNEL);
    output.accept(UNFIRED_CISTERN);

    // armor
    output.accept(UNFIRED_CLAY_PLATE);
    output.accept(CLAY_PLATE);
    output.accept(CLAY_HELMET);
    output.accept(CLAY_CHESTPLATE);
    output.accept(CLAY_LEGGINGS);
    output.accept(CLAY_BOOTS);

    // buckets
    Consumer<ItemStack> consumer = output::accept;
    EMPTY_CLAY_BUCKET.get().addVariants(consumer);
    CRACKED_EMPTY_CLAY_BUCKET.get().addVariants(consumer);
    FLUID_CLAY_BUCKET.get().addVariants(consumer);
    CRACKED_FLUID_CLAY_BUCKET.get().addVariants(consumer);
    MILK_CLAY_BUCKET.get().addVariants(consumer);
    CRACKED_MILK_CLAY_BUCKET.get().addVariants(consumer);
    EMPTY_CLAY_BUCKET.get().addVariants(consumer);
    CRACKED_SOLID_CLAY_BUCKET.get().addVariants(consumer);
  }

  /** Adds all members of an enum object to the given creative tab */
  private static void accept(Output output, MultiObject<? extends ItemLike> object) {
    object.forEach(output::accept);
  }

  /**
   * Standard hardened clay properties
   * @param color  Map color of block
   * @return  Block properties
   */
  private static BlockBehaviour.Properties terracottaProps(MapColor color) {
    return BlockBehaviour.Properties.of().mapColor(color).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.25F, 4.2F);
  }

  /**
   * Gets the terracotta map color for the given dye color
   * @param color  Dye color
   * @return  Material color
   */
  private static MapColor getTerracottaColor(DyeColor color) {
    return switch (color) {
      case WHITE      -> MapColor.TERRACOTTA_WHITE;
      case ORANGE     -> MapColor.TERRACOTTA_ORANGE;
      case MAGENTA    -> MapColor.TERRACOTTA_MAGENTA;
      case LIGHT_BLUE -> MapColor.TERRACOTTA_LIGHT_BLUE;
      case YELLOW     -> MapColor.TERRACOTTA_YELLOW;
      case LIME       -> MapColor.TERRACOTTA_LIGHT_GREEN;
      case PINK       -> MapColor.TERRACOTTA_PINK;
      case GRAY       -> MapColor.TERRACOTTA_GRAY;
      case LIGHT_GRAY -> MapColor.TERRACOTTA_LIGHT_GRAY;
      case CYAN       -> MapColor.TERRACOTTA_CYAN;
      case PURPLE     -> MapColor.TERRACOTTA_PURPLE;
      case BLUE       -> MapColor.TERRACOTTA_BLUE;
      case BROWN      -> MapColor.TERRACOTTA_BROWN;
      case GREEN      -> MapColor.TERRACOTTA_GREEN;
      case RED        -> MapColor.TERRACOTTA_RED;
      case BLACK      -> MapColor.TERRACOTTA_BLACK;
    };
  }
}

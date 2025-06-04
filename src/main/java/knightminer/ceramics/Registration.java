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
import knightminer.ceramics.blocks.entity.ChannelBlockEntity;
import knightminer.ceramics.blocks.entity.CisternBlockEntity;
import knightminer.ceramics.blocks.entity.FaucetBlockEntity;
import knightminer.ceramics.blocks.entity.KilnBlockEntity;
import knightminer.ceramics.items.ArmorMaterials;
import knightminer.ceramics.items.BaseClayBucketItem;
import knightminer.ceramics.items.CrackableBlockItem;
import knightminer.ceramics.items.EmptyClayBucketItem;
import knightminer.ceramics.items.FixedTooltipBlockItem;
import knightminer.ceramics.items.FluidClayBucketItem;
import knightminer.ceramics.items.MilkClayBucketItem;
import knightminer.ceramics.items.SolidClayBucketItem;
import knightminer.ceramics.menu.KilnMenu;
import knightminer.ceramics.recipe.CrackedClayRepairRecipe;
import knightminer.ceramics.recipe.KilnRecipe;
import knightminer.ceramics.recipe.NoNBTIngredient;
import knightminer.ceramics.util.EmptyFluidBucketCauldronInteraction;
import knightminer.ceramics.util.EmptySolidBucketCauldronInteraction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTab.ItemDisplayParameters;
import net.minecraft.world.item.CreativeModeTab.Output;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SimpleCookingSerializer;
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
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;
import net.minecraftforge.registries.RegistryObject;
import slimeknights.mantle.item.BlockTooltipItem;
import slimeknights.mantle.registration.deferred.BlockDeferredRegister;
import slimeknights.mantle.registration.deferred.BlockEntityTypeDeferredRegister;
import slimeknights.mantle.registration.deferred.ItemDeferredRegister;
import slimeknights.mantle.registration.deferred.MenuTypeDeferredRegister;
import slimeknights.mantle.registration.object.EnumObject;
import slimeknights.mantle.registration.object.ItemObject;
import slimeknights.mantle.registration.object.MultiObject;
import slimeknights.mantle.registration.object.WallBuildingBlockObject;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class Registration {
  private static final BlockDeferredRegister BLOCKS = new BlockDeferredRegister(Ceramics.MOD_ID);
  private static final ItemDeferredRegister ITEMS = new ItemDeferredRegister(Ceramics.MOD_ID);
  private static final MenuTypeDeferredRegister MENUS = new MenuTypeDeferredRegister(Ceramics.MOD_ID);
  private static final BlockEntityTypeDeferredRegister BLOCK_ENTITIES = new BlockEntityTypeDeferredRegister(Ceramics.MOD_ID);
  private static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, Ceramics.MOD_ID);
  private static final DeferredRegister<RecipeType<?>> RECIPE_TYPES = DeferredRegister.create(Registries.RECIPE_TYPE, Ceramics.MOD_ID);
  private static final DeferredRegister<CreativeModeTab> CREATIVE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Ceramics.MOD_ID);

  /**
   * Initializes the registries with the forge mod bus
   */
  static void init() {
    IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
    BLOCKS.register(bus);
    ITEMS.register(bus);
    MENUS.register(bus);
    BLOCK_ENTITIES.register(bus);
    RECIPE_SERIALIZERS.register(bus);
    RECIPE_TYPES.register(bus);
    CREATIVE_TABS.register(bus);
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
   * Item block function using {@link BlockTooltipItem}
   */
  private static final Function<Block, BlockItem> TOOLTIP_BLOCK_ITEM = (block) -> new BlockTooltipItem(block, new Item.Properties());
  /**
   * Item block function using {@link FixedTooltipBlockItem}
   */
  private static final Function<String, Function<Block, BlockItem>> FIXED_TOOLTIP = name -> block -> new FixedTooltipBlockItem(block, new Item.Properties(), name);

  /**
   * Mapping for terracotta to make registration easier
   */
  public static final EnumObject<DyeColor, Block> TERRACOTTA = new EnumObject.Builder<DyeColor, Block>(DyeColor.class)
    .put(DyeColor.WHITE, Blocks.WHITE_TERRACOTTA)
    .put(DyeColor.ORANGE, Blocks.ORANGE_TERRACOTTA)
    .put(DyeColor.MAGENTA, Blocks.MAGENTA_TERRACOTTA)
    .put(DyeColor.LIGHT_BLUE, Blocks.LIGHT_BLUE_TERRACOTTA)
    .put(DyeColor.YELLOW, Blocks.YELLOW_TERRACOTTA)
    .put(DyeColor.LIME, Blocks.LIME_TERRACOTTA)
    .put(DyeColor.PINK, Blocks.PINK_TERRACOTTA)
    .put(DyeColor.GRAY, Blocks.GRAY_TERRACOTTA)
    .put(DyeColor.LIGHT_GRAY, Blocks.LIGHT_GRAY_TERRACOTTA)
    .put(DyeColor.CYAN, Blocks.CYAN_TERRACOTTA)
    .put(DyeColor.PURPLE, Blocks.PURPLE_TERRACOTTA)
    .put(DyeColor.BLUE, Blocks.BLUE_TERRACOTTA)
    .put(DyeColor.BROWN, Blocks.BROWN_TERRACOTTA)
    .put(DyeColor.GREEN, Blocks.GREEN_TERRACOTTA)
    .put(DyeColor.RED, Blocks.RED_TERRACOTTA)
    .put(DyeColor.BLACK, Blocks.BLACK_TERRACOTTA)
    .build();

  /* Building blocks */
  public static final ItemObject<Block> UNFIRED_PORCELAIN_BLOCK = BLOCKS.register("unfired_porcelain_block", Block.Properties.of().mapColor(MapColor.CLAY).strength(0.6F).sound(SoundType.GRAVEL), DEFAULT_BLOCK_ITEM);

  // porcelain
  public static final EnumObject<DyeColor, Block> PORCELAIN_BLOCK = BLOCKS.registerEnum(DyeColor.values(), "porcelain", color -> new Block(Block.Properties.copy(TERRACOTTA.get(color))), DEFAULT_BLOCK_ITEM);
  public static final EnumObject<RainbowPorcelain, Block> RAINBOW_PORCELAIN = BLOCKS.registerEnum(RainbowPorcelain.values(), "rainbow_porcelain",
    color -> new Block(Block.Properties.of().mapColor(color.getColor()).instrument(NoteBlockInstrument.BASEDRUM).strength(2.0F, 6.0F).requiresCorrectToolForDrops()), TOOLTIP_BLOCK_ITEM);

  // clay bricks
  public static final WallBuildingBlockObject DARK_BRICKS = BLOCKS.registerWallBuilding("dark_bricks", Block.Properties.of().mapColor(MapColor.COLOR_RED).instrument(NoteBlockInstrument.BASEDRUM).strength(2.0F, 6.0F).requiresCorrectToolForDrops(), DEFAULT_BLOCK_ITEM);
  public static final WallBuildingBlockObject LAVA_BRICKS = BLOCKS.registerWallBuilding("lava_bricks", Block.Properties.of().mapColor(MapColor.COLOR_ORANGE).instrument(NoteBlockInstrument.BASEDRUM).strength(2.0F, 6.0F).requiresCorrectToolForDrops(), DEFAULT_BLOCK_ITEM);
  public static final WallBuildingBlockObject DRAGON_BRICKS = BLOCKS.registerWallBuilding("dragon_bricks", Block.Properties.of().mapColor(MapColor.PODZOL).instrument(NoteBlockInstrument.BASEDRUM).strength(2.0F, 6.0F).requiresCorrectToolForDrops(), DEFAULT_BLOCK_ITEM);

  // porcelain bricks
  public static final WallBuildingBlockObject PORCELAIN_BRICKS = BLOCKS.registerWallBuilding("porcelain_bricks", Block.Properties.of().mapColor(MapColor.SNOW).instrument(NoteBlockInstrument.BASEDRUM).strength(2.0F, 6.0F).requiresCorrectToolForDrops(), DEFAULT_BLOCK_ITEM);
  public static final WallBuildingBlockObject MONOCHROME_BRICKS = BLOCKS.registerWallBuilding("monochrome_bricks", Block.Properties.of().mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).strength(2.0F, 6.0F).requiresCorrectToolForDrops(), DEFAULT_BLOCK_ITEM);
  public static final WallBuildingBlockObject GOLDEN_BRICKS = BLOCKS.registerWallBuilding("golden_bricks", Block.Properties.of().mapColor(MapColor.COLOR_YELLOW).instrument(NoteBlockInstrument.BASEDRUM).strength(2.0F, 6.0F).requiresCorrectToolForDrops(), DEFAULT_BLOCK_ITEM);
  public static final WallBuildingBlockObject MARINE_BRICKS = BLOCKS.registerWallBuilding("marine_bricks", Block.Properties.of().mapColor(MapColor.COLOR_LIGHT_BLUE).instrument(NoteBlockInstrument.BASEDRUM).strength(2.0F, 6.0F).requiresCorrectToolForDrops(), DEFAULT_BLOCK_ITEM);
  public static final WallBuildingBlockObject RAINBOW_BRICKS = BLOCKS.registerWallBuilding("rainbow_bricks", Block.Properties.of().mapColor(MapColor.COLOR_GREEN).instrument(NoteBlockInstrument.BASEDRUM).strength(2.0F, 6.0F).requiresCorrectToolForDrops(), DEFAULT_BLOCK_ITEM);


  /* items */
  public static final ItemObject<Item> UNFIRED_PORCELAIN = ITEMS.register("unfired_porcelain");
  public static final ItemObject<Item> PORCELAIN_BRICK = ITEMS.register("porcelain_brick");

  // tools
  public static final ItemObject<Item> UNFIRED_CLAY_BUCKET;
  public static final ItemObject<EmptyClayBucketItem> EMPTY_CLAY_BUCKET, CRACKED_EMPTY_CLAY_BUCKET;
  public static final ItemObject<FluidClayBucketItem> FLUID_CLAY_BUCKET, CRACKED_FLUID_CLAY_BUCKET;
  public static final ItemObject<MilkClayBucketItem> MILK_CLAY_BUCKET, CRACKED_MILK_CLAY_BUCKET;
  public static final ItemObject<SolidClayBucketItem> SOLID_CLAY_BUCKET, CRACKED_SOLID_CLAY_BUCKET;
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
  public static final ItemObject<Item> UNFIRED_CLAY_PLATE = ITEMS.register("unfired_clay_plate");
  public static final ItemObject<Item> CLAY_PLATE = ITEMS.register("clay_plate");
  public static final ItemObject<ArmorItem> CLAY_HELMET, CLAY_CHESTPLATE, CLAY_LEGGINGS, CLAY_BOOTS;
  static {
    Item.Properties armorProps = new Item.Properties().stacksTo(1);
    CLAY_HELMET = ITEMS.register("clay_helmet", () -> new ArmorItem(ArmorMaterials.CLAY, ArmorItem.Type.HELMET, armorProps));
    CLAY_CHESTPLATE = ITEMS.register("clay_chestplate", () -> new ArmorItem(ArmorMaterials.CLAY, ArmorItem.Type.CHESTPLATE, armorProps));
    CLAY_LEGGINGS = ITEMS.register("clay_leggings", () -> new ArmorItem(ArmorMaterials.CLAY, ArmorItem.Type.LEGGINGS, armorProps));
    CLAY_BOOTS = ITEMS.register("clay_boots", () -> new ArmorItem(ArmorMaterials.CLAY, ArmorItem.Type.BOOTS, armorProps));
  }

  // kiln block
  public static final ItemObject<KilnBlock> KILN = BLOCKS.register("kiln", () -> new KilnBlock(terracottaProps(MapColor.COLOR_ORANGE).lightLevel(s -> s.getValue(KilnBlock.LIT) ? 13 : 0)), DEFAULT_BLOCK_ITEM);
  public static final RegistryObject<MenuType<KilnMenu>> KILN_MENU = MENUS.register("kiln", KilnMenu::new);
  public static final RegistryObject<BlockEntityType<KilnBlockEntity>> KILN_BLOCK_ENTITY = BLOCK_ENTITIES.register("kiln", KilnBlockEntity::new, KILN);
  // kiln recipes
  public static final RegistryObject<RecipeType<KilnRecipe>> KILN_RECIPE = RECIPE_TYPES.register("kiln", () -> new RecipeType<>() {
    @Override
    public String toString() {
      return "ceramics:kiln";
    }
  });
  public static final RegistryObject<SimpleCookingSerializer<KilnRecipe>> KILN_SERIALIZER = RECIPE_SERIALIZERS.register("kiln", () -> new SimpleCookingSerializer<>(KilnRecipe::new, 100));

  /*
   * fluid handling
   */
  // gauge
  public static final ItemObject<GaugeBlock> TERRACOTTA_GAUGE, PORCELAIN_GAUGE;

  static {
    Function<Block, BlockItem> GAUGE_BLOCK_ITEM = FIXED_TOOLTIP.apply("gauge.tooltip");
    BlockBehaviour.Properties GAUGE_PROPERTIES = BlockBehaviour.Properties.of().mapColor(MapColor.NONE).pushReaction(PushReaction.DESTROY).noCollission().strength(0.5F).noOcclusion().requiresCorrectToolForDrops();
    TERRACOTTA_GAUGE = BLOCKS.register("terracotta_gauge", () -> new GaugeBlock(GAUGE_PROPERTIES), GAUGE_BLOCK_ITEM);
    PORCELAIN_GAUGE = BLOCKS.register("porcelain_gauge", () -> new GaugeBlock(GAUGE_PROPERTIES), GAUGE_BLOCK_ITEM);
  }

  // raw clay blocks
  public static final ItemObject<CisternBlock> CLAY_CISTERN, UNFIRED_CISTERN;
  public static final ItemObject<FaucetBlock> CLAY_FAUCET, UNFIRED_FAUCET;
  public static final ItemObject<ChannelBlock> CLAY_CHANNEL, UNFIRED_CHANNEL;
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
  private static final Function<Block,BlockItem> TERRACOTTA_CISTERN_BLOCK_ITEM = CRACKABLE_BLOCK_ITEM.apply("terracotta_cistern.tooltip");
  private static final Function<Block,BlockItem> PORCELAIN_CISTERN_BLOCK_ITEM = FIXED_TOOLTIP.apply("porcelain_cistern.tooltip");
  public static final ItemObject<FluidCisternBlock> TERRACOTTA_CISTERN = BLOCKS.register("terracotta_cistern", () -> new FluidCisternBlock(terracottaProps(MapColor.COLOR_ORANGE).noOcclusion().randomTicks(), true), TERRACOTTA_CISTERN_BLOCK_ITEM);
  public static final EnumObject<DyeColor, FluidCisternBlock> COLORED_CISTERN = BLOCKS.registerEnum(DyeColor.values(), "terracotta_cistern", (color) -> new FluidCisternBlock(terracottaProps(getTerracottaColor(color)).noOcclusion().randomTicks(), true), TERRACOTTA_CISTERN_BLOCK_ITEM);
  public static final EnumObject<DyeColor, FluidCisternBlock> PORCELAIN_CISTERN = BLOCKS.registerEnum(DyeColor.values(), "porcelain_cistern", (color) -> new FluidCisternBlock(terracottaProps(getTerracottaColor(color)).noOcclusion(), false), PORCELAIN_CISTERN_BLOCK_ITEM);
  public static final RegistryObject<BlockEntityType<CisternBlockEntity>> CISTERN_BLOCK_ENTITY = BLOCK_ENTITIES.register("cistern", CisternBlockEntity::new, builder -> {
    builder.add(TERRACOTTA_CISTERN.get());
    builder.addAll(COLORED_CISTERN.values());
    builder.addAll(PORCELAIN_CISTERN.values());
  });

  // faucet
  public static final ItemObject<PouringFaucetBlock> TERRACOTTA_FAUCET = BLOCKS.register("terracotta_faucet", () -> new PouringFaucetBlock(terracottaProps(MapColor.COLOR_ORANGE).noOcclusion().randomTicks(), true), CRACKABLE_BLOCK_ITEM.apply("terracotta_faucet.tooltip"));
  public static final ItemObject<PouringFaucetBlock> PORCELAIN_FAUCET = BLOCKS.register("porcelain_faucet", () -> new PouringFaucetBlock(terracottaProps(MapColor.TERRACOTTA_WHITE).noOcclusion(), false), TOOLTIP_BLOCK_ITEM);
  public static final RegistryObject<BlockEntityType<FaucetBlockEntity>> FAUCET_BLOCK_ENTITY = BLOCK_ENTITIES.register("faucet", FaucetBlockEntity::new, builder -> builder.add(TERRACOTTA_FAUCET.get(), PORCELAIN_FAUCET.get()));

  // channel
  public static final ItemObject<FlowingChannelBlock> TERRACOTTA_CHANNEL = BLOCKS.register("terracotta_channel", () -> new FlowingChannelBlock(terracottaProps(MapColor.COLOR_ORANGE).noOcclusion().randomTicks(), true), CRACKABLE_BLOCK_ITEM.apply("terracotta_channel.tooltip"));
  public static final ItemObject<FlowingChannelBlock> PORCELAIN_CHANNEL = BLOCKS.register("porcelain_channel", () -> new FlowingChannelBlock(terracottaProps(MapColor.TERRACOTTA_WHITE).noOcclusion(), false), TOOLTIP_BLOCK_ITEM);
  public static final RegistryObject<BlockEntityType<ChannelBlockEntity>> CHANNEL_BLOCK_ENTITY = BLOCK_ENTITIES.register("channel", ChannelBlockEntity::new, builder -> builder.add(TERRACOTTA_CHANNEL.get(), PORCELAIN_CHANNEL.get()));

  // clay repair
  public static final RegistryObject<RecipeSerializer<?>> CLAY_REPAIR_RECIPE_SERIALIZER = RECIPE_SERIALIZERS.register("cracked_clay_repair", CrackedClayRepairRecipe.Serializer::new);

  @SubscribeEvent
  static void registerRecipeSerializer(RegisterEvent event) {
    if (event.getRegistryKey() == Registries.RECIPE_SERIALIZER) {
      CraftingHelper.register(Ceramics.getResource("no_nbt"), NoNBTIngredient.SERIALIZER);
    }
  }

  @SubscribeEvent
  static void commonSetup(FMLCommonSetupEvent event) {
    SolidClayBucketItem.loadBucketableBlocks();
    event.enqueueWork(() -> {
      record FillClayBucketCauldronInteraction(Predicate<BlockState> cauldron, ItemStack filledBucket, SoundEvent sound) implements CauldronInteraction {
        @Override
        public InteractionResult interact(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, ItemStack stack) {
          return CauldronInteraction.fillBucket(state, level, pos, player, hand, stack, filledBucket.copy(), cauldron, sound);
        }
      }

      // filling buckets - not needed on empty cauldron
      // TODO: this is pretty hardcoded, can this be generalized in any way?
      Predicate<BlockState> full = state -> state.getValue(LayeredCauldronBlock.LEVEL) == 3;
      CauldronInteraction.WATER.put(EMPTY_CLAY_BUCKET.get(),         new FillClayBucketCauldronInteraction(full, BaseClayBucketItem.withFluid(Fluids.WATER, false), SoundEvents.BUCKET_FILL));
      CauldronInteraction.WATER.put(CRACKED_EMPTY_CLAY_BUCKET.get(), new FillClayBucketCauldronInteraction(full, BaseClayBucketItem.withFluid(Fluids.WATER, true),  SoundEvents.BUCKET_FILL));
      CauldronInteraction.POWDER_SNOW.put(EMPTY_CLAY_BUCKET.get(),         new FillClayBucketCauldronInteraction(full, BaseClayBucketItem.withBlock(Blocks.POWDER_SNOW, false), SoundEvents.BUCKET_FILL_POWDER_SNOW));
      CauldronInteraction.POWDER_SNOW.put(CRACKED_EMPTY_CLAY_BUCKET.get(), new FillClayBucketCauldronInteraction(full, BaseClayBucketItem.withBlock(Blocks.POWDER_SNOW, true),  SoundEvents.BUCKET_FILL_POWDER_SNOW));
      CauldronInteraction fillLava = new FillClayBucketCauldronInteraction(state -> true, BaseClayBucketItem.withFluid(Fluids.LAVA, true), SoundEvents.BUCKET_FILL_LAVA);
      CauldronInteraction.LAVA.put(EMPTY_CLAY_BUCKET.get(),         fillLava);
      CauldronInteraction.LAVA.put(CRACKED_EMPTY_CLAY_BUCKET.get(), fillLava);

      // emptying buckets - needed on all as fluid can replace existing
      BiConsumer<ItemLike, CauldronInteraction> addAll = (item, interaction) -> {
        CauldronInteraction.EMPTY.put(item.asItem(), interaction);
        CauldronInteraction.WATER.put(item.asItem(), interaction);
        CauldronInteraction.LAVA.put(item.asItem(), interaction);
        CauldronInteraction.POWDER_SNOW.put(item.asItem(), interaction);
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

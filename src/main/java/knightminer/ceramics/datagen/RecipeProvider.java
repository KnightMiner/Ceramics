package knightminer.ceramics.datagen;

import knightminer.ceramics.Ceramics;
import knightminer.ceramics.Registration;
import knightminer.ceramics.blocks.RainbowPorcelain;
import knightminer.ceramics.recipe.CeramicsTags;
import net.minecraft.advancements.ICriterionInstance;
import net.minecraft.advancements.criterion.ItemPredicate;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.data.CookingRecipeBuilder;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.data.ShapedRecipeBuilder;
import net.minecraft.data.ShapelessRecipeBuilder;
import net.minecraft.data.SingleItemRecipeBuilder;
import net.minecraft.item.DyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.tags.ITag;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.Tags;
import slimeknights.mantle.registration.object.BuildingBlockObject;
import slimeknights.mantle.registration.object.WallBuildingBlockObject;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

@SuppressWarnings("ConstantConditions")
public class RecipeProvider extends net.minecraft.data.RecipeProvider {

  /** Vanilla bricks as a building block object */
  private static final WallBuildingBlockObject BRICKS = new WallBuildingBlockObject(new BuildingBlockObject(Blocks.BRICKS, Blocks.BRICK_SLAB, Blocks.BRICK_STAIRS), Blocks.BRICK_WALL);

  public RecipeProvider(DataGenerator gen) {
    super(gen);
  }

  @Override
  public String getName() {
    return "Ceramics Recipes";
  }

  @Override
  protected void registerRecipes(Consumer<IFinishedRecipe> consumer) {
    // recoloring terracotta
    ICriterionInstance terracottaCriteria = hasItem(CeramicsTags.Items.COLORED_TERRACOTTA);
    Registration.TERRACOTTA.forEach((color, item) ->
      ShapedRecipeBuilder.shapedRecipe(item, 8)
                         .key('B', CeramicsTags.Items.COLORED_TERRACOTTA)
                         .key('D', color.getTag())
                         .setGroup("stained_terracotta")
                         .patternLine("BBB")
                         .patternLine("BDB")
                         .patternLine("BBB")
                         .addCriterion("has_terracotta", terracottaCriteria)
                         .build(consumer, location(item.getRegistryName().getPath() + "_recolor"))
    );

    // crafting porcelain
    ShapelessRecipeBuilder.shapelessRecipe(Registration.UNFIRED_PORCELAIN, 4)
                          .addIngredient(Tags.Items.GEMS_QUARTZ)
                          .addIngredient(Items.CLAY_BALL)
                          .addIngredient(Items.CLAY_BALL)
                          .addIngredient(Items.CLAY_BALL)
                          .addCriterion("has_quartz", hasItem(Tags.Items.GEMS_QUARTZ))
                          .build(consumer);

    // unfired porcelain
    ShapedRecipeBuilder.shapedRecipe(Registration.UNFIRED_PORCELAIN_BLOCK)
                       .key('b', Registration.UNFIRED_PORCELAIN)
                       .patternLine("bb")
                       .patternLine("bb")
                       .addCriterion("has_item", hasItem(Registration.UNFIRED_PORCELAIN))
                       .build(consumer);
    // smelting porcelain
    kilnFurnaceRecipe(consumer, Registration.UNFIRED_PORCELAIN_BLOCK, Registration.PORCELAIN_BLOCK.get(DyeColor.WHITE), 0.3f);

    // colored porcelain
    ICriterionInstance porcelainCriteria = hasItem(Registration.PORCELAIN_BLOCK.get(DyeColor.WHITE));
    Registration.PORCELAIN_BLOCK.forEach((color, item) ->
      ShapedRecipeBuilder.shapedRecipe(item, 8)
                         .key('B', CeramicsTags.Items.PORCELAIN)
                         .key('D', color.getTag())
                         .setGroup(locationString("dye_porcelain"))
                         .patternLine("BBB")
                         .patternLine("BDB")
                         .patternLine("BBB")
                         .addCriterion("has_porcelain", porcelainCriteria)
                         .build(consumer)
    );
    // rainbow porcelain
    kilnFurnaceRecipe(consumer, CeramicsTags.Items.COLORED_PORCELAIN, Registration.RAINBOW_PORCELAIN.get(RainbowPorcelain.RED), 0.1f, location("rainbow_porcelain"));

    // smelt for full rainbow
    ICriterionInstance hasTheRainbow = hasItem(CeramicsTags.Items.RAINBOW_PORCELAIN);
    Registration.RAINBOW_PORCELAIN.forEach((color, item) ->
      SingleItemRecipeBuilder.stonecuttingRecipe(Ingredient.fromTag(CeramicsTags.Items.RAINBOW_PORCELAIN), item)
                             .addCriterion("has_the_rainbow", hasTheRainbow)
                             .build(consumer, item.getRegistryName())
    );

    // bricks
    // vanilla brick block shortcuts
    ICriterionInstance hasClayBrick = hasItem(Items.BRICK);
    ShapedRecipeBuilder.shapedRecipe(Items.BRICK_SLAB)
                       .key('b', Items.BRICK)
                       .patternLine("bb")
                       .addCriterion("has_bricks", hasClayBrick)
                       .setGroup(Items.BRICK_SLAB.getRegistryName().getPath())
                       .build(consumer, location("brick_slab_from_bricks"));
    // stairs shortcut
    ShapedRecipeBuilder.shapedRecipe(Items.BRICK_STAIRS)
                       .key('b', Items.BRICK)
                       .patternLine("b  ")
                       .patternLine("bb ")
                       .patternLine("bbb")
                       .addCriterion("has_bricks", hasClayBrick)
                       .setGroup(Items.BRICK_STAIRS.getRegistryName().getPath())
                       .build(consumer, location("brick_stairs_from_bricks"));
    // block from slab
    ShapedRecipeBuilder.shapedRecipe(Items.BRICKS)
                       .key('B', Items.BRICK_SLAB)
                       .patternLine("B")
                       .patternLine("B")
                       .addCriterion("has_item", hasItem(Items.BRICK_SLAB))
                       .setGroup(Items.BRICKS.getRegistryName().getPath())
                       .build(consumer, location("bricks_from_slab"));

    // dark bricks from smelting bricks
    eachBuilding(BRICKS, Registration.DARK_BRICKS, (input, output) ->
      kilnFurnaceRecipe(consumer, input, output, 0.1f)
    );
    registerSlabStairWall(consumer, Registration.DARK_BRICKS);

    // magma bricks from lava bucket
    addBrickRecipe(consumer, BRICKS, Items.LAVA_BUCKET, Registration.LAVA_BRICKS, "lava");
    registerSlabStairWall(consumer, Registration.LAVA_BRICKS);

    // dragon bricks from dragon's breath
    addBrickRecipe(consumer, BRICKS, Items.DRAGON_BREATH, Registration.DRAGON_BRICKS, "dragon");
    registerSlabStairWall(consumer, Registration.DRAGON_BRICKS);

    // porcelain bricks
    kilnFurnaceRecipe(consumer, Registration.UNFIRED_PORCELAIN, Registration.PORCELAIN_BRICK, 0.3f);
    // using bricks
    ICriterionInstance hasBricks = hasItem(Registration.PORCELAIN_BRICK);
    ShapedRecipeBuilder.shapedRecipe(Registration.PORCELAIN_BRICKS)
                       .key('b', Registration.PORCELAIN_BRICK)
                       .patternLine("bb")
                       .patternLine("bb")
                       .addCriterion("has_bricks", hasBricks)
                       .setGroup(Registration.PORCELAIN_BRICK.getRegistryName().toString())
                       .build(consumer);
    // slab shortcut
    IItemProvider porcelainSlab = Registration.PORCELAIN_BRICKS.getSlab();
    ShapedRecipeBuilder.shapedRecipe(porcelainSlab)
                       .key('b', Registration.PORCELAIN_BRICK)
                       .patternLine("bb")
                       .addCriterion("has_bricks", hasBricks)
                       .setGroup(porcelainSlab.asItem().getRegistryName().toString())
                       .build(consumer, suffix(porcelainSlab, "_from_bricks"));
    // stairs shortcut
    IItemProvider porcelainStairs = Registration.PORCELAIN_BRICKS.getStairs();
    ShapedRecipeBuilder.shapedRecipe(porcelainStairs)
                       .key('b', Registration.PORCELAIN_BRICK)
                       .patternLine("b  ")
                       .patternLine("bb ")
                       .patternLine("bbb")
                       .addCriterion("has_bricks", hasBricks)
                       .setGroup(porcelainStairs.asItem().getRegistryName().toString())
                       .build(consumer, suffix(porcelainStairs, "_from_bricks"));
    registerSlabStairWall(consumer, Registration.PORCELAIN_BRICKS);

    // golden bricks
    addBrickRecipe(consumer, Registration.PORCELAIN_BRICKS, Items.GOLD_NUGGET, Registration.GOLDEN_BRICKS, "gold");
    registerSlabStairWall(consumer, Registration.GOLDEN_BRICKS);

    // marine bricks
    addBrickRecipe(consumer, Registration.PORCELAIN_BRICKS, Items.PRISMARINE_SHARD, Registration.MARINE_BRICKS, "prismarine");
    registerSlabStairWall(consumer, Registration.MARINE_BRICKS);

    // monochrome uses ink, not black dye intentionally
    addBrickRecipe(consumer, Registration.PORCELAIN_BRICKS, Items.INK_SAC, Registration.MONOCHROME_BRICKS, "ink");
    registerSlabStairWall(consumer, Registration.MONOCHROME_BRICKS);

    // rainbow
    eachBuilding(Registration.PORCELAIN_BRICKS, Registration.RAINBOW_BRICKS, (input, output) ->
      kilnFurnaceRecipe(consumer, input, output, 0.1f)
    );
    registerSlabStairWall(consumer, Registration.RAINBOW_BRICKS);

    // buckets
    // unfired
    ShapedRecipeBuilder.shapedRecipe(Registration.UNFIRED_CLAY_BUCKET)
                       .key('c', Items.CLAY_BALL)
                       .patternLine("c c")
                       .patternLine(" c ")
                       .addCriterion("has_clay", hasItem(Items.CLAY_BALL))
                       .build(consumer);
    // fired
    kilnFurnaceRecipe(consumer, Registration.UNFIRED_CLAY_BUCKET, Registration.CLAY_BUCKET, 0.3f);
    // cracked
    kilnFurnaceRecipe(consumer, Registration.CLAY_BUCKET, Registration.CRACKED_CLAY_BUCKET, 0.2f);

    // cistern
    ShapedRecipeBuilder.shapedRecipe(Registration.CLAY_CISTERN, 2)
                       .key('c', Items.CLAY_BALL)
                       .patternLine("c c")
                       .patternLine("c c")
                       .patternLine("c c")
                       .addCriterion("has_clay", hasItem(Items.CLAY_BALL))
                       .build(consumer);
    ShapedRecipeBuilder.shapedRecipe(Registration.UNFIRED_CISTERN, 2)
                       .key('c', Registration.UNFIRED_PORCELAIN)
                       .patternLine("c c")
                       .patternLine("c c")
                       .patternLine("c c")
                       .addCriterion("has_clay", hasItem(Registration.UNFIRED_PORCELAIN))
                       .build(consumer);
    // fired
    kilnFurnaceRecipe(consumer, Registration.CLAY_CISTERN, Registration.TERRACOTTA_CISTERN, 0.3f);
    kilnFurnaceRecipe(consumer, Registration.UNFIRED_CISTERN, Registration.PORCELAIN_CISTERN.get(DyeColor.WHITE), 0.3f);
    // colored
    Registration.COLORED_CISTERN.forEach((color, block) ->
      ShapedRecipeBuilder.shapedRecipe(block, 4)
                         .key('c', CeramicsTags.Items.TERRACOTTA_CISTERNS)
                         .key('d', color.getTag())
                         .patternLine(" c ")
                         .patternLine("cdc")
                         .patternLine(" c ")
                         .addCriterion("has_cistern", hasItem(Registration.TERRACOTTA_CISTERN))
                         .setGroup(Ceramics.locationName("colored_cisterns"))
                         .build(consumer)
    );
    Registration.PORCELAIN_CISTERN.forEach((color, block) ->
      ShapedRecipeBuilder.shapedRecipe(block, 4)
                         .key('c', CeramicsTags.Items.PORCELAIN_CISTERNS)
                         .key('d', color.getTag())
                         .patternLine(" c ")
                         .patternLine("cdc")
                         .patternLine(" c ")
                         .addCriterion("has_cistern", hasItem(Registration.PORCELAIN_CISTERN.get(DyeColor.WHITE)))
                         .setGroup(Ceramics.locationName("porcelain_cisterns"))
                         .build(consumer)
    );

    // gauge
    ShapedRecipeBuilder.shapedRecipe(Registration.GAUGE, 4)
                       .key('b', Items.BRICK)
                       .key('p', Tags.Items.GLASS_PANES_COLORLESS)
                       .patternLine(" b ")
                       .patternLine("bpb")
                       .patternLine(" b ")
                       .addCriterion("has_cistern", hasItem(Registration.TERRACOTTA_CISTERN))
                       .build(consumer);

    // faucet
    ShapedRecipeBuilder.shapedRecipe(Registration.CLAY_FAUCET, 2)
                       .key('c', Items.CLAY_BALL)
                       .patternLine("ccc")
                       .patternLine(" c ")
                       .addCriterion("has_cistern", hasItem(Items.CLAY_BALL))
                       .build(consumer);
    ShapedRecipeBuilder.shapedRecipe(Registration.UNFIRED_FAUCET, 2)
                       .key('c', Registration.UNFIRED_PORCELAIN)
                       .patternLine("ccc")
                       .patternLine(" c ")
                       .addCriterion("has_cistern", hasItem(Registration.TERRACOTTA_CISTERN))
                       .build(consumer);
    kilnFurnaceRecipe(consumer, Registration.CLAY_FAUCET, Registration.TERRACOTTA_FAUCET, 0.3f);
    kilnFurnaceRecipe(consumer, Registration.UNFIRED_FAUCET, Registration.PORCELAIN_FAUCET, 0.3f);

    // channel
    ShapedRecipeBuilder.shapedRecipe(Registration.CLAY_CHANNEL, 3)
                       .key('p', Items.CLAY_BALL)
                       .patternLine("ppp")
                       .patternLine("ppp")
                       .addCriterion("has_cistern", hasItem(Items.CLAY_BALL))
                       .build(consumer);
    ShapedRecipeBuilder.shapedRecipe(Registration.UNFIRED_CHANNEL, 3)
                       .key('p', Registration.UNFIRED_PORCELAIN)
                       .patternLine("ppp")
                       .patternLine("ppp")
                       .addCriterion("has_cistern", hasItem(Registration.TERRACOTTA_CISTERN))
                       .build(consumer);
    kilnFurnaceRecipe(consumer, Registration.CLAY_CHANNEL, Registration.TERRACOTTA_CHANNEL, 0.3f);
    kilnFurnaceRecipe(consumer, Registration.UNFIRED_CHANNEL, Registration.PORCELAIN_CHANNEL, 0.3f);

    // armor
    // clay plates
    ShapedRecipeBuilder.shapedRecipe(Registration.UNFIRED_CLAY_PLATE)
                       .key('c', Items.CLAY_BALL)
                       .patternLine("cc")
                       .addCriterion("has_clay", hasItem(Items.CLAY_BALL))
                       .build(consumer);
    kilnFurnaceRecipe(consumer, Registration.UNFIRED_CLAY_PLATE, Registration.CLAY_PLATE, 0.3f);

    // helmet
    ICriterionInstance hasClayPlate = hasItem(Registration.CLAY_PLATE);
    ShapedRecipeBuilder.shapedRecipe(Registration.CLAY_HELMET)
                       .key('c', Registration.CLAY_PLATE)
                       .patternLine("ccc")
                       .patternLine("c c")
                       .addCriterion("has_plate", hasClayPlate)
                       .build(consumer);
    // chestplate
    ShapedRecipeBuilder.shapedRecipe(Registration.CLAY_CHESTPLATE)
                       .key('c', Registration.CLAY_PLATE)
                       .patternLine("c c")
                       .patternLine("ccc")
                       .patternLine("ccc")
                       .addCriterion("has_plate", hasClayPlate)
                       .build(consumer);
    // leggings
    ShapedRecipeBuilder.shapedRecipe(Registration.CLAY_LEGGINGS)
                       .key('c', Registration.CLAY_PLATE)
                       .patternLine("ccc")
                       .patternLine("c c")
                       .patternLine("c c")
                       .addCriterion("has_plate", hasClayPlate)
                       .build(consumer);
    // boots
    ShapedRecipeBuilder.shapedRecipe(Registration.CLAY_BOOTS)
                       .key('c', Registration.CLAY_PLATE)
                       .patternLine("c c")
                       .patternLine("c c")
                       .addCriterion("has_plate", hasClayPlate)
                       .build(consumer);

    // clay uncrafting
    ShapelessRecipeBuilder.shapelessRecipe(Items.CLAY_BALL, 4)
                          .addIngredient(Items.CLAY)
                          .addCriterion("has_unfired", hasItem(Items.CLAY))
                          .setGroup(locationString("clay_uncrafting"))
                          .build(consumer, location("clay_uncrafting_4"));
    ShapelessRecipeBuilder.shapelessRecipe(Items.CLAY_BALL, 3)
                          .addIngredient(Ingredient.fromItems(Registration.UNFIRED_CLAY_BUCKET, Registration.CLAY_CISTERN))
                          .addCriterion("has_unfired", hasItem(Registration.UNFIRED_CLAY_BUCKET))
                          .setGroup(locationString("clay_uncrafting"))
                          .build(consumer, location("clay_uncrafting_3"));
    ShapelessRecipeBuilder.shapelessRecipe(Items.CLAY_BALL, 2)
                          .addIngredient(Ingredient.fromItems(Registration.UNFIRED_CLAY_PLATE, Registration.CLAY_FAUCET, Registration.CLAY_CHANNEL))
                          .addCriterion("has_unfired", hasItem(Registration.UNFIRED_CLAY_PLATE))
                          .setGroup(locationString("clay_uncrafting"))
                          .build(consumer, location("clay_uncrafting_2"));
    // porcelain uncrafting
    ShapelessRecipeBuilder.shapelessRecipe(Registration.UNFIRED_PORCELAIN, 4)
                          .addIngredient(Registration.UNFIRED_PORCELAIN_BLOCK)
                          .addCriterion("has_unfired", hasItem(Registration.UNFIRED_PORCELAIN_BLOCK))
                          .setGroup(locationString("porcelain_uncrafting"))
                          .build(consumer, location("porcelain_uncrafting_4"));
    ShapelessRecipeBuilder.shapelessRecipe(Registration.UNFIRED_PORCELAIN, 2)
                          .addIngredient(Ingredient.fromItems(Registration.UNFIRED_FAUCET, Registration.UNFIRED_CHANNEL))
                          .addCriterion("has_unfired", hasItem(Registration.UNFIRED_FAUCET))
                          .setGroup(locationString("porcelain_uncrafting"))
                          .build(consumer, location("porcelain_uncrafting_2"));
    ShapelessRecipeBuilder.shapelessRecipe(Registration.UNFIRED_PORCELAIN, 3)
                          .addIngredient(Registration.CLAY_CISTERN)
                          .addCriterion("has_unfired", hasItem(Registration.UNFIRED_FAUCET))
                          .setGroup(locationString("porcelain_uncrafting"))
                          .build(consumer, location("porcelain_uncrafting"));
    // compat, wish there was a better way to do this
    ShapedRecipeBuilder.shapedRecipe(Blocks.CAKE)
                       .key('M', CeramicsTags.Items.MILK_BUCKETS)
                       .key('S', Items.SUGAR)
                       .key('W', Items.WHEAT)
                       .key('E', Items.EGG)
                       .patternLine("MMM")
                       .patternLine("SES")
                       .patternLine("WWW")
                       .addCriterion("has_egg", hasItem(Items.EGG))
                       .build(consumer, location("cake"));

    // kiln - crafting
    ShapedRecipeBuilder.shapedRecipe(Registration.KILN)
                       .key('B', Items.BRICK)
                       .key('F', Items.FURNACE)
                       .key('H', Items.BRICKS)
                       .patternLine("BBB")
                       .patternLine("BFB")
                       .patternLine("HHH")
                       .addCriterion("has_brick", hasItem(Items.BRICK))
                       .build(consumer);

    // add vanilla furnace recipes to the kiln
    // clay
    kilnRecipe(consumer, Items.CLAY_BALL, Items.BRICK, 0.3f);
    kilnRecipe(consumer, Blocks.CLAY, Blocks.TERRACOTTA, 0.3f);
    Registration.TERRACOTTA.forEach((color, input) ->
      kilnRecipe(consumer, input, GLAZED_TERRACOTTA.get(color), 0.1f)
    );
    // sand and glass
    kilnRecipe(consumer, ItemTags.SAND, Blocks.GLASS, 0.1f);
    kilnRecipe(consumer, Blocks.SANDSTONE, Blocks.SMOOTH_SANDSTONE, 0.1f);
    kilnRecipe(consumer, Blocks.RED_SANDSTONE, Blocks.SMOOTH_RED_SANDSTONE, 0.1f);
    // rock
    kilnRecipe(consumer, Blocks.COBBLESTONE, Blocks.STONE, 0.1f);
    kilnRecipe(consumer, Blocks.STONE, Blocks.SMOOTH_STONE, 0.1f);
    kilnRecipe(consumer, Blocks.STONE_BRICKS, Blocks.CRACKED_STONE_BRICKS, 0.1f);
    kilnRecipe(consumer, Blocks.NETHERRACK, Items.NETHER_BRICK, 0.1f);
    kilnRecipe(consumer, Blocks.QUARTZ_BLOCK, Blocks.SMOOTH_QUARTZ, 0.1f);

    // add recipes to smelt slabs and stairs directly for relevant blocks
    // sand
    kilnFurnaceRecipe(consumer, Blocks.SANDSTONE_SLAB, Blocks.SMOOTH_SANDSTONE_SLAB, 0.1f);
    kilnFurnaceRecipe(consumer, Blocks.SANDSTONE_STAIRS, Blocks.SMOOTH_SANDSTONE_STAIRS, 0.1f);
    kilnFurnaceRecipe(consumer, Blocks.RED_SANDSTONE_SLAB, Blocks.SMOOTH_RED_SANDSTONE_SLAB, 0.1f);
    kilnFurnaceRecipe(consumer, Blocks.RED_SANDSTONE_STAIRS, Blocks.SMOOTH_RED_SANDSTONE_STAIRS, 0.1f);
    // rock
    kilnFurnaceRecipe(consumer, Blocks.COBBLESTONE_SLAB, Blocks.STONE_SLAB, 0.1f);
    kilnFurnaceRecipe(consumer, Blocks.COBBLESTONE_STAIRS, Blocks.STONE_STAIRS, 0.1f);
    kilnFurnaceRecipe(consumer, Blocks.STONE_SLAB, Blocks.SMOOTH_STONE_SLAB, 0.1f);
    kilnFurnaceRecipe(consumer, Blocks.QUARTZ_STAIRS, Blocks.SMOOTH_QUARTZ_STAIRS, 0.1f);
  }


  /* Location helpers */

  /**
   * Gets a resource location for Ceramics
   * @param id  Location path
   * @return  Location for Ceramics
   */
  private static ResourceLocation location(String id) {
    return new ResourceLocation(Ceramics.MOD_ID, id);
  }

  /**
   * Gets a resource location as a string for Ceramics
   * @param id  Location path
   * @return  String location for Ceramics
   */
  private static String locationString(String id) {
    return Ceramics.MOD_ID + ":" + id;
  }

  /**
   * Suffixes the resource location path with the given value
   * @param loc     Location to suffix
   * @param suffix  Suffix value
   * @return  Resource location path
   */
  private static ResourceLocation suffix(ResourceLocation loc, String suffix) {
    return new ResourceLocation(loc.getNamespace(), loc.getPath() + suffix);
  }

  /**
   * Suffixes the item's resource location with the given value
   * @param item    Item to suffix
   * @param suffix  Suffix value
   * @return  Resource location path
   */
  private static ResourceLocation suffix(IItemProvider item, String suffix) {
    return suffix(item.asItem().getRegistryName(), suffix);
  }


  /* Enum blocks */
  /** Map of color to glazed terracotta types */
  private static final Map<DyeColor,Block> GLAZED_TERRACOTTA;
  static {
    EnumMap<DyeColor,Block> map = new EnumMap<>(DyeColor.class);
    map.put(DyeColor.WHITE,      Blocks.WHITE_GLAZED_TERRACOTTA);
    map.put(DyeColor.ORANGE,     Blocks.ORANGE_GLAZED_TERRACOTTA);
    map.put(DyeColor.MAGENTA,    Blocks.MAGENTA_GLAZED_TERRACOTTA);
    map.put(DyeColor.LIGHT_BLUE, Blocks.LIGHT_BLUE_GLAZED_TERRACOTTA);
    map.put(DyeColor.YELLOW,     Blocks.YELLOW_GLAZED_TERRACOTTA);
    map.put(DyeColor.LIME,       Blocks.LIME_GLAZED_TERRACOTTA);
    map.put(DyeColor.PINK,       Blocks.PINK_GLAZED_TERRACOTTA);
    map.put(DyeColor.GRAY,       Blocks.GRAY_GLAZED_TERRACOTTA);
    map.put(DyeColor.LIGHT_GRAY, Blocks.LIGHT_GRAY_GLAZED_TERRACOTTA);
    map.put(DyeColor.CYAN,       Blocks.CYAN_GLAZED_TERRACOTTA);
    map.put(DyeColor.PURPLE,     Blocks.PURPLE_GLAZED_TERRACOTTA);
    map.put(DyeColor.BLUE,       Blocks.BLUE_GLAZED_TERRACOTTA);
    map.put(DyeColor.BROWN,      Blocks.BROWN_GLAZED_TERRACOTTA);
    map.put(DyeColor.GREEN,      Blocks.GREEN_GLAZED_TERRACOTTA);
    map.put(DyeColor.RED,        Blocks.RED_GLAZED_TERRACOTTA);
    map.put(DyeColor.BLACK,      Blocks.BLACK_GLAZED_TERRACOTTA);
    GLAZED_TERRACOTTA = map;
  }


  /* Iteration helpers */

  /**
   * Runs the consumer once for each of block, slab, stairs, and wall
   * @param input      Recipe input for consumer
   * @param output     Recipe output for consumer
   * @param consumer   Consumer to create recipes
   */
  private void eachBuilding(WallBuildingBlockObject input, WallBuildingBlockObject output, BiConsumer<IItemProvider,IItemProvider> consumer) {
    consumer.accept(input.asItem(), output.asItem());
    consumer.accept(input.getSlab(), output.getSlab());
    consumer.accept(input.getStairs(), output.getStairs());
    consumer.accept(input.getWall(), output.getWall());
  }

  /**
   * Registers generic building block recipes
   * @param consumer  Recipe consumer
   * @param building  Building object instance
   */
  private void registerSlabStairWall(Consumer<IFinishedRecipe> consumer, WallBuildingBlockObject building) {
    Item item = building.asItem();
    ResourceLocation location = item.getRegistryName();
    ICriterionInstance hasBuilding = hasItem(ItemPredicate.Builder.create().item(item).build());
    Ingredient ingredient = Ingredient.fromItems(item);

    // slab
    IItemProvider slab = building.getSlab();
    ShapedRecipeBuilder.shapedRecipe(slab, 6)
        .key('B', item)
        .patternLine("BBB")
        .addCriterion("has_item", hasBuilding)
        .setGroup(slab.asItem().getRegistryName().toString())
        .build(consumer, suffix(location, "_slab_crafting"));
    SingleItemRecipeBuilder.stonecuttingRecipe(ingredient, slab, 2)
                           .addCriterion("has_item", hasBuilding)
                           .build(consumer, suffix(location, "_slab_stonecutter"));

    // stairs
    IItemProvider stairs = building.getStairs();
    ShapedRecipeBuilder.shapedRecipe(stairs, 4)
        .key('B', item)
        .patternLine("B  ")
        .patternLine("BB ")
        .patternLine("BBB")
        .addCriterion("has_item", hasBuilding)
        .setGroup(stairs.asItem().getRegistryName().toString())
        .build(consumer, suffix(location, "_stairs_crafting"));
    SingleItemRecipeBuilder.stonecuttingRecipe(ingredient, stairs)
                           .addCriterion("has_item", hasBuilding)
                           .build(consumer, suffix(location, "_stairs_stonecutter"));

    // wall
    IItemProvider wall = building.getWall();
    ShapedRecipeBuilder.shapedRecipe(wall, 6)
        .key('B', item)
        .patternLine("BBB")
        .patternLine("BBB")
        .addCriterion("has_item", hasBuilding)
        .build(consumer, suffix(location, "_wall_crafting"));
    SingleItemRecipeBuilder.stonecuttingRecipe(ingredient, wall)
                           .addCriterion("has_item", hasBuilding)
                           .build(consumer, suffix(location, "_wall_stonecutter"));

    // block from slab, its bricks so its easy
    ShapedRecipeBuilder.shapedRecipe(item)
                       .key('B', slab)
                       .patternLine("B")
                       .patternLine("B")
                       .addCriterion("has_item", hasItem(slab))
                       .setGroup(location.toString())
                       .build(consumer, suffix(location, "_from_slab"));
  }

  /**
   * Add recipes for surrouding an ingredient with a brick to get another brick
   * @param consumer    Recipe consumer
   * @param from        Input bricks
   * @param ingredient  Ingredient to transform
   * @param to          Output bricks
   * @param name        Recipe name
   */
  private void addBrickRecipe(Consumer<IFinishedRecipe> consumer, WallBuildingBlockObject from, Item ingredient, WallBuildingBlockObject to, String name) {
    ICriterionInstance criteria = hasItem(ingredient);
    eachBuilding(from, to, (input, output) ->
      ShapedRecipeBuilder.shapedRecipe(output, 8)
                         .key('B', input)
                         .key('i', ingredient)
                         .patternLine("BBB")
                         .patternLine("BiB")
                         .patternLine("BBB")
                         .addCriterion("has_" + name, criteria)
                         .setGroup(output.asItem().getRegistryName().toString())
                         .build(consumer, suffix(output, "_" + name))
    );
  }

  /* Kiln recipes */

  /**
   * Creates a kiln recipe builder
   * @param input       Recipe input
   * @param output      Recipe output
   * @param experience  Experience earned
   * @param cookTime    Cooking time
   * @return            Builder result
   */
  private static CookingRecipeBuilder kilnRecipe(Ingredient input, IItemProvider output, float experience, int cookTime) {
    return CookingRecipeBuilder.cookingRecipe(input, output, experience, cookTime, Registration.KILN_SERIALIZER.get());
  }

  /**
   * Shortcut to add a kiln recipe
   * @param consumer    Recipe consumer
   * @param input       Recipe input
   * @param criteria    Criteria to unlock the recipe
   * @param output      Recipe output
   * @param experience  Experience earned
   * @param name        Recipe name
   */
  private static void kilnRecipe(Consumer<IFinishedRecipe> consumer, Ingredient input, ICriterionInstance criteria, IItemProvider output, float experience, ResourceLocation name) {
    kilnRecipe(input, output, experience, 100)
        .addCriterion("has_item", criteria)
        .build(consumer, name);
  }

  /**
   * Adds a kiln recipe for vanilla item
   * @param consumer    Recipe consumer
   * @param input       Recipe input item
   * @param output      Recipe output
   * @param experience  Experience earned
   */
  private void kilnRecipe(Consumer<IFinishedRecipe> consumer, IItemProvider input, IItemProvider output, float experience) {
    kilnRecipe(consumer, Ingredient.fromItems(input), hasItem(input), output, experience, location(output.asItem().getRegistryName().getPath() + "_kiln"));
  }

  /**
   * Adds a kiln recipe for vanilla item
   * @param consumer    Recipe consumer
   * @param input       Recipe input tag
   * @param output      Recipe output
   * @param experience  Experience earned
   */
  private void kilnRecipe(Consumer<IFinishedRecipe> consumer, ITag<Item> input, IItemProvider output, float experience) {
    kilnRecipe(consumer, Ingredient.fromTag(input), hasItem(input), output, experience, location(output.asItem().getRegistryName().getPath() + "_kiln"));
  }

  /**
   * Adds a new recipe to both the kiln and the furnace
   * @param consumer    Recipe consumer
   * @param input       Recipe input
   * @param criteria    Criteria to unlock the recipe
   * @param output      Recipe output
   * @param experience  Experience earned
   * @param name        Recipe name
   */
  private static void kilnFurnaceRecipe(Consumer<IFinishedRecipe> consumer, Ingredient input, ICriterionInstance criteria, IItemProvider output, float experience, ResourceLocation name) {
    CookingRecipeBuilder.smeltingRecipe(input, output, experience, 200)
                        .addCriterion("has_item", criteria)
                        .build(consumer, suffix(name, "_smelting"));
    kilnRecipe(consumer, input, criteria, output, experience, suffix(name, "_kiln"));
  }

  /**
   * Adds a new recipe to both the kiln and the furnace using the output's registry name to name the recipe
   * @param consumer    Recipe consumer
   * @param input       Recipe item input
   * @param output      Recipe output
   * @param experience  Experience earned
   * @param name        Recipe name
   */
  private void kilnFurnaceRecipe(Consumer<IFinishedRecipe> consumer, IItemProvider input, IItemProvider output, float experience, ResourceLocation name) {
    kilnFurnaceRecipe(consumer, Ingredient.fromItems(input), hasItem(input), output, experience, name);
  }

  /**
   * Adds a new recipe to both the kiln and the furnace using the output's registry name to name the recipe
   * @param consumer    Recipe consumer
   * @param input       Recipe item input
   * @param output      Recipe output
   * @param experience  Experience earned
   */
  private void kilnFurnaceRecipe(Consumer<IFinishedRecipe> consumer, IItemProvider input, IItemProvider output, float experience) {
    kilnFurnaceRecipe(consumer, input, output, experience, location(output.asItem().getRegistryName().getPath()));
  }

  /**
   * Adds a new recipe to both the kiln and the furnace using the output's registry name to name the recipe
   * @param consumer    Recipe consumer
   * @param input       Recipe tag input
   * @param output      Recipe output
   * @param experience  Experience earned
   * @param name        Recipe name
   */
  private void kilnFurnaceRecipe(Consumer<IFinishedRecipe> consumer, ITag<Item> input, IItemProvider output, float experience, ResourceLocation name) {
    kilnFurnaceRecipe(consumer, Ingredient.fromTag(input), hasItem(input), output, experience, name);
  }
}

package knightminer.ceramics.datagen;

import knightminer.ceramics.Ceramics;
import knightminer.ceramics.Registration;
import knightminer.ceramics.recipe.CeramicsTags;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.BlockTagsProvider;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.data.ExistingFileHelper;
import slimeknights.mantle.registration.object.EnumObject;
import slimeknights.mantle.registration.object.ItemObject;
import slimeknights.mantle.registration.object.WallBuildingBlockObject;

public class BlockTagProvider extends BlockTagsProvider {
  public BlockTagProvider(DataGenerator gen, ExistingFileHelper helper) {
    super(gen, Ceramics.MOD_ID, helper);
  }

  @Override
  public String getName() {
    return "Ceramics Block Tags";
  }

  @Override
  protected void addTags() {
    // vanilla colored terracotta
    TagsProvider.TagAppender<Block> coloredTerracotta = this.tag(CeramicsTags.Blocks.COLORED_TERRACOTTA);
    Registration.TERRACOTTA.values().forEach(coloredTerracotta::add);

    // porcelain
    this.tag(BlockTags.ENDERMAN_HOLDABLE).add(Registration.UNFIRED_PORCELAIN_BLOCK.get());
    TagsProvider.TagAppender<Block> coloredPorcelain = this.tag(CeramicsTags.Blocks.COLORED_PORCELAIN);
    Registration.PORCELAIN_BLOCK.forEach((color, block) -> {
      if (color != DyeColor.WHITE) {
        coloredPorcelain.add(block);
      }
    });
    this.tag(CeramicsTags.Blocks.PORCELAIN)
        .add(Registration.PORCELAIN_BLOCK.get(DyeColor.WHITE))
        .addTag(CeramicsTags.Blocks.COLORED_PORCELAIN);

    // rainbow porcelain
    TagsProvider.TagAppender<Block> rainbow = this.tag(CeramicsTags.Blocks.RAINBOW_PORCELAIN);
    Registration.RAINBOW_PORCELAIN.values().forEach(rainbow::add);

    // bricks
    this.tag(CeramicsTags.Blocks.BRICKS).add(
        // clay
        Blocks.BRICKS,
        Registration.DARK_BRICKS.get(),
        Registration.DRAGON_BRICKS.get(),
        Registration.LAVA_BRICKS.get(),
        // porcelain
        Registration.PORCELAIN_BRICKS.get(),
        Registration.GOLDEN_BRICKS.get(),
        Registration.MARINE_BRICKS.get(),
        Registration.MONOCHROME_BRICKS.get(),
        Registration.RAINBOW_BRICKS.get()
    );
    this.tag(BlockTags.WALLS).add(
        // clay
        Registration.DARK_BRICKS.getWall(),
        Registration.DRAGON_BRICKS.getWall(),
        Registration.LAVA_BRICKS.getWall(),
        // porcelain
        Registration.PORCELAIN_BRICKS.getWall(),
        Registration.GOLDEN_BRICKS.getWall(),
        Registration.MARINE_BRICKS.getWall(),
        Registration.MONOCHROME_BRICKS.getWall(),
        Registration.RAINBOW_BRICKS.getWall()
    );
    // blocks that cisterns connect to
    this.tag(CeramicsTags.Blocks.CISTERN_CONNECTIONS)
        .add(Registration.TERRACOTTA_GAUGE.get(), Registration.PORCELAIN_GAUGE.get(),
             Registration.CLAY_FAUCET.get(), Registration.UNFIRED_FAUCET.get(), Registration.TERRACOTTA_FAUCET.get(), Registration.PORCELAIN_FAUCET.get(),
             Registration.CLAY_CHANNEL.get(), Registration.UNFIRED_CHANNEL.get(), Registration.TERRACOTTA_CHANNEL.get(), Registration.PORCELAIN_CHANNEL.get());
    // list of all terracotta cisterns
    TagsProvider.TagAppender<Block> terracottaCisterns = this.tag(CeramicsTags.Blocks.TERRACOTTA_CISTERNS)
                                                         .add(Registration.TERRACOTTA_CISTERN.get());
    Registration.COLORED_CISTERN.forEach(block -> terracottaCisterns.add(block));
    TagsProvider.TagAppender<Block> porcelainCisterns = this.tag(CeramicsTags.Blocks.PORCELAIN_CISTERNS);
    Registration.PORCELAIN_CISTERN.forEach(block -> porcelainCisterns.add(block));

    // pickaxe
    addTag(BlockTags.MINEABLE_WITH_PICKAXE, Registration.PORCELAIN_BLOCK, Registration.RAINBOW_PORCELAIN);
    addTag(BlockTags.MINEABLE_WITH_PICKAXE, Registration.KILN);
    addTag(BlockTags.MINEABLE_WITH_PICKAXE, Registration.TERRACOTTA_GAUGE, Registration.PORCELAIN_GAUGE);
    addTag(BlockTags.MINEABLE_WITH_PICKAXE, Registration.TERRACOTTA_CISTERN);
    addTag(BlockTags.MINEABLE_WITH_PICKAXE, Registration.PORCELAIN_CISTERN, Registration.COLORED_CISTERN);
    addTag(BlockTags.MINEABLE_WITH_PICKAXE, Registration.TERRACOTTA_FAUCET, Registration.PORCELAIN_FAUCET);
    addTag(BlockTags.MINEABLE_WITH_PICKAXE, Registration.TERRACOTTA_CHANNEL, Registration.PORCELAIN_CHANNEL);
    addTag(BlockTags.MINEABLE_WITH_PICKAXE,
           Registration.DARK_BRICKS, Registration.LAVA_BRICKS, Registration.DRAGON_BRICKS,
           Registration.PORCELAIN_BRICKS, Registration.MONOCHROME_BRICKS, Registration.GOLDEN_BRICKS,
           Registration.MARINE_BRICKS, Registration.RAINBOW_BRICKS);
    addTag(Tags.Blocks.NEEDS_GOLD_TOOL, Registration.GOLDEN_BRICKS);
    // shovel
    addTag(BlockTags.MINEABLE_WITH_SHOVEL, Registration.UNFIRED_PORCELAIN_BLOCK);
    addTag(BlockTags.MINEABLE_WITH_SHOVEL, Registration.CLAY_CISTERN, Registration.UNFIRED_CISTERN);
    addTag(BlockTags.MINEABLE_WITH_SHOVEL, Registration.CLAY_FAUCET, Registration.UNFIRED_FAUCET);
    addTag(BlockTags.MINEABLE_WITH_SHOVEL, Registration.CLAY_CHANNEL, Registration.UNFIRED_CHANNEL);
  }

  @SafeVarargs
  private <T extends Block> void addTag(TagKey<Block> tag, ItemObject<T>... blocks) {
    TagsProvider.TagAppender<Block> appender = this.tag(tag);
    for (ItemObject<T> block : blocks) {
      appender.add(block.get());
    }
  }

  @SafeVarargs
  private<T extends Block> void addTag(TagKey<Block> tag, EnumObject<?, T>... blocks) {
    TagsProvider.TagAppender<Block> appender = this.tag(tag);
    for (EnumObject<?, T> object : blocks) {
      object.forEach(block -> appender.add(block));
    }
  }

  private void addTag(TagKey<Block> tag, WallBuildingBlockObject... blocks) {
    TagsProvider.TagAppender<Block> appender = this.tag(tag);
    for (WallBuildingBlockObject object : blocks) {
      object.values().forEach(appender::add);
    }
  }
}

package knightminer.ceramics;

import knightminer.ceramics.blocks.RainbowPorcelain;
import knightminer.ceramics.blocks.TooltipBlock;
import knightminer.ceramics.registration.BlockDeferredRegister;
import knightminer.ceramics.registration.BlockItemObject;
import knightminer.ceramics.registration.EnumBlockObject;
import knightminer.ceramics.registration.ItemDeferredRegsister;
import knightminer.ceramics.registration.ItemObject;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.item.BlockItem;
import net.minecraft.item.DyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

import java.util.EnumMap;
import java.util.function.Supplier;

@Mod.EventBusSubscriber(modid=Ceramics.MOD_ID, bus=Bus.FORGE)
public class Registration {

  /** Registers blocks */
  private static final BlockDeferredRegister BLOCK_REGISTRY = new BlockDeferredRegister(Ceramics.MOD_ID);
  private static final ItemDeferredRegsister ITEM_REGISTRY = new ItemDeferredRegsister(Ceramics.MOD_ID);

  public static void init() {
    BLOCK_REGISTRY.init();
    ITEM_REGISTRY.init();
  }

  /** Creative tab for all of Ceramics */
  private static final ItemGroup GROUP = new ItemGroup(Ceramics.MOD_ID) {
    @Override
    @OnlyIn(Dist.CLIENT)
    public ItemStack createIcon() {
      return new ItemStack(UNFIRED_PORCELAIN);
    }
  };
  private static final Item.Properties GROUP_PROPS = new Item.Properties().group(GROUP);

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


  /* items */
  public static final ItemObject<Item> UNFIRED_PORCELAIN = ITEM_REGISTRY.register("unfired_porcelain", GROUP_PROPS);
}

package knightminer.ceramics;

import knightminer.ceramics.registration.BlockDeferredRegister;
import knightminer.ceramics.registration.EnumBlockObject;
import knightminer.ceramics.registration.ItemDeferredRegsister;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.DyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
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
      return new ItemStack(Items.BRICK);
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
}

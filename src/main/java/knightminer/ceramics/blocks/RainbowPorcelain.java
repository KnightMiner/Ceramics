package knightminer.ceramics.blocks;

import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.material.MapColor;

import java.util.Locale;

/**
 * Enum for all the different rainbow porcelain colors
 */
public enum RainbowPorcelain implements StringRepresentable {
  RED(MapColor.COLOR_RED),
  ORANGE(MapColor.COLOR_ORANGE),
  YELLOW(MapColor.COLOR_YELLOW),
  GREEN(MapColor.COLOR_GREEN),
  CYAN(MapColor.COLOR_CYAN),
  BLUE(MapColor.COLOR_BLUE),
  PURPLE(MapColor.COLOR_PURPLE),
  MAGENTA(MapColor.COLOR_MAGENTA);

  private final MapColor color;
  private final String name;
  RainbowPorcelain(MapColor color) {
    this.color = color;
    this.name = this.name().toLowerCase(Locale.US);
  }

  /** {@return MapColor for this color} */
  public MapColor getColor() {
    return color;
  }

  @Override
  public String toString() {
    return name;
  }

  @Override
  public String getSerializedName() {
    return name;
  }
}

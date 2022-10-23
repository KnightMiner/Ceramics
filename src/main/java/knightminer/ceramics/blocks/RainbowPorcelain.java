package knightminer.ceramics.blocks;

import net.minecraft.block.material.MaterialColor;
import net.minecraft.util.IStringSerializable;

import java.util.Locale;

/**
 * Enum for all the different rainbow porcelain colors
 */
public enum RainbowPorcelain implements IStringSerializable {
  RED(MaterialColor.COLOR_RED),
  ORANGE(MaterialColor.COLOR_ORANGE),
  YELLOW(MaterialColor.COLOR_YELLOW),
  GREEN(MaterialColor.COLOR_GREEN),
  CYAN(MaterialColor.COLOR_CYAN),
  BLUE(MaterialColor.COLOR_BLUE),
  PURPLE(MaterialColor.COLOR_PURPLE),
  MAGENTA(MaterialColor.COLOR_MAGENTA);

  private final MaterialColor color;
  private final String name;
  RainbowPorcelain(MaterialColor color) {
    this.color = color;
    this.name = this.name().toLowerCase(Locale.US);
  }

  /**
   * Gets the MaterialColor for the given color
   * @return  MaterialColor for this color
   */
  public MaterialColor getColor() {
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

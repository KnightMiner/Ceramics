package knightminer.ceramics.blocks;

import net.minecraft.block.material.MaterialColor;
import net.minecraft.util.IStringSerializable;

import java.util.Locale;

public enum RainbowPorcelain implements IStringSerializable {
  RED(MaterialColor.RED),
  ORANGE(MaterialColor.ADOBE),
  YELLOW(MaterialColor.YELLOW),
  GREEN(MaterialColor.GREEN),
  CYAN(MaterialColor.CYAN),
  BLUE(MaterialColor.BLUE),
  PURPLE(MaterialColor.PURPLE),
  MAGENTA(MaterialColor.MAGENTA);

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
  public String getName() {
    return name;
  }
}

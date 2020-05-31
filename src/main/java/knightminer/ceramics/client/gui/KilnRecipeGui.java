package knightminer.ceramics.client.gui;

import net.minecraft.client.gui.recipebook.AbstractRecipeBookGui;
import net.minecraft.item.Item;
import net.minecraft.tileentity.AbstractFurnaceTileEntity;

import java.util.Set;

public class KilnRecipeGui extends AbstractRecipeBookGui {
  @Override
  protected boolean func_212962_b() {
    return this.recipeBook.func_216762_h();
  }

  @Override
  protected void func_212959_a(boolean p_212959_1_) {
    this.recipeBook.func_216760_h(p_212959_1_);
  }

  @Override
  protected boolean func_212963_d() {
    return this.recipeBook.func_216759_g();
  }

  @Override
  protected void func_212957_c(boolean p_212957_1_) {
    this.recipeBook.func_216757_g(p_212957_1_);
  }

  @Override
  protected String func_212960_g() {
    return "gui.recipebook.toggleRecipes.ceramics.kiln";
  }

  @Override
  protected Set<Item> func_212958_h() {
    return AbstractFurnaceTileEntity.getBurnTimes().keySet();
  }
}

package knightminer.ceramics.client.screen;

import net.minecraft.client.gui.screens.recipebook.AbstractFurnaceRecipeBookComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;

import java.util.Set;

public class KilnRecipeBookComponent extends AbstractFurnaceRecipeBookComponent {
    private static final Component FILTER_NAME = Component.translatable("gui.recipebook.toggleRecipes.ceramics.kiln");

    @Override
    protected Component getRecipeFilterName() {
        return FILTER_NAME;
    }

    @Override
    protected Set<Item> getFuelItems() {
        return AbstractFurnaceBlockEntity.getFuel().keySet();
    }
}

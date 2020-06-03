package knightminer.ceramics.registration;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.tileentity.TileEntityType.Builder;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Supplier;

public class TileEntityTypeDeferredRegister extends RegisterWrapper<TileEntityType<?>> {

  public TileEntityTypeDeferredRegister(String modID) {
    super(ForgeRegistries.TILE_ENTITIES, modID);
  }

  public <T extends TileEntity> RegistryObject<TileEntityType<T>> register(final String name, final Supplier<Builder<T>> sup) {
    return register.register(name, () -> sup.get().build(null));
  }
}
package knightminer.ceramics.blocks;

import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.util.IStringSerializable;

public interface IBlockEnum<T extends Enum<T> & IStringSerializable & BlockEnumBase.IEnumMeta> {
	public PropertyEnum<T> getMappingProperty();
}

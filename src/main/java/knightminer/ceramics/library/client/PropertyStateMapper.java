package knightminer.ceramics.library.client;

import java.util.LinkedHashMap;

import javax.annotation.Nonnull;

import com.google.common.collect.Maps;

import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.util.ResourceLocation;

/**
 * Maps a single property to multiple blockstate files in order to make the mapping easier to handle
 */
public class PropertyStateMapper extends StateMapperBase {

	private final PropertyEnum<?> prop;
	private final IProperty<?>[] ignore;
	private boolean ignoreAll;

	private String name;

	public PropertyStateMapper(String name, PropertyEnum<?> prop, IProperty<?>... ignore) {
		this.name = name + "_";
		this.prop = prop;
		this.ignoreAll = false;
		this.ignore = ignore;
	}

	public PropertyStateMapper(String name, PropertyEnum<?> prop, boolean ignoreAll) {
		this.name = name + "_";
		this.prop = prop;
		this.ignoreAll = ignoreAll;
		this.ignore = new IProperty<?>[0];
	}

	@Nonnull
	@Override
	protected ModelResourceLocation getModelResourceLocation(@Nonnull IBlockState state) {
		ResourceLocation res = new ResourceLocation(state.getBlock().getRegistryName().getResourceDomain(), name + state.getValue(prop).getName());

		// if we ignore all properites, just use normal
		if(ignoreAll) {
			return new ModelResourceLocation(res, "normal");
		}

		LinkedHashMap<IProperty<?>, Comparable<?>> map = Maps.newLinkedHashMap(state.getProperties());
		map.remove(prop);
		for(IProperty<?> ignored : ignore) {
			map.remove(ignored);
		}
		return new ModelResourceLocation(res, this.getPropertyString(map));
	}

}

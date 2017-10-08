package knightminer.ceramics.library;

import java.util.Locale;

import com.google.common.collect.ImmutableMap;

import knightminer.ceramics.Ceramics;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;

public class Util {
	public static String resource(String name) {
		return String.format("%s:%s", Ceramics.modID, name.toLowerCase(Locale.US));
	}
	public static String prefix(String name) {
		return String.format("%s.%s", Ceramics.modID, name.toLowerCase(Locale.US));
	}

	public static ResourceLocation getResource(String res) {
		return new ResourceLocation(Ceramics.modID, res);
	}

	/* Position helpers */

	private static ImmutableMap<Vec3i, EnumFacing> offsetMap;
	static {
		ImmutableMap.Builder<Vec3i, EnumFacing> builder = ImmutableMap.builder();
		for(EnumFacing facing : EnumFacing.VALUES) {
			builder.put(facing.getDirectionVec(), facing);
		}
		offsetMap = builder.build();
	}

	/**
	 * Gets the offset direction from two blocks
	 * @param offset  Position offset
	 * @return  Direction of the offset, or null if no direction
	 */
	public static EnumFacing facingFromOffset(BlockPos offset) {
		return offsetMap.get(offset);
	}

	/**
	 * Gets the offset direction from two blocks
	 * @param pos       Base position
	 * @param neighbor  Position Neighbor position
	 * @return  Direction of the offset, or null if no direction
	 */
	public static EnumFacing facingFromNeighbor(BlockPos pos, BlockPos neighbor) {
		// neighbor is first. For example, neighbor height is 11, pos is 10, so result is 1 or up
		return facingFromOffset(neighbor.subtract(pos));
	}

	public static boolean clickAABB(AxisAlignedBB aabb, float hitX, float hitY, float hitZ) {
		return aabb.minX <= hitX && hitX <= aabb.maxX
				&& aabb.minY <= hitY && hitY <= aabb.maxY
				&& aabb.minZ <= hitZ && hitZ <= aabb.maxZ;
	}
}

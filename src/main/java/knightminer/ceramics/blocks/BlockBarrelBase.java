package knightminer.ceramics.blocks;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.Lists;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockBarrelBase extends Block {

	public static PropertyBool EXTENSION = PropertyBool.create("extension");
	public BlockBarrelBase(Material material) {
		super(material);
	}

	public boolean isExtension(IBlockState state) {
		return state.getValue(EXTENSION);
	}

	/**
	 * Gets the metadata of the item this Block can drop. This method is called when the block gets destroyed. It
	 * returns the metadata of the dropped item based on the old metadata of the block.
	 */
	@Override
	public int damageDropped(IBlockState state) {
		return getMetaFromState(state);
	}

	/* Block properties */

	/**
	 * Used to determine ambient occlusion and culling when rebuilding chunks for render
	 */
	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}

	@Override
	public boolean isFullCube(IBlockState state) {
		return false;
	}

	@Override
	@Deprecated
	public BlockFaceShape getBlockFaceShape(IBlockAccess world, IBlockState state, BlockPos pos, EnumFacing side) {
		return (side == EnumFacing.DOWN && !isExtension(state)) ? BlockFaceShape.SOLID : BlockFaceShape.UNDEFINED;
	}

	/* Collision box */
	protected static final AxisAlignedBB AABB_BASE       = new AxisAlignedBB(0.125,  0, 0.125,  0.875,  0.0625, 0.875);
	protected static final AxisAlignedBB AABB_WALL_NORTH = new AxisAlignedBB(0.0625, 0, 0.0625, 0.9375, 1,      0.125);
	protected static final AxisAlignedBB AABB_WALL_SOUTH = new AxisAlignedBB(0.0625, 0, 0.875D, 0.9375, 1,      0.9375);
	protected static final AxisAlignedBB AABB_WALL_EAST  = new AxisAlignedBB(0.875,  0, 0.0625, 0.9375, 1,      0.9375);
	protected static final AxisAlignedBB AABB_WALL_WEST  = new AxisAlignedBB(0.0625, 0, 0.0625, 0.125,  1,      0.9375);

	@Override
	public void addCollisionBoxToList(IBlockState state, World world, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, @Nullable Entity entity, boolean p_185477_7_) {
		if(!isExtension(state)) {
			addCollisionBoxToList(pos, entityBox, collidingBoxes, AABB_BASE);
		}
		addCollisionBoxToList(pos, entityBox, collidingBoxes, AABB_WALL_WEST);
		addCollisionBoxToList(pos, entityBox, collidingBoxes, AABB_WALL_NORTH);
		addCollisionBoxToList(pos, entityBox, collidingBoxes, AABB_WALL_EAST);
		addCollisionBoxToList(pos, entityBox, collidingBoxes, AABB_WALL_SOUTH);
	}

	/* Bounds */
	protected static final AxisAlignedBB BOUNDS = new AxisAlignedBB(0.0625, 0, 0.0625, 0.9375, 1, 0.9375);

	@Override
	@Deprecated
	@Nullable
	public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos) {
		return BOUNDS;
	}

	@Override
	@Deprecated
	@SideOnly(Side.CLIENT)
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		return BOUNDS;
	}

	// raytrace stuff
	protected static final AxisAlignedBB[] EXTENSION_BOUNDS = {
			AABB_WALL_NORTH,
			AABB_WALL_SOUTH,
			AABB_WALL_EAST,
			AABB_WALL_WEST
	};

	@Deprecated
	@Override
	public RayTraceResult collisionRayTrace(IBlockState state, @Nonnull World world, @Nonnull BlockPos pos, @Nonnull Vec3d start, @Nonnull Vec3d end) {
		// we don't have any holes in the normal barrel
		if(!isExtension(state)) {
			return super.collisionRayTrace(state, world, pos, start, end);
		}

		// basically the same BlockStairs does
		// Raytrace through all AABBs (plate, legs) and return the nearest one
		List<RayTraceResult> list = Lists.<RayTraceResult>newArrayList();
		for(AxisAlignedBB axisalignedbb : EXTENSION_BOUNDS) {
			list.add(rayTrace(pos, start, end, axisalignedbb));
		}

		RayTraceResult raytraceresult1 = null;
		double d1 = 0.0D;

		for(RayTraceResult raytraceresult : list) {
			if(raytraceresult != null) {
				double d0 = raytraceresult.hitVec.squareDistanceTo(end);

				if(d0 > d1) {
					raytraceresult1 = raytraceresult;
					d1 = d0;
				}
			}
		}

		return raytraceresult1;
	}
}

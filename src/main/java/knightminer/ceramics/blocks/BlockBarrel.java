package knightminer.ceramics.blocks;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.Lists;

import knightminer.ceramics.Ceramics;
import knightminer.ceramics.library.ModIDs;
import knightminer.ceramics.tileentity.TileBarrel;
import knightminer.ceramics.tileentity.TileBarrelBase;
import knightminer.ceramics.tileentity.TileBarrelExtension;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import slimeknights.tconstruct.library.smeltery.IFaucetDepth;

@Optional.Interface(iface="slimeknights.tconstruct.library.smeltery.IFaucetDepth", modid=ModIDs.TINKERS)
public class BlockBarrel extends Block implements ITileEntityProvider, IFaucetDepth {

	public static PropertyBool EXTENSION = PropertyBool.create("extension");

	public BlockBarrel(Material material) {
		super(material);

		this.isBlockContainer = true; // has TE


		this.setHardness(1.25F);
		this.setResistance(7.0F);
		this.setHarvestLevel("pickaxe", 0);
		this.setCreativeTab(Ceramics.tab);
	}

	public BlockBarrel() {
		this(Material.ROCK);

		this.setDefaultState(this.getBlockState().getBaseState().withProperty(EXTENSION, Boolean.FALSE));
	}

	public boolean isExtension(IBlockState state) {
		return state.getValue(EXTENSION);
	}

	/* Tile Entity logic */

	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		if(isExtension(getStateFromMeta(meta))) {
			return new TileBarrelExtension();
		}
		return new TileBarrel();
	}

	// check structure
	@Override
	public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
		TileEntity te = world.getTileEntity(pos);
		// only bases are relevant on placement to check structures since non-bases cannot possibly have a master
		if(te instanceof TileBarrel) {
			((TileBarrel) te).checkBarrelStructure();
		}
		// if we are not a base, try the block below us for either type of barrel
		else {
			te = world.getTileEntity(pos.down());
			if(te instanceof TileBarrelBase) {
				((TileBarrelBase) te).checkBarrelStructure();
			}
		}
	}

	// Extensions have to additionally check when broken to tell the master
	@Override
	public void breakBlock(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull IBlockState state) {
		TileEntity te = worldIn.getTileEntity(pos);
		if(te instanceof TileBarrelExtension) {
			((TileBarrelExtension) te).checkBarrelStructure();
		}

		super.breakBlock(worldIn, pos, state);
		worldIn.removeTileEntity(pos);
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, ItemStack stack, EnumFacing side, float hitX, float hitY, float hitZ) {
		TileEntity te = world.getTileEntity(pos);
		if(te == null || !te.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, side)) {
			return false;
		}

		IFluidHandler fluidHandler = te.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, side);
		if(FluidUtil.interactWithFluidHandler(stack, fluidHandler, player)) {
			return true;
		}

		// otherwise return true if it is a fluid handler to prevent in world placement
		return FluidUtil.getFluidHandler(stack) != null;
	}

	// rain filling!
	/**
	 * Called similar to random ticks, but only when it is raining.
	 */
	@Override
	public void fillWithRain(World world, BlockPos pos) {
		float f = world.getBiome(pos).getFloatTemperature(pos);
		if (world.getBiomeProvider().getTemperatureAtHeight(f, pos.getY()) >= 0.15f) {
			TileEntity te = world.getTileEntity(pos);
			if(te.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, EnumFacing.UP)) {
				IFluidHandler fluidHandler = te.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, EnumFacing.UP);

				// try and fill with water, if it fails nothing is lost
				// note that this is super slow, a cauldron would be faster
				fluidHandler.fill(new FluidStack(FluidRegistry.WATER, 100), true);
			}
		}
	}

	/* Blockstate */

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, EXTENSION);
	}

	@Override
	@Deprecated
	public IBlockState getStateFromMeta(int meta) {
		IBlockState state = this.getDefaultState();
		if(meta == 1) {
			state = state.withProperty(EXTENSION, Boolean.TRUE);
		}

		return state;
	}

	/**
	 * Convert the BlockState into the correct metadata value
	 */
	@Override
	public int getMetaFromState(IBlockState state) {
		if(state.getValue(EXTENSION)) {
			return 1;
		}

		return 0;
	}

	/**
	 * Gets the metadata of the item this Block can drop. This method is called when the block gets destroyed. It
	 * returns the metadata of the dropped item based on the old metadata of the block.
	 */
	@Override
	public int damageDropped(IBlockState state) {
		return getMetaFromState(state);
	}

	/**
	 * returns a list of blocks with the same ID, but different meta (eg: wood returns 4 blocks)
	 */
	@Override
	@SideOnly(Side.CLIENT)
	public void getSubBlocks(Item itemIn, CreativeTabs tab, List<ItemStack> list) {
		list.add(new ItemStack(itemIn, 1, 0));
		list.add(new ItemStack(itemIn, 1, 1));
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
	public boolean isPassable(IBlockAccess worldIn, BlockPos pos) {
		return true;
	}

	@Override
	public boolean hasComparatorInputOverride(IBlockState state) {
		return !isExtension(state);
	}

	@Override
	public int getComparatorInputOverride(IBlockState blockState, World world, BlockPos pos) {
		TileEntity te = world.getTileEntity(pos);
		if(te instanceof TileBarrel) {
			return ((TileBarrel) te).comparatorStrength();
		}

		return 0;
	}

	/* Collision box */
	protected static final AxisAlignedBB AABB_BASE       = new AxisAlignedBB(0.125,  0, 0.125,  0.875,  0.0625, 0.875);
	protected static final AxisAlignedBB AABB_WALL_NORTH = new AxisAlignedBB(0.0625, 0, 0.0625, 0.9375, 1,      0.125);
	protected static final AxisAlignedBB AABB_WALL_SOUTH = new AxisAlignedBB(0.0625, 0, 0.875D, 0.9375, 1,      0.9375);
	protected static final AxisAlignedBB AABB_WALL_EAST  = new AxisAlignedBB(0.875,  0, 0.0625, 0.9375, 1,      0.9375);
	protected static final AxisAlignedBB AABB_WALL_WEST  = new AxisAlignedBB(0.0625, 0, 0.0625, 0.125,  1,      0.9375);

	@Override
	public void addCollisionBoxToList(IBlockState state, World world, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, @Nullable Entity entity) {
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
	public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, World worldIn, BlockPos pos) {
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

	@Optional.Method(modid=ModIDs.TINKERS)
	@Override
	public float getFlowDepth(World world, BlockPos pos, IBlockState state) {
		if(isExtension(state)) {
			TileEntity te = world.getTileEntity(pos);
			if(te instanceof TileBarrelExtension) {
				BlockPos master = ((TileBarrelExtension)te).getMasterPos();
				if(master != null) {
					return 0.9375f + pos.getY() - master.getY();
				}
			}

			return 1;
		} else {
			// main barrel is just a flat return
			return 0.9375f;
		}
	}
}

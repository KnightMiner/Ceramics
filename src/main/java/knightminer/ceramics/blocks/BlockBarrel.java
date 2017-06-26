package knightminer.ceramics.blocks;

import javax.annotation.Nonnull;

import knightminer.ceramics.Ceramics;
import knightminer.ceramics.library.Config;
import knightminer.ceramics.library.IFaucetDepthFallback;
import knightminer.ceramics.tileentity.TileBarrel;
import knightminer.ceramics.tileentity.TileBarrelBase;
import knightminer.ceramics.tileentity.TileBarrelExtension;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;

//@Optional.Interface(iface="slimeknights.tconstruct.library.smeltery.IFaucetDepth", modid=ModIDs.TINKERS)
public class BlockBarrel extends BlockBarrelBase implements ITileEntityProvider, IFaucetDepthFallback {

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


	/* Tile Entity logic */

	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		if(isExtension(getStateFromMeta(meta))) {
			return new TileBarrelExtension();
		}
		return new TileBarrel(Fluid.BUCKET_VOLUME * 4);
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
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
		// check the TE
		TileEntity te = world.getTileEntity(pos);
		if(te == null || !te.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, side)) {
			return false;
		}

		IFluidHandler fluidHandler = te.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, side);
		boolean success = FluidUtil.interactWithFluidHandler(player, hand, world, pos, side);

		// display the level of the barrel
		if(!world.isRemote) {
			FluidStack fluid = fluidHandler.getTankProperties()[0].getContents();
			if(fluid == null) {
				player.sendStatusMessage(new TextComponentTranslation("ceramics.barrel.fluid.empty"), true);
			}
			else {
				player.sendStatusMessage(new TextComponentTranslation("ceramics.barrel.fluid.amount", new Object[] {
						fluid.amount,
						fluid.getLocalizedName()
				}), true);
			}
		}

		// otherwise return true if it is a fluid handler to prevent in world placement
		return success || FluidUtil.getFluidHandler(player.getHeldItem(hand)) != null;
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

	/**
	 * Used by the tile entity to determine if this block is a valid extension for the barrel
	 * @param state  State to test
	 * @return  True if the state is a valid extension for this barrel type
	 */
	public boolean isValidExtension(IBlockState state) {
		return state.getBlock() == Ceramics.clayBarrel && state.getValue(EXTENSION) || state.getBlock() == Ceramics.clayBarrelStainedExtension;
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
	 * returns a list of blocks with the same ID, but different meta (eg: wood returns 4 blocks)
	 */
	@Override
	public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> list) {
		if(Config.barrelEnabled) {
			list.add(new ItemStack(this, 1, 0));
			list.add(new ItemStack(this, 1, 1));
		}
	}

	/* Block properties */
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

package knightminer.ceramics.registration;

import net.minecraft.block.Block;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.StairsBlock;
import net.minecraft.block.WallBlock;
import net.minecraft.item.BlockItem;

public class BuildingBlockObject extends BlockItemObject<Block,BlockItem> {
  private final BlockItemObject<SlabBlock,BlockItem> slab;
  private final BlockItemObject<StairsBlock,BlockItem> stairs;
  private final BlockItemObject<WallBlock,BlockItem> wall;

  protected BuildingBlockObject(
      BlockItemObject<Block,BlockItem> block,
      BlockItemObject<SlabBlock,BlockItem> slab,
      BlockItemObject<StairsBlock,BlockItem> stairs,
      BlockItemObject<WallBlock,BlockItem> wall) {
    super(block.block, block.item);
    this.slab = slab;
    this.stairs = stairs;
    this.wall = wall;
  }

  public static BuildingBlockObject fromBlocks(Block block, Block slab, Block stairs, Block wall) {
    return new BuildingBlockObject(
        BlockItemObject.fromBlock(block),
        BlockItemObject.fromBlock((SlabBlock)slab),
        BlockItemObject.fromBlock((StairsBlock)stairs),
        BlockItemObject.fromBlock((WallBlock)wall)
    );
  }

  /** Gets the slab for this block */
  public SlabBlock getSlab() {
    return slab.get();
  }

  /** Gets the stairs for this block */
  public StairsBlock getStairs() {
    return stairs.get();
  }

  /** Gets the wall for this block */
  public WallBlock getWall() {
    return wall.get();
  }

  /** Gets the slab item for this block */
  public BlockItem getSlabItem() {
    return slab.asItem();
  }

  /** Gets the stairs item for this block */
  public BlockItem getStairsItem() {
    return stairs.asItem();
  }

  /** Gets the wall item for this block */
  public BlockItem getWallItem() {
    return wall.asItem();
  }
}

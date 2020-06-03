package knightminer.ceramics.registration.object;

import net.minecraft.block.Block;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.StairsBlock;
import net.minecraft.block.WallBlock;
import net.minecraft.item.Item;

public class WallBuildingBlockObject extends BlockItemObject<Block> {
  private final BlockItemObject<SlabBlock> slab;
  private final BlockItemObject<StairsBlock> stairs;
  private final BlockItemObject<WallBlock> wall;

  public WallBuildingBlockObject(
      BlockItemObject<Block> block,
      BlockItemObject<SlabBlock> slab,
      BlockItemObject<StairsBlock> stairs,
      BlockItemObject<WallBlock> wall) {
    super(block.block, block.item);
    this.slab = slab;
    this.stairs = stairs;
    this.wall = wall;
  }

  public static WallBuildingBlockObject fromBlocks(Block block, Block slab, Block stairs, Block wall) {
    return new WallBuildingBlockObject(
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
  public Item getSlabItem() {
    return slab.asItem();
  }

  /** Gets the stairs item for this block */
  public Item getStairsItem() {
    return stairs.asItem();
  }

  /** Gets the wall item for this block */
  public Item getWallItem() {
    return wall.asItem();
  }
}

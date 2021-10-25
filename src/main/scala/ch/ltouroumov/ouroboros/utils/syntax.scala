package ch.ltouroumov.ouroboros.utils

import net.minecraft.block.BlockState
import net.minecraft.block.pattern.BlockPattern
import net.minecraft.state.BooleanProperty
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

object syntax {

  implicit class BlockPatternOps(val pattern: BlockPattern) extends AnyVal {
    def positions(result: BlockPattern.PatternHelper): Seq[BlockPos] =
      for (
        y <- 0 until pattern.getHeight;
        x <- 0 until pattern.getWidth;
        z <- 0 until pattern.getDepth
      ) yield result.getBlock(x, y, z).getPos

    def blockData(
        result: BlockPattern.PatternHelper,
        world: World
    ): Seq[(BlockPos, BlockState, Option[TileEntity])] =
      positions(result).map(pos => (pos, world.getBlockState(pos), Option(world.getBlockEntity(pos))))
  }

  implicit class BlockStateOps(val state: BlockState) extends AnyVal {
    def setBoolValue(property: BooleanProperty, value: Boolean): BlockState =
      state.setValue(property, Boolean.box(value))
  }

}

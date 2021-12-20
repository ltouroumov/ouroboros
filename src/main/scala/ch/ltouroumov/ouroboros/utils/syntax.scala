package ch.ltouroumov.ouroboros.utils

import net.minecraft.core.BlockPos
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.pattern.BlockPattern
import net.minecraft.world.level.block.state.properties.BooleanProperty

object syntax {

  implicit class BlockPatternOps(val pattern: BlockPattern) extends AnyVal {
    def positions(result: BlockPattern.BlockPatternMatch): Seq[BlockPos] =
      for (
        y <- 0 until pattern.getHeight;
        x <- 0 until pattern.getWidth;
        z <- 0 until pattern.getDepth
      ) yield result.getBlock(x, y, z).getPos

    def blockData(
        result: BlockPattern.BlockPatternMatch,
        world: Level
    ): Seq[(BlockPos, BlockState, Option[BlockEntity])] =
      positions(result).map(pos => (pos, world.getBlockState(pos), Option(world.getBlockEntity(pos))))
  }

  implicit class BlockStateOps(val state: BlockState) extends AnyVal {
    def setBoolValue(property: BooleanProperty, value: Boolean): BlockState =
      state.setValue(property, Boolean.box(value))
  }

}

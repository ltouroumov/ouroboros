package ch.ltouroumov.ouroboros.blocks

import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.entity.{BlockEntity, BlockEntityType}
import net.minecraft.world.level.block.state.BlockState

object BaseBlockEntity {
  trait Companion[T <: BlockEntity] {
    def create(blockPos: BlockPos, blockState: BlockState): T
    def entityType: BlockEntityType[T]
  }
}

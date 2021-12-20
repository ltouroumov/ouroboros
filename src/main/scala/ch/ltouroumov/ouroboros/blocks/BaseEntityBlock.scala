package ch.ltouroumov.ouroboros.blocks

import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.EntityBlock
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.{BlockBehaviour, BlockState}

abstract class BaseEntityBlock(properties: BlockBehaviour.Properties) extends BaseBlock(properties) with EntityBlock

object BaseEntityBlock {
  trait Companion[M <: BaseEntityBlock, T <: BlockEntity] extends BaseBlock.Companion[M] {
    def entity(blockPos: BlockPos, blockState: BlockState): T
  }
}

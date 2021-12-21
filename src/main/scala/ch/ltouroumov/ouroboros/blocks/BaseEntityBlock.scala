package ch.ltouroumov.ouroboros.blocks

import net.minecraft.world.level.block.EntityBlock
import net.minecraft.world.level.block.entity.{BlockEntity, BlockEntityType}
import net.minecraft.world.level.block.state.BlockBehaviour

abstract class BaseEntityBlock(properties: BlockBehaviour.Properties) extends BaseBlock(properties) with EntityBlock

object BaseEntityBlock {
  trait Companion[M <: BaseEntityBlock, T <: BlockEntity] extends BaseBlock.Companion[M] {
    def entity: BaseBlockEntity.Companion[T]
    def entityType: BlockEntityType[T] = entity.entityType
  }
}

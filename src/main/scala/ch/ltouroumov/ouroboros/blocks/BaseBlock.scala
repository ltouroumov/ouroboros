package ch.ltouroumov.ouroboros.blocks

import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockBehaviour

abstract class BaseBlock(properties: BlockBehaviour.Properties) extends Block(properties)

object BaseBlock {
  trait Companion[T <: BlockBehaviour] {
    def apply(properties: BlockBehaviour.Properties): T

    def block(): T
  }
}

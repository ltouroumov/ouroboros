package ch.ltouroumov.ouroboros.blocks

import net.minecraft.block.{AbstractBlock, Block}

abstract class BaseBlock(properties: AbstractBlock.Properties) extends Block(properties)

object BaseBlock {
  trait Companion[T <: AbstractBlock] {
    def apply(properties: AbstractBlock.Properties): T

    def block(): T
  }
}

package ch.ltouroumov.ouroboros.blocks

import net.minecraft.block.AbstractBlock
import net.minecraft.tileentity.TileEntity

abstract class BaseEntityBlock(properties: AbstractBlock.Properties) extends BaseBlock(properties)

object BaseEntityBlock {
  trait Companion[M <: BaseEntityBlock, T <: TileEntity] extends BaseBlock.Companion[M] {
    def entity(): T
  }
}

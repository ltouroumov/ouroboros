package ch.ltouroumov.ouroboros.blocks.machine

import ch.ltouroumov.ouroboros.blocks.BaseEntityBlock
import ch.ltouroumov.ouroboros.registry.{BlocksRegistry, TileEntityRegistry}
import net.minecraft.block.AbstractBlock
import net.minecraft.tileentity.TileEntity

class CrusherMachineBlock(properties: AbstractBlock.Properties) extends BaseEntityBlock(properties)

object CrusherMachineBlock extends BaseEntityBlock.Companion[CrusherMachineBlock, CrusherMachineEntity] {
  def apply(properties: AbstractBlock.Properties): CrusherMachineBlock = new CrusherMachineBlock(properties)

  override def block(): CrusherMachineBlock = BlocksRegistry.CRUSHER.get()
  override def entity()                     = new CrusherMachineEntity()

}

class CrusherMachineEntity extends TileEntity(TileEntityRegistry.CRUSHER.get()) {}

package ch.ltouroumov.ouroboros.blocks.other

import ch.ltouroumov.ouroboros.blocks.Properties.MachineTier
import ch.ltouroumov.ouroboros.blocks.{BaseBlock, Properties}
import ch.ltouroumov.ouroboros.registry.BlocksRegistry
import net.minecraft.block.{AbstractBlock, Block, BlockState}
import net.minecraft.state.StateContainer

class StructureBlock(properties: AbstractBlock.Properties) extends BaseBlock(properties) {
  registerDefaultState(
    stateDefinition
      .any()
      .setValue(Properties.MACHINE_TIER, MachineTier.T0)
  )

  override def createBlockStateDefinition(builder: StateContainer.Builder[Block, BlockState]): Unit =
    builder.add(Properties.MACHINE_TIER)
}

object StructureBlock extends BaseBlock.Companion[StructureBlock] {
  override def apply(properties: AbstractBlock.Properties): StructureBlock = new StructureBlock(properties)

  override def block(): StructureBlock = BlocksRegistry.STRUCTURE.get()
}

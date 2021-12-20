package ch.ltouroumov.ouroboros.blocks.machine

import ch.ltouroumov.ouroboros.blocks.Properties
import ch.ltouroumov.ouroboros.blocks.other.StructureEntity
import ch.ltouroumov.ouroboros.registry.BlockEntityRegistry
import ch.ltouroumov.ouroboros.utils.syntax._
import ch.ltouroumov.ouroboros.utils.{StrictLogging, BlockEntityHelpers}
import net.minecraft.core.BlockPos
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.entity.{BlockEntity, BlockEntityTicker}
import net.minecraft.world.level.block.state.BlockState

class CrusherMachineEntity(blockPos: BlockPos, blockState: BlockState)
    extends BlockEntity(BlockEntityRegistry.CRUSHER.get(), blockPos, blockState)
    with BlockEntityHelpers
    with StrictLogging {

  var _tickCounter: Int         = 0
  var _structure: Seq[BlockPos] = Seq.empty

  def setStructureBlocks(positions: Seq[BlockPos]): CrusherMachineEntity =
    setter { _structure = positions }

  def structureDestroyed(position: BlockPos): Unit = {
    logger.debug(s"Structure destroyed at $position")
    _structure.foreach { pos =>
      getLevel.getBlockEntity(pos) match {
        case te: StructureEntity => te.clearMachinePos()
        case _                   => ()
      }

      getLevel.setBlockAndUpdate(
        pos,
        getLevel
          .getBlockState(pos)
          .setBoolValue(Properties.MACHINE_PART, value = false)
      )
    }
    _structure = Seq.empty
    setChanged()

    getLevel.setBlockAndUpdate(
      getBlockPos,
      getBlockState.setBoolValue(Properties.MACHINE_PART, value = false)
    )
  }
}

object CrusherMachineEntity {

  object ServerTick extends BlockEntityTicker[CrusherMachineEntity] with StrictLogging {
    override def tick(level: Level, pos: BlockPos, state: BlockState, entity: CrusherMachineEntity): Unit = {
      entity._tickCounter += 1

      if (entity._tickCounter >= 20) {
        logger.debug(s"Ticking crusher at $pos, state=$state")
        entity._tickCounter = 0
      }
    }
  }

}

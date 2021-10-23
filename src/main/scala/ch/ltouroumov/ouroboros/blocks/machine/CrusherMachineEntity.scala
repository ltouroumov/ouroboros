package ch.ltouroumov.ouroboros.blocks.machine

import ch.ltouroumov.ouroboros.blocks.Properties
import ch.ltouroumov.ouroboros.blocks.other.StructureEntity
import ch.ltouroumov.ouroboros.registry.TileEntityRegistry
import ch.ltouroumov.ouroboros.utils.{StrictLogging, TileEntityHelpers}
import ch.ltouroumov.ouroboros.utils.syntax._
import net.minecraft.tileentity.{ITickableTileEntity, TileEntity}
import net.minecraft.util.math.BlockPos

class CrusherMachineEntity
    extends TileEntity(TileEntityRegistry.CRUSHER.get())
    with TileEntityHelpers
    with ITickableTileEntity
    with StrictLogging {

  var _tickCounter: Int         = 0
  var _structure: Seq[BlockPos] = Seq.empty

  def setStructureBlocks(positions: Seq[BlockPos]): CrusherMachineEntity =
    setter { _structure = positions }

  def structureDestroyed(position: BlockPos): Unit = {
    logger.debug(s"Structure destroyed at $position")
    _structure.foreach { pos =>
      getLevel
        .getBlockEntity(pos)
        .asInstanceOf[StructureEntity]
        .clearMachinePos()

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

  override def tick(): Unit = {
    _tickCounter += 1

    if (_tickCounter >= 20) {
      logger.debug(s"Ticking crusher at $getBlockPos, state=$getBlockState")
      _tickCounter = 0
    }
  }
}

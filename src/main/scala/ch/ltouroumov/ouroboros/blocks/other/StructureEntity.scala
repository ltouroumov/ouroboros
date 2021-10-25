package ch.ltouroumov.ouroboros.blocks.other

import ch.ltouroumov.ouroboros.registry.TileEntityRegistry
import ch.ltouroumov.ouroboros.utils.{StrictLogging, TileEntityHelpers}
import net.minecraft.block.BlockState
import net.minecraft.nbt.{CompoundNBT, NBTUtil}
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.math.BlockPos

class StructureEntity extends TileEntity(TileEntityRegistry.STRUCTURE.get()) with TileEntityHelpers with StrictLogging {

  private var _machinePos: Option[BlockPos] = None

  def machinePos: Option[BlockPos] = _machinePos

  def setMachinePos(pos: BlockPos): StructureEntity =
    setter { _machinePos = Some(pos) }

  def clearMachinePos(): StructureEntity =
    setter { _machinePos = None }

  override def load(blockState: BlockState, nbtData: CompoundNBT): Unit = {
    super.load(blockState, nbtData)
    Option(nbtData.get("MachinePos")) match {
      case Some(posData: CompoundNBT) =>
        _machinePos = Some(NBTUtil.readBlockPos(posData))
      case Some(other) =>
        logger.warn(s"Got $other, expected CompoundNBT")
        _machinePos = None
      case None =>
        _machinePos = None
    }
  }

  override def save(nbtData: CompoundNBT): CompoundNBT = {
    super.save(nbtData)
    _machinePos match {
      case Some(pos) => nbtData.put("MachinePos", NBTUtil.writeBlockPos(pos))
      case None      => nbtData.remove("MachinePos")
    }
    nbtData
  }
}

package ch.ltouroumov.ouroboros.blocks.other

import ch.ltouroumov.ouroboros.registry.BlockEntityRegistry
import ch.ltouroumov.ouroboros.utils.{StrictLogging, BlockEntityHelpers}
import net.minecraft.core.BlockPos
import net.minecraft.nbt.{CompoundTag, NbtUtils}
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState

class StructureEntity(blockPos: BlockPos, blockState: BlockState)
    extends BlockEntity(BlockEntityRegistry.STRUCTURE.get(), blockPos, blockState)
    with BlockEntityHelpers
    with StrictLogging {

  private var _machinePos: Option[BlockPos] = None

  def machinePos: Option[BlockPos] = _machinePos

  def hasMachinePos(pos: BlockPos): Boolean =
    _machinePos.contains(pos)

  def setMachinePos(pos: BlockPos): StructureEntity =
    setter { _machinePos = Some(pos) }

  def clearMachinePos(): StructureEntity =
    setter { _machinePos = None }

  override def load(nbtData: CompoundTag): Unit = {
    super.load(nbtData)
    Option(nbtData.get("MachinePos")) match {
      case Some(posData: CompoundTag) =>
        _machinePos = Some(NbtUtils.readBlockPos(posData))
      case Some(other) =>
        logger.warn(s"Got $other, expected CompoundNBT")
        _machinePos = None
      case None =>
        _machinePos = None
    }
  }

  override def save(nbtData: CompoundTag): CompoundTag = {
    super.save(nbtData)
    _machinePos match {
      case Some(pos) => nbtData.put("MachinePos", NbtUtils.writeBlockPos(pos))
      case None      => nbtData.remove("MachinePos")
    }
    nbtData
  }
}

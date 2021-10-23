package ch.ltouroumov.ouroboros.blocks.machine

import ch.ltouroumov.ouroboros.registry.TileEntityRegistry
import ch.ltouroumov.ouroboros.utils.StrictLogging
import net.minecraft.tileentity.{ITickableTileEntity, TileEntity}

class CrusherMachineEntity
    extends TileEntity(TileEntityRegistry.CRUSHER.get())
    with ITickableTileEntity
    with StrictLogging {

  var _tickCounter: Int = 0

  override def tick(): Unit = {
    _tickCounter += 1

    if (_tickCounter >= 20) {
      logger.debug(s"Ticking crusher at $getBlockPos, state=$getBlockState")
      _tickCounter = 0
    }
  }
}

package ch.ltouroumov.ouroboros.blocks.machine

import ch.ltouroumov.ouroboros.registry.TileEntityRegistry
import ch.ltouroumov.ouroboros.utils.StrictLogging
import net.minecraft.tileentity.{ITickableTileEntity, TileEntity}

class CrusherMachineEntity
    extends TileEntity(TileEntityRegistry.CRUSHER.get())
    with ITickableTileEntity
    with StrictLogging {
  override def tick(): Unit = {
    logger.debug(s"Ticking crusher at $getBlockPos, state=$getBlockState")
  }
}

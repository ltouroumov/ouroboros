package ch.ltouroumov.ouroboros.utils

import net.minecraft.tileentity.TileEntity

trait TileEntityHelpers { self: TileEntity =>

  def setter(work: => Unit): self.type = {
    work
    setChanged()
    self
  }

}

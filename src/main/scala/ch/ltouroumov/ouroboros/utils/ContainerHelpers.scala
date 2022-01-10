package ch.ltouroumov.ouroboros.utils

import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.inventory.{AbstractContainerMenu, Slot}

trait ContainerHelpers {

  protected def playerInventoryMenu(inventory: Inventory, vOffset: Int)(addSlot: Slot => Unit): Unit = {
    for (row <- 0 until 3) {
      for (col <- 0 until 9) {
        addSlot(new Slot(inventory, col + row * 9 + 9, 8 + col * 18, 103 + row * 18 + vOffset))
      }
    }

    for (col <- 0 until 9) {
      addSlot(new Slot(inventory, col, 8 + col * 18, 161 + vOffset))
    }
  }

}

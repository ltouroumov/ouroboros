package ch.ltouroumov.ouroboros.blocks.crusher

import ch.ltouroumov.ouroboros.registry.ContainerRegistry
import ch.ltouroumov.ouroboros.utils.ContainerHelpers
import ch.ltouroumov.ouroboros.utils.syntax._
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.world.entity.player.{Inventory, Player}
import net.minecraft.world.inventory.{AbstractContainerMenu, Slot}

class CrusherMachineMenu(windowId: Int, val playerInventory: Inventory, val entity: CrusherMachineEntity)
    extends AbstractContainerMenu(ContainerRegistry.CRUSHER_MENU.get(), windowId)
    with ContainerHelpers {

  addSlot(new Slot(entity, 0, 80, 35))
  playerInventoryMenu(playerInventory, vOffset = 100)

  override def stillValid(player: Player): Boolean =
    entity.stillValid(player)
}

object CrusherMachineMenu {

  def apply(windowId: Int, playerInventory: Inventory, entity: CrusherMachineEntity): CrusherMachineMenu =
    new CrusherMachineMenu(windowId, playerInventory, entity)

  def apply(windowId: Int, playerInventory: Inventory, data: FriendlyByteBuf): CrusherMachineMenu = {
    val entityPos = data.readBlockPos()
    val entityOpt: Option[CrusherMachineEntity] =
      playerInventory.player.level.getBlockEntityOpt(entityPos, CrusherMachineEntity)

    entityOpt match {
      case Some(entity) => new CrusherMachineMenu(windowId, playerInventory, entity)
      case None         => throw new IllegalStateException(s"CrusherMachineEntity not found at $entityPos")
    }
  }

}

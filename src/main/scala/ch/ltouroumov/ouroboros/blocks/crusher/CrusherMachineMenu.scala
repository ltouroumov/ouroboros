package ch.ltouroumov.ouroboros.blocks.crusher

import ch.ltouroumov.ouroboros.registry.ContainerRegistry
import ch.ltouroumov.ouroboros.utils.ContainerHelpers
import ch.ltouroumov.ouroboros.utils.syntax._
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.world.Container
import net.minecraft.world.entity.player.{Inventory, Player}
import net.minecraft.world.inventory.{AbstractContainerMenu, DataSlot, Slot}

class CrusherMachineMenu(windowId: Int, val playerInventory: Inventory, val entity: CrusherMachineEntity)
    extends AbstractContainerMenu(ContainerRegistry.CRUSHER_MENU.get(), windowId)
    with ContainerHelpers {

  import ch.ltouroumov.ouroboros.utils.MenuBuilder._

  createContainer(addSlot)(
    VStack(
      Spacer(height = 22),
      HStack(
        Spacer(width = 19),
        Grid(rows = 3, cols = 3).addAll(
          for (slot <- 0 until 9) yield ItemSlotWrapper(entity, slot)
        ),
        Spacer(width = 36),
        Grid(rows = 3, cols = 3).addAll(
          for (slot <- 9 until 18) yield ItemSlotWrapper(entity, slot)
        )
      ),
      Spacer(height = 5),
      HStack(
        Spacer(width = 19),
        Grid(rows = 1, cols = 3).addAll(
          for (slot <- 18 until 21) yield ItemSlotWrapper(entity, slot)
        )
      ),
      Spacer(height = 27)
    ).addAll(playerInventoryViews(playerInventory))
  )

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

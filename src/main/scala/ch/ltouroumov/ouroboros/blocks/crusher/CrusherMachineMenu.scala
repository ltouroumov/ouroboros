package ch.ltouroumov.ouroboros.blocks.crusher

import ch.ltouroumov.ouroboros.blocks.crusher.CrusherMachineEntity.{
  ACTIVE_RECIPE_SLOT,
  INPUT_SLOTS,
  OUTPUT_SLOTS,
  UPGRADE_SLOTS
}
import ch.ltouroumov.ouroboros.blocks.crusher.CrusherMachineMenu.{ActiveRecipeSlot, OutputSlot}
import ch.ltouroumov.ouroboros.registry.ContainerRegistry
import ch.ltouroumov.ouroboros.utils.ContainerHelpers
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.world.entity.player.{Inventory, Player}
import net.minecraft.world.inventory.{AbstractContainerMenu, ContainerData, SimpleContainerData, Slot}
import net.minecraft.world.item.ItemStack
import net.minecraft.world.{Container, SimpleContainer}

class CrusherMachineMenu(
    windowId: Int,
    val playerInventory: Inventory,
    val container: Container,
    val data: ContainerData
) extends AbstractContainerMenu(ContainerRegistry.CRUSHER_MENU.get(), windowId)
    with ContainerHelpers {

  import ch.ltouroumov.ouroboros.utils.MenuBuilder._

  addDataSlots(data)
  createContainer(addSlot)(
    VStack(
      Spacer(height = 22),
      HStack(
        Spacer(width = 19),
        Grid(rows = 3, cols = 3).addAll(
          INPUT_SLOTS.map(ItemSlotWrapper.of(container, _))
        ),
        Spacer(width = 9),
        ItemSlotWrapper(new ActiveRecipeSlot(container, ACTIVE_RECIPE_SLOT, _, _)),
        Spacer(width = 9),
        Grid(rows = 3, cols = 3).addAll(
          OUTPUT_SLOTS.map(slot => ItemSlotWrapper(new OutputSlot(container, slot, _, _)))
        )
      ),
      Spacer(height = 5),
      HStack(
        Spacer(width = 19),
        Grid(rows = 1, cols = 3).addAll(UPGRADE_SLOTS.map(ItemSlotWrapper.of(container, _)))
      ),
      Spacer(height = 27)
    ).addAll(playerInventoryViews(playerInventory))
  )

  def processingProgress: Int = {
    val processProgress = data.get(0)
    val processTime     = data.get(1)

    if (processProgress > 0 && processTime > 0)
      (processProgress * 30) / processTime
    else 0
  }

  override def stillValid(player: Player): Boolean =
    container.stillValid(player)
}

object CrusherMachineMenu {

  def apply(windowId: Int, playerInventory: Inventory, container: Container, data: ContainerData): CrusherMachineMenu =
    new CrusherMachineMenu(windowId, playerInventory, container, data)

  def apply(windowId: Int, playerInventory: Inventory, data: FriendlyByteBuf): CrusherMachineMenu = {
    new CrusherMachineMenu(
      windowId,
      playerInventory,
      new SimpleContainer(CrusherMachineEntity.ContentsSize),
      new SimpleContainerData(CrusherMachineEntity.SlotsSize)
    )
  }

  class ActiveRecipeSlot(container: Container, slot: Int, x: Int, y: Int) extends Slot(container, slot, x, y) {
    override def mayPlace(stack: ItemStack): Boolean = false
    override def mayPickup(player: Player): Boolean  = false
  }

  class OutputSlot(container: Container, slot: Int, x: Int, y: Int) extends Slot(container, slot, x, y) {
    override def mayPlace(p_40231_ : ItemStack): Boolean = false
  }

}

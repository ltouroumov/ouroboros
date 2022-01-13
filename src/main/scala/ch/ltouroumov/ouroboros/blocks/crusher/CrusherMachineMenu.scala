package ch.ltouroumov.ouroboros.blocks.crusher

import ch.ltouroumov.ouroboros.blocks.crusher.CrusherMachineEntity._
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
          // Slots 0-8
          INPUT_SLOTS.map(ItemSlotWrapper.of(container, _))
        ),
        Spacer(width = 9),
        ItemSlotWrapper(
          // Slot 9
          new ActiveRecipeSlot(container, ACTIVE_RECIPE_SLOT, _, _)
        ),
        Spacer(width = 9),
        Grid(rows = 3, cols = 3).addAll(
          // Slots 10-18
          OUTPUT_SLOTS.map(slot => ItemSlotWrapper(new OutputSlot(container, slot, _, _)))
        )
      ),
      Spacer(height = 5),
      HStack(
        Spacer(width = 19),
        Grid(rows = 1, cols = 3).addAll(
          // Slots 19-21
          UPGRADE_SLOTS.map(ItemSlotWrapper.of(container, _))
        )
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

  // https://discord.com/channels/313125603924639766/915304642668290119/931240812052893786
  // https://github.com/Shadows-of-Fire/Placebo/blob/6f820bf7d7b89494894b067a2a73888fd579ea36/src/main/java/shadows/placebo/container/QuickMoveHandler.java
  override def quickMoveStack(player: Player, slotId: Int): ItemStack = {
    var outputStack = ItemStack.EMPTY
    val slot        = this.slots.get(slotId)
    if (slot != null && slot.hasItem) {
      val slotStack = slot.getItem
      outputStack = slotStack.copy

      if (slotId >= 0 && slotId < 9) {
        // Input slots => move to inventory
        if (!this.moveItemStackTo(slotStack, ContentsSize, this.slots.size, true)) {
          return ItemStack.EMPTY
        }
      } else if (slotId == 9) {
        // Active slot => do nothing
        return ItemStack.EMPTY
      } else if (slotId >= 10 && slotId < 19) {
        // Output slots => move to inventory
        if (!this.moveItemStackTo(slotStack, ContentsSize, this.slots.size, true)) {
          return ItemStack.EMPTY
        }
      } else if (slotId >= 19 && slotId < 22) {
        // Upgrade slots => move to inventory
        if (!this.moveItemStackTo(slotStack, ContentsSize, this.slots.size, true)) {
          return ItemStack.EMPTY
        }
      } else if (!this.moveItemStackTo(slotStack, 0, 9, false)) {
        return ItemStack.EMPTY
      }

      if (slotStack.isEmpty) {
        slot.set(ItemStack.EMPTY)
      } else {
        slot.setChanged()
      }
    }

    outputStack
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

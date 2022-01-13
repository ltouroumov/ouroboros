package ch.ltouroumov.ouroboros.utils

import net.minecraft.world.Container
import net.minecraft.world.inventory.Slot
import org.scalamock.scalatest.proxy.MockFactory
import org.scalatest.wordspec.AnyWordSpecLike

class MenuBuilderTest extends AnyWordSpecLike with MockFactory {
  import ch.ltouroumov.ouroboros.utils.MenuBuilder._

  "MenuBuilder.createContainer" in {
    val addSlot         = mockFunction[Slot, Slot]("addSlot")
    val entity          = mock[Container]
    val playerInventory = mock[Container]

    createContainer(addSlot)(
      VStack(
        Spacer(height = 21),
        HStack(
          Spacer(width = 18),
          Grid(rows = 3, cols = 3).addAll(
            for (slot <- 0 until 9) yield ItemSlotWrapper.of(entity, slot)
          ),
          Spacer(width = 36),
          Grid(rows = 3, cols = 3).addAll(
            for (slot <- 9 until 18) yield ItemSlotWrapper.of(entity, slot)
          )
        ),
        Spacer(height = 5),
        HStack(
          Spacer(width = 18),
          Grid(rows = 1, cols = 3).addAll(
            for (slot <- 18 until 21) yield ItemSlotWrapper.of(entity, slot)
          )
        ),
        Spacer(height = 27)
      ).addAll(playerInventoryViews(playerInventory))
    )

  }
}

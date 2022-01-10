package ch.ltouroumov.ouroboros.blocks.crusher

import ch.ltouroumov.ouroboros.blocks.structure.StructureEntity
import ch.ltouroumov.ouroboros.blocks.{BaseBlockEntity, Properties}
import ch.ltouroumov.ouroboros.registry.BlockEntityRegistry
import ch.ltouroumov.ouroboros.utils.syntax._
import ch.ltouroumov.ouroboros.utils.{BlockEntityHelpers, StrictLogging}
import net.minecraft.core.BlockPos
import net.minecraft.network.chat.{Component, TranslatableComponent}
import net.minecraft.world.entity.player.{Inventory, Player}
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.entity.{BaseContainerBlockEntity, BlockEntityTicker, BlockEntityType}
import net.minecraft.world.level.block.state.BlockState

import scala.collection.mutable

class CrusherMachineEntity(blockPos: BlockPos, blockState: BlockState)
    extends BaseContainerBlockEntity(BlockEntityRegistry.CRUSHER.get(), blockPos, blockState)
    with BlockEntityHelpers
    with StrictLogging {

  private val _contentsSize: Int = 20

  /** Slots Allocations Inputs: 0-8 Outputs: 9-17 Upgrades: 18-20
    */
  private val _contents: mutable.Map[Int, ItemStack] = mutable.Map.empty
  private var _tickCounter: Int                      = 0
  private var _structure: Seq[BlockPos]              = Seq.empty

  def checkValid(): Boolean = _structure.forall { pos =>
    level.getBlockEntity(pos) match {
      case entity: StructureEntity => entity.machinePos.contains(worldPosition)
      case _                       => false
    }
  }

  def setStructureBlocks(positions: Seq[BlockPos]): CrusherMachineEntity =
    setter { _structure = positions }

  def structureDestroyed(position: BlockPos): Unit = {
    logger.info(s"Structure destroyed at $position")
    _structure.foreach { pos =>
      getLevel.getBlockEntity(pos) match {
        case te: StructureEntity => te.clearMachinePos()
        case _                   => ()
      }

      getLevel.setBlockAndUpdate(
        pos,
        getLevel
          .getBlockState(pos)
          .setBoolValue(Properties.MACHINE_PART, value = false)
      )
    }
    _structure = Seq.empty
    setChanged()

    getLevel.setBlockAndUpdate(
      getBlockPos,
      getBlockState.setBoolValue(Properties.MACHINE_PART, value = false)
    )
  }

  override def getDefaultName: Component =
    new TranslatableComponent("container.crusher")

  override def createMenu(windowId: Int, playerInventory: Inventory): AbstractContainerMenu =
    CrusherMachineMenu(windowId, playerInventory, this)

  override def getContainerSize: Int = 1

  override def isEmpty: Boolean = _contents.isEmpty

  override def getItem(slot: Int): ItemStack = _contents.getOrElse(slot, ItemStack.EMPTY)

  override def removeItem(slot: Int, quantity: Int): ItemStack = {
    if (slot >= 0 && slot < _contentsSize && quantity > 0)
      _contents.get(slot) match {
        case Some(stack) if !stack.isEmpty => stack.split(quantity)
        case _                             => ItemStack.EMPTY
      }
    else
      ItemStack.EMPTY
  }

  override def removeItemNoUpdate(slot: Int): ItemStack = {
    if (slot >= 0 && slot < _contentsSize)
      _contents.put(slot, ItemStack.EMPTY) getOrElse ItemStack.EMPTY
    else
      ItemStack.EMPTY
  }

  override def setItem(slot: Int, stack: ItemStack): Unit = setter {
    if (stack.getCount > this.getMaxStackSize)
      stack.setCount(this.getMaxStackSize)
    _contents.put(slot, stack)
  }

  override def stillValid(player: Player) = true

  override def clearContent(): Unit = setter { _contents.clear() }
}

object CrusherMachineEntity extends BaseBlockEntity.Companion[CrusherMachineEntity] {

  override def create(blockPos: BlockPos, blockState: BlockState): CrusherMachineEntity =
    new CrusherMachineEntity(blockPos, blockState)

  override def entityType: BlockEntityType[CrusherMachineEntity] =
    BlockEntityRegistry.CRUSHER.get()

  object ServerTick extends BlockEntityTicker[CrusherMachineEntity] with StrictLogging {
    override def tick(level: Level, pos: BlockPos, state: BlockState, entity: CrusherMachineEntity): Unit = {
      entity._tickCounter += 1

      if (entity._tickCounter >= 20) {
        logger.info(s"Ticking crusher at $pos, state=$state")
        entity._tickCounter = 0
      }
    }
  }

}

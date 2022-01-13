package ch.ltouroumov.ouroboros.blocks.crusher

import ch.ltouroumov.ouroboros.blocks.crusher.CrusherMachineEntity.{
  ACTIVE_RECIPE_SLOT,
  ContentsSize,
  OUTPUT_SLOTS,
  SlotsSize
}
import ch.ltouroumov.ouroboros.blocks.structure.StructureEntity
import ch.ltouroumov.ouroboros.blocks.{BaseBlockEntity, Properties}
import ch.ltouroumov.ouroboros.registry.BlockEntityRegistry
import ch.ltouroumov.ouroboros.utils.syntax._
import ch.ltouroumov.ouroboros.utils.{BlockEntityHelpers, StrictLogging}
import net.minecraft.core.{BlockPos, NonNullList}
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.{Component, TranslatableComponent}
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.ContainerHelper
import net.minecraft.world.entity.player.{Inventory, Player}
import net.minecraft.world.inventory.{AbstractContainerMenu, ContainerData}
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.entity.{BaseContainerBlockEntity, BlockEntityTicker, BlockEntityType}
import net.minecraft.world.level.block.state.BlockState

import scala.jdk.OptionConverters.RichOptional

class CrusherMachineEntity(blockPos: BlockPos, blockState: BlockState)
    extends BaseContainerBlockEntity(BlockEntityRegistry.CRUSHER.get(), blockPos, blockState)
    with BlockEntityHelpers
    with StrictLogging {

  /** Slots Allocations: Inputs: 0-8, Outputs: 9-17, Upgrades: 18-20, Active: 21
    */
  private val _contents: NonNullList[ItemStack]           = NonNullList.withSize(ContentsSize, ItemStack.EMPTY)
  private var _structure: Seq[BlockPos]                   = Seq.empty
  private var _currentRecipeRef: Option[ResourceLocation] = None
  private var _currentRecipe: Option[CrusherRecipe]       = None
  private var _processingProgress: Int                    = 0
  private var _processingTime: Int                        = 0
  def processingProgress: Int                             = _processingProgress
  def processingTime: Int                                 = _processingTime

  val dataAccess: ContainerData = new ContainerData {
    override def get(slot: Int): Int = slot match {
      case 0 => _processingProgress
      case 1 => _processingTime
    }

    override def set(slot: Int, value: Int): Unit = slot match {
      case 0 => _processingProgress = value
      case 1 => _processingTime = value
    }

    override def getCount: Int = SlotsSize
  }

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

  def setCurrentRecipe(recipe: CrusherRecipe): Unit = setter {
    _currentRecipe = Some(recipe)
    _currentRecipeRef = Some(recipe.id)
    _processingTime = recipe.processingTime
  }
  def clearCurrentRecipe(): Unit = setter {
    _currentRecipe = None
    _currentRecipeRef = None
  }
  def clearProgress(): Unit    = setter { _processingProgress = 0 }
  def increaseProgress(): Unit = setter { _processingProgress += 1 }
  def tryFinishProcess(): Unit = inSingleUpdate {
    def canAddToSlot(slot: Int, stack: ItemStack): Boolean = {
      val itemInSlot = getItem(slot)
      itemInSlot.isEmpty || (
        ItemStack.isSame(itemInSlot, stack) &&
          itemInSlot.getCount + stack.getCount < itemInSlot.getMaxStackSize
      )
    }

    _currentRecipe match {
      case Some(recipe) =>
        // Get the recipe output
        val output = recipe.assemble(this)
        OUTPUT_SLOTS.find(canAddToSlot(_, output)) match {
          case Some(outputSlot) if getItem(outputSlot).isEmpty =>
            setItem(outputSlot, output)
            removeItemNoUpdate(ACTIVE_RECIPE_SLOT)
            clearCurrentRecipe()
          case Some(outputSlot) =>
            getItem(outputSlot).grow(output.getCount)
            removeItemNoUpdate(ACTIVE_RECIPE_SLOT)
            clearCurrentRecipe()
          case None =>
            logger.info("No free output slot, skip output")
            () // No possible outputs, wait until something is freed up
        }
      case None =>
        logger.error("Called finishProcess() but no recipe is set")
        clearCurrentRecipe()
    }
  }

  def isIdle: Boolean     = _currentRecipe.isEmpty
  def isComplete: Boolean = _currentRecipe.isDefined && _processingProgress >= _processingTime

  override def getDefaultName: Component =
    new TranslatableComponent("container.crusher")

  override def createMenu(windowId: Int, playerInventory: Inventory): AbstractContainerMenu =
    CrusherMachineMenu(windowId, playerInventory, this, this.dataAccess)

  override def getContainerSize: Int = 1

  override def isEmpty: Boolean = _contents.isEmpty

  override def getItem(slot: Int): ItemStack = _contents.get(slot)

  override def removeItem(slot: Int, quantity: Int): ItemStack =
    ContainerHelper.removeItem(_contents, slot, quantity)

  override def removeItemNoUpdate(slot: Int): ItemStack =
    ContainerHelper.takeItem(_contents, slot)

  override def setItem(slot: Int, stack: ItemStack): Unit = setter {
    if (stack.getCount > this.getMaxStackSize)
      stack.setCount(this.getMaxStackSize)
    _contents.set(slot, stack)
  }

  override def stillValid(player: Player) = true

  override def clearContent(): Unit = setter { _contents.clear() }

  override def load(tag: CompoundTag): Unit = {
    super.load(tag)
    ContainerHelper.loadAllItems(tag, _contents)
    _currentRecipeRef = Option.when(tag.contains("CurrentRecipe"))(
      ResourceLocation.tryParse(tag.getString("CurrentRecipe"))
    )
    _processingProgress = tag.getInt("ProcessingProgress")
    _processingTime = tag.getInt("ProcessingTime")
  }

  override def saveAdditional(tag: CompoundTag): Unit = {
    super.save(tag)
    ContainerHelper.saveAllItems(tag, _contents)
    _currentRecipe.foreach { recipe =>
      tag.putString("CurrentRecipe", recipe.id.toString)
    }
    tag.putInt("ProcessingProgress", _processingProgress)
    tag.putInt("ProcessingTime", _processingTime)
  }
}

object CrusherMachineEntity extends BaseBlockEntity.Companion[CrusherMachineEntity] {
  val ContentsSize: Int       = 22
  val SlotsSize: Int          = 2
  val INPUT_SLOTS: Range      = Range.inclusive(0, 8)
  val OUTPUT_SLOTS: Range     = Range.inclusive(9, 17)
  val UPGRADE_SLOTS: Range    = Range.inclusive(18, 20)
  val ACTIVE_RECIPE_SLOT: Int = 21

  override def create(blockPos: BlockPos, blockState: BlockState): CrusherMachineEntity =
    new CrusherMachineEntity(blockPos, blockState)

  override def entityType: BlockEntityType[CrusherMachineEntity] =
    BlockEntityRegistry.CRUSHER.get()

  object ServerTick extends BlockEntityTicker[CrusherMachineEntity] with StrictLogging {
    override def tick(level: Level, pos: BlockPos, state: BlockState, entity: CrusherMachineEntity): Unit = {
      val recipeManager = level.getRecipeManager

      if (entity._currentRecipeRef.isDefined && entity._currentRecipe.isEmpty) {
        logger.info(s"Restoring recipe ${entity._currentRecipeRef}")
        recipeManager.byKey(entity._currentRecipeRef.get).ifPresent {
          case recipe: CrusherRecipe =>
            entity.setCurrentRecipe(recipe)
          case _ =>
            logger.error(s"Recipe ${entity._currentRecipeRef.get} in crusher is not a crusher recipe")
            entity.clearCurrentRecipe()
        }
      }

      if (entity.isIdle) {
        logger.info("Crusher is idle, looking for a recipe")
        // Fetch the first item that can be processed in slot order
        val recipeOpt =
          recipeManager
            .getRecipeFor(CrusherRecipe.RecipeType, entity, level)
            .toScala

        val firstMatch =
          recipeOpt.flatMap { recipe =>
            INPUT_SLOTS
              .map(slot => slot -> entity.getItem(slot))
              .collectFirst {
                case (slot, item) if !item.isEmpty && recipe.input.test(item) => recipe -> slot
              }
          }

        firstMatch match {
          case Some((recipe, slot)) =>
            // Move item to working slot
            val item = entity.removeItem(slot, quantity = 1)
            entity.inSingleUpdate {
              entity.setItem(ACTIVE_RECIPE_SLOT, item)
              // Set the recipe
              entity.setCurrentRecipe(recipe)
              entity.clearProgress()
            }
          case None =>
            if (entity._currentRecipe.isDefined)
              entity.inSingleUpdate {
                entity.clearCurrentRecipe()
                entity.clearProgress()
              }
        }
      } else if (entity.isComplete) {
        logger.info("Recipe complete")
        entity.tryFinishProcess()
      } else {
        entity.increaseProgress()
      }
    }
  }

}

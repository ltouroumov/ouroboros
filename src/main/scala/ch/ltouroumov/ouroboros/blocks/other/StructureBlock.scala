package ch.ltouroumov.ouroboros.blocks.other

import ch.ltouroumov.ouroboros.blocks
import ch.ltouroumov.ouroboros.blocks.Properties.MachineTier
import ch.ltouroumov.ouroboros.blocks.machine.CrusherMachineEntity
import ch.ltouroumov.ouroboros.blocks.{BaseEntityBlock, Properties}
import ch.ltouroumov.ouroboros.registry.BlocksRegistry
import ch.ltouroumov.ouroboros.utils.StrictLogging
import ch.ltouroumov.ouroboros.utils.syntax.BlockStateOps
import net.minecraft.core.BlockPos
import net.minecraft.world.{InteractionHand, InteractionResult}
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.{BlockBehaviour, BlockState, StateDefinition}
import net.minecraft.world.phys.BlockHitResult

class StructureBlock(properties: BlockBehaviour.Properties) extends BaseEntityBlock(properties) with StrictLogging {
  registerDefaultState(
    stateDefinition
      .any()
      .setValue(Properties.MACHINE_TIER, MachineTier.T0)
      .setBoolValue(Properties.MACHINE_PART, value = false)
  )

  override def createBlockStateDefinition(builder: StateDefinition.Builder[Block, BlockState]): Unit =
    builder.add(Properties.MACHINE_TIER, Properties.MACHINE_PART)

  override def newBlockEntity(blockPos: BlockPos, blockState: BlockState): BlockEntity =
    Option.when(blockState.getValue(Properties.MACHINE_PART))(StructureBlock.entity(blockPos, blockState)).orNull

  override def playerWillDestroy(
      world: Level,
      position: BlockPos,
      blockState: BlockState,
      player: Player
  ): Unit = {
    super.playerWillDestroy(world, position, blockState, player)
    getMachineEntity(world, position) match {
      case Some(entity) =>
        logger.debug("Has machine, forwarding destruction")
        entity.structureDestroyed(position)
      case None =>
        logger.debug("No machine")
    }
  }

  override def use(
      state: BlockState,
      world: Level,
      position: BlockPos,
      player: Player,
      hand: InteractionHand,
      hit: BlockHitResult
  ): InteractionResult = {
    if (world.isClientSide)
      InteractionResult.SUCCESS
    else
      getMachineBlockState(world, position) match {
        case Some(block) =>
          logger.debug(s"Structure block $position ($block) forward use call")
          block.use(world, player, hand, hit)
        case None =>
          logger.debug(s"Structure block $position has no entity")
          InteractionResult.PASS
      }
  }

  private def getMachinePos(world: Level, position: BlockPos) = {
    Option(world.getBlockEntity(position))
      .flatMap {
        case value: StructureEntity => value.machinePos
        case _                      => None
      }
  }
  private def getMachineEntity(world: Level, position: BlockPos): Option[CrusherMachineEntity] =
    getMachinePos(world, position)
      .flatMap { machinePos =>
        Option(world.getBlockEntity(machinePos))
      }
      .flatMap {
        case value: CrusherMachineEntity => Some(value)
        case _                           => None
      }

  private def getMachineBlockState(world: Level, position: BlockPos): Option[BlockState] =
    getMachinePos(world, position)
      .flatMap { machinePos =>
        Option(world.getBlockState(machinePos))
      }

}

object StructureBlock extends blocks.BaseEntityBlock.Companion[StructureBlock, StructureEntity] {
  override def apply(properties: BlockBehaviour.Properties): StructureBlock = new StructureBlock(properties)

  override def block(): StructureBlock = BlocksRegistry.STRUCTURE.get()
  override def entity(blockPos: BlockPos, blockState: BlockState): StructureEntity =
    new StructureEntity(blockPos, blockState)
}

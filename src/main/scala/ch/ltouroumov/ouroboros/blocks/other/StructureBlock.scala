package ch.ltouroumov.ouroboros.blocks.other

import ch.ltouroumov.ouroboros.blocks
import ch.ltouroumov.ouroboros.blocks.Properties.MachineTier
import ch.ltouroumov.ouroboros.blocks.machine.{CrusherMachineBlock, CrusherMachineEntity}
import ch.ltouroumov.ouroboros.blocks.{BaseEntityBlock, Properties}
import ch.ltouroumov.ouroboros.registry.BlocksRegistry
import ch.ltouroumov.ouroboros.utils.StrictLogging
import ch.ltouroumov.ouroboros.utils.syntax.BlockStateOps
import net.minecraft.block.{AbstractBlock, Block, BlockState}
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.state.StateContainer
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.math.{BlockPos, BlockRayTraceResult}
import net.minecraft.util.{ActionResultType, Hand}
import net.minecraft.world.{IBlockReader, World}

class StructureBlock(properties: AbstractBlock.Properties) extends BaseEntityBlock(properties) with StrictLogging {
  registerDefaultState(
    stateDefinition
      .any()
      .setValue(Properties.MACHINE_TIER, MachineTier.T0)
      .setBoolValue(Properties.MACHINE_PART, value = false)
  )

  override def createBlockStateDefinition(builder: StateContainer.Builder[Block, BlockState]): Unit =
    builder.add(Properties.MACHINE_TIER, Properties.MACHINE_PART)

  override def hasTileEntity(state: BlockState): Boolean =
    state.getValue(Properties.MACHINE_PART)

  override def createTileEntity(state: BlockState, world: IBlockReader): TileEntity =
    Option.when(state.getValue(Properties.MACHINE_PART))(StructureBlock.entity()).orNull

  override def playerWillDestroy(
      world: World,
      position: BlockPos,
      blockState: BlockState,
      player: PlayerEntity
  ): Unit = {
    super.playerWillDestroy(world, position, blockState, player)
    getMachineEntity(world, position).foreach { entity =>
      entity.structureDestroyed(position)
    }
  }

  override def use(
      blockState: BlockState,
      world: World,
      position: BlockPos,
      player: PlayerEntity,
      hand: Hand,
      hit: BlockRayTraceResult
  ): ActionResultType = {
    if (world.isClientSide)
      ActionResultType.SUCCESS
    else
      getMachineBlockState(world, position) match {
        case Some(block) =>
          logger.debug(s"Structure block $position ($blockState) forward use call")
          block.use(world, player, hand, hit)
        case None =>
          logger.debug(s"Structure block ($blockState) has no entity")
          ActionResultType.PASS
      }
  }

  private def getMachinePos(world: World, position: BlockPos) = {
    Option(world.getBlockEntity(position))
      .flatMap {
        case value: StructureEntity => value.machinePos
        case _                      => None
      }
  }
  private def getMachineEntity(world: World, position: BlockPos): Option[CrusherMachineEntity] =
    getMachinePos(world, position)
      .flatMap { machinePos =>
        Option(world.getBlockEntity(machinePos))
      }
      .flatMap {
        case value: CrusherMachineEntity => Some(value)
        case _                           => None
      }

  private def getMachineBlockState(world: World, position: BlockPos): Option[BlockState] =
    getMachinePos(world, position)
      .flatMap { machinePos =>
        Option(world.getBlockState(machinePos))
      }

}

object StructureBlock extends blocks.BaseEntityBlock.Companion[StructureBlock, StructureEntity] {
  override def apply(properties: AbstractBlock.Properties): StructureBlock = new StructureBlock(properties)

  override def block(): StructureBlock   = BlocksRegistry.STRUCTURE.get()
  override def entity(): StructureEntity = new StructureEntity()
}

package ch.ltouroumov.ouroboros.blocks.crusher

import ch.ltouroumov.ouroboros.blocks.Properties.MachineTier
import ch.ltouroumov.ouroboros.blocks.crusher.CrusherMachineBlock.{
  MachineBlockResult,
  MultiblockResult,
  StructureBlockResult
}
import ch.ltouroumov.ouroboros.blocks.structure.{StructureBlock, StructureEntity}
import ch.ltouroumov.ouroboros.blocks.{BaseBlockEntity, BaseEntityBlock, Properties}
import ch.ltouroumov.ouroboros.registry.{BlockEntityRegistry, BlockRegistry}
import ch.ltouroumov.ouroboros.utils.syntax.{BlockPatternOps, BlockStateOps, LevelOps}
import ch.ltouroumov.ouroboros.utils.{BlockEntityHelpers, StrictLogging}
import net.minecraft.Util
import net.minecraft.core.BlockPos
import net.minecraft.network.chat.TextComponent
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.entity.{BlockEntity, BlockEntityTicker, BlockEntityType}
import net.minecraft.world.level.block.state.pattern.{BlockInWorld, BlockPattern, BlockPatternBuilder}
import net.minecraft.world.level.block.state.predicate.BlockStatePredicate
import net.minecraft.world.level.block.state.{BlockBehaviour, BlockState, StateDefinition}
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.{InteractionHand, InteractionResult}
import net.minecraftforge.network.NetworkHooks

class CrusherMachineBlock(properties: BlockBehaviour.Properties)
    extends BaseEntityBlock(properties)
    with StrictLogging {

  registerDefaultState(
    stateDefinition
      .any()
      .setValue(Properties.MACHINE_TIER, MachineTier.T0)
      .setBoolValue(Properties.MACHINE_PART, value = false)
  )

  override def createBlockStateDefinition(builder: StateDefinition.Builder[Block, BlockState]): Unit =
    builder.add(Properties.MACHINE_TIER, Properties.MACHINE_PART)

  override def newBlockEntity(blockPos: BlockPos, blockState: BlockState): BlockEntity =
    Option.when(blockState.getValue(Properties.MACHINE_PART))(CrusherMachineEntity.create(blockPos, blockState)).orNull

  override def getTicker[T <: BlockEntity](
      level: Level,
      state: BlockState,
      entityType: BlockEntityType[T]
  ): BlockEntityTicker[T] =
    BlockEntityHelpers.createTicker[T, CrusherMachineEntity](
      level,
      entityType,
      CrusherMachineEntity.entityType,
      CrusherMachineEntity.ServerTick
    )

  override def playerWillDestroy(
      world: Level,
      position: BlockPos,
      blockState: BlockState,
      player: Player
  ): Unit = {
    super.playerWillDestroy(world, position, blockState, player)
    world.modifyBlockEntity(position, CrusherMachineEntity) { entity =>
      entity.structureDestroyed(position)
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
    if (world.isClientSide) {
      InteractionResult.SUCCESS
    } else {
      logger.info(s"Right clicked block $state at $position")
      world.getBlockEntityOpt(position, CrusherMachineEntity) match {
        case Some(entity) if entity.checkValid() =>
          logger.info(s"Open GUI for $position")
          NetworkHooks.openGui(player.asInstanceOf[ServerPlayer], entity, position)
          InteractionResult.CONSUME
        case _ =>
          tryCreateStructure(world, position, player)
      }
    }
  }

  private def tryCreateStructure(world: Level, position: BlockPos, player: Player): InteractionResult = {
    val pattern = CrusherMachineBlock.CrusherPattern
    Option(pattern.find(world, position)) match {
      case Some(result) =>
        logger.info(
          s"Found the pattern: up=${result.getUp} forwards=${result.getForwards} front=${result.getFrontTopLeft}, pos=$position"
        )

        val (invalid: Seq[BlockPos], valid: Seq[MultiblockResult]) =
          pattern.blockData(result, world).partitionMap {
            case (pos, blockState, None) if blockState.getBlock == StructureBlock.block =>
              Right(StructureBlockResult(pos, blockState, None))

            case (pos, blockState, Some(te: StructureEntity)) if blockState.getBlock == StructureBlock.block =>
              Right(StructureBlockResult(pos, blockState, Some(te)))

            case (pos, blockState, None) if blockState.getBlock == CrusherMachineBlock.block =>
              Right(MachineBlockResult(pos, blockState, None))

            case (pos, blockState, Some(te: CrusherMachineEntity))
                if blockState.getBlock == CrusherMachineBlock.block =>
              Right(MachineBlockResult(pos, blockState, Some(te)))

            case (pos, _, _) =>
              Left(pos)
          }

        logger.info(s"Invalid parts: $invalid")
        logger.info(s"Valid parts: $valid")

        val (structureBlocks: Seq[StructureBlockResult], machineBlock: Seq[MachineBlockResult]) =
          valid.partitionMap {
            case res: StructureBlockResult => Left(res)
            case res: MachineBlockResult   => Right(res)
          }

        if (invalid.isEmpty && machineBlock.size == 1 && structureBlocks.size == 17) {
          val machineResult: MachineBlockResult = machineBlock.head

          val (invalidStructures, _) =
            structureBlocks.partitionMap {
              // Does not belong to an existing machine
              case StructureBlockResult(_, _, None) => Right(())
              // Belongs to the current machine or references a missing machine
              case StructureBlockResult(pos, _, Some(te)) =>
                Either.cond(
                  te.machinePos.contains(machineResult.position) ||
                    te.machinePos.exists(mp => world.getBlockEntity(mp) == null),
                  (),
                  pos
                )
            }

          if (invalidStructures.isEmpty) {
            world.setBlockAndUpdate(
              machineResult.position,
              machineResult.blockState.setBoolValue(Properties.MACHINE_PART, value = true)
            )

            structureBlocks.foreach { case StructureBlockResult(position, blockState, _) =>
              world.setBlockAndUpdate(
                position,
                blockState.setBoolValue(Properties.MACHINE_PART, value = true)
              )
              world.modifyBlockEntity(position, StructureEntity) { entity =>
                entity.setMachinePos(machineResult.position)
              }
            }

            world.modifyBlockEntity(machineResult.position, CrusherMachineEntity) { entity =>
              entity.setStructureBlocks(structureBlocks.map(_.position))
            }

            player.sendMessage(new TextComponent("Machine created"), Util.NIL_UUID)
            InteractionResult.CONSUME
          } else {
            player.sendMessage(
              new TextComponent(s"Invalid structure blocks in ${invalidStructures.mkString(", ")}"),
              Util.NIL_UUID
            )
            InteractionResult.FAIL
          }
        } else {
          player.sendMessage(new TextComponent(s"Invalid structure at ${invalid.mkString(", ")}"), Util.NIL_UUID)
          InteractionResult.FAIL
        }
      case None =>
        logger.info("Did not find the pattern")
        player.sendMessage(new TextComponent("Invalid structure"), Util.NIL_UUID)
        InteractionResult.FAIL
    }
  }

}

object CrusherMachineBlock extends BaseEntityBlock.Companion[CrusherMachineBlock, CrusherMachineEntity] {
  def apply(properties: BlockBehaviour.Properties): CrusherMachineBlock = new CrusherMachineBlock(properties)

  override def block: CrusherMachineBlock                              = BlockRegistry.CRUSHER.get()
  override def entity: BaseBlockEntity.Companion[CrusherMachineEntity] = CrusherMachineEntity

  sealed trait MultiblockResult
  final case class StructureBlockResult(position: BlockPos, blockState: BlockState, entity: Option[StructureEntity])
      extends MultiblockResult
  final case class MachineBlockResult(position: BlockPos, blockState: BlockState, entity: Option[CrusherMachineEntity])
      extends MultiblockResult

  lazy val CrusherPattern: BlockPattern = {
    val S = BlockInWorld.hasState(BlockStatePredicate.forBlock(BlockRegistry.STRUCTURE.get()))
    val M = BlockInWorld.hasState(BlockStatePredicate.forBlock(BlockRegistry.CRUSHER.get()))
    BlockPatternBuilder
      .start()
      .aisle("SSS", "SMS")
      .aisle("SSS", "MSM")
      .aisle("SSS", "SMS")
      .where('S', S)
      .where('M', M or S)
      .build()
  }

}

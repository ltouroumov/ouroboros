package ch.ltouroumov.ouroboros.blocks.machine

import cats.syntax.all._
import ch.ltouroumov.ouroboros.blocks.Properties.MachineTier
import ch.ltouroumov.ouroboros.blocks.machine.CrusherMachineBlock.ValidResult
import ch.ltouroumov.ouroboros.blocks.other.{StructureBlock, StructureEntity}
import ch.ltouroumov.ouroboros.blocks.{BaseEntityBlock, Properties}
import ch.ltouroumov.ouroboros.registry.{BlockEntityRegistry, BlocksRegistry}
import ch.ltouroumov.ouroboros.utils.syntax.{BlockPatternOps, BlockStateOps}
import ch.ltouroumov.ouroboros.utils.{BlockEntityHelpers, StrictLogging}
import net.minecraft.Util
import net.minecraft.core.BlockPos
import net.minecraft.network.chat.TextComponent
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.entity.{BlockEntity, BlockEntityTicker, BlockEntityType}
import net.minecraft.world.level.block.state.pattern.{BlockInWorld, BlockPattern, BlockPatternBuilder}
import net.minecraft.world.level.block.state.predicate.BlockStatePredicate
import net.minecraft.world.level.block.state.{BlockBehaviour, BlockState, StateDefinition}
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.{InteractionHand, InteractionResult}

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
    Option.when(blockState.getValue(Properties.MACHINE_PART))(CrusherMachineBlock.entity(blockPos, blockState)).orNull

  override def getTicker[T <: BlockEntity](
      level: Level,
      state: BlockState,
      entityType: BlockEntityType[T]
  ): BlockEntityTicker[T] =
    BlockEntityHelpers.createTicker[T, CrusherMachineEntity](
      level,
      entityType,
      BlockEntityRegistry.CRUSHER.get(),
      CrusherMachineEntity.ServerTick
    )

  override def playerWillDestroy(
      world: Level,
      position: BlockPos,
      blockState: BlockState,
      player: Player
  ): Unit = {
    super.playerWillDestroy(world, position, blockState, player)
    Option(world.getBlockEntity(position)) match {
      case Some(value: CrusherMachineEntity) =>
        value.structureDestroyed(position)
      case _ =>
        ()
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
      logger.debug(s"Right clicked block $state at $position")
      Option(world.getBlockEntity(position)) match {
        case Some(_) =>
          logger.debug(s"Open GUI for $position")
          InteractionResult.CONSUME
        case None =>
          tryCreateStructure(world, position, player)
      }
    }
  }

  private def tryCreateStructure(world: Level, position: BlockPos, player: Player): InteractionResult = {
    val pattern = CrusherMachineBlock.CrusherPattern
    Option(pattern.find(world, position)) match {
      case Some(result) =>
        logger.debug(
          s"Found the pattern: up=${result.getUp} forwards=${result.getForwards} front=${result.getFrontTopLeft}, pos=$position"
        )

        def validStructureBlock(pos: BlockPos, blockState: BlockState): ValidResult =
          ValidResult(
            pos,
            Some(blockState.setBoolValue(Properties.MACHINE_PART, value = true)),
            { case te: StructureEntity => te.setMachinePos(position) }
          )

        val (invalid: Seq[BlockPos], valid: Seq[ValidResult]) =
          pattern.blockData(result, world).partitionMap {
            case (pos, blockState, None) if blockState.getBlock == StructureBlock.block() =>
              logger.debug(s"Structure block at $pos has state=$blockState")
              validStructureBlock(pos, blockState).asRight
            case (pos, blockState, Some(te: StructureEntity)) if blockState.getBlock == StructureBlock.block() =>
              logger.debug(s"Structure block at $pos has state=$blockState and entity=$te")
              if (te.hasMachinePos(position)) {
                validStructureBlock(pos, blockState).asRight
              } else {
                Left(pos)
              }
            case (pos, blockState, _) if blockState.getBlock == CrusherMachineBlock.block() =>
              logger.debug(s"Machine block at $pos has state=$blockState")
              ValidResult(
                pos,
                Some(blockState.setBoolValue(Properties.MACHINE_PART, value = true))
              ).asRight
            case (pos, blockState, entity) =>
              logger.debug(s"Block at $pos has state=$blockState, entity=$entity")
              Left(pos)
          }

        if (invalid.isEmpty) {
          valid.foreach { case ValidResult(pos, updatedState, entityMod) =>
            updatedState.foreach { blockState =>
              world.setBlockAndUpdate(pos, blockState)
            }
            val tileEntity = world.getBlockEntity(pos)
            entityMod.applyOrElse(tileEntity, { _: BlockEntity => () })

            logger.debug(s"Updated $pos to state=$updatedState, entity=$tileEntity")
          }

          world.getBlockEntity(position) match {
            case machine: CrusherMachineEntity =>
              machine.setStructureBlocks(valid.map(_.position))
            case _ =>
              logger.warn(s"CrusherMachineEntity not found at $position")
          }

          player.sendMessage(new TextComponent("Valid structure"), Util.NIL_UUID)
          InteractionResult.CONSUME
        } else {
          player.sendMessage(new TextComponent(s"Invalid structure at ${invalid.mkString(", ")}"), Util.NIL_UUID)
          InteractionResult.FAIL
        }
      case None =>
        logger.debug("Did not find the pattern")
        player.sendMessage(new TextComponent("Invalid structure"), Util.NIL_UUID)
        InteractionResult.FAIL
    }
  }

}

object CrusherMachineBlock extends BaseEntityBlock.Companion[CrusherMachineBlock, CrusherMachineEntity] {
  def apply(properties: BlockBehaviour.Properties): CrusherMachineBlock = new CrusherMachineBlock(properties)

  override def block(): CrusherMachineBlock                       = BlocksRegistry.CRUSHER.get()
  override def entity(blockPos: BlockPos, blockState: BlockState) = new CrusherMachineEntity(blockPos, blockState)

  final case class ValidResult(
      position: BlockPos,
      updatedState: Some[BlockState],
      entityMod: PartialFunction[BlockEntity, Unit] = { _: BlockEntity => () }
  )

  lazy val CrusherPattern: BlockPattern =
    BlockPatternBuilder
      .start()
      .aisle("SSS", "SMS")
      .aisle("SSS", "SSS")
      .aisle("SSS", "SSS")
      .where(
        'S',
        BlockInWorld.hasState(BlockStatePredicate.forBlock(BlocksRegistry.STRUCTURE.get()))
      )
      .where(
        'M',
        BlockInWorld.hasState(BlockStatePredicate.forBlock(BlocksRegistry.CRUSHER.get()))
      )
      .build()

}

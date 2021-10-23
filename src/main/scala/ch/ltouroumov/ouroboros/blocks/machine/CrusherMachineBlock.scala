package ch.ltouroumov.ouroboros.blocks.machine

import cats.implicits.catsSyntaxEitherId
import ch.ltouroumov.ouroboros.blocks.Properties.MachineTier
import ch.ltouroumov.ouroboros.blocks.machine.CrusherMachineBlock.ValidResult
import ch.ltouroumov.ouroboros.blocks.other.{StructureBlock, StructureEntity}
import ch.ltouroumov.ouroboros.blocks.{BaseEntityBlock, Properties}
import ch.ltouroumov.ouroboros.registry.BlocksRegistry
import ch.ltouroumov.ouroboros.utils.StrictLogging
import ch.ltouroumov.ouroboros.utils.syntax.{BlockPatternOps, BlockStateOps}
import com.google.common.base.Predicates
import net.minecraft.block.pattern.{BlockPattern, BlockPatternBuilder, BlockStateMatcher}
import net.minecraft.block.{AbstractBlock, Block, BlockState}
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.state.StateContainer
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.math.{BlockPos, BlockRayTraceResult}
import net.minecraft.util.text.StringTextComponent
import net.minecraft.util.{ActionResultType, CachedBlockInfo, Hand, Util}
import net.minecraft.world.{IBlockReader, World}

class CrusherMachineBlock(properties: AbstractBlock.Properties) extends BaseEntityBlock(properties) with StrictLogging {

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
    Option.when(state.getValue(Properties.MACHINE_PART))(CrusherMachineBlock.entity()).orNull

  override def playerWillDestroy(
      world: World,
      position: BlockPos,
      blockState: BlockState,
      player: PlayerEntity
  ): Unit = {
    super.playerWillDestroy(world, position, blockState, player)
    Option(world.getBlockEntity(position)) match {
      case Some(value: CrusherMachineEntity) =>
        value.structureDestroyed(position)
      case None =>
        ()
    }
  }

  override def use(
      state: BlockState,
      world: World,
      position: BlockPos,
      player: PlayerEntity,
      hand: Hand,
      hit: BlockRayTraceResult
  ): ActionResultType = {
    if (world.isClientSide)
      ActionResultType.SUCCESS
    else
      Option(world.getBlockEntity(position)) match {
        case Some(_) =>
          logger.debug(s"Open GUI for $position")
          ActionResultType.CONSUME
        case None =>
          tryCreateStructure(world, position, player)
      }
  }

  private def tryCreateStructure(world: World, position: BlockPos, player: PlayerEntity): ActionResultType = {
    val pattern = CrusherMachineBlock.CrusherPattern
    Option(pattern.find(world, position)) match {
      case Some(result) =>
        logger.debug(
          s"Found the pattern: up=${result.getUp} forwards=${result.getForwards} front=${result.getFrontTopLeft}, pos=$position"
        )
        val (invalid, valid: Seq[ValidResult]) =
          pattern.blockData(result, world).partitionMap {
            case (pos, blockState, None) if blockState.getBlock == StructureBlock.block() =>
              logger.debug(s"Structure block at $pos has state=$blockState")
              ValidResult(
                pos,
                Some(blockState.setBoolValue(Properties.MACHINE_PART, value = true)),
                { case te: StructureEntity => te.setMachinePos(position) }
              ).asRight
            case (pos, blockState, None) if blockState.getBlock == CrusherMachineBlock.block() =>
              logger.debug(s"Machine block at $pos has state=$blockState")
              ValidResult(
                pos,
                Some(blockState.setBoolValue(Properties.MACHINE_PART, value = true)),
                { case _: CrusherMachineEntity => () }
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
            entityMod.applyOrElse(tileEntity, { _: TileEntity => () })

            logger.debug(s"Updated $pos to state=$updatedState, entity=$tileEntity")
          }

          player.sendMessage(new StringTextComponent("Valid structure"), Util.NIL_UUID)
          ActionResultType.CONSUME
        } else {
          player.sendMessage(new StringTextComponent(s"Invalid structure at ${invalid.mkString(", ")}"), Util.NIL_UUID)
          ActionResultType.FAIL
        }
      case None =>
        logger.debug("Did not find the pattern")
        player.sendMessage(new StringTextComponent("Invalid structure"), Util.NIL_UUID)
        ActionResultType.FAIL
    }
  }
}

object CrusherMachineBlock extends BaseEntityBlock.Companion[CrusherMachineBlock, CrusherMachineEntity] {
  def apply(properties: AbstractBlock.Properties): CrusherMachineBlock = new CrusherMachineBlock(properties)

  override def block(): CrusherMachineBlock = BlocksRegistry.CRUSHER.get()
  override def entity()                     = new CrusherMachineEntity()

  final case class ValidResult(
      position: BlockPos,
      updatedState: Some[BlockState],
      entityMod: PartialFunction[TileEntity, Unit]
  )

  lazy val CrusherPattern: BlockPattern =
    BlockPatternBuilder
      .start()
      .aisle("SSS", "SMS")
      .aisle("SSS", "SSS")
      .aisle("SSS", "SSS")
      .where(
        'S',
        CachedBlockInfo.hasState(
          BlockStateMatcher
            .forBlock(BlocksRegistry.STRUCTURE.get())
            .where(Properties.MACHINE_PART, Predicates.equalTo(false))
        )
      )
      .where(
        'M',
        CachedBlockInfo.hasState(BlockStateMatcher.forBlock(BlocksRegistry.CRUSHER.get()))
      )
      .build()

}

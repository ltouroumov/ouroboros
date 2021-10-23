package ch.ltouroumov.ouroboros.blocks.machine

import cats.implicits.catsSyntaxEitherId
import ch.ltouroumov.ouroboros.blocks.other.StructureBlock
import ch.ltouroumov.ouroboros.blocks.{BaseEntityBlock, Properties}
import ch.ltouroumov.ouroboros.registry.BlocksRegistry
import ch.ltouroumov.ouroboros.utils.StrictLogging
import ch.ltouroumov.ouroboros.utils.syntax.{BlockPatternOps, BlockStateOps}
import com.google.common.base.Predicates
import net.minecraft.block.pattern.{BlockPattern, BlockPatternBuilder, BlockStateMatcher}
import net.minecraft.block.{AbstractBlock, BlockState}
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.math.{BlockPos, BlockRayTraceResult}
import net.minecraft.util.text.StringTextComponent
import net.minecraft.util.{ActionResultType, CachedBlockInfo, Hand, Util}
import net.minecraft.world.World

class CrusherMachineBlock(properties: AbstractBlock.Properties) extends BaseEntityBlock(properties) with StrictLogging {
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
        val (invalid, valid) =
          pattern.blockData(result, world).partitionMap {
            case (pos, blockState, None) if blockState.getBlock == StructureBlock.block() =>
              logger.debug(s"Structure block at $pos has state=$blockState")
              (
                pos,
                Some(blockState.setBoolValue(Properties.MACHINE_PART, value = true)),
                StructureBlock.entity()
              ).asRight
            case (pos, blockState, None) if blockState.getBlock == CrusherMachineBlock.block() =>
              logger.debug(s"Machine block at $pos has state=$blockState")
              (
                pos,
                None,
                CrusherMachineBlock.entity()
              ).asRight
            case (pos, blockState, entity) =>
              logger.debug(s"Block at $pos has state=$blockState, entity=$entity")
              Left(pos)
          }

        if (invalid.isEmpty) {
          valid.foreach { case (pos, maybeBlockState, te) =>
            logger.debug(s"Updating $pos to state=$maybeBlockState, entity=$te")
            maybeBlockState.foreach { blockState =>
              world.setBlockAndUpdate(pos, blockState)
            }
            world.setBlockEntity(pos, te)
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

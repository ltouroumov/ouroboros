package ch.ltouroumov.ouroboros.blocks.other

import ch.ltouroumov.ouroboros.blocks
import ch.ltouroumov.ouroboros.blocks.Properties.MachineTier
import ch.ltouroumov.ouroboros.blocks.{BaseEntityBlock, Properties}
import ch.ltouroumov.ouroboros.registry.BlocksRegistry
import ch.ltouroumov.ouroboros.utils.StrictLogging
import ch.ltouroumov.ouroboros.utils.syntax.BlockStateOps
import net.minecraft.block.{AbstractBlock, Block, BlockState}
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.state.StateContainer
import net.minecraft.util.math.{BlockPos, BlockRayTraceResult}
import net.minecraft.util.{ActionResultType, Hand}
import net.minecraft.world.World

class StructureBlock(properties: AbstractBlock.Properties) extends BaseEntityBlock(properties) with StrictLogging {
  registerDefaultState(
    stateDefinition
      .any()
      .setValue(Properties.MACHINE_TIER, MachineTier.T0)
      .setBoolValue(Properties.MACHINE_PART, value = false)
  )

  override def createBlockStateDefinition(builder: StateContainer.Builder[Block, BlockState]): Unit =
    builder.add(Properties.MACHINE_TIER, Properties.MACHINE_PART)

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
      Option(world.getBlockEntity(position)) match {
        case Some(value: StructureEntity) =>
          value.machinePos match {
            case Some(pos) =>
              logger.debug(s"Structure block $position forward use call to $pos")
              world.getBlockState(pos).use(world, player, hand, hit)
            case None =>
              ActionResultType.PASS
          }
        case Some(other) =>
          logger.warn(s"Wrong TileEntity is set to Structure: $other")
          ActionResultType.PASS
        case None =>
          ActionResultType.PASS
      }
  }
}

object StructureBlock extends blocks.BaseEntityBlock.Companion[StructureBlock, StructureEntity] {
  override def apply(properties: AbstractBlock.Properties): StructureBlock = new StructureBlock(properties)

  override def block(): StructureBlock   = BlocksRegistry.STRUCTURE.get()
  override def entity(): StructureEntity = new StructureEntity()
}

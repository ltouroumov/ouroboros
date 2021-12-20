package ch.ltouroumov.ouroboros.utils

import net.minecraft.world.level.Level
import net.minecraft.world.level.block.entity.{BlockEntity, BlockEntityTicker, BlockEntityType}

trait BlockEntityHelpers { self: BlockEntity =>

  def setter(work: => Unit): self.type = {
    work
    setChanged()
    self
  }

}

object BlockEntityHelpers {
  def createTicker[T <: BlockEntity, E <: BlockEntity](
      level: Level,
      levelEntityType: BlockEntityType[T],
      tickerEntityType: BlockEntityType[E],
      ticker: BlockEntityTicker[E]
  ): BlockEntityTicker[T] = {
    //noinspection ComparingUnrelatedTypes
    Option
      .when(!level.isClientSide && levelEntityType.eq(tickerEntityType))(
        ticker.asInstanceOf[BlockEntityTicker[T]]
      )
      .orNull
  }
}

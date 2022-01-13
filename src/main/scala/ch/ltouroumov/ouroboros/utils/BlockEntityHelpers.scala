package ch.ltouroumov.ouroboros.utils

import net.minecraft.world.level.Level
import net.minecraft.world.level.block.entity.{BlockEntity, BlockEntityTicker, BlockEntityType}

trait BlockEntityHelpers { self: BlockEntity =>

  private var _singleUpdate: Boolean = false
  private var _needsUpdate: Boolean  = false

  def modifier[T](work: => T): T = {
    val res = work
    if (_singleUpdate)
      _needsUpdate = true
    else setChanged()
    res
  }

  def setter(work: => Unit): self.type = {
    modifier(work)
    self
  }

  def inSingleUpdate[T](block: => T): T = {
    _singleUpdate = true
    val res = block
    if (_needsUpdate) {
      setChanged()
      _needsUpdate = false
    }
    _singleUpdate = false
    res
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

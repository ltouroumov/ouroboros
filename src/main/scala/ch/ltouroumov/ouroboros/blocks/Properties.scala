package ch.ltouroumov.ouroboros.blocks

import ch.ltouroumov.ouroboros.utils.StringEnumProperty
import enumeratum.values.{StringEnum, StringEnumEntry}

object Properties {
  val MACHINE_TIER: StringEnumProperty[MachineTier] = StringEnumProperty("machine_tier", MachineTier)

  sealed abstract class MachineTier(val value: String) extends StringEnumEntry with Comparable[MachineTier] {
    override def compareTo(o: MachineTier): Int = value.compareTo(o.value)
  }
  object MachineTier extends StringEnum[MachineTier] {
    val values: IndexedSeq[MachineTier] = findValues

    final case object T0 extends MachineTier("t0")
    final case object T1 extends MachineTier("t1")
    final case object T2 extends MachineTier("t2")
    final case object T3 extends MachineTier("t3")
    final case object T4 extends MachineTier("t4")
  }
}

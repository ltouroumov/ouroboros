package ch.ltouroumov.ouroboros.blocks

import ch.ltouroumov.ouroboros.utils.StringEnumProperty
import ch.ltouroumov.ouroboros.utils.enumerata.{StringEnum}
import net.minecraft.world.level.block.state.properties.BooleanProperty

object Properties {
  val MACHINE_TIER: StringEnumProperty[MachineTier] = StringEnumProperty("machine_tier", MachineTier)
  val MACHINE_PART: BooleanProperty                 = BooleanProperty.create("machine_part")

  sealed abstract class MachineTier(val value: String) extends StringEnum.Entry with Comparable[MachineTier] {
    override def compareTo(o: MachineTier): Int = value.compareTo(o.value)
  }
  object MachineTier extends StringEnum[MachineTier] {
    lazy val entries: Seq[MachineTier] = Seq(T0, T1, T2, T3, T4)

    final case object T0 extends MachineTier("t0")
    final case object T1 extends MachineTier("t1")
    final case object T2 extends MachineTier("t2")
    final case object T3 extends MachineTier("t3")
    final case object T4 extends MachineTier("t4")
  }
}

package ch.ltouroumov.ouroboros.utils.enumerata

abstract class StringEnum[V <: StringEnum.Entry] {
  def entries: Seq[V]
  lazy val entriesMap: Map[String, V] =
    entries.map(entry => (entry.value, entry)).toMap

  def withValueOpt(value: String): Option[V] =
    entriesMap.get(value)
}

object StringEnum {
  trait Entry {
    def value: String
  }
}

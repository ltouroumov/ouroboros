package ch.ltouroumov.ouroboros.utils

import ch.ltouroumov.ouroboros.utils.enumerata.StringEnum
import net.minecraft.world.level.block.state.properties.Property

import java.util
import java.util.Optional
import scala.jdk.CollectionConverters._
import scala.jdk.OptionConverters._
import scala.reflect.ClassTag

class StringEnumProperty[T <: StringEnum.Entry with Comparable[T]](
    name: String,
    val values: StringEnum[T]
)(implicit CT: ClassTag[T])
    extends Property[T](name, CT.runtimeClass.asInstanceOf[Class[T]]) {

  override def getPossibleValues: util.Collection[T] = values.entries.asJava
  override def getName(entry: T): String             = entry.value
  override def getValue(name: String): Optional[T]   = values.withValueOpt(name).toJava

}

object StringEnumProperty {

  def apply[T <: StringEnum.Entry with Comparable[T]: ClassTag](
      name: String,
      values: StringEnum[T]
  ): StringEnumProperty[T] =
    new StringEnumProperty(name, values)

}

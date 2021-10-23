package ch.ltouroumov.ouroboros.utils

import enumeratum.values.{StringEnum, StringEnumEntry}
import net.minecraft.state.Property

import java.util
import java.util.Optional
import scala.jdk.CollectionConverters._
import scala.jdk.OptionConverters._
import scala.reflect.ClassTag

class StringEnumProperty[T <: StringEnumEntry with Comparable[T]](
    name: String,
    val values: StringEnum[T]
)(implicit CT: ClassTag[T])
    extends Property[T](name, CT.runtimeClass.asInstanceOf[Class[T]]) {

  override def getPossibleValues: util.Collection[T] = values.values.asJava
  override def getName(entry: T): String             = entry.value
  override def getValue(name: String): Optional[T]   = values.withValueOpt(name).toJava

}

object StringEnumProperty {

  def apply[T <: StringEnumEntry with Comparable[T]: ClassTag](
      name: String,
      values: StringEnum[T]
  ): StringEnumProperty[T] =
    new StringEnumProperty(name, values)

}

package ch.ltouroumov.ouroboros.utils

import net.minecraft.world.Container
import net.minecraft.world.inventory.Slot

object MenuBuilder {

  case class Size(width: Int, height: Int) {
    def +(other: Size): Size       = Size(width + other.width, height + other.height)
    def extendH(other: Size): Size = Size(width + other.width, Math.max(height, other.height))
    def extendV(other: Size): Size = Size(Math.max(width, other.width), height + other.height)
  }
  object Size {
    val ZERO: Size = Size(0, 0)
    val SLOT: Size = Size(18, 18)
  }

  sealed trait View {
    def size: Size
  }
  sealed trait ContainerView extends View {
    def add(item: View): ContainerView
    def addAll(items: Seq[View]): ContainerView
  }
  case class VStack(items: Vector[View] = Vector.empty) extends ContainerView {
    override def add(item: View): VStack           = copy(items = items :+ item)
    override def addAll(_items: Seq[View]): VStack = copy(items = items ++ _items)

    lazy val size: Size =
      items.map(_.size).foldLeft(Size.ZERO)(_ extendV _)
  }
  object VStack {
    def apply(items: View*): VStack = VStack(items.toVector)
  }
  case class HStack(items: Vector[View] = Vector.empty) extends ContainerView {
    override def add(item: View): HStack           = copy(items = items :+ item)
    override def addAll(_items: Seq[View]): HStack = copy(items = items ++ _items)

    lazy val size: Size =
      items.map(_.size).foldLeft(Size.ZERO)(_ extendH _)
  }
  object HStack {
    def apply(items: View*): HStack = HStack(items.toVector)
  }
  case class Spacer(width: Int = 0, height: Int = 0) extends View {
    lazy val size: Size = Size(width, height)
  }
  case class Grid(rows: Int, cols: Int, itemSize: Size = Size.SLOT, items: Vector[View] = Vector.empty)
      extends ContainerView {
    override def add(item: View): Grid           = copy(items = items :+ item)
    override def addAll(_items: Seq[View]): Grid = copy(items = items ++ _items)

    def itemRows: Iterator[Vector[View]] = items.grouped(cols)

    lazy val size: Size = Size(cols + itemSize.width, rows * itemSize.height)
  }
  case class ItemSlotWrapper(container: Container, slot: Int) extends View {
    val size: Size = Size.SLOT
  }
  case class Margins(left: Int = 0, top: Int = 0, view: View) extends View {
    lazy val size: Size = Size(left, top) + view.size
  }

  def createContainer(addSlotF: Slot => Slot)(view: ContainerView): Unit = {

    def walk(view: View, leftPos: Int, topPos: Int): Size =
      view match {
        case VStack(items) =>
          items.foldLeft(Size.ZERO) { (size, innerView) =>
            walk(innerView, leftPos, topPos + size.height).extendV(size)
          }
        case HStack(items) =>
          items.foldLeft(Size.ZERO) { (size, innerView) =>
            walk(innerView, leftPos, topPos + size.height).extendH(size)
          }
        case grid: Grid =>
          grid.itemRows.foldLeft(Size.ZERO) { (rowSize, rowItems) =>
            rowItems
              .foldLeft(Size.ZERO) { (colSize, colItem) =>
                walk(
                  view = colItem,
                  leftPos = leftPos + rowSize.width + colSize.width,
                  topPos = topPos + rowSize.height + colSize.height
                ).extendH(colSize)
              }
              .extendV(rowSize)
          }
        case spacer: Spacer =>
          spacer.size
        case itemSlot @ ItemSlotWrapper(container, slot) =>
          addSlotF(new Slot(container, slot, leftPos, topPos))
          itemSlot.size
        case Margins(left, top, view) =>
          walk(view, leftPos + left, topPos + top)
      }

    walk(view, topPos = 0, leftPos = 0)
  }

  def playerInventoryViews(inventory: Container): Seq[View] = {
    val inventoryView =
      HStack(
        Spacer(width = 9),
        Grid(rows = 3, cols = 9).addAll(
          for {
            row <- 0 until 3
            col <- 0 until 9
          } yield ItemSlotWrapper(inventory, col + row * 9 + 9)
        )
      )

    val hotbarView =
      HStack(
        Spacer(width = 9),
        Grid(rows = 1, cols = 9).addAll(
          for (col <- 0 until 9) yield ItemSlotWrapper(inventory, col)
        )
      )

    Vector(inventoryView, Spacer(height = 4), hotbarView)
  }

}

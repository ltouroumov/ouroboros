package ch.ltouroumov.ouroboros.registry

import ch.ltouroumov.ouroboros.Ouroboros
import net.minecraft.world.item.{BlockItem, Item}
import net.minecraft.world.level.block.Block
import net.minecraftforge.registries.{ForgeRegistries, RegistryObject}

object BlockItemRegistry extends AbstractRegistry(ForgeRegistries.ITEMS) {

  private def forBlock(name: String, block: RegistryObject[_ <: Block]): RegistryObject[BlockItem] =
    registry.register[BlockItem](
      name,
      () => new BlockItem(block.get(), new Item.Properties().tab(Ouroboros.CREATIVE_TAB))
    )

  val STRUCTURE_T0: RegistryObject[BlockItem] = forBlock("structure", BlockRegistry.STRUCTURE)
  val CRUSHER_T0: RegistryObject[BlockItem]   = forBlock("crusher", BlockRegistry.CRUSHER)

}

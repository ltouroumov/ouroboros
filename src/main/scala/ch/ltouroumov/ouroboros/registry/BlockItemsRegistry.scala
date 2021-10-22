package ch.ltouroumov.ouroboros.registry

import ch.ltouroumov.ouroboros.Ouroboros
import net.minecraft.block.Block
import net.minecraft.item.{BlockItem, Item}
import net.minecraftforge.fml.RegistryObject
import net.minecraftforge.registries.{DeferredRegister, ForgeRegistries}

object BlockItemsRegistry {

  val _registry: DeferredRegister[Item] = DeferredRegister.create(ForgeRegistries.ITEMS, Ouroboros.MOD_ID)

  private def forBlock(name: String, block: RegistryObject[Block]): RegistryObject[BlockItem] =
    _registry.register[BlockItem](
      name,
      () => new BlockItem(block.get(), new Item.Properties().tab(Ouroboros.CREATIVE_TAB))
    )

  val STRUCTURE_T0: RegistryObject[BlockItem] = forBlock("structure_t0", BlocksRegistry.STRUCTURE_T0)
  val CRUSHER_T0: RegistryObject[BlockItem]   = forBlock("crusher_t0", BlocksRegistry.CRUSHER_T0)

}

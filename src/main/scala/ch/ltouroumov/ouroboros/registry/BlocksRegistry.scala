package ch.ltouroumov.ouroboros.registry

import ch.ltouroumov.ouroboros.Ouroboros
import net.minecraft.block.material.Material
import net.minecraft.block.{AbstractBlock, Block, SoundType}
import net.minecraftforge.common.ToolType
import net.minecraftforge.fml.RegistryObject
import net.minecraftforge.registries.{DeferredRegister, ForgeRegistries}

object BlocksRegistry {
  val _registry: DeferredRegister[Block] = DeferredRegister.create(ForgeRegistries.BLOCKS, Ouroboros.MOD_ID)

  private def createBlock[T <: Block](name: String, blockF: => T): RegistryObject[T] =
    _registry.register[T](name, () => blockF)

  private def createSimpleBlock(name: String, properties: => AbstractBlock.Properties): RegistryObject[Block] =
    createBlock[Block](name, new Block(properties))

  val STRUCTURE_T0: RegistryObject[Block] = createSimpleBlock(
    name = "structure_t0",
    properties = AbstractBlock.Properties
      .of(Material.METAL)
      .strength(5.0f, 6.0f)
      .sound(SoundType.METAL)
      .harvestLevel(0)
      .harvestTool(ToolType.PICKAXE)
      .requiresCorrectToolForDrops()
  )

  val CRUSHER_T0: RegistryObject[Block] = createSimpleBlock(
    name = "crusher_t0",
    properties = AbstractBlock.Properties
      .of(Material.METAL)
      .strength(5.0f, 6.0f)
      .sound(SoundType.METAL)
      .harvestLevel(0)
      .harvestTool(ToolType.PICKAXE)
      .requiresCorrectToolForDrops()
  )

}

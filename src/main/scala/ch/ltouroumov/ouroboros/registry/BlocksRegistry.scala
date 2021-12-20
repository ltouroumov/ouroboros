package ch.ltouroumov.ouroboros.registry

import ch.ltouroumov.ouroboros.Ouroboros
import ch.ltouroumov.ouroboros.blocks.BaseBlock
import ch.ltouroumov.ouroboros.blocks.machine.CrusherMachineBlock
import ch.ltouroumov.ouroboros.blocks.other.StructureBlock
import net.minecraft.world.level.block.{Block, SoundType}
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.material.Material
import net.minecraftforge.registries.{DeferredRegister, ForgeRegistries, RegistryObject}

object BlocksRegistry {
  val _registry: DeferredRegister[Block] = DeferredRegister.create(ForgeRegistries.BLOCKS, Ouroboros.MOD_ID)

  private def createBlock0[T <: Block](name: String, block: => T): RegistryObject[T] =
    _registry.register[T](name, () => block)

  private def createBlock[T <: BaseBlock](
      name: String,
      block: BaseBlock.Companion[T],
      properties: => BlockBehaviour.Properties
  ): RegistryObject[T] =
    createBlock0[T](name, block(properties))

  val STRUCTURE: RegistryObject[StructureBlock] = createBlock(
    name = "structure",
    block = StructureBlock,
    properties = BlockBehaviour.Properties
      .of(Material.METAL)
      .strength(5.0f, 6.0f)
      .sound(SoundType.METAL)
      .requiresCorrectToolForDrops()
  )

  val CRUSHER: RegistryObject[CrusherMachineBlock] = createBlock(
    name = "crusher",
    block = CrusherMachineBlock,
    properties = BlockBehaviour.Properties
      .of(Material.METAL)
      .strength(5.0f, 6.0f)
      .sound(SoundType.METAL)
      .requiresCorrectToolForDrops()
  )

}

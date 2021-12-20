package ch.ltouroumov.ouroboros.registry

import ch.ltouroumov.ouroboros.Ouroboros
import ch.ltouroumov.ouroboros.blocks.BaseEntityBlock
import ch.ltouroumov.ouroboros.blocks.machine.{CrusherMachineBlock, CrusherMachineEntity}
import ch.ltouroumov.ouroboros.blocks.other.{StructureBlock, StructureEntity}
import net.minecraft.world.level.block.entity.{BlockEntity, BlockEntityType}
import net.minecraftforge.registries.{DeferredRegister, ForgeRegistries, RegistryObject}

object BlockEntityRegistry {

  val _registry: DeferredRegister[BlockEntityType[_]] =
    DeferredRegister.create(ForgeRegistries.BLOCK_ENTITIES, Ouroboros.MOD_ID)

  def forEntityBlock[M <: BaseEntityBlock, T <: BlockEntity](
      name: String,
      companion: BaseEntityBlock.Companion[M, T]
  ): RegistryObject[BlockEntityType[T]] =
    _registry.register(
      name,
      { () =>
        BlockEntityType.Builder
          .of(companion.entity, companion.block())
          .build(null)
      }
    )

  val STRUCTURE: RegistryObject[BlockEntityType[StructureEntity]] =
    forEntityBlock("structure", StructureBlock)

  val CRUSHER: RegistryObject[BlockEntityType[CrusherMachineEntity]] =
    forEntityBlock("crusher", CrusherMachineBlock)

}

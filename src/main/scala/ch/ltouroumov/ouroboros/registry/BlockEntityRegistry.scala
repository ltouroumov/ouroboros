package ch.ltouroumov.ouroboros.registry

import ch.ltouroumov.ouroboros.blocks.{BaseBlockEntity, BaseEntityBlock}
import ch.ltouroumov.ouroboros.blocks.crusher.{CrusherMachineBlock, CrusherMachineEntity}
import ch.ltouroumov.ouroboros.blocks.structure.{StructureBlock, StructureEntity}
import net.minecraft.world.level.block.entity.{BlockEntity, BlockEntityType}
import net.minecraftforge.registries.{ForgeRegistries, RegistryObject}

object BlockEntityRegistry extends AbstractRegistry(ForgeRegistries.BLOCK_ENTITIES) {

  def forEntityBlock[M <: BaseEntityBlock, T <: BlockEntity](
      name: String,
      companion: BaseEntityBlock.Companion[M, T],
      entity: BaseBlockEntity.Companion[T]
  ): RegistryObject[BlockEntityType[T]] =
    registry.register(
      name,
      { () =>
        BlockEntityType.Builder
          .of(entity.create, companion.block)
          .build(null)
      }
    )

  val STRUCTURE: RegistryObject[BlockEntityType[StructureEntity]] =
    forEntityBlock("structure", StructureBlock, StructureEntity)

  val CRUSHER: RegistryObject[BlockEntityType[CrusherMachineEntity]] =
    forEntityBlock("crusher", CrusherMachineBlock, CrusherMachineEntity)

}

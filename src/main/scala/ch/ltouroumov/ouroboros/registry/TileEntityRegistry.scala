package ch.ltouroumov.ouroboros.registry

import ch.ltouroumov.ouroboros.Ouroboros
import ch.ltouroumov.ouroboros.blocks.BaseEntityBlock
import ch.ltouroumov.ouroboros.blocks.machine.{CrusherMachineBlock, CrusherMachineEntity}
import net.minecraft.tileentity.{TileEntity, TileEntityType}
import net.minecraftforge.fml.RegistryObject
import net.minecraftforge.registries.{DeferredRegister, ForgeRegistries}

object TileEntityRegistry {

  val _registry: DeferredRegister[TileEntityType[_]] =
    DeferredRegister.create(ForgeRegistries.TILE_ENTITIES, Ouroboros.MOD_ID)

  def forMachineBlock[M <: BaseEntityBlock, T <: TileEntity](
      name: String,
      companion: BaseEntityBlock.Companion[M, T]
  ): RegistryObject[TileEntityType[T]] =
    _registry.register(
      name,
      { () =>
        TileEntityType.Builder
          .of(() => companion.entity(), companion.block())
          .build(null)
      }
    )

  val CRUSHER: RegistryObject[TileEntityType[CrusherMachineEntity]] =
    forMachineBlock("crusher", CrusherMachineBlock)

}

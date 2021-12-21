package ch.ltouroumov.ouroboros.registry

import ch.ltouroumov.ouroboros.Ouroboros
import net.minecraftforge.registries.{DeferredRegister, IForgeRegistry, IForgeRegistryEntry}

abstract class AbstractRegistry[T <: IForgeRegistryEntry[T]](forgeRegistry: IForgeRegistry[T]) {
  val registry: DeferredRegister[T] = DeferredRegister.create(forgeRegistry, Ouroboros.MOD_ID)
}

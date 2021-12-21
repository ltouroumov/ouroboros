package ch.ltouroumov.ouroboros.registry

import ch.ltouroumov.ouroboros.blocks.crusher.CrusherMachineMenu
import net.minecraft.world.inventory.MenuType
import net.minecraftforge.common.extensions.IForgeMenuType
import net.minecraftforge.registries.{ForgeRegistries, RegistryObject}

object ContainerRegistry extends AbstractRegistry(ForgeRegistries.CONTAINERS) {

  val CRUSHER_MENU: RegistryObject[MenuType[CrusherMachineMenu]] =
    registry.register(
      "crusher_menu",
      () => IForgeMenuType.create(CrusherMachineMenu.apply _)
    )

}

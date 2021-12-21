package ch.ltouroumov.ouroboros.blocks

import ch.ltouroumov.ouroboros.blocks.crusher.CrusherMachineScreen
import ch.ltouroumov.ouroboros.registry.ContainerRegistry
import net.minecraft.client.gui.screens.MenuScreens
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent

object BlockEventHandler {

  def onClientSetup(event: FMLClientSetupEvent): Unit = {
    MenuScreens.register(ContainerRegistry.CRUSHER_MENU.get(), CrusherMachineScreen.apply _)
  }

}

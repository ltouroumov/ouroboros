package ch.ltouroumov.ouroboros

import ch.ltouroumov.ouroboros.registry.{BlockEntityRegistry, BlockItemsRegistry, BlocksRegistry}
import ch.ltouroumov.ouroboros.utils.StrictLogging
import net.minecraft.world.item.{CreativeModeTab, ItemStack}
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.server.ServerStartingEvent
import net.minecraftforge.eventbus.api.{IEventBus, SubscribeEvent}
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext

// The value here should match an entry in the META-INF/mods.toml file
@Mod(Ouroboros.MOD_ID)
object Ouroboros extends StrictLogging {
  final val MOD_ID = "ouroboros"

  private val _meb: IEventBus = FMLJavaModLoadingContext.get.getModEventBus

  BlocksRegistry._registry.register(_meb)
  BlockItemsRegistry._registry.register(_meb)
  BlockEntityRegistry._registry.register(_meb)

  // Register ourselves for server and other game events we are interested in
  MinecraftForge.EVENT_BUS.register(this)

  // Custom ItemGroup TAB
  val CREATIVE_TAB: CreativeModeTab = new CreativeModeTab("ouroboros") {
    override def makeIcon: ItemStack = new ItemStack(BlockItemsRegistry.STRUCTURE_T0.get)
  }

  // You can use SubscribeEvent and let the Event Bus discover methods to call
  @SubscribeEvent
  def onServerStarting(event: ServerStartingEvent): Unit = {
    // do something when the server starts
    logger.info("HELLO from server starting")
  }

}

package ch.ltouroumov.ouroboros

import ch.ltouroumov.ouroboros.registry.{BlockItemsRegistry, BlocksRegistry, TileEntityRegistry}
import net.minecraft.block.{Block, Blocks}
import net.minecraft.item.{Item, ItemGroup, ItemStack}
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.RegistryEvent
import net.minecraftforge.eventbus.api.{IEventBus, SubscribeEvent}
import net.minecraftforge.fml.InterModComms
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.event.lifecycle.{
  FMLClientSetupEvent,
  FMLCommonSetupEvent,
  InterModEnqueueEvent,
  InterModProcessEvent
}
import net.minecraftforge.fml.event.server.FMLServerStartingEvent
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext
import org.apache.logging.log4j.LogManager

// The value here should match an entry in the META-INF/mods.toml file
@Mod(Ouroboros.MOD_ID)
object Ouroboros {
  final val MOD_ID = "ouroboros"
  // Directly reference a log4j logger.
  private val LOGGER = LogManager.getLogger

  private val _meb: IEventBus = FMLJavaModLoadingContext.get.getModEventBus

  _meb.addListener(this.setup)
  _meb.addListener(this.enqueueIMC)
  _meb.addListener(this.processIMC)
  _meb.addListener(this.doClientStuff)

  BlocksRegistry._registry.register(_meb)
  BlockItemsRegistry._registry.register(_meb)
  TileEntityRegistry._registry.register(_meb)

  // Register ourselves for server and other game events we are interested in
  MinecraftForge.EVENT_BUS.register(this)

  // Custom ItemGroup TAB
  val CREATIVE_TAB: ItemGroup = new ItemGroup("ouroboros") {
    override def makeIcon: ItemStack = new ItemStack(BlockItemsRegistry.STRUCTURE_T0.get)
  }

  private def setup(event: FMLCommonSetupEvent): Unit = {
    // some preinit code
    LOGGER.info("HELLO FROM PREINIT")
    LOGGER.info("DIRT BLOCK >> {}", BlocksRegistry.STRUCTURE.getId)
  }

  private def doClientStuff(event: FMLClientSetupEvent): Unit = {
    // do something that can only be done on the client
    LOGGER.info("Got game settings {}", event.getMinecraftSupplier.get.options)
  }

  private def enqueueIMC(event: InterModEnqueueEvent): Unit = {
    // some example code to dispatch IMC to another mod
    InterModComms.sendTo(
      "ouroboros",
      "helloworld",
      () => {
        LOGGER.info("Hello world from the MDK")
        "Hello world"
      }
    )
  }

  private def processIMC(event: InterModProcessEvent): Unit = {
    // some example code to receive and process InterModComms from other mods
    import scala.jdk.StreamConverters._
    val messages = event.getIMCStream.toScala(LazyList).map(_.getMessageSupplier[String].get())
    LOGGER.info("Got IMC {}", messages.mkString(", "))
  }

  // You can use SubscribeEvent and let the Event Bus discover methods to call
  @SubscribeEvent
  def onServerStarting(event: FMLServerStartingEvent): Unit = {
    // do something when the server starts
    LOGGER.info("HELLO from server starting")
  }

}

// You can use EventBusSubscriber to automatically subscribe events on the contained class (this is subscribing to the MOD
// Event bus for receiving Registry Events)
// The object must be at the top-level. Don't forget to fill modid.
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = Ouroboros.MOD_ID)
object RegistryEvents {
  private val LOGGER = LogManager.getLogger

  @SubscribeEvent
  def onBlocksRegistry(blockRegistryEvent: RegistryEvent.Register[Block]): Unit = {
    // register a new block here
    LOGGER.info("HELLO from Register Block")
  }

  @SubscribeEvent
  def onItemsRegistry(blockRegistryEvent: RegistryEvent.Register[Item]): Unit = {
    // register a new block here
    LOGGER.info("HELLO from Register Item")
  }
}

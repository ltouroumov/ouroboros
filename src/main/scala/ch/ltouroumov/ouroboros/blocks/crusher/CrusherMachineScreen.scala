package ch.ltouroumov.ouroboros.blocks.crusher

import ch.ltouroumov.ouroboros.Ouroboros
import ch.ltouroumov.ouroboros.blocks.crusher.CrusherMachineScreen.CONTAINER_BACKGROUND
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.client.renderer.GameRenderer
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.player.Inventory
import net.minecraftforge.api.distmarker.{Dist, OnlyIn}

@OnlyIn(Dist.CLIENT)
class CrusherMachineScreen(menu: CrusherMachineMenu, playerInventory: Inventory, menuName: Component)
    extends AbstractContainerScreen[CrusherMachineMenu](menu, playerInventory, menuName) {

  imageWidth = 180
  imageHeight = 208

  override def render(poseStack: PoseStack, mouseX: Int, mouseY: Int, partialTicks: Float): Unit = {
    this.renderBackground(poseStack)
    super.render(poseStack, mouseX, mouseY, partialTicks)
    this.renderTooltip(poseStack, mouseX, mouseY)
  }

  override protected def renderBg(poseStack: PoseStack, mouseX: Float, mouseY: Int, partialTicks: Int): Unit = {
    RenderSystem.setShader(GameRenderer.getPositionTexShader _)
    RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f)
    RenderSystem.setShaderTexture(0, CONTAINER_BACKGROUND)
    this.blit(poseStack, leftPos, topPos, 0, 0, this.imageWidth, this.imageHeight)
  }
}

object CrusherMachineScreen {

  val CONTAINER_BACKGROUND: ResourceLocation =
    new ResourceLocation(Ouroboros.MOD_ID, "textures/gui/crusher_gui.png")

  def apply(menu: CrusherMachineMenu, playerInventory: Inventory, menuName: Component): CrusherMachineScreen =
    new CrusherMachineScreen(menu, playerInventory, menuName)

}

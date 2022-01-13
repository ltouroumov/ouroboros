package ch.ltouroumov.ouroboros.blocks.crusher

import ch.ltouroumov.ouroboros.utils.StrictLogging
import com.google.gson.JsonObject
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.GsonHelper
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.{Ingredient, Recipe, RecipeSerializer, RecipeType}
import net.minecraft.world.level.Level
import net.minecraftforge.common.crafting.CraftingHelper
import net.minecraftforge.registries.ForgeRegistryEntry

class CrusherRecipe(val id: ResourceLocation, val input: Ingredient, val output: ItemStack, val processingTime: Int)
    extends Recipe[CrusherMachineEntity]
    with StrictLogging {
  override def matches(container: CrusherMachineEntity, level: Level): Boolean = {
    CrusherMachineEntity.INPUT_SLOTS.exists { slot =>
      input.test(container.getItem(slot))
    }
  }

  override def assemble(container: CrusherMachineEntity): ItemStack      = output.copy()
  override def canCraftInDimensions(p_43999: Int, p_44000: Int): Boolean = true
  override def getResultItem: ItemStack                                  = output
  override def getId: ResourceLocation                                   = id

  override def getSerializer: RecipeSerializer[CrusherRecipe] = CrusherRecipe.Serializer
  override def getType: RecipeType[CrusherRecipe]             = CrusherRecipe.RecipeType
}

object CrusherRecipe {

  val DefaultProcessingTime: Int = 100

  object RecipeType extends RecipeType[CrusherRecipe]

  object Serializer
      extends ForgeRegistryEntry[RecipeSerializer[_]]
      with RecipeSerializer[CrusherRecipe]
      with StrictLogging {
    override def fromJson(location: ResourceLocation, json: JsonObject): CrusherRecipe = {
      val input          = Ingredient.fromJson(GsonHelper.getAsJsonObject(json, "input"))
      val output         = CraftingHelper.getItemStack(GsonHelper.getAsJsonObject(json, "output"), true, true)
      val processingTime = GsonHelper.getAsInt(json, "processing_time", DefaultProcessingTime);
      new CrusherRecipe(location, input, output, processingTime)
    }

    override def fromNetwork(location: ResourceLocation, buffer: FriendlyByteBuf): CrusherRecipe = {
      val input          = Ingredient.fromNetwork(buffer)
      val output         = buffer.readItem()
      val processingTime = buffer.readVarInt()

      new CrusherRecipe(location, input, output, processingTime)
    }

    override def toNetwork(buffer: FriendlyByteBuf, recipe: CrusherRecipe): Unit = {
      recipe.input.toNetwork(buffer)
      buffer.writeItemStack(recipe.output, false)
      buffer.writeVarInt(recipe.processingTime)
    }
  }

}

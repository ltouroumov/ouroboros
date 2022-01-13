package ch.ltouroumov.ouroboros.registry

import ch.ltouroumov.ouroboros.blocks.crusher.CrusherRecipe
import net.minecraftforge.registries.ForgeRegistries

object RecipeTypeRegistry extends AbstractRegistry(ForgeRegistries.RECIPE_SERIALIZERS) {

  registry.register(
    "crusher",
    { () =>
      CrusherRecipe.Serializer
    }
  )

}

package io.github.nattocb.treasure_seas.common.registry;

import io.github.nattocb.treasure_seas.TreasureSeas;
import io.github.nattocb.treasure_seas.common.recipe.serializer.GyotakuRecipeSerializer;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModRecipeSerializers {

    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, TreasureSeas.MOD_ID);

    public static final RegistryObject<RecipeSerializer<?>> GYOTAKU_RECIPE_SERIALIZER =
            SERIALIZERS.register("gyotaku_shapeless", GyotakuRecipeSerializer::new);

}

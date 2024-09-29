package io.github.nattocb.treasure_seas.common.recipe.serializer;

import com.google.gson.JsonObject;
import io.github.nattocb.treasure_seas.common.recipe.GyotakuShapelessRecipe;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import org.jetbrains.annotations.NotNull;

public class GyotakuRecipeSerializer extends ShapelessRecipe.Serializer {

    @Override
    public @NotNull GyotakuShapelessRecipe fromJson(@NotNull ResourceLocation recipeId, @NotNull JsonObject json) {
        ShapelessRecipe recipe = super.fromJson(recipeId, json);
        return new GyotakuShapelessRecipe(
                recipe.getId(),
                recipe.getGroup(),
                recipe.getResultItem(),
                recipe.getIngredients()
        );
    }

    @Override
    public GyotakuShapelessRecipe fromNetwork(@NotNull ResourceLocation recipeId, @NotNull FriendlyByteBuf buffer) {
        ShapelessRecipe recipe = super.fromNetwork(recipeId, buffer);
        return new GyotakuShapelessRecipe(
                recipe.getId(),
                recipe.getGroup(),
                recipe.getResultItem(),
                recipe.getIngredients()
        );
    }

}


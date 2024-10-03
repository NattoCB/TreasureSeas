package io.github.nattocb.treasure_seas.core.mixin;

import io.github.nattocb.treasure_seas.common.registry.ModEnchantments;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FishingHook.class)
public abstract class FishingHookMixin {


    /**
     * 在 retrieve 方法执行后注入代码，以根据附魔效果重置鱼竿耐久度。
     *
     * @param stack 鱼竿的 ItemStack
     * @param cir   回调信息
     */
    @Inject(
            method = "retrieve",
            at = @At("RETURN")
    )
    private void onRetrieveAfter(ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        if (stack != null) {
            // 检查鱼竿是否具有 Fish Fighter 附魔
            int enchantLevel = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.FISH_FIGHTER.get(), stack);
            if (enchantLevel > 0) {
                // 重置鱼竿耐久度为0
                stack.setDamageValue(0);
            }
        }
    }

}

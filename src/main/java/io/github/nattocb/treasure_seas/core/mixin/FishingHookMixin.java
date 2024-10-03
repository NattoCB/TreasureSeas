package io.github.nattocb.treasure_seas.core.mixin;

import io.github.nattocb.treasure_seas.common.registry.ModEnchantments;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public abstract class FishingHookMixin {

    /**
     * 在 isDamageableItem 方法开始处注入代码，
     * 如果 ItemStack 具有 Fish Fighter 附魔，则返回 false。
     *
     * @param cir 回调信息
     */
    @Inject(method = "isDamageableItem", at = @At("HEAD"), cancellable = true)
    private void onIsDamageableItem(CallbackInfoReturnable<Boolean> cir) {
        ItemStack stack = (ItemStack) (Object) this;
        // 检查鱼竿是否具有 Fish Fighter 附魔
        int enchantLevel = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.FISH_FIGHTER.get(), stack);
        if (enchantLevel > 0) {
            // 如果具有附魔，则取消方法的默认行为并返回 false
            cir.setReturnValue(false);
        }
    }

}

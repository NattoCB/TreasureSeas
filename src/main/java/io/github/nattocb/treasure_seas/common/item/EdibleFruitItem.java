package io.github.nattocb.treasure_seas.common.item;

import io.github.nattocb.treasure_seas.TreasureSeas;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

public class EdibleFruitItem extends Item {

    private final double attributeAmount;
    private final String attributeName;
    private final int maxUses;
    private final String useTag;
    private final List<Component> baseLore;

    public EdibleFruitItem(Properties properties, double attributeAmount, String attributeName, int maxUses, String useTag, List<Component> baseLore) {
        super(properties.food(new FoodProperties.Builder().nutrition(4).saturationMod(0.3F).alwaysEat().build()));
        this.attributeAmount = attributeAmount;
        this.attributeName = attributeName;
        this.maxUses = maxUses;
        this.useTag = useTag;
        this.baseLore = baseLore;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level world, LivingEntity entity) {
        if (!world.isClientSide && entity instanceof Player player) {
            int uses = player.getPersistentData().getInt(useTag);

            if (uses < maxUses) {
                AttributeModifier modifier = new AttributeModifier(
                        "EdibleFruitItemModifier",
                        attributeAmount,
                        AttributeModifier.Operation.ADDITION
                );

                AttributeInstance attributeInstance = null;

                switch (attributeName) {
                    case "generic.attack_damage":
                        attributeInstance = player.getAttribute(Attributes.ATTACK_DAMAGE);
                        break;
                    case "generic.max_health":
                        attributeInstance = player.getAttribute(Attributes.MAX_HEALTH);
                        break;
                }

                if (attributeInstance != null) {
                    attributeInstance.addPermanentModifier(modifier);
                } else {
                    TreasureSeas.getLogger().error("Warning: Attribute '" + attributeName + "' not found for player " + player.getName().getString());
                }

                player.getPersistentData().putInt(useTag, uses + 1);
            }
        }
        return super.finishUsingItem(stack, world, entity);
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.EAT;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, world, tooltip, flag);
        tooltip.addAll(baseLore);
        String owner = getOwner(stack);
        if (owner != null) {
            tooltip.add(new TranslatableComponent("item.treasure_seas.bound_to", owner));
        }
    }

    public static void setOwner(ItemStack stack, Player player) {
        CompoundTag tag = stack.getOrCreateTag();
        if (!tag.contains("owner")) {
            tag.putString("owner", player.getName().getString());
        }
    }

    public static String getOwner(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null ? tag.getString("owner") : null;
    }

    public static boolean isOwner(ItemStack stack, Player player) {
        String owner = getOwner(stack);
        return owner == null || owner.equals(player.getName().getString());
    }

}
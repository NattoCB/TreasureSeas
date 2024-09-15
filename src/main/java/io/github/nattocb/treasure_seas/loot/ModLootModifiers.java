package io.github.nattocb.treasure_seas.loot;

import io.github.nattocb.treasure_seas.TreasureSeas;
import net.minecraftforge.common.loot.GlobalLootModifierSerializer;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModLootModifiers {
    public static final DeferredRegister<GlobalLootModifierSerializer<?>> LOOT_MODIFIER_SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.Keys.LOOT_MODIFIER_SERIALIZERS, TreasureSeas.MOD_ID);

    /**
     * add_loot_table_modifier 对应 data/treasure_seas/loot_modifiers/target.json 的 type 项
     */
    public static final RegistryObject<GlobalLootModifierSerializer<AddLootTableModifier>> ADD_LOOT_TABLE_MODIFIER =
            LOOT_MODIFIER_SERIALIZERS.register("add_loot_table_modifier", AddLootTableModifier.Serializer::new);
}

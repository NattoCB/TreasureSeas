package io.github.nattocb.treasure_seas.loot;

import io.github.nattocb.treasure_seas.TreasureSeas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraftforge.common.loot.GlobalLootModifierSerializer;
import net.minecraftforge.common.loot.LootModifier;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import javax.annotation.Nonnull;
import com.google.gson.JsonObject;

import java.util.List;

public class AddLootTableModifier extends LootModifier {

    /**
     * Forge 先通过 data/forge/loot_modifiers/global_loot_modifier.json 遍历所有下列 target.json 文件
     * 对应 data/treasure_seas/loot_modifiers/target.json 的 lootTable 项内容
     * 也就是最终指向 data/treasure_seas/loot_tables/target2.json
     * 即本 lootTable 成员的内容，从而实际在对应事件触发时，对应的 doApply 会被执行附加 lootTable 到原本的 lootTable
     */
    private final ResourceLocation lootTable;

    protected AddLootTableModifier(LootItemCondition[] conditionsIn, ResourceLocation lootTable) {
        super(conditionsIn);
        this.lootTable = lootTable;
    }

    @Nonnull
    @Override
    protected List<ItemStack> doApply(List<ItemStack> generatedLoot, LootContext context) {
        // Retrieve the loot table from the context
        final LootTable lootTable = context.getLootTable(this.lootTable);
        if (lootTable != null) {
            TreasureSeas.getLogger().dev("Applying loot table: {}", this.lootTable);
            // Add all items from the additional loot table to the generated loot
            lootTable.getRandomItems(context, generatedLoot::add);
            TreasureSeas.getLogger().dev("Generated additional loot: {}", generatedLoot);
        }
        return generatedLoot;
    }

    public static class Serializer extends GlobalLootModifierSerializer<AddLootTableModifier> {
        @Override
        public AddLootTableModifier read(ResourceLocation location, JsonObject object, LootItemCondition[] conditions) {
            TreasureSeas.getLogger().dev("Reading loot modifier for: {}", location);
            ResourceLocation lootTable = new ResourceLocation(GsonHelper.getAsString(object, "loot_table"));
            return new AddLootTableModifier(conditions, lootTable);
        }
        @Override
        public JsonObject write(AddLootTableModifier instance) {
            JsonObject json = this.makeConditions(instance.conditions);
            json.addProperty("loot_table", instance.lootTable.toString());
            return json;
        }
    }

}

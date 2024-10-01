package io.github.nattocb.treasure_seas.common.tag;

import io.github.nattocb.treasure_seas.TreasureSeas;
import io.github.nattocb.treasure_seas.core.config.ConfigManager;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackCompatibility;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.repository.RepositorySource;
import net.minecraftforge.event.AddPackFindersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = TreasureSeas.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class DataPackInjector {

    @SubscribeEvent
    public static void onAddPackFinders(AddPackFindersEvent event) {
        if (event.getPackType() == PackType.SERVER_DATA) {
            ConfigManager configManager = TreasureSeas.getInstance().getConfigManager();
            try (PackResources resourcePack = new DynamicResourcePack(configManager.getFishWrapperMap())) {
                RepositorySource repositorySource = (packConsumer, packConstructor) -> {
                    Pack pack = Pack.create(
                            "treasure_seas_dynamic_resource_pack",
                            true,
                            () -> resourcePack,
                            (id, title, required, supplier, metadata, position, source, hidden) -> new Pack(
                                    id,
                                    required,
                                    supplier,
                                    new TextComponent("Treasure Seas Dynamic Data Pack"),
                                    metadata.getDescription(),
                                    PackCompatibility.forMetadata(metadata, PackType.SERVER_DATA),
                                    position,
                                    false,
                                    source,
                                    hidden
                            ),
                            Pack.Position.TOP,
                            PackSource.BUILT_IN
                    );
                    packConsumer.accept(pack);
                };

                event.addRepositorySource(repositorySource);
            } catch (Exception e) {
                TreasureSeas.getLogger().error("Failed to addPackFinders: {}", e.getMessage(), e);
            }
        }
    }

}
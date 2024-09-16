package io.github.nattocb.treasure_seas.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.github.nattocb.treasure_seas.TreasureSeas;
import io.github.nattocb.treasure_seas.config.FishConfigManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber
public class TreasureSeasCommand {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        LiteralArgumentBuilder<CommandSourceStack> builder = Commands.literal("treasureseas")
                .then(Commands.literal("reload")
                        .then(Commands.literal("client")
                                .requires(source -> source.hasPermission(0))
                                .executes(context -> reloadClient(context.getSource())))
                        .then(Commands.literal("common")
                                .requires(source -> source.hasPermission(2))
                                .executes(context -> reloadCommon(context.getSource()))))
                .then(Commands.literal("log_biomes")
                        .requires(source -> source.hasPermission(2))
                        .executes(context -> logBiomes(context.getSource())))
                .then(Commands.literal("log_world_paths")
                        .requires(source -> source.hasPermission(2))
                        .executes(context -> logWorldPaths(context.getSource())));

        dispatcher.register(builder);
    }

    private static int reloadCommon(CommandSourceStack source) {
        long startTs = System.currentTimeMillis();
        FishConfigManager configManager = TreasureSeas.getInstance().getFishConfigManager();
        configManager.loadCommonConfig();
        configManager.loadServerConfig();
        source.sendSuccess(new TextComponent("TreasureSeas common configuration reloaded in " + (System.currentTimeMillis() - startTs) + " ms!"), true);
        return 1;
    }

    private static int reloadClient(CommandSourceStack source) {
        long startTs = System.currentTimeMillis();
        FishConfigManager configManager = TreasureSeas.getInstance().getFishConfigManager();
        configManager.loadClientConfig();
        source.sendSuccess(new TextComponent("TreasureSeas client configuration reloaded in " + (System.currentTimeMillis() - startTs) + " ms!"), true);
        return 1;
    }

    private static int logBiomes(CommandSourceStack source) {
        TreasureSeas.getLogger().info("Logging all registered biomes:");
        for (Biome biome : ForgeRegistries.BIOMES) {
            ResourceLocation key = ForgeRegistries.BIOMES.getKey(biome);
            if (key != null) {
                String biomeName = key.toString();
                TreasureSeas.getLogger().info("Biome: " + biomeName);
            }
        }
        source.sendSuccess(new TextComponent("Logged all biomes to the console."), true);
        return 1;
    }

    private static int logWorldPaths(CommandSourceStack source) {
        MinecraftServer server = source.getServer();
        TreasureSeas.getLogger().info("Logging all world paths:");
        for (ServerLevel world : server.getAllLevels()) {
            String worldPath = world.dimension().location().getPath();
            TreasureSeas.getLogger().info("World path: " + worldPath);
        }
        source.sendSuccess(new TextComponent("Logged all world paths to the console."), true);
        return 1;
    }

}

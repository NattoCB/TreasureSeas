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
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("reload")
                        .executes(context -> reloadConfig(context.getSource())))
                .then(Commands.literal("log_biomes")
                        .executes(context -> logBiomes(context.getSource())))
                .then(Commands.literal("log_world_paths")
                        .executes(context -> logWorldPaths(context.getSource())));

        dispatcher.register(builder);
    }

    private static int reloadConfig(CommandSourceStack source) {
        long startTs = System.currentTimeMillis();
        FishConfigManager configManager = TreasureSeas.getInstance().getFishConfigManager();
        configManager.loadConfig();
        source.sendSuccess(new TextComponent("TreasureSeas configuration reloaded in " + (System.currentTimeMillis() - startTs) + " ms!"), true);
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

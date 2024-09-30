package io.github.nattocb.treasure_seas.core.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.github.nattocb.treasure_seas.TreasureSeas;
import io.github.nattocb.treasure_seas.core.FishWrapper;
import io.github.nattocb.treasure_seas.core.config.ConfigManager;
import io.github.nattocb.treasure_seas.core.packet.FishFightPacket;
import io.github.nattocb.treasure_seas.core.packet.PacketHandler;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Map;
import java.util.stream.Collectors;

@Mod.EventBusSubscriber
public class TSeasCommands {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        LiteralArgumentBuilder<CommandSourceStack> builder = Commands.literal("treasureseas")
                .then(Commands.literal("reload")
                        .then(Commands.literal("common")
                                .requires(source -> source.hasPermission(2))
                                .executes(context -> reloadCommon(context.getSource()))))
                .then(Commands.literal("log_biomes")
                        .requires(source -> source.hasPermission(2))
                        .executes(context -> logBiomes(context.getSource())))
                .then(Commands.literal("log_world_paths")
                        .requires(source -> source.hasPermission(2))
                        .executes(context -> logWorldPaths(context.getSource())))
                .then(Commands.literal("test")
                        .then(Commands.argument("fish_name", StringArgumentType.word())
                                .requires(source -> source.hasPermission(2))
                                .suggests(
                                        (context, builder1) ->
                                                SharedSuggestionProvider.suggest(
                                                        TreasureSeas.getInstance().getConfigManager().getFishWrapperMap().keySet()
                                                                .stream().map(k -> k.replace(":", "+")),
                                                        builder1)
                                )
                                .executes(context -> testCommand(context.getSource(), StringArgumentType.getString(context, "fish_name")))));

        dispatcher.register(builder);
    }

    private static int reloadCommon(CommandSourceStack source) {
        long startTs = System.currentTimeMillis();
        ConfigManager configManager = TreasureSeas.getInstance().getConfigManager();
        configManager.loadCommonConfig();
        configManager.loadServerConfig();
        source.sendSuccess(new TranslatableComponent("command.treasureseas.reload.success", (System.currentTimeMillis() - startTs)), true);
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
        source.sendSuccess(new TranslatableComponent("command.treasureseas.log_biomes.success"), true);
        return 1;
    }

    private static int logWorldPaths(CommandSourceStack source) {
        MinecraftServer server = source.getServer();
        TreasureSeas.getLogger().info("Logging all world paths:");
        for (ServerLevel world : server.getAllLevels()) {
            String worldPath = world.dimension().location().getPath();
            TreasureSeas.getLogger().info("World path: " + worldPath);
        }
        source.sendSuccess(new TranslatableComponent("command.treasureseas.log_world_paths.success"), true);
        return 1;
    }


    private static int testCommand(CommandSourceStack source, String fishName) {
        try {
            fishName = fishName.replace("+", ":");
            ServerPlayer player = source.getPlayerOrException();
            Map<String, FishWrapper> fishWrapperMap = TreasureSeas.getInstance().getConfigManager().getFishWrapperMap();
            FishWrapper chosenFish = fishWrapperMap.get(fishName);
            if (chosenFish == null) {
                source.sendFailure(new TranslatableComponent("command.treasureseas.test.not_found", fishName));
                return 0;
            }
            // Send the packet to the player
            PacketHandler.CHANNEL.send(
                    PacketDistributor.PLAYER.with(() -> player),
                    new FishFightPacket(player.position(), chosenFish)
            );
            source.sendSuccess(new TranslatableComponent("command.treasureseas.test.success", fishName), true);
            return 1;
        } catch (Exception e) {
            source.sendFailure(new TranslatableComponent("command.treasureseas.test.error", e.getMessage()));
            return 0;
        }
    }

}

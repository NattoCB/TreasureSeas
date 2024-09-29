package io.github.nattocb.treasure_seas;

import io.github.nattocb.treasure_seas.core.config.ConfigManager;
import io.github.nattocb.treasure_seas.common.enchantment.FishFighterEnchantment;
import io.github.nattocb.treasure_seas.core.proxy.ClientProxy;
import io.github.nattocb.treasure_seas.core.proxy.CommonProxy;
import io.github.nattocb.treasure_seas.common.registry.*;
import io.github.nattocb.treasure_seas.core.utility.LogManager;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.LoggerFactory;

import java.util.Random;

/**
 * @author Natto_CB
 */
@Mod(TreasureSeas.MOD_ID)
@Mod.EventBusSubscriber(modid = TreasureSeas.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class TreasureSeas {

    public static final String MOD_ID = "treasure_seas";

    private static TreasureSeas instance;

    public static final Random RANDOM = new Random();

    private static ConfigManager configManager;

    private static LogManager logManager;

    public static CommonProxy PROXY;

    public TreasureSeas() {
        // 单例注册
        instance = this;
        // 同方法下的双端独有逻辑注册
        PROXY = DistExecutor.safeRunForDist(
                () -> ClientProxy::new,
                () -> CommonProxy::new
        );
        // 注册日志管理
        logManager = new LogManager(LoggerFactory.getLogger(TreasureSeas.class));
        // 方块、物品、附魔、容器、战利品注册
        ModBlockEntities.BLOCK_ENTITIES.register(FMLJavaModLoadingContext.get().getModEventBus());
        ModContainerTypes.CONTAINERS.register(FMLJavaModLoadingContext.get().getModEventBus());
        ModBlocks.BLOCKS.register(FMLJavaModLoadingContext.get().getModEventBus());
        ModBlocks.ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
        ModItems.ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
        ModLootModifiers.LOOT_MODIFIER_SERIALIZERS.register(FMLJavaModLoadingContext.get().getModEventBus());
        ModEnchantments.register();
    }

    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event) {
        // 因为配置中可能涉及到其他模组的鱼类物品，所以在这里才初始化，确保其余模组在 FML 已经加载完成
        configManager = new ConfigManager();
        configManager.loadCommonConfig();
        configManager.loadServerConfig();
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        // 只在客户端加载 client 配置
        configManager.loadClientConfig();
    }

    public static TreasureSeas getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public static LogManager getLogger() {
        return logManager;
    }

    // todo 增加暮色默认鱼
    // todo 个人计分板（服务器排行榜？如何联动）

}

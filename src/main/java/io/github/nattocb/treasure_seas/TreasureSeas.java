package io.github.nattocb.treasure_seas;

import io.github.nattocb.treasure_seas.config.FishConfigManager;
import io.github.nattocb.treasure_seas.enchantment.FishFighterEnchantment;
import io.github.nattocb.treasure_seas.proxy.ClientProxy;
import io.github.nattocb.treasure_seas.proxy.CommonProxy;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
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

    private static FishConfigManager fishConfigManager;

    private static LogManager logManager;

    public static RegistryObject<Enchantment> FISH_FIGHTER;

    public static CommonProxy PROXY;

    public TreasureSeas() {
        // 单例注册
        instance = this;
        // 同方法下的双端独有逻辑注册
        PROXY = DistExecutor.safeRunForDist(
                () -> ClientProxy::new,
                () -> CommonProxy::new
        );
        // 方块、物品、附魔、容器注册
        DeferredRegister<Enchantment> enchantments = DeferredRegister.create(ForgeRegistries.ENCHANTMENTS, TreasureSeas.MOD_ID);
        enchantments.register(FMLJavaModLoadingContext.get().getModEventBus());
        FISH_FIGHTER = enchantments.register("fish_fighter", FishFighterEnchantment::new);
        ModBlockEntities.BLOCK_ENTITIES.register(FMLJavaModLoadingContext.get().getModEventBus());
        ModContainerTypes.CONTAINERS.register(FMLJavaModLoadingContext.get().getModEventBus());
        ModBlocks.BLOCKS.register(FMLJavaModLoadingContext.get().getModEventBus());
        ModBlocks.ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
        ModItems.ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
        // 注册日志 handler
        logManager = new LogManager(LoggerFactory.getLogger(TreasureSeas.class));
    }

    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event) {
        // 因为配置中可能涉及到其他模组的鱼类物品，所以在这里才初始化，确保其余模组在 FML 已经加载完成
        fishConfigManager = new FishConfigManager();
        fishConfigManager.loadCommonConfig();
        fishConfigManager.loadClientConfig();
    }

    public static TreasureSeas getInstance() {
        return instance;
    }

    public FishConfigManager getFishConfigManager() {
        return fishConfigManager;
    }

    public static LogManager getLogger() {
        return logManager;
    }

    // todo 附魔等级越高 loot 该附魔书几率越低
    // todo 低等级附魔钓不到某个百分比阈值以上的长度的鱼（是否需要加？）
    // todo 统计水域面积，显示过小适中广阔，过小时只能掉到垃圾，但避免计算无限大，最大不要超过 16 * 16，过大则 tootip 显示（广阔）
    // todo 客户端配置在每次进入游戏时重载
    // todo 不同的稀有度会越来越难钓（对于低等级钓竿来说）
    // todo 甩竿决定深度，新 gui
    // todo 模组建议：搭配：
    //          旅行背包
    //          水产业
    //          Mouse Tweak
    //          可选联动：Unusual Fish Mod、Fish of Thieves、螃农乐事、炽海生机
    //          （提示配置者可自行配置新mod鱼类（降低本模组的加载优先级，在最后））
    // todo 如果 world path not found，默认用 overworld（但前提是，世界类型需要是 overworld）从而支持 multiverse 的其他主世界
    // todo mod logo png in toml file
    // todo 个人计分板（服务器排行榜？如何联动）
    // todo 写 readme
    // todo 岩浆、末地、暮色 default fish
}

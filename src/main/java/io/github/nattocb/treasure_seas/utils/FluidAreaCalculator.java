package io.github.nattocb.treasure_seas.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.*;

/**
 * 液体裸露面积计算器
 * 基于世界坐标缓存，自动定时清理
 */
public class FluidAreaCalculator {

    private static final ConcurrentHashMap<BlockPos, CachedFluidArea> FLUID_AREA_CACHE = new ConcurrentHashMap<>();

    // 缓存生命周期毫秒
    private static final long CACHE_EXPIRY_TIME = 60 * 1000;

    // 超过阈值及时停止
    private static final int AREA_THRESHOLD = 20;

    // 定时任务，每分钟清理一次缓存
    static {
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        executor.scheduleAtFixedRate(() -> {
            long currentTime = System.currentTimeMillis();
            FLUID_AREA_CACHE.entrySet().removeIf(entry -> currentTime - entry.getValue().timestamp > CACHE_EXPIRY_TIME);
        }, 1, 1, TimeUnit.MINUTES);
    }

    private static int calculateExposedFluidArea(Level world, BlockPos startPos) {
        // 检查缓存
        CachedFluidArea cachedArea = FLUID_AREA_CACHE.get(startPos);
        if (cachedArea != null && (System.currentTimeMillis() - cachedArea.timestamp) <= CACHE_EXPIRY_TIME) {
            return cachedArea.count;
        }

        // 没有缓存或缓存已过期，计算水域大小
        if (!isExposedFluid(world, startPos)) {
            return 0;
        }

        int count = computeFluidArea(world, startPos);

        // 更新缓存
        FLUID_AREA_CACHE.put(startPos, new CachedFluidArea(count, System.currentTimeMillis()));
        return count;
    }

    private static int computeFluidArea(Level world, BlockPos startPos) {
        Queue<BlockPos> queue = new ArrayDeque<>();
        Set<BlockPos> visited = new HashSet<>();
        int areaCount = 0;

        queue.add(startPos);
        visited.add(startPos);

        while (!queue.isEmpty()) {
            BlockPos current = queue.poll();

            if (isExposedFluid(world, current)) {

                areaCount++;

                // 提前终止
                if (areaCount >= AREA_THRESHOLD) {
                    return areaCount;
                }

                // 检查四个方向
                for (Direction dir : Direction.Plane.HORIZONTAL) {
                    BlockPos neighbor = current.relative(dir);
                    if (!visited.contains(neighbor) && isFluid(world, neighbor)) {
                        visited.add(neighbor);
                        queue.add(neighbor);
                    }
                }
            }
        }

        return areaCount;
    }

    private static boolean isExposedFluid(Level world, BlockPos pos) {
        return isFluid(world, pos) && world.canSeeSky(pos.above());
    }

    private static boolean isFluid(Level world, BlockPos pos) {
        BlockState blockState = world.getBlockState(pos);
        FluidState fluidState = blockState.getFluidState();
        return !fluidState.isEmpty();
    }

    private static class CachedFluidArea {
        int count;
        long timestamp;

        CachedFluidArea(int count, long timestamp) {
            this.count = count;
            this.timestamp = timestamp;
        }
    }

    /**
     * 根据液体区域裸露面积返回 i18n component
     */
    @NotNull
    public static FluidAreaLevel getFluidAreaLevel(Level world, BlockPos startPos) {
        int fluidArea = FluidAreaCalculator.calculateExposedFluidArea(world, startPos);
        return FluidAreaLevel.getLevel(fluidArea);
    }

    /**
     * 返回液体区域裸露面积
     */
    public static int getFluidArea(Level world, BlockPos startPos) {
        return FluidAreaCalculator.calculateExposedFluidArea(world, startPos);
    }

    public enum FluidAreaLevel {
        SMALL("tooltip.area.small", 5),
        MEDIUM("tooltip.area.medium", 12),
        LARGE("tooltip.area.large", Integer.MAX_VALUE);

        private final TranslatableComponent component;
        private final int maxArea;

        FluidAreaLevel(String translationKey, int maxArea) {
            this.component = new TranslatableComponent(translationKey);
            this.maxArea = maxArea;
        }

        public TranslatableComponent getIi8nComponent() {
            return component;
        }

        public int getMaxArea() {
            return maxArea;
        }

        public static FluidAreaLevel getLevel(int fluidArea) {
            for (FluidAreaLevel level : values()) {
                if (fluidArea < level.getMaxArea()) {
                    return level;
                }
            }
            return LARGE;
        }
    }
}

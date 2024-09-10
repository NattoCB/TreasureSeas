package io.github.nattocb.treasure_seas.utils;

import io.github.nattocb.treasure_seas.TreasureSeas;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.*;

/**
 * 矩形区域判断
 * 基于世界坐标缓存，自动定时清理
 */
public class FluidShapeHandler {

    private static final ConcurrentHashMap<BlockPos, CachedRectangleArea> RECTANGLE_AREA_CACHE = new ConcurrentHashMap<>();

    // 缓存生命周期（毫秒）
    private static final long CACHE_EXPIRY_TIME = 60 * 1000;

    // 定时任务，每分钟清理一次缓存
    static {
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        executor.scheduleAtFixedRate(() -> {
            long currentTime = System.currentTimeMillis();
            RECTANGLE_AREA_CACHE.entrySet().removeIf(entry -> currentTime - entry.getValue().timestamp > CACHE_EXPIRY_TIME);
        }, 1, 1, TimeUnit.MINUTES);
    }

    /**
     * 计算给定 BlockPos 的合法矩形区域并缓存结果。
     * @param world     Level 实例
     * @param startPos  垂钓点位 BlockPos
     * @return FluidShape 枚举
     */
    public static FluidShape getFluidShape(@NotNull Level world, @NotNull BlockPos startPos) {

        int[] rectangleAreaInfo;

        // 检查缓存
        CachedRectangleArea cachedArea = RECTANGLE_AREA_CACHE.get(startPos);
        if (cachedArea != null) {
            rectangleAreaInfo = cachedArea.areaData;
            return getFluidShape(rectangleAreaInfo[0], rectangleAreaInfo[1]);
        }

        // 没有缓存或缓存已过期，重新计算区域
        rectangleAreaInfo = calculateRectangleAroundBlock(world, startPos);

        // 更新缓存
        RECTANGLE_AREA_CACHE.put(startPos, new CachedRectangleArea(rectangleAreaInfo, System.currentTimeMillis()));

        // 根据液体区域裸露面积返回 i18n component enum
        return getFluidShape(rectangleAreaInfo[0], rectangleAreaInfo[1]);
    }

    private static FluidShape getFluidShape(int totalValidBlocks, int distanceToNearestEdge) {

        if (totalValidBlocks < 0) {
            TreasureSeas.getLogger().warn("FluidAreaCalculator.getFluidAreaType: totalValidBlocks out of range: {}.", totalValidBlocks);
            return FluidShape.UNKNOWN;
        }
        if (distanceToNearestEdge < 0) {
            TreasureSeas.getLogger().warn("FluidAreaCalculator.getFluidAreaType: distanceToNearestEdge out of range: {}.", distanceToNearestEdge);
            return FluidShape.UNKNOWN;
        }


        // POOL（短且窄）
        if (totalValidBlocks <= 10) {
            return FluidShape.POOL;
        }
        if (distanceToNearestEdge <= 3) {
            // NEAR_SHORE (长且宽，近岸)
            return FluidShape.NEAR_SHORE;
        } else {
            // OPEN_WATER (长且宽，远岸)
            return FluidShape.OPEN_WATER;
        }

    }

    private static class CachedRectangleArea {
        int[] areaData;
        long timestamp;
        CachedRectangleArea(int[] areaData, long timestamp) {
            this.areaData = areaData;
            this.timestamp = timestamp;
        }
    }

    /**
     * 洪水填充给定 BlockPos 的水域合法区域，并返回相关信息
     * 使用 BFS，从中心位置开始向东、南、西、北四个方向扩展，直到遇到非法方块为止
     *
     * @param world     Level 实例
     * @param centerPos 中心位置的 BlockPos，从该位置开始计算区域
     * @return 一个长度为 2 的 int 数组:
     *         - int[0]: 洪水填充经过的总合法方块数量（最大阈值 100）
     *         - int[1]: 从中心点出发，东、南、西、北四个方向到遇到非合法方块的最小距离
     */
    public static int[] calculateRectangleAroundBlock(Level world, BlockPos centerPos) {
        // 使用队列实现广度优先搜索（BFS）
        Queue<BlockPos> queue = new LinkedList<>();
        queue.add(centerPos);

        // 用于跟踪已经访问过的方块，防止重复计算
        Set<BlockPos> visited = new HashSet<>();
        visited.add(centerPos);

        int totalValidBlocks = 0; // 总合法方块数量
        int minDistance = Integer.MAX_VALUE; // 最小碰壁距离

        // 定义四个扩展方向（东、南、西、北）
        int[][] directions = {
                {1, 0},  // 向东
                {-1, 0}, // 向西
                {0, 1},  // 向南
                {0, -1}  // 向北
        };

        // BFS 扩展，定义最大扩展上限
        int maxBlocks = 100;
        int expandedBlocks = 0;

        while (!queue.isEmpty() && expandedBlocks < maxBlocks) {
            BlockPos current = queue.poll();
            totalValidBlocks++;
            expandedBlocks++;

            for (int[] direction : directions) {
                BlockPos nextPos = current.offset(direction[0], 0, direction[1]);

                // 跳过已经访问过的方块
                if (visited.contains(nextPos)) {
                    continue;
                }

                // 检查下一个方块是否是合法的水或冰
                if (isValidBlock(world, nextPos)) {
                    queue.add(nextPos);
                    visited.add(nextPos);
                } else {
                    // 计算从中心到非合法方块的距离
                    int distance = Math.abs(nextPos.getX() - centerPos.getX()) + Math.abs(nextPos.getZ() - centerPos.getZ());
                    minDistance = Math.min(minDistance, distance);
                }
            }
        }

        // 返回总合法方块数量和最小碰壁距离
        return new int[] { totalValidBlocks, minDistance };
    }

    /**
     * 检查给定 BlockPos 处的方块是否合法。合法方块为液体方块或冰方块
     *
     * @param world 进行方块检查的 Level（世界）实例
     * @param pos   要检查的方块所在的 BlockPos（X, Y, Z 坐标）
     * @return 如果方块是液体方块或冰方块，返回 true；否则返回 false
     */
    private static boolean isValidBlock(Level world, BlockPos pos) {
        BlockState blockState = world.getBlockState(pos);
        boolean isFluid = !blockState.getFluidState().isEmpty();
        boolean isIce = blockState.is(Blocks.ICE) || blockState.is(Blocks.PACKED_ICE) || blockState.is(Blocks.BLUE_ICE);
        return isFluid || isIce;
    }

    public enum FluidShape {
        UNKNOWN("tooltip.area.unknown"),
        POOL("tooltip.area.pool"),
        NEAR_SHORE("tooltip.area.nearshore"),
        OPEN_WATER("tooltip.area.openwater");

        private final TranslatableComponent component;

        FluidShape(String translationKey) {
            this.component = new TranslatableComponent(translationKey);
        }

        public TranslatableComponent getIi8nComponent() {
            return component;
        }
    }

}

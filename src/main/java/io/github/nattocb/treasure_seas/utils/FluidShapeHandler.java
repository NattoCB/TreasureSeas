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

    private static final ConcurrentHashMap<BlockPos, CachedFluidShape> RECTANGLE_AREA_CACHE = new ConcurrentHashMap<>();

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

        // 检查缓存
        CachedFluidShape cachedShape = RECTANGLE_AREA_CACHE.get(startPos);
        if (cachedShape != null) {
            // 洞顶是 NARROW 类型才继续检查洞内是否有广阔水域
            if (cachedShape.fluidShape != FluidShape.NARROW) {
                return cachedShape.fluidShape;
            } else {
                return checkForHole(world, startPos, cachedShape.fluidShape);
            }
        }

        // 没有缓存，重新计算 startPos 的形状并缓存
        FluidShape newShape = calculateFluidShape(world, startPos);
        RECTANGLE_AREA_CACHE.put(startPos, new CachedFluidShape(newShape, System.currentTimeMillis()));

        // 检查 HOLE
        if (newShape != FluidShape.NARROW) {
            return newShape;
        } else {
            return checkForHole(world, startPos, newShape);
        }

    }

    private static FluidShape checkForHole(Level world, BlockPos startPos, FluidShape currentShape) {

        // 检查 Y - 1 位置的 FluidShape
        BlockPos belowPos = startPos.below();

        // 检查 Y - 1 位置的缓存
        CachedFluidShape cachedBelowShape = RECTANGLE_AREA_CACHE.get(belowPos);
        FluidShape belowShape;
        if (cachedBelowShape != null) {
            belowShape = cachedBelowShape.fluidShape;
        } else {
            // 没有缓存，重新计算 Y - 1 位置的形状并缓存
            belowShape = calculateFluidShape(world, belowPos);
            RECTANGLE_AREA_CACHE.put(belowPos, new CachedFluidShape(belowShape, System.currentTimeMillis()));
        }

        // 如果 Y - 1 位置为 OPEN_WATER 或 NEAR_SHORE，返回 HOLE
        if (belowShape == FluidShape.OPEN_WATER || belowShape == FluidShape.NEAR_SHORE) {
            return FluidShape.HOLE;
        }

        // 否则返回当前形状
        return currentShape;
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


        // 池塘级别判断
        if (totalValidBlocks <= 12) {
            return FluidShape.NARROW;
        }
        else if (totalValidBlocks <= 45) {
            return FluidShape.PONDLET;
        }
        else if (totalValidBlocks <= 85) {
            return FluidShape.POND;
        }

        if (distanceToNearestEdge <= 5) {
            // NEAR_SHORE (长且宽，近岸)
            return FluidShape.NEAR_SHORE;
        } else {
            // OPEN_WATER (长且宽，远岸)
            return FluidShape.OPEN_WATER;
        }

    }

    private static class CachedFluidShape {
        FluidShape fluidShape;
        long timestamp;

        CachedFluidShape(FluidShape fluidShape, long timestamp) {
            this.fluidShape = fluidShape;
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
    public static FluidShape calculateFluidShape(Level world, BlockPos centerPos) {
        // BFS
        Queue<BlockPos> queue = new LinkedList<>();
        queue.add(centerPos);

        // 已经访问过的方块
        Set<BlockPos> visited = new HashSet<>();
        visited.add(centerPos);

        int totalValidBlocks = 0; // 总合法方块数量
        int minDistance = Integer.MAX_VALUE; // 最小碰壁距离

        // 四个扩展方向（东、南、西、北）
        int[][] directions = {
                {1, 0},  // 向东
                {-1, 0}, // 向西
                {0, 1},  // 向南
                {0, -1}  // 向北
        };

        // BFS 最大扩展上限
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

        // 根据计算结果返回相应的 FluidShape
        return getFluidShape(totalValidBlocks, minDistance);
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
        NARROW("tooltip.area.narrow"),
        PONDLET("tooltip.area.pondlet"),
        POND("tooltip.area.pond"),
        NEAR_SHORE("tooltip.area.nearshore"),
        OPEN_WATER("tooltip.area.openwater"),
        HOLE("tooltip.area.hole");

        private final TranslatableComponent component;

        FluidShape(String translationKey) {
            this.component = new TranslatableComponent(translationKey);
        }

        public TranslatableComponent getIi8nComponent() {
            return component;
        }
    }

}

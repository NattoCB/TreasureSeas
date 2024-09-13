package io.github.nattocb.treasure_seas.utils;

import io.github.nattocb.treasure_seas.TreasureSeas;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.concurrent.*;

/**
 * 矩形区域判断
 * 基于世界坐标缓存（因Tooltip每帧调用一次计算）自动定时清理
 */
public class FluidShapeHandler {

    private static final ConcurrentHashMap<BlockPos, CachedFluidShape> RECTANGLE_AREA_CACHE = new ConcurrentHashMap<>();

    // 缓存生命周期（毫秒）
    private static final long CACHE_EXPIRY_TIME = 60 * 1000;

    // 判断液体区域类型的格数阈值
    private static final int POOL_THRESHOLD = 12;
    private static final int PONDLET_THRESHOLD = 45;
    private static final int POND_THRESHOLD = 200;

    // 定时任务，每分钟清理一次缓存
    static {
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        executor.scheduleAtFixedRate(() -> {
            long currentTime = System.currentTimeMillis();
            RECTANGLE_AREA_CACHE.entrySet().removeIf(entry -> currentTime - entry.getValue().timestamp > CACHE_EXPIRY_TIME);
        }, 1, 1, TimeUnit.MINUTES);
    }

    /**
     * 计算给定 BlockPos 的合法矩形区域并缓存结果
     * @param world     Level 实例
     * @param startPos  垂钓点位 BlockPos
     * @return FluidShape 枚举
     */
    public static FluidShape getFluidShape(@NotNull Level world, @NotNull BlockPos startPos) {

        // 确保 startPos 是该 Y 轴下的水体顶层液面位置
        BlockPos adjustedPos;
        if (!isFluid(world, startPos)) {
            return FluidShape.UNKNOWN;
        } else {
            adjustedPos = startPos;
            while (adjustedPos.getY() < world.getMaxBuildHeight() && isFluid(world, adjustedPos.above())) {
                adjustedPos = adjustedPos.above();
            }
        }

        // 检查缓存
        CachedFluidShape cachedShape = RECTANGLE_AREA_CACHE.get(adjustedPos);
        if (cachedShape != null) {
            return cachedShape.fluidShape;
        }

        // 没有缓存，重新计算 startPos 的形状并缓存
        FluidShape newShape = calculateRawFluidShape(world, adjustedPos);
        newShape = checkForHole(world, adjustedPos, newShape);
        RECTANGLE_AREA_CACHE.put(adjustedPos, new CachedFluidShape(newShape, System.currentTimeMillis()));

        return newShape;
    }

    /**
     * 基于 rawShape 进行额外判断，得到进一步细化的 shape 类型
     * 如基于 NARROW 判断是否为 HOLE、WELL shapes
     */
    private static FluidShape checkForHole(Level world, BlockPos startPos, FluidShape rawShape) {
        switch (rawShape) {
            case NARROW:
                // 检查 Y - 1 位置的 FluidShape
                BlockPos belowPos = startPos.below();
                FluidShape belowShape = calculateRawFluidShape(world, belowPos);
                // 如果 Y - 1 位置为 OPEN_WATER 或 NEAR_SHORE
                if (belowShape == FluidShape.OPEN_WATER || belowShape == FluidShape.NEAR_SHORE) {
                    // 下方深度大于 2，且上层 15 * 15 范围内液体占比小于等于 10% 才算 HOLE
                    if (FishUtils.calculateFluidDepth(startPos, world) > 2) {
                        double fluidPercentage = getAreaValidPercentage(world, startPos, FluidShapeHandler::isFluid);
                        if (fluidPercentage <= 10.0) {
                            return FluidShape.HOLE;
                        } else {
                            return FluidShape.SURFACE;
                        }
                    }
                }
                // 如果 Y - 1 位置也很窄（非 OPEN_WATER 或 NEAR_SHORE）那么判断是否为 WELL shape
                if (belowShape == FluidShape.NARROW && FishUtils.calculateFluidDepth(startPos, world) >= 10) {
                    return FluidShape.WELL;
                }
            case PONDLET:
            case POND:
                // 检查 Y - 1 位置的 FluidShape
                BlockPos belowPos1 = startPos.below();
                FluidShape belowShape1 = calculateRawFluidShape(world, belowPos1);
                if (belowShape1 == FluidShape.OPEN_WATER || belowShape1 == FluidShape.NEAR_SHORE) {
                    // 下方深度大于 2，按 SURFACE 返回
                    if (FishUtils.calculateFluidDepth(startPos, world) > 2) {
                        return FluidShape.SURFACE;
                    }
                }
            case NEAR_SHORE:
            case OPEN_WATER:
                // 检查 Y + 1 位置非空气方块占比，大于 90% 则为 HOLE
                double validPercentage = getAreaValidPercentage(world, startPos.above(), FluidShapeHandler::isAir);
                if (validPercentage <= 10.0) {
                    return FluidShape.HOLE;
                } else {
                    return rawShape;
                }
        }
        // HOLE, WELL 均不满足
        return rawShape;
    }

    private static double getAreaValidPercentage(Level world, BlockPos startPos, BiPredicate<Level, BlockPos> condition) {
        int cnt = 0;
        int totalBlocks = 15 * 15;
        int radius = 7;
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();

        // 遍历上层 15 * 15 区域
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                // 获取当前坐标
                mutablePos.set(startPos.getX() + dx, startPos.getY(), startPos.getZ() + dz);
                // 使用传入的 BiPredicate 来判断是否满足条件
                if (condition.test(world, mutablePos)) {
                    cnt++;
                }
            }
        }

        // 计算满足条件的区域占比
        return (cnt / (double) totalBlocks) * 100.0;
    }

    private static FluidShape getRawFluidShape(int totalValidBlocks, int distanceToNearestEdge) {

        if (totalValidBlocks < 0) {
            TreasureSeas.getLogger().warn("FluidAreaCalculator.getFluidAreaType: totalValidBlocks out of range: {}.", totalValidBlocks);
            return FluidShape.UNKNOWN;
        }
        if (distanceToNearestEdge < 0) {
            TreasureSeas.getLogger().warn("FluidAreaCalculator.getFluidAreaType: distanceToNearestEdge out of range: {}.", distanceToNearestEdge);
            return FluidShape.UNKNOWN;
        }


        // 池塘级别判断
        if (totalValidBlocks < POOL_THRESHOLD) {
            return FluidShape.NARROW;
        }
        else if (totalValidBlocks < PONDLET_THRESHOLD) {
            return FluidShape.PONDLET;
        }
        else if (totalValidBlocks < POND_THRESHOLD) {
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
     * 叫做 rawShape 因为本方法不包含后续额外形状的判断（如 HOLE / WELL shapes 基于 NARROW shape）
     *
     * @param world     Level 实例
     * @param centerPos 中心位置的 BlockPos，从该位置开始计算区域
     * @return 一个长度为 2 的 int 数组:
     *         - int[0]: 洪水填充经过的总合法方块数量（最大阈值 100）
     *         - int[1]: 从中心点出发，东、南、西、北四个方向到遇到非合法方块的最小距离
     */
    public static FluidShape calculateRawFluidShape(Level world, BlockPos centerPos) {

        if (!isFluid(world, centerPos)) {
            return FluidShape.UNKNOWN;
        }

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
        int maxBlocks = POND_THRESHOLD;
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
                if (isFluid(world, nextPos)) {
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
        return getRawFluidShape(totalValidBlocks, minDistance);
    }

    /**
     * 检查给定 BlockPos 处的方块是否合法。合法方块为液体方块
     *
     * @param world 进行方块检查的 Level（世界）实例
     * @param pos   要检查的方块所在的 BlockPos（X, Y, Z 坐标）
     * @return 如果方块是液体方块，返回 true；否则返回 false
     */
    private static boolean isFluid(Level world, BlockPos pos) {
        BlockState blockState = world.getBlockState(pos);
        return !blockState.getFluidState().isEmpty();
    }

    private static boolean isAir(Level world, BlockPos pos) {
        BlockState blockState = world.getBlockState(pos);
        return blockState.getBlock() == Blocks.AIR;
    }

    public enum FluidShape {
        UNKNOWN("tooltip.area.unknown"),
        /**
         * 极小水域
         */
        NARROW("tooltip.area.narrow"),
        /**
         * 小池塘
         */
        PONDLET("tooltip.area.pondlet"),
        /**
         * 大池塘
         */
        POND("tooltip.area.pond"),
        /**
         * 靠岸广阔水域
         */
        NEAR_SHORE("tooltip.area.nearshore"),
        /**
         * 离岸广阔水域
         */
        OPEN_WATER("tooltip.area.openwater"),
        /**
         * 洞口
         */
        HOLE("tooltip.area.hole"),
        /**
         * 井口
         */
        WELL("tooltip.area.well"),
        /**
         * 多坑洼地
         */
        SURFACE("tooltip.area.surface");

        private final TranslatableComponent component;

        FluidShape(String translationKey) {
            this.component = new TranslatableComponent(translationKey);
        }

        public TranslatableComponent getIi8nComponent() {
            return component;
        }
    }

}

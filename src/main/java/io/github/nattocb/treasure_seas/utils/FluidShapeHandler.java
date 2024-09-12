package io.github.nattocb.treasure_seas.utils;

import io.github.nattocb.treasure_seas.TreasureSeas;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
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
     * 计算给定 BlockPos 的合法矩形区域并缓存结果。
     * @param world     Level 实例
     * @param startPos  垂钓点位 BlockPos
     * @return FluidShape 枚举
     */
    public static FluidShape getFluidShape(@NotNull Level world, @NotNull BlockPos startPos) {

        // 检查缓存
        CachedFluidShape cachedShape = RECTANGLE_AREA_CACHE.get(startPos);
        if (cachedShape != null) {
            return cachedShape.fluidShape;
        }

        // 没有缓存，重新计算 startPos 的形状并缓存
        FluidShape newShape = calculateRawFluidShape(world, startPos);
        if (newShape == FluidShape.NARROW) {
            // 洞顶是 NARROW 类型才继续检查洞内是否有广阔水域
            newShape = checkForHole(world, startPos, newShape);
        }
        RECTANGLE_AREA_CACHE.put(startPos, new CachedFluidShape(newShape, System.currentTimeMillis()));

        return newShape;
    }

    /**
     * 基于 rawShape 进行额外判断，得到进一步细化的 shape 类型
     * 如基于 NARROW 判断是否为 HOLE、WELL shapes
     */
    private static FluidShape checkForHole(Level world, BlockPos startPos, FluidShape rawShape) {

        // 入参检查
        if (rawShape != FluidShape.NARROW) {
            TreasureSeas.getLogger().warn("FluidShapeHandler.checkForHole: this method only supports to process NARROW shape");
            return rawShape;
        }

        // 检查 Y - 1 位置的 FluidShape
        BlockPos belowPos = startPos.below();

        // 检查 Y - 1 位置的缓存
        // 水下 shape 无需再考虑 advanceShape，直接缓存 rawShape
        FluidShape belowShape = getFluidShapeFromCacheOrCalculate(world, belowPos);

        // 如果 Y - 1 位置为 OPEN_WATER 或 NEAR_SHORE
        if (belowShape == FluidShape.OPEN_WATER || belowShape == FluidShape.NEAR_SHORE) {
            // 下方深度大于 2，且上层 15 * 15 范围内液体占比小于等于 20% 才算 HOLE
            if (FishUtils.calculateFluidDepth(startPos, world) > 2) {
                int fluidCount = 0;
                int totalBlocks = 15 * 15;
                int radius = 7;
                BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
                // 遍历上层 15 * 15 区域
                for (int dx = -radius; dx <= radius; dx++) {
                    for (int dz = -radius; dz <= radius; dz++) {
                        // 获取当前坐标
                        mutablePos.set(startPos.getX() + dx, startPos.getY(), startPos.getZ() + dz);
                        // 判断是否为液体
                        if (isValidBlock(world, mutablePos)) {
                            fluidCount++;
                        }
                    }
                }
                // 计算液体占比
                double fluidPercentage = (fluidCount / (double) totalBlocks) * 100.0;
                // 如果液体占比小于等于 20%，返回 HOLE
                if (fluidPercentage <= 20.0) {
                    return FluidShape.HOLE;
                }
            }
        }

        // 如果 Y - 1 位置也很窄（非 OPEN_WATER 或 NEAR_SHORE）那么判断是否为 WELL shape
        if (belowShape == FluidShape.NARROW && FishUtils.calculateFluidDepth(startPos, world) >= 10) {
            // 判断 10 格深度下的每一层 blockPos 的 shape，均为 NARROW 则为井口类型（通体狭窄，下方深）
            boolean isWell = true;
            BlockPos currentCheckingPos = belowPos;
            for (int i = 0; i < 10; i++) {
                currentCheckingPos = currentCheckingPos.below(); // 指针下移
                FluidShape depthShape = getFluidShapeFromCacheOrCalculate(world, currentCheckingPos);
                if (depthShape != FluidShape.NARROW) {
                    isWell = false;
                    break;
                }
            }
            if (isWell) {
                return FluidShape.WELL;
            }
        }

        // HOLE, WELL 均不满足
        return FluidShape.NARROW;
    }

    private static FluidShape getFluidShapeFromCacheOrCalculate(Level world, BlockPos pos) {
        CachedFluidShape cachedShape = RECTANGLE_AREA_CACHE.get(pos);
        if (cachedShape != null) {
            return cachedShape.fluidShape;
        } else {
            FluidShape calculatedShape = calculateRawFluidShape(world, pos);
            RECTANGLE_AREA_CACHE.put(pos, new CachedFluidShape(calculatedShape, System.currentTimeMillis()));
            return calculatedShape;
        }
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
        return getRawFluidShape(totalValidBlocks, minDistance);
    }

    /**
     * 检查给定 BlockPos 处的方块是否合法。合法方块为液体方块
     *
     * @param world 进行方块检查的 Level（世界）实例
     * @param pos   要检查的方块所在的 BlockPos（X, Y, Z 坐标）
     * @return 如果方块是液体方块，返回 true；否则返回 false
     */
    private static boolean isValidBlock(Level world, BlockPos pos) {
        BlockState blockState = world.getBlockState(pos);
        boolean isFluid = !blockState.getFluidState().isEmpty();
        return isFluid;
    }

    public enum FluidShape {
        UNKNOWN("tooltip.area.unknown"),
        NARROW("tooltip.area.narrow"),
        PONDLET("tooltip.area.pondlet"),
        POND("tooltip.area.pond"),
        NEAR_SHORE("tooltip.area.nearshore"),
        OPEN_WATER("tooltip.area.openwater"),
        HOLE("tooltip.area.hole"),
        WELL("tooltip.area.well");

        private final TranslatableComponent component;

        FluidShape(String translationKey) {
            this.component = new TranslatableComponent(translationKey);
        }

        public TranslatableComponent getIi8nComponent() {
            return component;
        }
    }

}

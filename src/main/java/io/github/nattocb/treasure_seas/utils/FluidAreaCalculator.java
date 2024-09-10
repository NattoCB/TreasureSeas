package io.github.nattocb.treasure_seas.utils;

import io.github.nattocb.treasure_seas.TreasureSeas;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.*;

/**
 * 矩形区域计算器
 * 基于世界坐标缓存，自动定时清理
 */
public class FluidAreaCalculator {

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
            return getFluidShape(rectangleAreaInfo[0], rectangleAreaInfo[1], rectangleAreaInfo[2]);
        }

        // 没有缓存或缓存已过期，重新计算区域
        rectangleAreaInfo = calculateRectangleAroundBlock(world, startPos);

        // 更新缓存
        RECTANGLE_AREA_CACHE.put(startPos, new CachedRectangleArea(rectangleAreaInfo, System.currentTimeMillis()));

        // 根据液体区域裸露面积返回 i18n component enum
        return getFluidShape(rectangleAreaInfo[0], rectangleAreaInfo[1], rectangleAreaInfo[2]);
    }

    private static FluidShape getFluidShape(int length, int width, int distanceToNearestEdge) {

        if (width > length) {
            int temp = length;
            length = width;
            width = temp;
        }
        if (length < 0 || length > 15) {
            TreasureSeas.getLogger().warn("FluidAreaCalculator.getFluidAreaType: Length out of range: {}.", length);
            return FluidShape.UNKNOWN;
        }
        if (width < 0) {
            TreasureSeas.getLogger().warn("FluidAreaCalculator.getFluidAreaType: Width out of range: {}.", width);
            return FluidShape.UNKNOWN;
        }
        if (distanceToNearestEdge < 0) {
            TreasureSeas.getLogger().warn("FluidAreaCalculator.getFluidAreaType: Distance to nearest edge out of range: {}.", distanceToNearestEdge);
            return FluidShape.UNKNOWN;
        }

        // POOL（短且窄）
        if (length <= 5) {
            return FluidShape.POOL;
        }
        // STREAM (长且窄)
        if (width <= 5) {
            return FluidShape.STREAM;
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
     * 计算围绕给定 BlockPos 的 XZ 平面上的矩形区域
     * 矩形从中心 BlockPos 向外沿 X 和 Z 轴扩展，且该矩形只包含合法方块（水方块或冰方块）
     * 扩展范围限制为中心位置每个方向最多 15 格
     *
     * @param world     进行搜索的 Level（世界）实例
     * @param centerPos 中心 BlockPos（X, Y, Z 坐标），以其为中心形成矩形
     * @return 一个长度为 3 的 int 数组
     *         - int[0]: 矩形的长度（沿 X 轴的方块数）
     *         - int[1]: 矩形的宽度（沿 Z 轴的方块数）
     *         - int[2]: centerPos 到矩形最近边缘的距离（向下取整的格数）
     */
    public static int[] calculateRectangleAroundBlock(Level world, BlockPos centerPos) {
        int length;
        int width;
        int nearestDistance;

        // 跟踪矩形边界的变量
        int minX = centerPos.getX();
        int maxX = centerPos.getX();
        int minZ = centerPos.getZ();
        int maxZ = centerPos.getZ();

        // 使用 MutableBlockPos 避免频繁创建 BlockPos 实例
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();

        // 定义最大扩展范围（15格）
        int maxRange = 15;

        boolean canExpandX = true;
        boolean canExpandZ = true;

        // 开始扩展，直到两轴都无法再扩展
        while (canExpandX || canExpandZ) {
            // 尝试扩展 X 轴
            if (canExpandX) {
                minX--;
                maxX++;

                if ((maxX - centerPos.getX()) > maxRange) {
                    // 停止扩展 X 轴
                    canExpandX = false;
                } else {
                    // 检查 X 轴边界是否合法
                    for (int z = minZ; z <= maxZ; z++) {
                        mutablePos.set(minX, centerPos.getY(), z);
                        if (!isValidBlock(world, mutablePos)) {
                            canExpandX = false;
                            break;
                        }
                        mutablePos.set(maxX, centerPos.getY(), z);
                        if (!isValidBlock(world, mutablePos)) {
                            canExpandX = false;
                            break;
                        }
                    }
                }
            }

            // 尝试扩展 Z 轴
            if (canExpandZ) {
                minZ--;
                maxZ++;

                if ((maxZ - centerPos.getZ()) > maxRange) {
                    // 停止扩展 Z 轴
                    canExpandZ = false;
                } else {
                    // 检查 Z 轴边界是否合法
                    for (int x = minX; x <= maxX; x++) {
                        mutablePos.set(x, centerPos.getY(), minZ);
                        if (!isValidBlock(world, mutablePos)) {
                            canExpandZ = false;
                            break;
                        }
                        mutablePos.set(x, centerPos.getY(), maxZ);
                        if (!isValidBlock(world, mutablePos)) {
                            canExpandZ = false;
                            break;
                        }
                    }
                }
            }
        }

        // 计算矩形的长度、宽度以及从 centerPos 到最近边缘的距离
        length = maxX - minX + 1;
        width = maxZ - minZ + 1;
        nearestDistance = Math.min(Math.abs(centerPos.getX() - minX), Math.abs(centerPos.getX() - maxX));
        nearestDistance = Math.min(nearestDistance, Math.min(Math.abs(centerPos.getZ() - minZ), Math.abs(centerPos.getZ() - maxZ)));

        // 返回结果数组
        return new int[] {length, width, nearestDistance};
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
        FluidState fluidState = blockState.getFluidState();
        return !fluidState.isEmpty() || world.getBlockState(pos).getBlock() == Blocks.ICE;
    }

    public enum FluidShape {
        UNKNOWN("tooltip.area.unknown"),
        POOL("tooltip.area.pool"),
        STREAM("tooltip.area.stream"),
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

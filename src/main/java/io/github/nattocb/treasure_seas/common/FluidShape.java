package io.github.nattocb.treasure_seas.common;

import net.minecraft.network.chat.TranslatableComponent;

public enum FluidShape {
    UNKNOWN("tooltip.treasure_seas.area.unknown"),
    /**
     * 极小水域
     */
    NARROW("tooltip.treasure_seas.area.narrow"),
    /**
     * 小池塘
     */
    PONDLET("tooltip.treasure_seas.area.pondlet"),
    /**
     * 大池塘
     */
    POND("tooltip.treasure_seas.area.pond"),
    /**
     * 靠岸广阔水域
     */
    NEAR_SHORE("tooltip.treasure_seas.area.nearshore"),
    /**
     * 离岸广阔水域
     */
    OPEN_WATER("tooltip.treasure_seas.area.openwater"),
    /**
     * 洞口
     */
    HOLE("tooltip.treasure_seas.area.hole"),
    /**
     * 井口
     */
    WELL("tooltip.treasure_seas.area.well"),
    /**
     * 多坑洼地
     */
    SURFACE("tooltip.treasure_seas.area.surface");

    private final TranslatableComponent component;

    FluidShape(String translationKey) {
        this.component = new TranslatableComponent(translationKey);
    }

    public TranslatableComponent getIi8nComponent() {
        return component;
    }
}

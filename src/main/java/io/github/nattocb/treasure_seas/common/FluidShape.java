package io.github.nattocb.treasure_seas.common;

import net.minecraft.network.chat.TranslatableComponent;

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

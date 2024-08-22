package io.github.nattocb.treasure_seas.gui;

import io.github.nattocb.treasure_seas.utils.FishUtils;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix4f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class FishingTooltipRenderer extends GuiComponent {

    private static final int BG_Z_INDEX = 400;
    private static final int BG_OUTER_COLOR = -267386864;
    private static final int BG_INNER_COLOR_START = 1347420415;
    private static final int BG_INNER_COLOR_END = 1344798847;
    private static final int BG_OFFSET_LEFT_RIGHT = 3;
    private static final int BG_OFFSET_TOP_BOTTOM = 4;
    private static final int BG_BORDER_SIZE = 1;

    public static void checkAndRenderTooltip(PoseStack poseStack, int posX, int posY) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;

        // 确保玩家和客户端存在
        if (player == null || minecraft.level == null) {
            return;
        }

        // 获取玩家的位置和视线方向
        Vec3 eyePosition = player.getEyePosition(1.0F);
        Vec3 viewVector = player.getViewVector(1.0F);
        double reachDistance = 20.0D;
        Vec3 targetPosition = eyePosition.add(viewVector.scale(reachDistance));

        // 获取所有的鱼钩实体
        List<Entity> fishingHooks = minecraft.level.getEntities(
                player,
                new AABB(eyePosition, targetPosition),
                entity -> entity instanceof FishingHook
        );

        // 遍历鱼钩实体，找出最近的一个
        Optional<FishingHook> closestHook = fishingHooks.stream()
                .map(entity -> (FishingHook) entity)
                .min((hook1, hook2) -> {
                    double dist1 = hook1.distanceToSqr(eyePosition);
                    double dist2 = hook2.distanceToSqr(eyePosition);
                    return Double.compare(dist1, dist2);
                });

        // 如果找到最近的鱼钩，则渲染工具提示
        closestHook.ifPresent(hook -> {
            List<ClientTooltipComponent> components = generateTooltipComponents(player, hook);
            renderTooltipInternal(poseStack, posX, posY, components);
        });
    }

    public static void renderTooltipInternal(PoseStack poseStack, int posX, int posY, List<ClientTooltipComponent> components) {
        if (components.isEmpty()) return;

        Font font = Minecraft.getInstance().font;
        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();

        int totalWidth = 0;
        int totalHeight = components.size() == 1 ? -2 : 0;

        // 预计算 tooltip 的整体宽高
        for (ClientTooltipComponent component : components) {
            int componentWidth = component.getWidth(font);
            if (componentWidth > totalWidth) {
                totalWidth = componentWidth;
            }
            totalHeight += component.getHeight();
        }

        // tooltip 渲染起始位置
        int tooltipX = posX + 12;
        int tooltipY = posY - 12;

        // 保存当前矩阵状态以进行变换
        poseStack.pushPose();

        // 更新 itemRender 的渲染优先级（z-index），确保 tooltip 显示在其他元素之上
        float initialBlitOffset = itemRenderer.blitOffset;
        itemRenderer.blitOffset = 400.0F;

        // 渲染 tooltip background
        var matrix4f = renderBackground(poseStack, tooltipX, tooltipY, totalWidth, totalHeight);

        // 渲染 tooltip texts
        MultiBufferSource.BufferSource bufferSource = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
        poseStack.translate(0.0F, 0.0F, 400.0F); // 提升渲染层级
        int curRenderYOffset = tooltipY;
        for (ClientTooltipComponent component : components) {
            component.renderText(font, tooltipX, curRenderYOffset, matrix4f, bufferSource);
            curRenderYOffset += component.getHeight() + (components.indexOf(component) == 0 ? 2 : 0);
        }
        bufferSource.endBatch();

        // 恢复矩阵状态
        poseStack.popPose();
        curRenderYOffset = tooltipY;

        // 渲染 tooltip fish rod icon
        for (ClientTooltipComponent component : components) {
            // 使用 tooltipX 和 curRenderYOffset 作为 icon 的起始 blit 位置
            component.renderImage(font, tooltipX, curRenderYOffset, poseStack, itemRenderer, 400);
            curRenderYOffset += component.getHeight() + (components.indexOf(component) == 0 ? 2 : 0);
        }

        // 恢复渲染优先级
        itemRenderer.blitOffset = initialBlitOffset;
    }

    @NotNull
    private static Matrix4f renderBackground(PoseStack poseStack, int tooltipX, int tooltipY, int maxWidth, int totalHeight) {

        Tesselator tesselator = Tesselator.getInstance();
        var bufferBuilder = tesselator.getBuilder();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        var matrix4f = poseStack.last().pose();

        fillGradient(matrix4f, bufferBuilder, tooltipX - BG_OFFSET_LEFT_RIGHT, tooltipY - BG_OFFSET_TOP_BOTTOM, tooltipX + maxWidth + BG_OFFSET_LEFT_RIGHT, tooltipY - BG_OFFSET_TOP_BOTTOM + BG_BORDER_SIZE, BG_Z_INDEX, BG_OUTER_COLOR, BG_OUTER_COLOR);
        fillGradient(matrix4f, bufferBuilder, tooltipX - BG_OFFSET_LEFT_RIGHT, tooltipY + totalHeight + BG_OFFSET_LEFT_RIGHT, tooltipX + maxWidth + BG_OFFSET_LEFT_RIGHT, tooltipY + totalHeight + BG_OFFSET_LEFT_RIGHT + BG_BORDER_SIZE, BG_Z_INDEX, BG_OUTER_COLOR, BG_OUTER_COLOR);
        fillGradient(matrix4f, bufferBuilder, tooltipX - BG_OFFSET_LEFT_RIGHT, tooltipY - BG_OFFSET_TOP_BOTTOM + BG_BORDER_SIZE, tooltipX + maxWidth + BG_OFFSET_LEFT_RIGHT, tooltipY + totalHeight + BG_OFFSET_LEFT_RIGHT, BG_Z_INDEX, BG_OUTER_COLOR, BG_OUTER_COLOR);
        fillGradient(matrix4f, bufferBuilder, tooltipX - BG_OFFSET_TOP_BOTTOM, tooltipY - BG_OFFSET_TOP_BOTTOM + BG_BORDER_SIZE, tooltipX - BG_OFFSET_LEFT_RIGHT, tooltipY + totalHeight + BG_OFFSET_LEFT_RIGHT, BG_Z_INDEX, BG_OUTER_COLOR, BG_OUTER_COLOR);
        fillGradient(matrix4f, bufferBuilder, tooltipX + maxWidth + BG_OFFSET_LEFT_RIGHT, tooltipY - BG_OFFSET_TOP_BOTTOM + BG_BORDER_SIZE, tooltipX + maxWidth + BG_OFFSET_LEFT_RIGHT + BG_BORDER_SIZE, tooltipY + totalHeight + BG_OFFSET_LEFT_RIGHT, BG_Z_INDEX, BG_OUTER_COLOR, BG_OUTER_COLOR);

        fillGradient(matrix4f, bufferBuilder, tooltipX - BG_OFFSET_LEFT_RIGHT, tooltipY - BG_OFFSET_TOP_BOTTOM + BG_BORDER_SIZE, tooltipX - BG_OFFSET_LEFT_RIGHT + BG_BORDER_SIZE, tooltipY + totalHeight + BG_OFFSET_LEFT_RIGHT - BG_BORDER_SIZE, BG_Z_INDEX, BG_INNER_COLOR_START, BG_INNER_COLOR_END);
        fillGradient(matrix4f, bufferBuilder, tooltipX + maxWidth + BG_OFFSET_LEFT_RIGHT - BG_BORDER_SIZE, tooltipY - BG_OFFSET_TOP_BOTTOM + BG_BORDER_SIZE, tooltipX + maxWidth + BG_OFFSET_LEFT_RIGHT, tooltipY + totalHeight + BG_OFFSET_LEFT_RIGHT - BG_BORDER_SIZE, BG_Z_INDEX, BG_INNER_COLOR_START, BG_INNER_COLOR_END);
        fillGradient(matrix4f, bufferBuilder, tooltipX - BG_OFFSET_LEFT_RIGHT, tooltipY - BG_OFFSET_TOP_BOTTOM + BG_BORDER_SIZE, tooltipX + maxWidth + BG_OFFSET_LEFT_RIGHT, tooltipY - BG_OFFSET_TOP_BOTTOM + 2 * BG_BORDER_SIZE, BG_Z_INDEX, BG_INNER_COLOR_START, BG_INNER_COLOR_START);
        fillGradient(matrix4f, bufferBuilder, tooltipX - BG_OFFSET_LEFT_RIGHT, tooltipY + totalHeight + BG_OFFSET_LEFT_RIGHT - BG_BORDER_SIZE, tooltipX + maxWidth + BG_OFFSET_LEFT_RIGHT, tooltipY + totalHeight + BG_OFFSET_LEFT_RIGHT, BG_Z_INDEX, BG_INNER_COLOR_END, BG_INNER_COLOR_END);

        RenderSystem.enableDepthTest();
        RenderSystem.disableTexture();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        bufferBuilder.end();
        BufferUploader.end(bufferBuilder);
        RenderSystem.disableBlend();
        RenderSystem.enableTexture();

        return matrix4f;
    }


    private static List<ClientTooltipComponent> generateTooltipComponents(Player player, FishingHook hook) {

        int fishRodEnchantLevel = FishUtils.getFishRodFighterEnchantLevel(player);
        int waterDepth = FishUtils.calculateFluidDepth(hook.getOnPos(), hook.getLevel());
        int depthCapacity = FishUtils.getRodDepthCapacity(fishRodEnchantLevel);
        Biome biome = hook.getLevel().getBiome(hook.getOnPos()).value();
        ResourceLocation biomeRes = hook.getLevel().registryAccess().registryOrThrow(ForgeRegistries.Keys.BIOMES).getKey(biome);

        List<Component> textComponents;
        if (waterDepth == 0) {
            textComponents = List.of(
                    new TranslatableComponent("tooltip.fishing_hook"),
                    new TranslatableComponent("tooltip.water_depth", waterDepth),
                    new TranslatableComponent("tooltip.depth_capacity", Math.min(depthCapacity, waterDepth))
            );
        } else {
            String biomeName = biomeRes == null ? "unknown" : biomeRes.getPath();
            textComponents = List.of(
                    new TranslatableComponent("tooltip.fishing_hook"),
                    new TranslatableComponent("tooltip.water_depth", waterDepth),
                    new TranslatableComponent("tooltip.depth_capacity", Math.min(depthCapacity, waterDepth)),
                    new TranslatableComponent("tooltip.biome", biomeName)
            );
        }

        List<ClientTooltipComponent> tooltipComponents = textComponents.stream()
                .flatMap(text -> Minecraft.getInstance().font.split(text, Integer.MAX_VALUE).stream())
                .map(ClientTooltipComponent::create)
                .collect(Collectors.toList());

        tooltipComponents.add(0, new ClientTooltipComponent() {
            @Override
            public int getHeight() {
                return 16;
            }
            @Override
            public int getWidth(Font font) {
                return 16;
            }
            @Override
            public void renderImage(Font font, int x, int y, PoseStack poseStack, ItemRenderer itemRenderer, int p_230456_) {
                ItemStack itemStack = new ItemStack(Items.FISHING_ROD);
                poseStack.pushPose();
                poseStack.translate(x, y, 0);
                itemRenderer.renderGuiItem(itemStack, x, y);
                poseStack.popPose();
            }
            @Override
            public void renderText(Font font, int x, int y, Matrix4f matrix4f, MultiBufferSource.BufferSource bufferSource) {
                // 不需要文本渲染
            }
        });

        return tooltipComponents;
    }

}
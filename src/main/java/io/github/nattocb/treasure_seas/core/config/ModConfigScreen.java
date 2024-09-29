package io.github.nattocb.treasure_seas.core.config;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.nattocb.treasure_seas.TreasureSeas;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ModConfigScreen extends Screen {

    private final FishConfigManager configManager = TreasureSeas.getInstance().getFishConfigManager();
    private final Screen parent;

    private EditBox customPosXBox;
    private EditBox customPosYBox;

    public ModConfigScreen(Screen parent) {
        super(new TranslatableComponent("title.config"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int labelX = this.width / 2 - 160;  // 左侧标签的 X 位置
        int buttonX = this.width / 2 + 50;  // 右侧按钮的 X 位置
        int buttonWidth = 100;  // 按钮宽度
        int buttonHeight = 20;  // 按钮高度
        int optionY = 35;  // 第一项的 Y 位置
        int gap = 30;  // 每行之间的间距

        // 添加配置项 1: HUD 启用/禁用
        addConfigOption(optionY,
                configManager.isHudFishingInfoEnable(), (button) -> {
                    boolean currentValue = configManager.isHudFishingInfoEnable();
                    configManager.setHudFishingInfoEnable(!currentValue);
                    button.setMessage(getStatusText(!currentValue));
                }, labelX, buttonX, buttonWidth, buttonHeight);

        // 添加配置项 2: 自定义位置启用/禁用
        addConfigOption(optionY += gap,
                configManager.isHudFishingInfoEnableCustomPosition(), (button) -> {
                    boolean currentValue = configManager.isHudFishingInfoEnableCustomPosition();
                    configManager.setHudFishingInfoEnableCustomPosition(!currentValue);
                    button.setMessage(getStatusText(!currentValue));
                }, labelX, buttonX, buttonWidth, buttonHeight);

        // 添加配置项 3: 自定义 X 位置
        customPosXBox = new EditBox(this.font, buttonX, optionY += gap, buttonWidth, buttonHeight, new TranslatableComponent("option.hudFishingInfoCustomPositionX"));
        customPosXBox.setValue(Integer.toString(configManager.getHudFishingInfoCustomX()));
        this.addRenderableWidget(customPosXBox);

        // 添加配置项 4: 自定义 Y 位置
        customPosYBox = new EditBox(this.font, buttonX, optionY += gap, buttonWidth, buttonHeight, new TranslatableComponent("option.hudFishingInfoCustomPositionY"));
        customPosYBox.setValue(Integer.toString(configManager.getHudFishingInfoCustomY()));
        this.addRenderableWidget(customPosYBox);

        // 添加配置项 5: 调试模式启用/禁用
        addConfigOption(optionY += gap,
                configManager.isLogDebugModeEnable(), (button) -> {
                    boolean currentValue = configManager.isLogDebugModeEnable();
                    configManager.setLogDebugModeEnable(!currentValue);
                    button.setMessage(getStatusText(!currentValue));
                }, labelX, buttonX, buttonWidth, buttonHeight);

        // 底部保存和取消按钮并排显示
        int bottomY = this.height - 40;
        int buttonSpacing = 20;
        int bottomButtonWidth = 150;

        // 保存并退出按钮
        this.addRenderableWidget(new Button(this.width / 2 - bottomButtonWidth - buttonSpacing, bottomY, bottomButtonWidth, buttonHeight,
                new TranslatableComponent("menu.saveAndExit"),
                (button) -> {
                    // 保存配置值
                    configManager.setHudFishingInfoCustomX(Integer.parseInt(customPosXBox.getValue()));
                    configManager.setHudFishingInfoCustomY(Integer.parseInt(customPosYBox.getValue()));
                    configManager.saveClientConfig();
                    this.minecraft.setScreen(parent);
                }
        ));

        // 取消按钮
        this.addRenderableWidget(new Button(this.width / 2 + buttonSpacing, bottomY, bottomButtonWidth, buttonHeight,
                new TranslatableComponent("menu.cancel"),
                (button) -> {
                    this.minecraft.setScreen(parent);
                }
        ));
    }

    // 添加配置项（左侧描述文本 + 右侧按钮），可以传递按钮高度参数
    private void addConfigOption(int y, boolean initialValue, Button.OnPress onPress, int labelX, int buttonX, int buttonWidth, int buttonHeight) {
        // 确保按钮的完整边框显示，并调整位置
        this.addRenderableWidget(new Button(buttonX, y, buttonWidth, buttonHeight, getStatusText(initialValue), onPress));
    }

    // Helper method: 获取配置状态文本 (Enabled/Disabled)
    private Component getStatusText(boolean isEnabled) {
        return new TranslatableComponent(isEnabled ? "status.enabled" : "status.disabled");
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float delta) {
        // 确保渲染前保存状态
        poseStack.pushPose();

        // 将标题上移，使其更加贴近屏幕顶部
        this.renderBackground(poseStack);
        drawCenteredString(poseStack, this.font, this.title.getString(), this.width / 2, 10, 0xFFFFFF);

        // 渲染每个配置项的标签，稍微下移使其居中
        drawString(poseStack, this.font, new TranslatableComponent("option.hudFishingInfoEnable").getString(), this.width / 2 - 150, 40, 0xFFFFFF);
        drawString(poseStack, this.font, new TranslatableComponent("option.hudFishingInfoCustomPositionEnable").getString(), this.width / 2 - 150, 70, 0xFFFFFF);
        drawString(poseStack, this.font, new TranslatableComponent("option.hudFishingInfoCustomPositionX").getString(), this.width / 2 - 150, 100, 0xFFFFFF);
        drawString(poseStack, this.font, new TranslatableComponent("option.hudFishingInfoCustomPositionY").getString(), this.width / 2 - 150, 130, 0xFFFFFF);
        drawString(poseStack, this.font, new TranslatableComponent("option.logDebugModeEnable").getString(), this.width / 2 - 150, 160, 0xFFFFFF);

        // 确保渲染后恢复状态
        poseStack.popPose();

        super.render(poseStack, mouseX, mouseY, delta);
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(parent);
    }
}

package io.github.nattocb.treasure_seas.gui;

import io.github.nattocb.treasure_seas.TreasureSeas;
import io.github.nattocb.treasure_seas.eventsubscriber.client_only.MouseEventHandler;
import io.github.nattocb.treasure_seas.packet.FishFightResultPacket;
import io.github.nattocb.treasure_seas.packet.PacketHandler;
import io.github.nattocb.treasure_seas.config.FishWrapper;
import io.github.nattocb.treasure_seas.utils.FishUtils;
import io.github.nattocb.treasure_seas.utils.MathUtils;
import io.github.nattocb.treasure_seas.utils.WaveGenerator;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

@OnlyIn(Dist.CLIENT)
public class FishFightGui extends Screen {

    private final WaveGenerator waveGenerator;

    private final FishWrapper fishWrapper;

    ResourceLocation TEXTURE = new ResourceLocation(TreasureSeas.MOD_ID, "textures/gui/fight_bg.png");

    /**
     * 音效播放相关变量
     */
    private static final long SOUND_PLAY_INTERVAL_FISH_IN_OUT_BAR_MS = 500;
    private static final long SOUND_PLAY_INTERVAL_FISH_IN_BAR_RANGE_MS = 500;
    private long lastSoundPlayTimeFishMoveInOutBar = 0;
    private long lastSoundPlayTimeFishInBarRange = 0;
    private boolean isCatchingFish = false;
    private boolean prevCatchingStatus = false;

    /**
     * 鱼漂位置，发胜利数据包时，奖品鱼实体从这里生成
     */
    private final Vec3 bobberPosition;

    /**
     * 鱼起始位置
     */
    private Double initialFishY = null;

    /**
     * 捕获条未碰到鱼时的进度条缓降保护时长
     */
    private final double protectedTicks = 30;

    private final int progressBarHeight = 125;

    /**
     * 时间计算 tps、fps、timestamp mills
     */
    private long tickCounter = 0L;
    private float previousFrame = 0.0F;
    private float realTimeCounter = 0.0F;

    /**
     * 捕鱼条 y 位置
     */
    private float catchBarCurYLocationOffset = 0.0F;

    /**
     * 鱼移动速度的激进程度
     */
    private final float fishSpeedMultiplier;

    /**
     * 战胜鱼所需的时间（tick）
     */
    private float winTime;

    /**
     * 初始进度条位置
     */
    private final float initCaughtTime;

    /**
     * 记录当前抓到的累计时间
     */
    private float caughtTime;

    /**
     * 钓竿等级
     */
    private int enchantLvl;

    public FishFightGui(Component titleIn, Vec3 bobberPosition, FishWrapper fishWrapper) {
        super(titleIn);
        LocalPlayer player = Minecraft.getInstance().player;
        enchantLvl = player == null ? 1 : FishUtils.getFishRodFighterEnchantLevel(player);
        this.fishWrapper = fishWrapper;
        this.bobberPosition = bobberPosition;
        double yDiffLimit = 1.0;
        // todo test big value
        double[] yRange = {-1.155, 1.155};
         yRange = MathUtils.getRandomSubInterval(yRange, 0.7);
        this.winTime = fishWrapper.getTicksToWin();
        this.fishSpeedMultiplier = fishWrapper.getSpeedModifier() * (1 - (enchantLvl-1) * 0.05F);
        this.initCaughtTime = ((float) 4 / 15) * winTime;
        this.caughtTime = initCaughtTime;
        float[] flatSegmentRange = fishWrapper.getFlatSegmentRandomRange();
        float[] nonFlatSegmentRange = fishWrapper.getFluxSegmentRandomRange();
        // make fishing easier based on enchant level
        float enchantModifier = (1 + Math.min(enchantLvl - this.fishWrapper.getLowestLootableEnchantmentLevel(), 0) * 0.05F);
        flatSegmentRange[0] = flatSegmentRange[0] * enchantModifier;
        flatSegmentRange[1] = flatSegmentRange[1] * enchantModifier;
        nonFlatSegmentRange[0] = nonFlatSegmentRange[0] * enchantModifier;
        nonFlatSegmentRange[1] = nonFlatSegmentRange[1] * enchantModifier;

        this.waveGenerator = new WaveGenerator(yDiffLimit, yRange, flatSegmentRange, nonFlatSegmentRange);
        // 防止一开始时滑条自动往上跑，让 isClicked 在 GUI 初始时为 false，让滑条下沉
        MouseEventHandler.resetStatus();
    }

    public void render(@NotNull PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {

        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            super.render(matrixStack, mouseX, mouseY, partialTicks);
            return;
        }

        // 平滑帧
        long currentTime = System.currentTimeMillis();
        float ticksSinceLastFrame = (float) this.tickCounter + partialTicks - this.previousFrame;
        this.renderBackground(matrixStack);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, TEXTURE);

        // 绘制用户提示鼠标左键logo
        blit(matrixStack,
                this.width / 2 + 25,
                this.height / 4 + 85,
                0,
                0,
                27,
                25,
                512,
                512
        );

        // 绘制钓鱼背景条
        int backgroundBaseY = this.height / 2 - progressBarHeight / 2;
        int backgroundWidth = 15;
        int backgroundHeight = 126;
        int backgroundTexStartX = player.level.dimension() == Level.NETHER ? 42 : 0;
        blit(matrixStack,
                this.width / 2 - 2,
                backgroundBaseY,
                backgroundTexStartX,
                170,
                backgroundWidth,
                backgroundHeight,
                512,
                512
        );

        // 绘制成功进度条背景
        int progressBarWidth = 8;
        int progressBarHeight = 126;
        int progressBarBaseX = this.width / 2 + 13;
        int progressBarTexStartX = player.level.dimension() == Level.NETHER ? 42 + 23 : 23;
        blit(matrixStack,
                progressBarBaseX,
                backgroundBaseY,
                progressBarTexStartX,
                170,
                progressBarWidth,
                progressBarHeight,
                512,
                512
        );

        // 绘制成功进度条：
        // 纹理从左上角（0, 0）开始向下和向右绘制，那么  m x y u v texW, texH, texMaxW, texMaxH 的 后 8 个参数的动态变动需要计算
        float progressProportion = this.caughtTime / this.winTime;
        int currentHeight = (int) (progressProportion * this.progressBarHeight);
        int actualScreenY = backgroundBaseY + (this.progressBarHeight - currentHeight);
        int texBaseY = 170 + this.progressBarHeight - currentHeight;
        int progressionTexStartX = player.level.dimension() == Level.NETHER ? 42 + 15 : 15;
        drawProgressBar(
                matrixStack,
                progressBarBaseX,
                actualScreenY,
                progressBarWidth,
                currentHeight,
                progressionTexStartX,
                texBaseY,
                512,
                512
        );

        // 计算鱼的移动
        double fishInitCenterX = this.width / 2 + 2;
        double fishInitCenterY = this.height / 2 + 2;
        double fishY = this.getFishY(fishInitCenterY, ticksSinceLastFrame);
        // 鱼的不能移动超过背景条的 y 范围
        int backgroundMinY = backgroundBaseY;
        int backgroundMaxY = backgroundBaseY + backgroundHeight;
        fishY = Math.max(backgroundMinY, Math.min(fishY, backgroundMaxY));
        // 留存初始鱼 y 位置记录，用于计算后续浮动
        initialFishY = initialFishY == null ? fishY : initialFishY;

        // 绘制鼠标控制的捕鱼滑条
        int slideBarHeight = 40;
        int slideBarHeightStartPosition = (int) (initialFishY + this.catchBarCurYLocationOffset) - 15;
        int slideBarHeightEndPosition = slideBarHeightStartPosition + slideBarHeight;
        int slideBarTexStartX = player.level.dimension() == Level.NETHER ? 42 + 31 : 31;
        blit(matrixStack,
                this.width / 2,
                slideBarHeightStartPosition,
                slideBarTexStartX,
                184,
                11,
                slideBarHeight,
                512,
                512
        );

        // 绘制鱼
        this.drawFish(player.level.dimension(), matrixStack, (int) fishInitCenterX, fishY);

        // 捕鱼滑条在下一帧的移动
        if (MouseEventHandler.isMouseClicked()) {
            // check upper limit
            if (slideBarHeightStartPosition > backgroundBaseY + 2) {
                this.catchBarCurYLocationOffset -= (ticksSinceLastFrame * 1.875F);
            }
        } else {
            // check lower limit
            if (slideBarHeightEndPosition < backgroundBaseY + backgroundHeight - 2) {
                this.catchBarCurYLocationOffset += (ticksSinceLastFrame * 1.75F);
            }
        }

        // 捕鱼状态判断，音效播放，及成功进度的增减
        boolean inRange = fishY >= slideBarHeightStartPosition && fishY <= slideBarHeightEndPosition;
        if (inRange) {
            // 滑条上升
            // 根据钓竿等级略微增加成功速度，并且减缓鱼的波动速度
            float enchantLvlMultiplier = 0.05F;
            if (player.isCreative()) {
                caughtTime = caughtTime + (ticksSinceLastFrame * 5) * (1 + (enchantLvl - 1) * enchantLvlMultiplier);
            } else {
                caughtTime = caughtTime + (ticksSinceLastFrame * 2) * (1 + (enchantLvl - 1) * enchantLvlMultiplier);
            }
            isCatchingFish = true;
            // 播放 inRange 音效
            boolean isSoundPlayable = currentTime - lastSoundPlayTimeFishInBarRange >= SOUND_PLAY_INTERVAL_FISH_IN_BAR_RANGE_MS;
            if (isSoundPlayable) {
                player.playSound(SoundEvents.FISHING_BOBBER_RETRIEVE, 0.75F, 3.0F);
                lastSoundPlayTimeFishInBarRange = currentTime;
            }
        } else {
            // 让进度下降速度放缓，低于上升速度
            if (realTimeCounter <= protectedTicks) {
                // 初始时间段内的跌落保护
                caughtTime = caughtTime - ticksSinceLastFrame * 1.25F;
            } else {
                // 而后跌落加速
                caughtTime = caughtTime - ticksSinceLastFrame * 3;
            }
            isCatchingFish = false;
        }
        // 播放鱼进入和离开 SLIDE-BAR 的声音
        if (isCatchingFish && !prevCatchingStatus) {
            boolean isSoundPlayable = currentTime - lastSoundPlayTimeFishMoveInOutBar >= SOUND_PLAY_INTERVAL_FISH_IN_OUT_BAR_MS;
            if (isSoundPlayable) {
                player.playSound(SoundEvents.ITEM_PICKUP, 1.0F, 1.0F);
                lastSoundPlayTimeFishMoveInOutBar = currentTime;
            }
        } else if (!isCatchingFish && prevCatchingStatus) {
            boolean isSoundPlayable = currentTime - lastSoundPlayTimeFishMoveInOutBar >= SOUND_PLAY_INTERVAL_FISH_IN_OUT_BAR_MS;
            if (isSoundPlayable) {
                player.playSound(SoundEvents.ITEM_PICKUP, 1.0F, 1.0F);
                lastSoundPlayTimeFishMoveInOutBar = currentTime;
            }
        }
        prevCatchingStatus = isCatchingFish;

        // 判断输赢
        if (caughtTime >= winTime) {
            // 关闭 GUI，发送成功结果的数据包到服务器，处理奖品发放
            Level world = player.level;
            FishWrapper.AllowedTime currentTimeEnum = FishUtils.getCurrentTimeEnum(world);
            boolean isNightTime = currentTimeEnum == FishWrapper.AllowedTime.EVENING || currentTimeEnum == FishWrapper.AllowedTime.NIGHT ;
            PacketHandler.CHANNEL.sendToServer(new FishFightResultPacket(
                    true,
                    this.bobberPosition,
                    this.fishWrapper,
                    world.isRaining(),
                    world.isThundering(),
                    isNightTime
            ));
            // 播放音效
            player.playSound(SoundEvents.VILLAGER_YES, 0.7F, 1.0F);
            this.onClose();
        }
        if (caughtTime <= 0.0F) {
            // 关闭 GUI，发送失败结果
            Level world = player.level;
            FishWrapper.AllowedTime currentTimeEnum = FishUtils.getCurrentTimeEnum(world);
            boolean isNightTime = currentTimeEnum == FishWrapper.AllowedTime.EVENING || currentTimeEnum == FishWrapper.AllowedTime.NIGHT ;
            PacketHandler.CHANNEL.sendToServer(new FishFightResultPacket(
                    false,
                    this.bobberPosition,
                    this.fishWrapper,
                    world.isRaining(),
                    world.isThundering(),
                    isNightTime
            ));
            // 播放音效
            player.playSound(SoundEvents.VILLAGER_NO, 0.5F, 1.0F);
            this.onClose();
        }

        // 更新 partialTick and counters
        this.previousFrame = (float) this.tickCounter + partialTicks;
        this.realTimeCounter += ticksSinceLastFrame;

        super.render(matrixStack, mouseX, mouseY, partialTicks);
    }

    /**
     * 该方法每 tick 被调用一次
     */
    public void tick() {
        // 累计 fight time
        tickCounter++;
        super.tick();
    }

    public boolean isPauseScreen() {
        return false;
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null) {
            player.playSound(SoundEvents.FISHING_BOBBER_RETRIEVE, 1.0F, 1.0F);
        }
        return true;
    }

    /**
     * 绘制鱼，返回计算后的鱼 y 位置
     *
     * @param matrixStack
     * @param centerX
     * @return
     */
    private void drawFish(ResourceKey<Level> dimension, PoseStack matrixStack, int centerX, double fishY) {
        matrixStack.pushPose();
        // 按波动值移动鱼
        matrixStack.translate(centerX, fishY, 0.0D);
        // 根据捕鱼类别决定使用素材的坐标
        float u, v;
        int x, y, w, h;
        if (this.fishWrapper.isUltimateTreasure()) {
            x = -3;
            y = -5;
            u = 37.0F;
            v = 21.0F;
            w = 13;
            h = 13;
        } else {
            x = -2;
            y = -5;
            if (dimension == Level.NETHER) {
                u = 50.0F;
                v = 15.0F;
                w = 11;
                h = 6;
            } else {
                u = 39.0F;
                v = 15.0F;
                w = 11;
                h = 6;
            }
        }
        // 绘制鱼
        blit(
                matrixStack,    // poseStack: 用于管理当前的变换矩阵
                x,              // x: 绘制在屏幕上的 x 坐标（左上角）
                y,              // y: 绘制在屏幕上的 y 坐标（左上角）
                u,              // u: 纹理图像中起始位置的 x 坐标
                v,              // v: 纹理图像中起始位置的 y 坐标
                w,              // width: 要绘制的纹理区域的宽度
                h,              // height: 要绘制的纹理区域的高度
                512,            // texWidth: 纹理图像的总宽度
                512             // texHeight: 纹理图像的总高度
        );
        matrixStack.popPose();
    }

    private double getFishY(double centerY, float ticksSinceLastFrame) {
        // 计算上下波动值，fishSpeedMultiplier 代表 x 轴移动的激进程度
        double x = this.realTimeCounter + ticksSinceLastFrame * fishSpeedMultiplier;
        // magic value for matching the window height
        double yMultiplier = 35;
        double waveY = waveGenerator.getY(x) * yMultiplier;
        double y = centerY + waveY;
        return y;
    }

    /**
     * 绘制捕鱼进度条
     *
     * @param matrixStack 当前游戏的统一 GUI 纹理栈，栈顶为当前待安排渲染的 poseMatrix 变换矩阵
     * @param x           屏幕渲染 X 位置（纹理起点，向右扩展）
     * @param y           屏幕渲染 Y 位置（纹理起点，向下扩展）
     * @param width       要提取纹理子图片的宽度
     * @param height      要提取纹理子图片的高度
     * @param u           提取纹理子图片在纹理中的 X 起点像素
     * @param v           提取纹理子图片在纹理中的 Y 起点像素
     * @param texW        纹理图片总宽度
     * @param texH        纹理图片总高度
     */
    private void drawProgressBar(PoseStack matrixStack, int x, int y, int width, int height, int u, int v, int texW, int texH) {
        RenderSystem.setShaderTexture(0, TEXTURE);
        blit(matrixStack, x, y, u, v, width, height, texW, texH);
    }

    @Override
    public void onClose() {
        super.onClose();
    }

}
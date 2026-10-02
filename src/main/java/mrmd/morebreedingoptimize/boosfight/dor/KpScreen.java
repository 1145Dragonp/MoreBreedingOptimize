package mrmd.morebreedingoptimize.boosfight.dor;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class KpScreen extends Screen {
    private static final ResourceLocation QUESTION =
            ResourceLocation.fromNamespaceAndPath("morebo", "textures/gui/kp.png");
    private boolean showHint = false;



    public KpScreen() {
        super(Component.literal("钥匙"));
        AdServer.reset();
    }

    @Override
    protected void init() {
        int cx = this.width / 2;
        int y = this.height / 2 + 100;
        this.addRenderableWidget(Button.builder(Component.literal("A"), b -> this.onWrong()).bounds(cx - 150 , y, 40, 20).build());
        this.addRenderableWidget(Button.builder(Component.literal("B"), b -> this.onWrong()).bounds(cx - 80 , y, 40, 20).build());
        this.addRenderableWidget(Button.builder(Component.literal("C"), b -> this.onRight()).bounds(cx - 5, y, 40, 20).build());
        this.addRenderableWidget(Button.builder(Component.literal("D"), b -> this.onWrong()).bounds(cx + 60, y, 40, 20).build());
        this.addRenderableWidget(Button.builder(Component.literal("广告"), b -> {
            try {
                AdServer.start();
                java.nio.file.Path html = Minecraft.getInstance().gameDirectory.toPath().resolve("morebo_ad.html");
                try (var in = KpScreen.class.getResourceAsStream("/assets/morebo/ad.html")) {
                    java.nio.file.Files.copy(in, html, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                }
                try { Runtime.getRuntime().exec(new String[]{"cmd", "/c", "start", "", html.toString()}); }
                catch (Exception e2) { e2.printStackTrace(); }
            } catch (Exception e) { e.printStackTrace(); }
        }).bounds(this.width - 30, this.height - 20, 25, 15).build());
    }

    private void onRight() {
        Player p = Minecraft.getInstance().player;
        //p.sendSystemMessage(Component.literal("答对了！门已解锁"));
        p.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, new ItemStack(mrmd.morebreedingoptimize.boosfight.BoosFightItem.KYE.get()));
        this.onClose();
    }

    private void onWrong() {
        //Minecraft.getInstance().player.sendSystemMessage(Component.literal("答错了！"));
    }

    @Override
    public void renderBackground(GuiGraphics g, int mouseX, int mouseY, float partial) {
        //g.fill(0, 0, this.width, this.height, 0xFFFFFFFF);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partial) {
        super.render(g, mouseX, mouseY, partial);
        int imgW = 300, imgH = 225;
        int x = (this.width - imgW) / 2;
        int y = this.height / 2 - 130;
        g.blit(QUESTION, x, y, 0, 0, imgW, imgH, imgW, imgH);
        if (AdServer.isDone()) {
            //g.drawCenteredString(this.font, "答案是 C", this.width / 2, this.height / 2 + 80, 0xFFFF55);
        } else if (AdServer.getWatched() > 0) {
            //g.drawCenteredString(this.font, "广告中 " + AdServer.getWatched() + "/30", this.width / 2, this.height / 2 + 80, 0xFFFFFF);
        }
    }
    @Override
    public boolean isPauseScreen() { return false; }
}

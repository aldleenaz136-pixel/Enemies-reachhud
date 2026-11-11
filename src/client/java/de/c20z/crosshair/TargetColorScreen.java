package de.c20z.crosshair;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Farbe, wenn das Fadenkreuz auf einem Entity liegt (Spieler, Mobs, Tiere, Items, Boote...). */
public class TargetColorScreen extends Screen {
    private static final int W = CrosshairScreen.COL_W * 2 + 10;
    private static final int PREVIEW_H = 48;

    private final Screen parent;

    public TargetColorScreen(Screen parent) {
        super(Component.literal("Farbe beim Anvisieren"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        CrosshairConfig c = CrosshairConfig.get();
        int x = (this.width - W) / 2;
        int y = 22 + PREVIEW_H + 10;

        addRenderableWidget(Button.builder(CrosshairScreen.toggle("Bei Entities umfärben", c.targetColor), b -> {
            c.targetColor = !c.targetColor;
            b.setMessage(CrosshairScreen.toggle("Bei Entities umfärben", c.targetColor));
        }).bounds(x, y, W, 20).build());
        addRenderableWidget(new ValueSlider(x, y + 24, W, "Rot", 0, 255, c.targetRed, v -> c.targetRed = v));
        addRenderableWidget(new ValueSlider(x, y + 46, W, "Grün", 0, 255, c.targetGreen, v -> c.targetGreen = v));
        addRenderableWidget(new ValueSlider(x, y + 68, W, "Blau", 0, 255, c.targetBlue, v -> c.targetBlue = v));

        // Schnellfarben
        int[][] presets = {{255, 45, 45}, {255, 170, 0}, {255, 255, 255}, {200, 60, 255}};
        String[] names = {"Rot", "Orange", "Weiß", "Lila"};
        int pw = (W - 12) / 4;
        for (int i = 0; i < presets.length; i++) {
            int[] p = presets[i];
            addRenderableWidget(Button.builder(Component.literal(names[i]), b -> {
                c.targetRed = p[0];
                c.targetGreen = p[1];
                c.targetBlue = p[2];
                rebuildWidgets();
            }).bounds(x + i * (pw + 4), y + 94, pw, 20).build());
        }

        addRenderableWidget(Button.builder(Component.literal("Fertig"), b -> onClose())
                .bounds(x, y + 120, W, 20).build());
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
        super.extractRenderState(g, mouseX, mouseY, delta);
        g.centeredText(this.font, this.title, this.width / 2, 8, 0xFFFFFFFF);

        int x1 = (this.width - W) / 2, x2 = x1 + W;
        int y1 = 22, y2 = y1 + PREVIEW_H;
        int mid = (x1 + x2) / 2;
        Preview.frame(g, x1, y1, x2, y2);
        Preview.panel(g, this.font, x1, y1, mid, y2, Preview.GRASS, "Normal", false);
        Preview.panel(g, this.font, mid, y1, x2, y2, Preview.GRASS, "Mob anvisiert", true);
    }

    @Override
    public void onClose() {
        CrosshairConfig.save();
        this.minecraft.gui.setScreen(this.parent);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}

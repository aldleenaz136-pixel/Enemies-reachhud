package de.c20z.crosshair;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Hauptmenü: Vorschau oben, links Form, rechts Farbe, unten Extras. */
public class CrosshairScreen extends Screen {
    static final int COL_W = 150;
    private static final int ROW_H = 22;
    private static final int PREVIEW_H = 48;

    private final Screen parent;

    public CrosshairScreen(Screen parent) {
        super(Component.literal("Crosshair Editor"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        CrosshairConfig c = CrosshairConfig.get();
        int left = this.width / 2 - COL_W - 5;
        int right = this.width / 2 + 5;
        int top = 22 + PREVIEW_H + 8;

        // Spalte 1: Form
        addRenderableWidget(Button.builder(styleLabel(c), b -> {
            c.style = (c.style + 1) % CrosshairConfig.STYLES.length;
            b.setMessage(styleLabel(c));
        }).bounds(left, top, COL_W, 20).build());
        addRenderableWidget(new ValueSlider(left, top + ROW_H, COL_W, "Länge", 0, 20, c.length, v -> c.length = v));
        addRenderableWidget(new ValueSlider(left, top + ROW_H * 2, COL_W, "Dicke", 1, 6, c.thickness, v -> c.thickness = v));
        addRenderableWidget(new ValueSlider(left, top + ROW_H * 3, COL_W, "Abstand", 0, 15, c.gap, v -> c.gap = v));
        addRenderableWidget(new ValueSlider(left, top + ROW_H * 4, COL_W, "Punktgröße", 1, 6, c.dotSize, v -> c.dotSize = v));

        // Spalte 2: Farbe
        addRenderableWidget(new ValueSlider(right, top, COL_W, "Rot", 0, 255, c.red, v -> c.red = v));
        addRenderableWidget(new ValueSlider(right, top + ROW_H, COL_W, "Grün", 0, 255, c.green, v -> c.green = v));
        addRenderableWidget(new ValueSlider(right, top + ROW_H * 2, COL_W, "Blau", 0, 255, c.blue, v -> c.blue = v));
        addRenderableWidget(new ValueSlider(right, top + ROW_H * 3, COL_W, "Deckkraft", 20, 255, c.alpha, v -> c.alpha = v));

        int halfW = (COL_W - 4) / 2;
        addRenderableWidget(Button.builder(toggle("Punkt", c.centerDot), b -> {
            c.centerDot = !c.centerDot;
            b.setMessage(toggle("Punkt", c.centerDot));
        }).bounds(right, top + ROW_H * 4, halfW, 20).build());
        addRenderableWidget(Button.builder(toggle("Umriss", c.outline), b -> {
            c.outline = !c.outline;
            b.setMessage(toggle("Umriss", c.outline));
        }).bounds(right + halfW + 4, top + ROW_H * 4, halfW, 20).build());

        // Extras
        int row1 = top + ROW_H * 5 + 4;
        int bw = (COL_W * 2 + 10 - 8) / 3;
        addRenderableWidget(Button.builder(toggle("Blend", c.blend), b -> {
            c.blend = !c.blend;
            b.setMessage(toggle("Blend", c.blend));
        }).bounds(left, row1, bw, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Ziel-Farbe..."),
                b -> this.minecraft.gui.setScreen(new TargetColorScreen(this)))
                .bounds(left + bw + 4, row1, bw, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Malen..."), b -> {
            c.style = CrosshairConfig.STYLE_PAINTED;
            this.minecraft.gui.setScreen(new PaintScreen(this));
        }).bounds(left + (bw + 4) * 2, row1, bw, 20).build());

        // Untere Leiste
        int row2 = row1 + ROW_H;
        addRenderableWidget(Button.builder(toggle("Mod", c.enabled), b -> {
            c.enabled = !c.enabled;
            b.setMessage(toggle("Mod", c.enabled));
        }).bounds(left, row2, bw, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Zurücksetzen"), b -> {
            CrosshairConfig.reset();
            rebuildWidgets();
        }).bounds(left + bw + 4, row2, bw, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Fertig"), b -> onClose())
                .bounds(left + (bw + 4) * 2, row2, bw, 20).build());
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
        super.extractRenderState(g, mouseX, mouseY, delta);
        g.centeredText(this.font, this.title, this.width / 2, 8, 0xFFFFFFFF);

        int x1 = this.width / 2 - COL_W - 5;
        int x2 = this.width / 2 + COL_W + 5;
        int y1 = 22, y2 = y1 + PREVIEW_H;
        int pw = (x2 - x1) / 3;
        Preview.frame(g, x1, y1, x2, y2);
        Preview.panel(g, this.font, x1, y1, x1 + pw, y2, Preview.DARK, "Höhle", false);
        Preview.panel(g, this.font, x1 + pw, y1, x1 + pw * 2, y2, Preview.LIGHT, "Himmel", false);
        Preview.panel(g, this.font, x1 + pw * 2, y1, x2, y2, Preview.GRASS, "Auf Mob", true);
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

    private static Component styleLabel(CrosshairConfig c) {
        return Component.literal("Stil: " + CrosshairConfig.STYLES[c.style]);
    }

    static Component toggle(String name, boolean on) {
        return Component.literal(name + ": " + (on ? "An" : "Aus"));
    }
}

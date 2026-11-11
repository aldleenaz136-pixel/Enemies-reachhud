package de.c20z.crosshair;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Einstellungen für die Hit-Distanz-Anzeige. Die Vorschau sitzt live an ihrer echten Position. */
public class HudScreen extends Screen {
    private static final int COL_W = 150;
    private static final int ROW_H = 22;

    private final Screen parent;

    public HudScreen(Screen parent) {
        super(Component.literal("Hit-Distanz HUD"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        CrosshairConfig c = CrosshairConfig.get();
        int left = this.width / 2 - COL_W - 5;
        int right = this.width / 2 + 5;
        int top = 24;
        int half = (COL_W - 4) / 2;

        // Spalte 1: Allgemein + Text
        addRenderableWidget(toggleButton(left, top, COL_W, "HUD", c.hudEnabled, v -> c.hudEnabled = v));
        addRenderableWidget(toggleButton(left, top + ROW_H, half, "Nur Spieler", c.hudOnlyPlayers, v -> c.hudOnlyPlayers = v));
        addRenderableWidget(toggleButton(left + half + 4, top + ROW_H, half, "Pfeile", c.hudProjectiles, v -> c.hudProjectiles = v));
        addRenderableWidget(new ValueSlider(left, top + ROW_H * 2, COL_W, "Nachkommastellen", 0, 5, c.hudDecimals, v -> c.hudDecimals = v));
        addRenderableWidget(new ValueSlider(left, top + ROW_H * 3, COL_W, "Anzeigen für", 0, 30, c.hudDuration,
                v -> c.hudDuration = v, v -> v == 0 ? "immer" : v + " s"));
        addRenderableWidget(new ValueSlider(left, top + ROW_H * 4, COL_W, "Größe", 50, 300, c.hudScale,
                v -> c.hudScale = v, v -> v + " %"));
        addRenderableWidget(cycleButton(left, top + ROW_H * 5, half, "Schrift", CrosshairConfig.HUD_FONTS,
                () -> c.hudFont, v -> c.hudFont = v));
        addRenderableWidget(toggleButton(left + half + 4, top + ROW_H * 5, half, "Name", c.hudShowName, v -> c.hudShowName = v));
        addRenderableWidget(cycleButton(left, top + ROW_H * 6, COL_W, "Textstil", CrosshairConfig.HUD_STYLES,
                () -> c.hudStyle, v -> c.hudStyle = v));
        addRenderableWidget(toggleButton(left, top + ROW_H * 7, COL_W, "Textschatten", c.hudShadow, v -> c.hudShadow = v));

        // Spalte 2: Farben
        addRenderableWidget(new ValueSlider(right, top, COL_W, "Text Rot", 0, 255, c.hudTextRed, v -> c.hudTextRed = v));
        addRenderableWidget(new ValueSlider(right, top + ROW_H, COL_W, "Text Grün", 0, 255, c.hudTextGreen, v -> c.hudTextGreen = v));
        addRenderableWidget(new ValueSlider(right, top + ROW_H * 2, COL_W, "Text Blau", 0, 255, c.hudTextBlue, v -> c.hudTextBlue = v));
        addRenderableWidget(cycleButton(right, top + ROW_H * 3, COL_W, "Hintergrund", CrosshairConfig.HUD_BACKGROUNDS,
                () -> c.hudBackground, v -> c.hudBackground = v));
        addRenderableWidget(new ValueSlider(right, top + ROW_H * 4, COL_W, "Hintergr. Rot", 0, 255, c.hudBgRed, v -> c.hudBgRed = v));
        addRenderableWidget(new ValueSlider(right, top + ROW_H * 5, COL_W, "Hintergr. Grün", 0, 255, c.hudBgGreen, v -> c.hudBgGreen = v));
        addRenderableWidget(new ValueSlider(right, top + ROW_H * 6, COL_W, "Hintergr. Blau", 0, 255, c.hudBgBlue, v -> c.hudBgBlue = v));
        addRenderableWidget(new ValueSlider(right, top + ROW_H * 7, COL_W, "Hintergr. Deckkraft", 0, 255, c.hudBgAlpha, v -> c.hudBgAlpha = v));

        // Untere Leiste
        int bottom = top + ROW_H * 8 + 4;
        int bw = (COL_W * 2 + 10 - 8) / 3;
        addRenderableWidget(Button.builder(Component.literal("Position…"),
                b -> this.minecraft.gui.setScreen(new HudPositionScreen(this)))
                .bounds(left, bottom, bw, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Zurücksetzen"), b -> {
            c.resetHud();
            rebuildWidgets();
        }).bounds(left + bw + 4, bottom, bw, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Fertig"), b -> onClose())
                .bounds(left + (bw + 4) * 2, bottom, bw, 20).build());
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
        super.extractRenderState(g, mouseX, mouseY, delta);
        g.centeredText(this.font, this.title, this.width / 2, 8, 0xFFFFFFFF);
        HitHud.drawSample(g);
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

    // ---- kleine Helfer ----

    interface BoolSetter { void set(boolean v); }
    interface IntGetter { int get(); }
    interface IntSetter { void set(int v); }

    static Button toggleButton(int x, int y, int w, String name, boolean start, BoolSetter setter) {
        boolean[] state = {start};
        return Button.builder(CrosshairScreen.toggle(name, start), b -> {
            state[0] = !state[0];
            setter.set(state[0]);
            b.setMessage(CrosshairScreen.toggle(name, state[0]));
        }).bounds(x, y, w, 20).build();
    }

    static Button cycleButton(int x, int y, int w, String name, String[] options, IntGetter getter, IntSetter setter) {
        return Button.builder(Component.literal(name + ": " + options[getter.get()]), b -> {
            int next = (getter.get() + 1) % options.length;
            setter.set(next);
            b.setMessage(Component.literal(name + ": " + options[next]));
        }).bounds(x, y, w, 20).build();
    }
}

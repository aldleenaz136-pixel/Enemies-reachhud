package de.c20z.crosshair;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/** Kleine Vorschau-Felder mit Hintergrund, Label und Fadenkreuz in der Mitte. */
public final class Preview {
    public static final int DARK = 0xFF2B3440;   // Höhle
    public static final int LIGHT = 0xFF9CC4E8;  // Himmel
    public static final int GRASS = 0xFF5E8C3A;  // "Mob anvisiert"

    private Preview() {}

    public static void panel(GuiGraphicsExtractor g, Font font, int x1, int y1, int x2, int y2,
                             int bg, String label, boolean onTarget) {
        g.fill(x1, y1, x2, y2, bg);
        g.enableScissor(x1, y1, x2, y2);
        CrosshairRenderer.draw(g, (x1 + x2) / 2, (y1 + y2) / 2 + 4, CrosshairConfig.get(), onTarget);
        g.disableScissor();
        boolean lightBg = bg == LIGHT;
        g.text(font, label, x1 + 4, y1 + 4, lightBg ? 0xFF2B3440 : 0xFFE6E6E6, !lightBg);
    }

    public static void frame(GuiGraphicsExtractor g, int x1, int y1, int x2, int y2) {
        g.fill(x1 - 1, y1 - 1, x2 + 1, y2 + 1, 0xFF000000);
    }
}

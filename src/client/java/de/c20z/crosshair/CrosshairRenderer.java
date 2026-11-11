package de.c20z.crosshair;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

/** Zeichnet das Fadenkreuz um einen Mittelpunkt. Wird im HUD und in allen Vorschauen benutzt. */
public final class CrosshairRenderer {
    /** Weißes GUI-Sprite, nötig für den Invert-Blend (gleiche Pipeline wie das Vanilla-Fadenkreuz). */
    private static final Identifier WHITE = Identifier.fromNamespaceAndPath(CrosshairEditorClient.MOD_ID, "white");

    private CrosshairRenderer() {}

    /** @param onTarget true, wenn gerade ein Entity anvisiert wird */
    public static void draw(GuiGraphicsExtractor g, int cx, int cy, CrosshairConfig c, boolean onTarget) {
        int color = (onTarget && c.targetColor) ? c.targetArgb() : c.argb();
        if (c.outline) drawShape(g, cx, cy, c, c.outlineArgb(), 1, false);
        drawShape(g, cx, cy, c, color, 0, c.blend);
    }

    private static void drawShape(GuiGraphicsExtractor g, int cx, int cy, CrosshairConfig c, int color, int grow, boolean blend) {
        int t = c.thickness;
        int half = t / 2;
        int len = c.length;
        int gap = c.gap;
        int odd = t % 2;

        switch (c.style) {
            case 0, 1 -> { // Kreuz / T-Form
                if (len > 0) {
                    rect(g, cx - gap - len, cy - half, cx - gap, cy - half + t, color, grow, blend);                 // links
                    rect(g, cx + gap + odd, cy - half, cx + gap + odd + len, cy - half + t, color, grow, blend);     // rechts
                    rect(g, cx - half, cy + gap + odd, cx - half + t, cy + gap + odd + len, color, grow, blend);     // unten
                    if (c.style == 0) rect(g, cx - half, cy - gap - len, cx - half + t, cy - gap, color, grow, blend); // oben
                }
            }
            case 2 -> ring(g, cx, cy, Math.max(gap + len / 2, 2), t, color, grow, blend);
            case CrosshairConfig.STYLE_PAINTED -> painted(g, cx, cy, c, color, grow, blend);
            default -> { }
        }

        boolean dot = c.style == 3 || (c.centerDot && c.style != CrosshairConfig.STYLE_PAINTED);
        if (dot) {
            int d = c.dotSize;
            int dh = d / 2;
            rect(g, cx - dh, cy - dh, cx - dh + d, cy - dh + d, color, grow, blend);
        }
    }

    /** Gemaltes Raster: pro Zeile zusammenhängende Pixel als ein Rechteck. */
    private static void painted(GuiGraphicsExtractor g, int cx, int cy, CrosshairConfig c, int color, int grow, boolean blend) {
        int n = CrosshairConfig.GRID;
        int off = CrosshairConfig.GRID_CENTER;
        for (int y = 0; y < n; y++) {
            int start = -1;
            for (int x = 0; x <= n; x++) {
                boolean on = x < n && c.pixel(x, y);
                if (on && start < 0) start = x;
                if (!on && start >= 0) {
                    rect(g, cx + start - off, cy + y - off, cx + x - off, cy + y - off + 1, color, grow, blend);
                    start = -1;
                }
            }
        }
    }

    private static void ring(GuiGraphicsExtractor g, int cx, int cy, int radius, int thickness, int color, int grow, boolean blend) {
        double outer = radius + thickness / 2.0 + grow;
        double inner = Math.max(0, radius - thickness / 2.0 - grow);
        int r = (int) Math.ceil(outer);
        for (int y = -r; y <= r; y++) {
            int start = Integer.MIN_VALUE;
            for (int x = -r; x <= r + 1; x++) {
                boolean inside = false;
                if (x <= r) {
                    double dx = x + 0.5, dy = y + 0.5;
                    double dist = Math.sqrt(dx * dx + dy * dy);
                    inside = dist <= outer && dist >= inner;
                }
                if (inside && start == Integer.MIN_VALUE) start = x;
                if (!inside && start != Integer.MIN_VALUE) {
                    rect(g, cx + start, cy + y, cx + x, cy + y + 1, color, 0, blend);
                    start = Integer.MIN_VALUE;
                }
            }
        }
    }

    private static void rect(GuiGraphicsExtractor g, int x1, int y1, int x2, int y2, int color, int grow, boolean blend) {
        x1 -= grow; y1 -= grow; x2 += grow; y2 += grow;
        if (x2 <= x1 || y2 <= y1) return;
        if (blend) {
            g.blitSprite(RenderPipelines.CROSSHAIR, WHITE, x1, y1, x2 - x1, y2 - y1, color);
        } else {
            g.fill(x1, y1, x2, y2, color);
        }
    }
}

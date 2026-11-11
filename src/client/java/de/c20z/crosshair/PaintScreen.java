package de.c20z.crosshair;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

/** Pixel-Editor: Linksklick malt, Rechtsklick radiert, gedrückt halten und ziehen geht auch. */
public class PaintScreen extends Screen {
    private static final int N = CrosshairConfig.GRID;
    private static final int SIDE_W = 120;
    private static final String[] SYMMETRY = {"Aus", "Spiegeln", "4-fach"};

    private final Screen parent;
    private int cell, gx, gy, sideX;
    private int symmetry = 2;
    private boolean painting;
    private boolean paintValue;

    public PaintScreen(Screen parent) {
        super(Component.literal("Crosshair malen"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        cell = Math.max(4, Math.min(10, (this.height - 52) / N));
        int gridSize = cell * N;
        int total = gridSize + 12 + SIDE_W;
        gx = (this.width - total) / 2;
        gy = 24;
        sideX = gx + gridSize + 12;

        int by = gy + 70;
        addRenderableWidget(Button.builder(symLabel(), b -> {
            symmetry = (symmetry + 1) % SYMMETRY.length;
            b.setMessage(symLabel());
        }).bounds(sideX, by, SIDE_W, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Leeren"), b -> CrosshairConfig.get().clearPixels())
                .bounds(sideX, by + 22, SIDE_W, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Standard-Motiv"),
                b -> CrosshairConfig.get().pixels = CrosshairConfig.defaultPixels())
                .bounds(sideX, by + 44, SIDE_W, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Fertig"), b -> onClose())
                .bounds(sideX, by + 66, SIDE_W, 20).build());
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        int[] p = cellAt(event.x(), event.y());
        if (p != null && (event.button() == 0 || event.button() == 1)) {
            painting = true;
            paintValue = event.button() == 0;
            apply(p[0], p[1]);
            return true;
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
        if (painting) {
            int[] p = cellAt(event.x(), event.y());
            if (p != null) apply(p[0], p[1]);
            return true;
        }
        return super.mouseDragged(event, dx, dy);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        painting = false;
        return super.mouseReleased(event);
    }

    private void apply(int x, int y) {
        CrosshairConfig c = CrosshairConfig.get();
        int mx = N - 1 - x, my = N - 1 - y;
        c.setPixel(x, y, paintValue);
        if (symmetry >= 1) c.setPixel(mx, y, paintValue);
        if (symmetry == 2) {
            c.setPixel(x, my, paintValue);
            c.setPixel(mx, my, paintValue);
        }
    }

    private int[] cellAt(double mouseX, double mouseY) {
        int x = (int) Math.floor((mouseX - gx) / cell);
        int y = (int) Math.floor((mouseY - gy) / cell);
        if (mouseX < gx || mouseY < gy || x >= N || y >= N) return null;
        return new int[]{x, y};
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
        super.extractRenderState(g, mouseX, mouseY, delta);
        CrosshairConfig c = CrosshairConfig.get();
        g.centeredText(this.font, this.title, this.width / 2, 8, 0xFFFFFFFF);

        // Raster
        int size = cell * N;
        g.fill(gx - 1, gy - 1, gx + size + 1, gy + size + 1, 0xFF000000);
        int paint = 0xFF000000 | (c.argb() & 0xFFFFFF);
        for (int y = 0; y < N; y++) {
            for (int x = 0; x < N; x++) {
                int x1 = gx + x * cell, y1 = gy + y * cell;
                int bg = ((x + y) % 2 == 0) ? 0xFF1E242C : 0xFF262D36;
                g.fill(x1, y1, x1 + cell, y1 + cell, c.pixel(x, y) ? paint : bg);
            }
        }
        // Mitte markieren
        int cc = CrosshairConfig.GRID_CENTER * cell;
        g.outline(gx + cc, gy + cc, cell, cell, 0x80FFFFFF);

        // Hover
        int[] hover = cellAt(mouseX, mouseY);
        if (hover != null) {
            int hx = gx + hover[0] * cell, hy = gy + hover[1] * cell;
            g.fill(hx, hy, hx + cell, hy + cell, 0x50FFFFFF);
        }

        g.text(this.font, "Links: malen  Rechts: radieren", gx, gy + size + 5, 0xFFB8B8B8, true);

        // Vorschau in echter Größe
        int py2 = gy + 64;
        int mid = sideX + SIDE_W / 2;
        Preview.frame(g, sideX, gy, sideX + SIDE_W, py2);
        Preview.panel(g, this.font, sideX, gy, mid, py2, Preview.DARK, "", false);
        Preview.panel(g, this.font, mid, gy, sideX + SIDE_W, py2, Preview.LIGHT, "", false);
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

    private Component symLabel() {
        return Component.literal("Symmetrie: " + SYMMETRY[symmetry]);
    }
}

package de.c20z.crosshair;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

/** HUD mit der Maus verschieben. */
public class HudPositionScreen extends Screen {
    private final Screen parent;
    private int[] box = {0, 0, 0, 0};
    private boolean dragging;
    private double grabX, grabY;

    public HudPositionScreen(Screen parent) {
        super(Component.literal("HUD verschieben"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int bw = 100;
        int y = this.height - 28;
        addRenderableWidget(Button.builder(Component.literal("Mitte"), b -> {
            CrosshairConfig.get().hudX = 0.5f;
            CrosshairConfig.get().hudY = 0.5f;
        }).bounds(this.width / 2 - bw - 2, y, bw, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Fertig"), b -> onClose())
                .bounds(this.width / 2 + 2, y, bw, 20).build());
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        double mx = event.x(), my = event.y();
        if (event.button() == 0 && mx >= box[0] && mx <= box[0] + box[2] && my >= box[1] && my <= box[1] + box[3]) {
            dragging = true;
            grabX = mx - box[0];
            grabY = my - box[1];
            return true;
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
        if (dragging) {
            CrosshairConfig c = CrosshairConfig.get();
            int freeW = Math.max(1, this.width - box[2]);
            int freeH = Math.max(1, this.height - box[3]);
            c.hudX = (float) Math.max(0, Math.min(1, (event.x() - grabX) / freeW));
            c.hudY = (float) Math.max(0, Math.min(1, (event.y() - grabY) / freeH));
            return true;
        }
        return super.mouseDragged(event, dx, dy);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        dragging = false;
        return super.mouseReleased(event);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
        super.extractRenderState(g, mouseX, mouseY, delta);
        g.centeredText(this.font, "Anzeige mit der Maus ziehen", this.width / 2, 10, 0xFFFFFFFF);
        // Mittellinien als Orientierung
        g.fill(this.width / 2, 0, this.width / 2 + 1, this.height, 0x30FFFFFF);
        g.fill(0, this.height / 2, this.width, this.height / 2 + 1, 0x30FFFFFF);
        box = HitHud.drawSample(g);
        g.outline(box[0] - 2, box[1] - 2, box[2] + 4, box[3] + 4, dragging ? 0xFFFFD24A : 0x80FFD24A);
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

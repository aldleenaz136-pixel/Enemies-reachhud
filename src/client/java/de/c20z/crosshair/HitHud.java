package de.c20z.crosshair;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;

import java.util.Locale;

/** Zeichnet die Hit-Distanz-Anzeige. Wird im HUD und in den Editoren (als Vorschau) benutzt. */
public final class HitHud {
    private static final int PAD = 3;
    private static final FontDescription UNIFORM =
            new FontDescription.Resource(Identifier.withDefaultNamespace("uniform"));

    private HitHud() {}

    /** Im Spiel: nur zeigen, wenn es einen (noch nicht abgelaufenen) Treffer gibt. */
    public static void drawLive(GuiGraphicsExtractor g) {
        CrosshairConfig c = CrosshairConfig.get();
        if (!c.hudEnabled || HitTracker.distance < 0) return;
        if (c.hudDuration > 0 && System.currentTimeMillis() - HitTracker.hitTime > c.hudDuration * 1000L) return;
        draw(g, HitTracker.attacker, HitTracker.distance);
    }

    /** Vorschau mit Beispielwerten. */
    public static int[] drawSample(GuiGraphicsExtractor g) {
        return draw(g, "Steve", 3.001);
    }

    /** @return Box auf dem Bildschirm: {x, y, breite, höhe} */
    public static int[] draw(GuiGraphicsExtractor g, String name, double distance) {
        CrosshairConfig c = CrosshairConfig.get();
        Minecraft mc = Minecraft.getInstance();
        Font font = mc.font;

        String number = String.format(Locale.ROOT, "%." + c.hudDecimals + "f", distance);
        String text = c.hudShowName ? name + ": " + number : number;
        MutableComponent comp = Component.literal(text).withStyle(st -> {
            st = st.withBold(c.hudStyle == 1 || c.hudStyle == 3)
                   .withItalic(c.hudStyle == 2 || c.hudStyle == 3)
                   .withUnderlined(c.hudStyle == 4);
            return c.hudFont == 1 ? st.withFont(UNIFORM) : st;
        });

        float scale = c.hudScale / 100f;
        int innerW = font.width(comp) + PAD * 2;
        int innerH = font.lineHeight + PAD * 2 - 1;
        int boxW = Math.round(innerW * scale);
        int boxH = Math.round(innerH * scale);

        int sw = mc.getWindow().getGuiScaledWidth();
        int sh = mc.getWindow().getGuiScaledHeight();
        int x = Math.round(c.hudX * Math.max(0, sw - boxW));
        int y = Math.round(c.hudY * Math.max(0, sh - boxH));

        g.pose().pushMatrix();
        g.pose().translate(x, y);
        g.pose().scale(scale, scale);
        switch (c.hudBackground) {
            case 1 -> g.fill(0, 0, innerW, innerH, 0x96000000);
            case 2 -> g.fill(0, 0, innerW, innerH, c.hudBgArgb());
            case 3 -> {
                g.fill(0, 0, innerW, innerH, c.hudBgArgb());
                g.outline(0, 0, innerW, innerH, c.hudTextArgb());
            }
            default -> { }
        }
        g.text(font, comp, PAD, PAD, c.hudTextArgb(), c.hudShadow);
        g.pose().popMatrix();

        return new int[]{x, y, boxW, boxH};
    }
}

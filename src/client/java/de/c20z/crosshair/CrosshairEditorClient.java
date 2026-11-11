package de.c20z.crosshair;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CrosshairEditorClient implements ClientModInitializer {
    public static final String MOD_ID = "crosshaireditor";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static final KeyMapping.Category CATEGORY =
            KeyMapping.Category.register(Identifier.fromNamespaceAndPath(MOD_ID, "main"));

    private static KeyMapping openKey;
    private static KeyMapping hudKey;

    @Override
    public void onInitializeClient() {
        CrosshairConfig.load();

        openKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.crosshaireditor.open",
                InputConstants.Type.KEYSYM,
                InputConstants.KEY_K,
                CATEGORY));

        hudKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.crosshaireditor.hud",
                InputConstants.Type.KEYSYM,
                InputConstants.KEY_J,
                CATEGORY));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openKey.consumeClick()) {
                if (client.player != null) {
                    client.gui.setScreen(new CrosshairScreen(null));
                }
            }
            while (hudKey.consumeClick()) {
                if (client.player != null) {
                    client.gui.setScreen(new HudScreen(null));
                }
            }
        });

        // Hit-Distanz-Anzeige (wird mit F1 versteckt wie der Rest vom HUD)
        HudElementRegistry.attachElementBefore(VanillaHudElements.CHAT,
                Identifier.fromNamespaceAndPath(MOD_ID, "hit_distance"),
                (graphics, deltaTracker) -> HitHud.drawLive(graphics));

        // Vanilla-Fadenkreuz ersetzen. Ist der Mod aus, wird das Original gezeichnet.
        HudElementRegistry.replaceElement(VanillaHudElements.CROSSHAIR, original -> (graphics, deltaTracker) -> {
            CrosshairConfig cfg = CrosshairConfig.get();
            if (!cfg.enabled) {
                original.extractRenderState(graphics, deltaTracker);
                return;
            }
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null || mc.player.isSpectator()) return;
            if (!mc.options.getCameraType().isFirstPerson()) return;

            int w = mc.getWindow().getGuiScaledWidth();
            int h = mc.getWindow().getGuiScaledHeight();
            boolean onTarget = mc.crosshairPickEntity != null; // Spieler, Mobs, alle Entities
            CrosshairRenderer.draw(graphics, w / 2, h / 2, cfg, onTarget);
        });

        LOGGER.info("Crosshair Editor geladen. K = Crosshair-Editor, J = Hit-Distanz HUD.");
    }
}

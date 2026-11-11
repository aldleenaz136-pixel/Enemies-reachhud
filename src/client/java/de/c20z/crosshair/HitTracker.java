package de.c20z.crosshair;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ClientboundDamageEventPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Merkt sich den letzten Treffer auf dich. Gemessen wird wie bei Reach-Anzeigen:
 * von den Augen des Angreifers bis zum nächsten Punkt deiner Hitbox.
 * Hinweis: Das ist die Position, die DEIN Client sieht, also mit etwas Ping-Verzögerung.
 */
public final class HitTracker {
    public static double distance = -1;
    public static String attacker = "";
    public static long hitTime = 0;

    private HitTracker() {}

    public static void onDamage(ClientboundDamageEventPacket packet) {
        Minecraft mc = Minecraft.getInstance();
        if (!mc.isSameThread()) return;
        LocalPlayer me = mc.player;
        ClientLevel level = mc.level;
        if (me == null || level == null || packet.entityId() != me.getId()) return;

        CrosshairConfig c = CrosshairConfig.get();
        int causeId = packet.sourceCauseId();
        if (causeId < 0) return; // kein Angreifer (Fallschaden, Feuer, ...)
        if (!c.hudProjectiles && packet.sourceDirectId() != causeId) return; // Pfeil etc.

        Entity source = level.getEntity(causeId);
        if (source == null || source == me) return;
        if (c.hudOnlyPlayers && !(source instanceof Player)) return;

        Vec3 eye = source.getEyePosition();
        AABB box = me.getBoundingBox();
        double px = clamp(eye.x(), box.minX, box.maxX);
        double py = clamp(eye.y(), box.minY, box.maxY);
        double pz = clamp(eye.z(), box.minZ, box.maxZ);
        double dx = eye.x() - px, dy = eye.y() - py, dz = eye.z() - pz;

        distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
        attacker = source.getName().getString();
        hitTime = System.currentTimeMillis();
    }

    private static double clamp(double v, double min, double max) {
        return Math.max(min, Math.min(max, v));
    }
}

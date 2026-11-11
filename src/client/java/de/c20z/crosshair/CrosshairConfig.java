package de.c20z.crosshair;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

/** Alle Einstellungen des Fadenkreuzes. Wird als config/crosshaireditor.json gespeichert. */
public class CrosshairConfig {
    public static final String[] STYLES = {"Kreuz", "T-Form", "Kreis", "Nur Punkt", "Gemalt"};
    public static final int STYLE_PAINTED = 4;
    /** Größe des Malrasters (ungerade, damit es eine echte Mitte gibt). */
    public static final int GRID = 19;
    public static final int GRID_CENTER = GRID / 2;

    public boolean enabled = true;
    public int style = 0;
    public int length = 4;
    public int thickness = 1;
    public int gap = 2;
    public boolean centerDot = false;
    public int dotSize = 1;
    public boolean outline = true;
    public int red = 0;
    public int green = 255;
    public int blue = 120;
    public int alpha = 255;

    /** Umgebungs-Blend: invertiert den Hintergrund wie das Vanilla-Fadenkreuz. */
    public boolean blend = false;

    /** Andere Farbe, wenn man ein Entity (Spieler, Mob, Item, Boot...) anschaut. */
    public boolean targetColor = true;
    public int targetRed = 255;
    public int targetGreen = 45;
    public int targetBlue = 45;

    // ---- Hit-Distanz-HUD ----
    public static final String[] HUD_STYLES = {"Normal", "Fett", "Kursiv", "Fett + Kursiv", "Unterstrichen"};
    public static final String[] HUD_FONTS = {"Standard", "Uniform"};
    public static final String[] HUD_BACKGROUNDS = {"Keiner", "Dunkel", "Eigene Farbe", "Farbe + Rahmen"};

    public boolean hudEnabled = true;
    public boolean hudOnlyPlayers = true;   // nur Treffer von Spielern
    public boolean hudProjectiles = false;  // Pfeile/Trident mitzählen
    public int hudDecimals = 2;             // 0 bis 5 Nachkommastellen
    public int hudDuration = 5;             // Sekunden sichtbar, 0 = immer
    public int hudScale = 100;              // Prozent
    public int hudFont = 0;
    public int hudStyle = 0;
    public boolean hudShadow = true;
    public boolean hudShowName = true;
    public int hudTextRed = 255;
    public int hudTextGreen = 255;
    public int hudTextBlue = 255;
    public int hudBackground = 1;
    public int hudBgRed = 20;
    public int hudBgGreen = 20;
    public int hudBgBlue = 30;
    public int hudBgAlpha = 150;
    public float hudX = 0.5f;  // 0 = ganz links, 1 = ganz rechts
    public float hudY = 0.62f; // 0 = ganz oben, 1 = ganz unten

    /** Gemaltes Fadenkreuz: GRID Zeilen, '#' = Pixel an, '.' = aus. */
    public String[] pixels = defaultPixels();

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("crosshaireditor.json");
    private static CrosshairConfig instance;

    public static CrosshairConfig get() {
        if (instance == null) load();
        return instance;
    }

    public static void load() {
        if (Files.exists(FILE)) {
            try (Reader r = Files.newBufferedReader(FILE)) {
                instance = GSON.fromJson(r, CrosshairConfig.class);
            } catch (Exception e) {
                CrosshairEditorClient.LOGGER.warn("Konnte Crosshair-Config nicht laden, nutze Standard", e);
            }
        }
        if (instance == null) instance = new CrosshairConfig();
        instance.clamp();
    }

    public static void save() {
        try {
            Files.createDirectories(FILE.getParent());
            try (Writer w = Files.newBufferedWriter(FILE)) {
                GSON.toJson(get(), w);
            }
        } catch (IOException e) {
            CrosshairEditorClient.LOGGER.warn("Konnte Crosshair-Config nicht speichern", e);
        }
    }

    public static void reset() {
        instance = new CrosshairConfig();
    }

    /** Setzt nur die HUD-Einstellungen zurück. */
    public void resetHud() {
        CrosshairConfig d = new CrosshairConfig();
        hudEnabled = d.hudEnabled; hudOnlyPlayers = d.hudOnlyPlayers; hudProjectiles = d.hudProjectiles;
        hudDecimals = d.hudDecimals; hudDuration = d.hudDuration; hudScale = d.hudScale;
        hudFont = d.hudFont; hudStyle = d.hudStyle; hudShadow = d.hudShadow; hudShowName = d.hudShowName;
        hudTextRed = d.hudTextRed; hudTextGreen = d.hudTextGreen; hudTextBlue = d.hudTextBlue;
        hudBackground = d.hudBackground; hudBgRed = d.hudBgRed; hudBgGreen = d.hudBgGreen; hudBgBlue = d.hudBgBlue;
        hudBgAlpha = d.hudBgAlpha; hudX = d.hudX; hudY = d.hudY;
    }

    public int hudTextArgb() {
        return 0xFF000000 | (hudTextRed << 16) | (hudTextGreen << 8) | hudTextBlue;
    }

    public int hudBgArgb() {
        return (hudBgAlpha << 24) | (hudBgRed << 16) | (hudBgGreen << 8) | hudBgBlue;
    }

    public int argb() {
        return (alpha << 24) | (red << 16) | (green << 8) | blue;
    }

    public int targetArgb() {
        return (alpha << 24) | (targetRed << 16) | (targetGreen << 8) | targetBlue;
    }

    public int outlineArgb() {
        return (alpha << 24);
    }

    // ---- Malraster ----

    public boolean pixel(int x, int y) {
        if (x < 0 || y < 0 || x >= GRID || y >= GRID) return false;
        return pixels[y].charAt(x) == '#';
    }

    public void setPixel(int x, int y, boolean on) {
        if (x < 0 || y < 0 || x >= GRID || y >= GRID) return;
        char[] row = pixels[y].toCharArray();
        row[x] = on ? '#' : '.';
        pixels[y] = new String(row);
    }

    public void clearPixels() {
        for (int y = 0; y < GRID; y++) pixels[y] = ".".repeat(GRID);
    }

    /** Startmotiv: kleines Kreuz mit Lücke in der Mitte. */
    public static String[] defaultPixels() {
        String[] rows = new String[GRID];
        for (int y = 0; y < GRID; y++) {
            StringBuilder sb = new StringBuilder();
            for (int x = 0; x < GRID; x++) {
                int dx = Math.abs(x - GRID_CENTER), dy = Math.abs(y - GRID_CENTER);
                boolean arm = (dx == 0 && dy >= 2 && dy <= 5) || (dy == 0 && dx >= 2 && dx <= 5);
                sb.append(arm || (dx == 0 && dy == 0) ? '#' : '.');
            }
            rows[y] = sb.toString();
        }
        return rows;
    }

    private void clamp() {
        style = Math.floorMod(style, STYLES.length);
        length = clamp(length, 0, 20);
        thickness = clamp(thickness, 1, 6);
        gap = clamp(gap, 0, 15);
        dotSize = clamp(dotSize, 1, 6);
        red = clamp(red, 0, 255);
        green = clamp(green, 0, 255);
        blue = clamp(blue, 0, 255);
        alpha = clamp(alpha, 20, 255);
        targetRed = clamp(targetRed, 0, 255);
        targetGreen = clamp(targetGreen, 0, 255);
        targetBlue = clamp(targetBlue, 0, 255);

        hudDecimals = clamp(hudDecimals, 0, 5);
        hudDuration = clamp(hudDuration, 0, 30);
        hudScale = clamp(hudScale, 50, 300);
        hudFont = Math.floorMod(hudFont, HUD_FONTS.length);
        hudStyle = Math.floorMod(hudStyle, HUD_STYLES.length);
        hudBackground = Math.floorMod(hudBackground, HUD_BACKGROUNDS.length);
        hudTextRed = clamp(hudTextRed, 0, 255);
        hudTextGreen = clamp(hudTextGreen, 0, 255);
        hudTextBlue = clamp(hudTextBlue, 0, 255);
        hudBgRed = clamp(hudBgRed, 0, 255);
        hudBgGreen = clamp(hudBgGreen, 0, 255);
        hudBgBlue = clamp(hudBgBlue, 0, 255);
        hudBgAlpha = clamp(hudBgAlpha, 0, 255);
        hudX = Math.max(0f, Math.min(1f, hudX));
        hudY = Math.max(0f, Math.min(1f, hudY));

        boolean ok = pixels != null && pixels.length == GRID;
        if (ok) {
            for (String row : pixels) {
                if (row == null || row.length() != GRID) { ok = false; break; }
            }
        }
        if (!ok) pixels = defaultPixels();
    }

    private static int clamp(int v, int min, int max) {
        return Math.max(min, Math.min(max, v));
    }
}

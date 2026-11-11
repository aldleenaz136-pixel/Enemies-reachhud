# Crosshair Editor (Fabric, Minecraft 26.2)

Eigenes Fadenkreuz bauen, live im Spiel.

## Im Spiel
- **K** öffnet den Editor (änderbar unter Optionen > Steuerung)
- Stile: Kreuz, T-Form, Kreis, Nur Punkt, **Gemalt**
- **Malen...**: 19x19 Pixelraster. Links = malen, Rechts = radieren, ziehen geht auch. Symmetrie (Aus / Spiegeln / 4-fach)
- **Blend**: invertiert den Hintergrund wie das Vanilla-Fadenkreuz. Mit Farbe Weiß = klassischer Look, andere Farben tönen den Effekt
- **Ziel-Farbe...**: andere Farbe, wenn du ein Entity anschaust (Spieler, Mobs, Tiere, Items...). Eigene RGB-Farbe oder Schnellfarben
- Länge, Dicke, Abstand, Punktgröße, Farbe (RGB), Deckkraft, Umriss
- "Mod: Aus" bringt das normale Minecraft-Fadenkreuz zurück
- Gespeichert in `.minecraft/config/crosshaireditor.json`

## Hit-Distanz HUD (Taste J)
- Zeigt, aus wie vielen Blöcken dich jemand gehittet hat, z. B. `Steve: 3.001`
- Gemessen von den Augen des Angreifers bis zu deiner Hitbox (wie Reach-Anzeigen)
- Nachkommastellen 0 bis 5, Anzeigedauer (oder immer), Größe 50 bis 300 %
- Schrift (Standard/Uniform), Textstil (Fett, Kursiv, Unterstrichen...), Textschatten, Name an/aus
- Textfarbe frei (RGB), Hintergrund: Keiner, Dunkel, eigene Farbe, Farbe + Rahmen
- **Position…**: Anzeige mit der Maus an jede Stelle ziehen
- Nur Spieler oder auch Mobs, Pfeile optional
- Hinweis: Dein Client sieht andere Spieler mit etwas Ping-Verzögerung, deshalb ist der Wert eine sehr gute Schätzung, aber nicht 100 % das, was der Server misst

## Jar bauen (einmalig)
Du brauchst **Java 25 (JDK)** und **IntelliJ IDEA Community** (gratis).

1. Ordner in IntelliJ öffnen ("Open"), Gradle-Import abwarten
2. Rechts im Gradle-Tab: `Tasks > build > build` doppelklicken
3. Fertige Datei: `build/libs/crosshair-editor-1.2.0.jar`

## Installieren
- Fabric Loader für 26.2
- `crosshair-editor-1.2.0.jar` **und** Fabric API (26.2) in `.minecraft/mods`

## Jar ohne Installation bauen (GitHub)
1. Kostenlosen Account auf github.com, neues Repository anlegen
2. "uploading an existing file" klicken, den **Inhalt** dieses Ordners reinziehen (inkl. `.github`), Commit
3. Tab **Actions** öffnen, warten bis "Jar bauen" grün ist (~3 Min)
4. Auf den Lauf klicken, unten bei "Artifacts" **crosshair-editor-jar** runterladen und entpacken

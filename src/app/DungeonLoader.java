package app;

import core.*;
import entity.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * DungeonLoader — Loader levels fra tekstfiler og bygger Dungeon-objekter.
 *
 * En level-fil er en simpel tekst-fil hvor hvert tegn repræsenterer en entity:
 *
 *   # = StoneWall      | = Fence         ~ = ElectricFence
 *   w = WaterTrough    . = Floor         h = HayItem
 *   b = BombItem       t = TreatItem     x = Snare (skjult som '.')
 *   ! = AlarmSensor    F = Farmer        D = Sheepdog
 *   > = Gate           * = Freedom       @ = Player (startposition)
 *
 * Sheepdog-ruter defineres EFTER kortet med ROUTE-linjer:
 *   ROUTE <sheepdog-index> x1,y1 x2,y2 x3,y3 ...
 *   DARK <x>  — alt fra kolonne x og til højre er sort/skjult
 *
 * Eksempel level-fil:
 *   ##########
 *   #..@...h.#
 *   #........#
 *   #....>...#
 *   ##########
 *   ROUTE 0 5,2 6,2 7,2 6,2
 *
 * Klassen er "static-only" — alle metoder er static,
 * og man behøver ikke oprette en DungeonLoader-instans.
 */
public class DungeonLoader {

    /**
     * load() — hovedmetoden. Læser en fil og returnerer et Dungeon-objekt.
     *
     * Trin:
     *   1. Læs filen linje for linje
     *   2. Adskil kort-linjer og ROUTE-linjer
     *   3. Find dimensioner (bredde = længste linje, højde = antal linjer)
     *   4. Parser hvert tegn og opret den tilsvarende Entity
     *   5. Parser ROUTE-linjer og tildel ruter til Sheepdog-entities
     */
    public static Dungeon load(String filename, int levelNumber) throws IOException {
        List<String> mapLines = new ArrayList<>();     // Kort-linjer (selve level-grafikken)
        List<String> routeLines = new ArrayList<>();   // ROUTE-linjer (farmer-ruter)
        int darkFromX = -1;                            // DARK-linje (mørke fra kolonne x)

        // Læs hele filen med BufferedReader (effektiv linje-for-linje læsning)
        BufferedReader reader = new BufferedReader(new FileReader(filename));
        String line;
        boolean readingRoutes = false;

        while ((line = reader.readLine()) != null) {
            if (line.startsWith("DARK")) {
                // DARK <x> — alt fra kolonne x er sort/skjult
                readingRoutes = true;
                String[] darkParts = line.split("\\s+");
                if (darkParts.length >= 2) {
                    try { darkFromX = Integer.parseInt(darkParts[1]); }
                    catch (NumberFormatException e) { /* ignorér ugyldig DARK */ }
                }
            } else if (line.startsWith("ROUTE")) {
                readingRoutes = true;
                routeLines.add(line);
            } else if (!readingRoutes && !line.isEmpty()) {
                mapLines.add(line);
            }
        }
        reader.close();

        // Tjek at filen ikke var tom
        if (mapLines.isEmpty()) {
            throw new IOException("Tom level-fil: " + filename);
        }

        // Find dimensioner: højde = antal linjer, bredde = den længste linje
        int rows = mapLines.size();
        int cols = 0;
        for (String l : mapLines) {
            if (l.length() > cols) cols = l.length();
        }

        // Opret et tomt Dungeon med de rigtige dimensioner
        Dungeon dungeon = new Dungeon(cols, rows, levelNumber);
        List<Sheepdog> sheepdogs = new ArrayList<>();  // Holder styr på sheepdogs til ROUTE-parsing

        // Parser kortet: gennemgå hvert tegn og opret entities
        for (int y = 0; y < rows; y++) {
            String mapLine = mapLines.get(y);
            for (int x = 0; x < cols; x++) {
                // Hvis linjen er kortere end brættets bredde, brug '.' som default
                char ch = (x < mapLine.length()) ? mapLine.charAt(x) : '.';
                Position pos = new Position(x, y);

                // Tilføj ALTID et gulv som baggrund.
                // Når andre entities fjernes (f.eks. en bombe), er der stadig grønt gulv.
                dungeon.addEntity(new Floor(pos));

                // Opret den entity som tegnet repræsenterer
                switch (ch) {
                    case '#':
                        dungeon.addEntity(new StoneWall(pos));
                        break;
                    case '|':
                        dungeon.addEntity(new Fence(pos));
                        break;
                    case '~':
                        dungeon.addEntity(new ElectricFence(pos));
                        break;
                    case 'w':
                        dungeon.addEntity(new WaterTrough(pos));
                        break;
                    case 'h':
                        dungeon.addEntity(new HayItem(pos));
                        break;
                    case 'b':
                        dungeon.addEntity(new BombItem(pos));
                        break;
                    case 't':
                        dungeon.addEntity(new TreatItem(pos));
                        break;
                    case 'x':
                        dungeon.addEntity(new Snare(pos));
                        break;
                    case '!':
                        dungeon.addEntity(new AlarmSensor(pos));
                        break;
                    case 'F':
                        dungeon.addEntity(new Farmer(pos));
                        break;
                    case 'D':
                        Sheepdog sheepdog = new Sheepdog(pos);
                        dungeon.addEntity(sheepdog);
                        sheepdogs.add(sheepdog);  // Gem reference til ROUTE-parsing senere
                        break;
                    case '>':
                        dungeon.addEntity(new Gate(pos));
                        break;
                    case '*':
                        dungeon.addEntity(new Freedom(pos));
                        break;
                    case '@':
                        dungeon.addEntity(new Player(pos));
                        break;
                    case '.':
                    case ' ':
                        // Bare gulv — allerede tilføjet ovenfor
                        break;
                    default:
                        // Ukendt tegn — behandles som gulv (ignoreres)
                        break;
                }
            }
        }

        // Parser ROUTE-linjer og tildel ruter til sheepdogs
        for (String routeLine : routeLines) {
            parseRoute(routeLine, sheepdogs);
        }

        // Sæt mørke hvis DARK var angivet i level-filen
        if (darkFromX >= 0) {
            dungeon.setDarkFromX(darkFromX);
        }

        return dungeon;
    }

    /**
     * parseRoute() — parser en ROUTE-linje og sætter ruten på en Sheepdog.
     *
     * Format: "ROUTE <sheepdog-index> x1,y1 x2,y2 x3,y3 ..."
     * Eksempel: "ROUTE 0 5,2 6,2 7,2 6,2"
     *   → Sheepdog #0 får ruten [(5,2), (6,2), (7,2), (6,2)]
     *
     * sheepdog-index refererer til den n'te Sheepdog i filen (0-baseret).
     */
    private static void parseRoute(String line, List<Sheepdog> sheepdogs) {
        String[] parts = line.split("\\s+");  // Split på whitespace
        if (parts.length < 3) return;         // Mindst "ROUTE", index, og ét punkt

        // Parse sheepdog-index
        int dogIndex;
        try {
            dogIndex = Integer.parseInt(parts[1]);
        } catch (NumberFormatException e) {
            return;  // Ugyldigt index — spring over
        }

        if (dogIndex < 0 || dogIndex >= sheepdogs.size()) return;

        // Parse koordinater: "x,y" → Position(x, y)
        Sheepdog dog = sheepdogs.get(dogIndex);
        List<Position> route = new ArrayList<>();

        for (int i = 2; i < parts.length; i++) {
            String[] coords = parts[i].split(",");
            if (coords.length == 2) {
                try {
                    int x = Integer.parseInt(coords[0]);
                    int y = Integer.parseInt(coords[1]);
                    route.add(new Position(x, y));
                } catch (NumberFormatException e) {
                    // Ugyldigt koordinat — spring over dette punkt
                }
            }
        }

        if (!route.isEmpty()) {
            dog.setRoute(route);
        }
    }
}

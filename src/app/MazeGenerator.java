package app;

import core.*;
import entity.*;
import java.util.*;

/**
 * MazeGenerator — Genererer tilfældige labyrinter for levels 2-4.
 *
 * Bruger recursive backtracking (DFS) til at generere en perfekt labyrint,
 * fjerner derefter ~75% tilfældige indre vægge for at gøre den mere åben og spillbar.
 *
 * Level 4 har en lodret hegnsvæg (Fence) i midten af banen og mørke på højre side.
 * Spilleren starter på venstre side og skal sprænge hegnet med en bombe for at nå
 * Freedom (*) på den mørke højre side.
 *
 * Algoritme:
 *   1. Opret et grid hvor alle celler er vægge
 *   2. Maze-celler er på ulige positioner: (1,1), (1,3), (3,1), (3,3), ...
 *   3. DFS fra (1,1): besøg tilfældige naboer og "carve" stier imellem dem
 *   4. Fjern ~75% tilfældige indre vægge for at åbne labyrinten op
 *   5. Placer entities baseret på sværhedsgrad (level-nummer)
 *   6. Find korridorer til sheepdog-patruljeruter
 */
public class MazeGenerator {

    private static final Random random = new Random();

    /**
     * generate() — generer en tilfældig labyrint for det angivne level.
     * Level 2: 21×11, Level 3: 25×15, Level 4: 29×17.
     */
    public static Dungeon generate(int levelNumber) {
        // Vælg dimensioner (ulige tal for korrekt maze-grid)
        int cols, rows;
        switch (levelNumber) {
            case 3:  cols = 25; rows = 15; break;
            case 4:  cols = 29; rows = 17; break;
            default: cols = 21; rows = 11; break;
        }

        // Generer labyrint: false = mur, true = passable
        boolean[][] passable = new boolean[cols][rows];
        carveMaze(passable, cols, rows);
        openUpWalls(passable, cols, rows);

        // Byg dungeon med vægge og gulv
        Dungeon dungeon = new Dungeon(cols, rows, levelNumber);
        int fenceX = (levelNumber == 4) ? cols / 2 : -1;

        for (int x = 0; x < cols; x++) {
            for (int y = 0; y < rows; y++) {
                Position pos = new Position(x, y);
                dungeon.addEntity(new Floor(pos));

                if (levelNumber == 4 && x == fenceX && y > 0 && y < rows - 1) {
                    // Level 4: lodret hegnsvæg i midten (sprængbar med bombe)
                    dungeon.addEntity(new Fence(pos));
                } else if (!passable[x][y]) {
                    dungeon.addEntity(new StoneWall(pos));
                }
            }
        }

        // Placer entities (spiller, fjender, items, fælder)
        placeEntities(dungeon, passable, cols, rows, levelNumber, fenceX);

        // Level 4: mørke på højre side af hegnet
        if (levelNumber == 4) {
            dungeon.setDarkFromX(fenceX + 1);
        }

        return dungeon;
    }

    // ====================================================================
    //  LABYRINT-GENERERING
    // ====================================================================

    /**
     * carveMaze() — generer en perfekt labyrint med recursive backtracking (DFS).
     *
     * Maze-celler er på ulige (x,y) positioner. Vægge mellem celler er på lige positioner.
     * Ydre ring (x=0, y=0, x=cols-1, y=rows-1) forbliver altid som mure.
     *
     * Algoritmen starter fra celle (1,1) og besøger tilfældige naboer.
     * Når alle naboer er besøgt, backtrackes der til en celle med ubesøgte naboer.
     * Dette garanterer at ALLE celler er forbundne — der er altid en vej mellem to punkter.
     */
    private static void carveMaze(boolean[][] passable, int cols, int rows) {
        passable[1][1] = true;
        Deque<int[]> stack = new ArrayDeque<>();
        stack.push(new int[]{1, 1});

        // Retninger: 2 celler i hver retning (springer over væggen imellem)
        int[][] dirs = {{0, -2}, {0, 2}, {-2, 0}, {2, 0}};

        while (!stack.isEmpty()) {
            int[] cur = stack.peek();
            int cx = cur[0], cy = cur[1];

            // Find ubesøgte naboer (2 celler væk i hver retning)
            List<int[]> neighbors = new ArrayList<>();
            for (int[] d : dirs) {
                int nx = cx + d[0], ny = cy + d[1];
                if (nx > 0 && nx < cols - 1 && ny > 0 && ny < rows - 1 && !passable[nx][ny]) {
                    neighbors.add(new int[]{nx, ny});
                }
            }

            if (neighbors.isEmpty()) {
                stack.pop(); // Backtrack — ingen ubesøgte naboer
            } else {
                // Vælg en tilfældig nabo og carve stien dertil
                int[] next = neighbors.get(random.nextInt(neighbors.size()));
                // Fjern væggen mellem nuværende og næste celle
                passable[(cx + next[0]) / 2][(cy + next[1]) / 2] = true;
                passable[next[0]][next[1]] = true;
                stack.push(next);
            }
        }
    }

    /**
     * openUpWalls() — fjern ~75% tilfældige indre vægge.
     * Gør labyrinten bredere og mere spillbar med alternative ruter.
     * Uden dette ville labyrinten have én eneste vej mellem to punkter
     * (en "perfekt" labyrint), hvilket er for svært for et action-spil.
     */
    private static void openUpWalls(boolean[][] passable, int cols, int rows) {
        List<int[]> walls = new ArrayList<>();
        for (int x = 1; x < cols - 1; x++) {
            for (int y = 1; y < rows - 1; y++) {
                if (!passable[x][y]) {
                    walls.add(new int[]{x, y});
                }
            }
        }
        Collections.shuffle(walls, random);
        int count = walls.size() * 3 / 4;
        for (int i = 0; i < count; i++) {
            passable[walls.get(i)[0]][walls.get(i)[1]] = true;
        }
    }

    // ====================================================================
    //  ENTITY-PLACERING
    // ====================================================================

    /**
     * placeEntities() — placer alle entities på tilfældige passable positioner.
     *
     * Antal og typer af entities bestemmes af level-nummeret:
     *   Level 2: basis-fjender, få fælder, ingen bombe/treat
     *   Level 3: flere fjender og fælder, alarm, treat
     *   Level 4: bombe (venstre side), freedom (højre side), 2 sheepdogs
     */
    private static void placeEntities(Dungeon dungeon, boolean[][] passable,
                                       int cols, int rows, int level, int fenceX) {
        // Saml passable positioner opdelt i regioner
        List<Position> allOpen = new ArrayList<>();
        List<Position> leftOpen = new ArrayList<>();
        List<Position> rightOpen = new ArrayList<>();

        for (int x = 1; x < cols - 1; x++) {
            for (int y = 1; y < rows - 1; y++) {
                if (!passable[x][y]) continue;
                if (level == 4 && x == fenceX) continue; // Fence-kolonne er blokeret
                Position p = new Position(x, y);
                allOpen.add(p);
                if (fenceX > 0) {
                    if (x < fenceX) leftOpen.add(p);
                    else if (x > fenceX) rightOpen.add(p);
                }
            }
        }

        Set<Position> used = new HashSet<>();

        // --- Spiller (@) ---
        Position playerPos;
        if (level == 4) {
            // Level 4: spiller starter på venstre side
            playerPos = pickPosition(leftOpen, used, null, 0);
        } else {
            // Levels 2-3: spiller starter i venstre tredjedel
            playerPos = pickPositionInRange(allOpen, used, 1, cols / 3, null, 0);
        }
        used.add(playerPos);
        dungeon.addEntity(new Player(playerPos));

        // --- Gate (>) for levels 2-3, Freedom (*) for level 4 ---
        if (level != 4) {
            Position gatePos = pickPositionInRange(allOpen, used,
                cols * 2 / 3, cols - 2, playerPos, 5);
            used.add(gatePos);
            dungeon.addEntity(new Gate(gatePos));
        } else {
            Position freedomPos = pickPosition(rightOpen, used, null, 0);
            used.add(freedomPos);
            dungeon.addEntity(new Freedom(freedomPos));
        }

        // --- Entity-antal per level ---
        int farmerCount = level;
        int dogCount    = level;
        int snareCount  = (level == 2) ? 2 : 3;
        int hayCount    = 1;
        int bombCount   = (level == 4) ? 3 : 2;
        int treatCount  = (level == 2) ? 0 : 1;
        int eFenceCount = (level == 2) ? 1 : 2;
        int waterCount  = 1;
        int alarmCount  = (level == 3) ? 1 : 0;

        // Hovedpool: venstre side for level 4, hele banen ellers
        List<Position> mainPool = (level == 4) ? leftOpen : allOpen;

        // --- Sheepdog (D) med patruljerute ---
        for (int i = 0; i < dogCount; i++) {
            List<Position> route = findCorridor(passable, cols, rows, used, fenceX);
            if (route != null && route.size() >= 2) {
                used.add(route.get(0));
                Sheepdog d = new Sheepdog(route.get(0));
                d.setRoute(route);
                dungeon.addEntity(d);
            }
        }

        // --- Farmer (F) ---
        for (int i = 0; i < farmerCount; i++) {
            Position p = pickPosition(mainPool, used, playerPos, 4);
            if (p != null) { used.add(p); dungeon.addEntity(new Farmer(p)); }
        }

        // --- Snare (x) — skjulte fælder ---
        for (int i = 0; i < snareCount; i++) {
            Position p = pickPosition(allOpen, used, playerPos, 3);
            if (p != null) { used.add(p); dungeon.addEntity(new Snare(p)); }
        }

        // --- HayItem (h) — helbredelse ---
        for (int i = 0; i < hayCount; i++) {
            Position p = pickPosition(mainPool, used, null, 0);
            if (p != null) { used.add(p); dungeon.addEntity(new HayItem(p)); }
        }

        // --- BombItem (b) — placeres på venstre side for level 4, mainPool ellers ---
        for (int i = 0; i < bombCount; i++) {
            List<Position> pool = (level == 4) ? leftOpen : mainPool;
            Position p = pickPosition(pool, used, null, 0);
            if (p != null) { used.add(p); dungeon.addEntity(new BombItem(p)); }
        }

        // --- TreatItem (t) — hundegodbid ---
        for (int i = 0; i < treatCount; i++) {
            Position p = pickPosition(mainPool, used, null, 0);
            if (p != null) { used.add(p); dungeon.addEntity(new TreatItem(p)); }
        }

        // --- ElectricFence (~) — skade ved kontakt ---
        for (int i = 0; i < eFenceCount; i++) {
            Position p = pickPosition(allOpen, used, playerPos, 3);
            if (p != null) { used.add(p); dungeon.addEntity(new ElectricFence(p)); }
        }

        // --- WaterTrough (w) — helbredelse ved kontakt ---
        for (int i = 0; i < waterCount; i++) {
            Position p = pickPosition(mainPool, used, null, 0);
            if (p != null) { used.add(p); dungeon.addEntity(new WaterTrough(p)); }
        }

        // --- AlarmSensor (!) — kun level 3 ---
        for (int i = 0; i < alarmCount; i++) {
            Position p = pickPosition(allOpen, used, playerPos, 3);
            if (p != null) { used.add(p); dungeon.addEntity(new AlarmSensor(p)); }
        }
    }

    // ====================================================================
    //  HJÆLPE-METODER TIL POSITIONS-VALG
    // ====================================================================

    /**
     * pickPosition() — find en tilfældig passable position der ikke allerede er brugt,
     * og som er mindst minDist (Manhattan-afstand) fra awayFrom.
     * Returnerer null hvis ingen position kan findes.
     */
    private static Position pickPosition(List<Position> candidates, Set<Position> used,
                                          Position awayFrom, int minDist) {
        List<Position> shuffled = new ArrayList<>(candidates);
        Collections.shuffle(shuffled, random);

        // Forsøg med afstandskrav
        for (Position p : shuffled) {
            if (used.contains(p)) continue;
            if (awayFrom != null && p.distanceTo(awayFrom) < minDist) continue;
            return p;
        }
        // Fallback: ignorér afstandskrav
        for (Position p : shuffled) {
            if (!used.contains(p)) return p;
        }
        return null;
    }

    /**
     * pickPositionInRange() — som pickPosition(), men begrænset til x i [minX, maxX].
     * Bruges til at placere spilleren i venstre side og gate i højre side.
     */
    private static Position pickPositionInRange(List<Position> candidates, Set<Position> used,
                                                 int minX, int maxX,
                                                 Position awayFrom, int minDist) {
        List<Position> filtered = new ArrayList<>();
        for (Position p : candidates) {
            if (p.getX() >= minX && p.getX() <= maxX) {
                filtered.add(p);
            }
        }
        if (filtered.isEmpty()) return pickPosition(candidates, used, awayFrom, minDist);
        return pickPosition(filtered, used, awayFrom, minDist);
    }

    /**
     * findCorridor() — find en tilfældig vandret eller lodret korridor (≥3 celler)
     * til brug som sheepdog-patruljerute.
     *
     * Scanner brættet for sekvenser af sammenhængende passable celler i en linje.
     * Den valgte korridor konverteres til en frem-og-tilbage rute:
     *   [A, B, C, D] → [A, B, C, D, C, B]
     * Så farmeren patruljer frem og tilbage.
     */
    private static List<Position> findCorridor(boolean[][] passable, int cols, int rows,
                                                Set<Position> used, int fenceX) {
        List<List<Position>> corridors = new ArrayList<>();

        // Scan vandrette korridorer
        for (int y = 1; y < rows - 1; y++) {
            List<Position> run = new ArrayList<>();
            for (int x = 1; x < cols - 1; x++) {
                Position p = new Position(x, y);
                if (passable[x][y] && (fenceX < 0 || x != fenceX) && !used.contains(p)) {
                    run.add(p);
                } else {
                    if (run.size() >= 3) corridors.add(new ArrayList<>(run));
                    run.clear();
                }
            }
            if (run.size() >= 3) corridors.add(run);
        }

        // Scan lodrette korridorer
        for (int x = 1; x < cols - 1; x++) {
            if (fenceX >= 0 && x == fenceX) continue;
            List<Position> run = new ArrayList<>();
            for (int y = 1; y < rows - 1; y++) {
                Position p = new Position(x, y);
                if (passable[x][y] && !used.contains(p)) {
                    run.add(p);
                } else {
                    if (run.size() >= 3) corridors.add(new ArrayList<>(run));
                    run.clear();
                }
            }
            if (run.size() >= 3) corridors.add(run);
        }

        if (corridors.isEmpty()) return null;

        // Vælg en tilfældig korridor og lav en frem-og-tilbage rute
        List<Position> corridor = corridors.get(random.nextInt(corridors.size()));
        List<Position> route = new ArrayList<>(corridor);
        for (int i = corridor.size() - 2; i > 0; i--) {
            route.add(corridor.get(i));
        }
        return route;
    }
}

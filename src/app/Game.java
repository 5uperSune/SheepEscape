package app;

import core.*;
import entity.*;
import java.io.*;
import java.util.List;
import java.util.Scanner;

/**
 * Game — Hovedklassen med main() og spil-loopet.
 *
 * Denne klasse styrer hele spiloplevelsen:
 *   1. Vis velkomstskærm
 *   2. Load level fra fil (via DungeonLoader)
 *   3. Spil-loop: vis bræt → læs input → bevæg → enemy AI → tjek status
 *   4. Håndter level-skift, game over og sejr
 *
 * Bruger Scanner til at læse spillerens input. Hvert træk kræver
 * et bogstav efterfulgt af Enter (f.eks. "w" + Enter for at gå op).
 *
 * Game klassen er ansvarlig for ALT der har med UI at gøre:
 *   - Konsol-input (Scanner)
 *   - Udskrift af HP-bar, inventory, beskeder
 *   - Clear screen
 *
 * Dungeon klassen er ansvarlig for SPILMEKANIK:
 *   - Bevægelse og kollision
 *   - Entity-interaktioner
 *   - Enemy AI (via tick())
 *
 * Denne opdeling er et eksempel på "Separation of Concerns" (SoC) —
 * UI-logik og spilmekanik er adskilt i forskellige klasser.
 */
public class Game {

    private Dungeon dungeon;       // Det aktuelle level
    private int currentLevel;      // Hvilket level vi er på (1-4)
    private int totalLevels;       // Totalt antal levels
    private boolean playing;       // Er spillet i gang?
    private Scanner scanner;       // Scanner til at læse spillerens input
    private String[] levelFiles;   // Stier til level-filerne
    private OutputStream rawOut;   // Rå output-stream — omgår PrintStreams 8KB buffer

    /** Constructor — sæt default-værdier, level-stier og Scanner. */
    public Game() {
        this.currentLevel = 1;
        this.totalLevels = 4;
        this.playing = true;
        this.scanner = new Scanner(System.in);
        this.rawOut = new FileOutputStream(FileDescriptor.out);
        this.levelFiles = new String[] {
            "levels/level1.txt",
            "levels/level2.txt",
            "levels/level3.txt",
            "levels/level4.txt"
        };
    }

    /**
     * readInput() — læs én linje fra spilleren (blokerer indtil Enter trykkes).
     * Returnerer det første tegn i linjen, eller '\0' hvis linjen er tom.
     */
    private char readInput() {
        String line = scanner.nextLine().trim();
        if (line.isEmpty()) return '\0';
        return line.charAt(0);
    }

    /**
     * charToDirection() — konverter et tegn til en bevægeretning.
     * Understøtter w/a/s/d (store og små bogstaver).
     * Returnerer null hvis tegnet ikke er en retnings-tast.
     */
    private Direction charToDirection(char ch) {
        switch (ch) {
            case 'w': case 'W': return Direction.NORTH;
            case 's': case 'S': return Direction.SOUTH;
            case 'd': case 'D': return Direction.EAST;
            case 'a': case 'A': return Direction.WEST;
            default: return null;
        }
    }

    /** Vent på at spilleren trykker Enter. */
    private void waitForEnter() {
        scanner.nextLine();
    }

    /**
     * play() — hovedloopet. Kører indtil spilleren vinder, taber eller quitter.
     *
     * Strukturen er en klassisk game loop:
     *   while (playing) {
     *       render();    // Vis brættet
     *       input();     // Læs spillerens handling
     *       update();    // Opdater verden (bevægelse, AI)
     *       check();     // Tjek om spillet er slut
     *   }
     */
    public void play() {
        // Velkomstskærm med kontroller
        System.out.println("=== SHEEP ESCAPE ===");
        System.out.println("Et får der flygter fra folden!");
        System.out.println();
        System.out.println("Kontroller:");
        System.out.println("  w/a/s/d + Enter       - Bevæg dig");
        System.out.println("  1-9 + Enter           - Brug item fra inventory");
        System.out.println("  q + Enter             - Afslut spillet");
        System.out.println();
        System.out.println("Tryk Enter for at starte...");
        System.out.flush();
        waitForEnter();

        // Load det første level
        loadLevel(currentLevel);

        // ===== GAME LOOP =====
        while (playing) {
            // --- RENDER ---
            // Byg hele frame'n som én String i en StringBuilder.
            // Printes i ét kald, så terminalen aldrig viser halve frames.
            StringBuilder frame = new StringBuilder();
            if (dungeon.consumeBeep()) frame.append('\007');  // BEL-lyd
            frame.append("\033[H\033[2J\033[3J");  // Clear screen
            frame.append(dungeon.show());          // Bræt
            buildPlayerStats(frame);               // HP-bar
            buildMessages(frame);                  // Beskedlog
            buildInventory(frame);                 // Inventory
            buildControls(frame);                  // Kontroller
            frame.append(">> ");
            printFrame(frame);

            // --- INPUT ---
            char input;
            try {
                input = readInput();
            } catch (Exception e) {
                continue;
            }

            // Fortolk input: bevægelse, item-brug eller quit
            Direction dir = charToDirection(input);

            if (dir != null) {
                // --- UPDATE ---
                dungeon.move(dir);   // Flyt spilleren
                dungeon.tick();      // Enemy AI og timers
            } else if (input == 'q' || input == 'Q') {
                playing = false;
                printFrame(new StringBuilder("Spillet afsluttet.\r\n"));
                continue;
            } else if (input >= '1' && input <= '9') {
                // Item-brug
                int itemIndex = input - '1';  // '1' → 0, '2' → 1, osv.
                useItem(itemIndex);
                dungeon.tick();  // Fjender reagerer også når man bruger items
            } else {
                continue;  // Ukendt input — ignorer
            }

            // --- CHECK ---
            if (dungeon.isGameWon()) {
                // Spilleren nåede Freedom (*) — spillet er vundet!
                StringBuilder win = new StringBuilder();
                win.append("\033[H\033[2J\033[3J");
                win.append(dungeon.show());
                win.append("\r\n\007");
                win.append("*** TILLYKKE! Du slap fri! ***\r\n");
                win.append("Fåret fandt friheden!\r\n");
                printFrame(win);
                playing = false;

            } else if (dungeon.isLevelComplete()) {
                // Spilleren nåede Gate (>) — gå til næste level
                if (currentLevel < totalLevels) {
                    currentLevel++;
                    printFrame(new StringBuilder("Level gennemført! Tryk Enter for næste level...\r\n"));
                    waitForEnter();
                    loadLevel(currentLevel);
                } else {
                    // Sidste level gennemført
                    StringBuilder win = new StringBuilder();
                    win.append("\033[H\033[2J\033[3J");
                    win.append(dungeon.show());
                    win.append("\r\n\007");
                    win.append("*** TILLYKKE! Du klarede alle levels! ***\r\n");
                    printFrame(win);
                    playing = false;
                }

            } else if (dungeon.isGameLost()) {
                // Spilleren er død — tilbyd retry
                StringBuilder lose = new StringBuilder();
                lose.append("\033[H\033[2J\033[3J");
                lose.append(dungeon.show());
                lose.append("\r\n\007");
                lose.append("*** GAME OVER ***\r\n");
                lose.append("Fåret blev fanget...\r\n");
                lose.append("\r\n");
                lose.append("Prøv igen? (j/n + Enter)\r\n");
                printFrame(lose);
                while (true) {
                    char retry = readInput();
                    if (retry == 'j' || retry == 'J') {
                        loadLevel(currentLevel);  // Genstart level
                        break;
                    } else if (retry == 'n' || retry == 'N') {
                        playing = false;
                        break;
                    }
                }
            }
        }

        scanner.close();
    }

    /**
     * loadLevel() — load et level.
     * Level 1 loades fra fil (statisk tutorial-level).
     * Levels 2-4 genereres tilfældigt som labyrinter via MazeGenerator.
     */
    private void loadLevel(int level) {
        try {
            if (level == 1) {
                dungeon = DungeonLoader.load(levelFiles[0], level);
            } else {
                dungeon = MazeGenerator.generate(level);
            }
            dungeon.addMessage("Level " + level + " - Start!");
        } catch (Exception e) {
            printFrame(new StringBuilder("Kunne ikke loade level " + level + ": " + e.getMessage() + "\r\n"));
            playing = false;
        }
    }

    /**
     * useItem() — brug et item fra spillerens inventory.
     * Forskellige items har forskellige use()-metoder:
     *   - HayItem: healer spilleren
     *   - BombItem: placerer en bombe
     *   - TreatItem: spørger om retning og kaster godbid
     *
     * Vi bruger instanceof til at finde den rigtige type,
     * og caster derefter for at kalde den specifikke use()-metode.
     */
    private void useItem(int index) {
        Player player = dungeon.getPlayer();
        if (player == null) return;

        List<Entity> inventory = player.getInventory();
        if (index < 0 || index >= inventory.size()) {
            dungeon.addMessage("Intet item på plads " + (index + 1));
            return;
        }

        // Fjern item'et fra inventory og brug det
        Entity item = player.useItem(index);
        if (item instanceof HayItem) {
            ((HayItem) item).use(player, dungeon);
        } else if (item instanceof BombItem) {
            ((BombItem) item).use(player, dungeon);
        } else if (item instanceof TreatItem) {
            // Treat kræver en retning
            printFrame(new StringBuilder("Kast retning? (w/a/s/d + Enter)\r\n"));
            Direction throwDir = Direction.NORTH;  // Default
            char dirCh = readInput();
            Direction chosen = charToDirection(dirCh);
            if (chosen != null) throwDir = chosen;
            ((TreatItem) item).use(player, dungeon, throwDir);
        }
    }

    // ====================================================================
    //  UI-METODER
    // ====================================================================

    /**
     * printFrame() — skriv hele frame'n til terminalen i ét OS-kald.
     *
     * Vi omgår System.out (PrintStream) fordi den har en intern
     * BufferedWriter på 8192 tegn. Level 4's bræt (29×17 celler med
     * ANSI-farvekoder) fylder ~19.000 tegn — mere end bufferen.
     * Når bufferen fyldes, flusher PrintStream midtvejs i frame'n,
     * og terminalen viser en ufærdig frame som et synligt "hop".
     *
     * Ved at skrive direkte til rawOut (FileOutputStream på FileDescriptor.out)
     * sendes alle bytes i ét write()-kald, og terminalen modtager hele
     * frame'n atomisk — ingen mellemtilstande, ingen flimmer.
     */
    private void printFrame(StringBuilder frame) {
        try {
            rawOut.write(frame.toString().getBytes("UTF-8"));
            rawOut.flush();
        } catch (IOException e) {
            // Fallback til System.out hvis rawOut fejler
            System.out.print(frame.toString());
            System.out.flush();
        }
    }

    /** Tilføj level-header og HP-bar til frame-bufferen (1 linje). */
    private void buildPlayerStats(StringBuilder frame) {
        Player player = dungeon.getPlayer();
        if (player == null) return;

        int hp = player.getHealth();
        int max = player.getMaxHealth();
        int barLen = 20;
        int filled = (int) ((double) hp / max * barLen);
        frame.append("Level ").append(dungeon.getLevelNumber()).append(" | HP: [");
        for (int i = 0; i < barLen; i++) {
            frame.append(i < filled ? '#' : '.');
        }
        frame.append("] ").append(hp).append("/").append(max).append("\r\n");
    }

    /** Tilføj inventory til frame-bufferen. */
    private void buildInventory(StringBuilder frame) {
        Player player = dungeon.getPlayer();
        if (player == null) return;

        List<Entity> inv = player.getInventory();
        if (inv.isEmpty()) {
            frame.append("Inventory: (tom)\r\n");
        } else {
            frame.append("Inventory: ");
            for (int i = 0; i < inv.size(); i++) {
                frame.append("[").append(i + 1).append("] ").append(inv.get(i));
                if (i < inv.size() - 1) frame.append("  ");
            }
            frame.append("\r\n");
        }
    }

    /**
     * Tilføj beskedlog til frame-bufferen.
     * Altid præcis 3 linjer, uanset antal beskeder — så frame-højden
     * er konstant og terminalen ikke scroller når nye beskeder dukker op.
     */
    private void buildMessages(StringBuilder frame) {
        List<String> msgs = dungeon.getMessages();
        for (int i = 0; i < 3; i++) {
            if (i < msgs.size()) {
                frame.append("  >> ").append(msgs.get(i));
            }
            frame.append("\r\n");
        }
    }

    /** Tilføj kontroller til frame-bufferen. */
    private void buildControls(StringBuilder frame) {
        frame.append("w/a/s/d = bevæg | 1-9 = brug item | q = afslut\r\n");
    }

    /** Main — entry point for hele programmet. */
    public static void main(String[] args) throws Exception {
        // Sæt konsollen til UTF-8 så emojis vises korrekt i Windows Terminal.
        if (System.getProperty("os.name").contains("Windows")) {
            new ProcessBuilder("cmd", "/c", "chcp 65001")
                .inheritIO().start().waitFor();
        }
        // autoFlush=false: output bufferes og sendes først ved eksplicit flush().
        // Dette sikrer at clear-screen + hele frame sendes som ét chunk,
        // så terminalen aldrig viser en tom skærm imellem frames.
        System.setOut(new PrintStream(System.out, false, "UTF-8"));

        Game game = new Game();
        game.play();
    }
}

package core;

/**
 * Canvas — Tegner et grid med forgrunds- og baggrundsfarver.
 *
 * Erstatter BoardDisplay fra Assignment 4. Den store forskel er at Canvas
 * IKKE har et væg-system — i SheepEscape er vægge bare entities (StoneWall,
 * Fence osv.) som fylder en hel celle, i stedet for at sidde mellem celler.
 *
 * EMOJI-SYSTEM:
 * Hver celle er 2 terminal-kolonner bred. Det gør at vi kan bruge emojis
 * (🐑, 🐕, 👨 osv.) som fylder præcis 2 kolonner i terminalen.
 * Ikke-emoji entities bruger 2-tegns strenge (f.eks. "██" for stenmur).
 *
 * Sådan virker det:
 *   1. Dungeon kalder clear() for at nulstille hele grid
 *   2. Hver Entity kalder set() via renderOn() med sit sprite (2-kolonne streng)
 *   3. Dungeon kalder render() som returnerer hele grid som én String med ANSI-farver
 *
 * Lagring:
 *   - sprite[x][y]   = den 2-kolonne streng der vises (f.eks. "🐑", "██", "  ")
 *   - fgColor[x][y]  = forgrunds/tekstfarve (virker på ASCII, ikke på emojis)
 *   - bgColor[x][y]  = baggrundsfarve (virker på alt)
 */
public class Canvas {

    private int rows;   // Antal rækker (højde)
    private int cols;   // Antal kolonner (bredde)

    // Tre parallelle 2D-arrays — ét for sprite, ét for fg-farve, ét for bg-farve.
    // Alle er indekseret som [x][y] (kolonne først, så række).
    // sprite[][] holder 2-kolonne strenge i stedet for enkelte tegn.
    private String[][] sprite;
    private Color[][] fgColor;
    private Color[][] bgColor;

    /**
     * Constructor — opret et tomt Canvas med givne dimensioner.
     * Bemærk: parameterordenen er (cols, rows) = (bredde, højde),
     * fordi cols svarer til x og rows svarer til y.
     */
    public Canvas(int cols, int rows) {
        this.rows = rows;
        this.cols = cols;
        this.sprite = new String[cols][rows];
        this.fgColor = new Color[cols][rows];
        this.bgColor = new Color[cols][rows];
        clear();  // Fyld med tomme celler fra start
    }

    /**
     * set() — sæt sprite og farver for én celle.
     * sprite skal være en streng der fylder præcis 2 terminal-kolonner
     * (enten en emoji eller 2 ASCII-tegn).
     * Sikkerhedstjek: ignorerer koordinater der er uden for brættet,
     * så vi ikke får ArrayIndexOutOfBoundsException.
     */
    public void set(int x, int y, String sprite, Color fg, Color bg) {
        if (x >= 0 && x < cols && y >= 0 && y < rows) {
            this.sprite[x][y] = sprite;
            fgColor[x][y] = fg;
            bgColor[x][y] = bg;
        }
    }

    /**
     * clear() — nulstil alle celler til 2 mellemrum med sort baggrund.
     * Kaldes i starten af hver frame (før entities tegner sig selv).
     */
    public void clear() {
        for (int x = 0; x < cols; x++) {
            for (int y = 0; y < rows; y++) {
                sprite[x][y] = "  ";
                fgColor[x][y] = Color.WHITE;
                bgColor[x][y] = Color.BLACK;
            }
        }
    }

    /**
     * render() — byg hele grid'et som én String med ANSI fg+bg farver.
     *
     * Returnerer strengen i stedet for at printe den, så Game kan samle
     * hele frame'n (clear + board + stats) i én StringBuilder og sende
     * det hele til terminalen i ét write()-kald — hvilket forhindrer flimmer.
     *
     * Hver celle outputter sin sprite (2 kolonner bred), så det totale
     * output er cols*2 tegn bredt per række.
     */
    public String render() {
        StringBuilder sb = new StringBuilder();

        // Gennemløb række for række (y = rækker, x = kolonner)
        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                Color fg = fgColor[x][y];
                Color bg = bgColor[x][y];
                // Tilføj ANSI-farvekode + sprite (2 kolonner bred)
                sb.append(Color.colorCode(bg, fg));
                sb.append(sprite[x][y]);
            }
            // Nulstil farver og gå til næste linje
            sb.append(Color.RESET);
            sb.append("\r\n");
        }
        sb.append(Color.RESET);  // Ekstra reset til sidst for en sikkerheds skyld

        return sb.toString();
    }

    public int getRows() { return rows; }
    public int getCols() { return cols; }
}

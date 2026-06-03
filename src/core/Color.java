package core;

/**
 * Color — RGB-farve med ANSI terminal-support.
 *
 * Genbrugt fra Assignment 4 og udvidet med nye farvekonstanter til SheepEscape.
 *
 * ANSI escape-koder er specielle tegnsekvenser som farvelægger tekst i terminalen.
 * Formatet er: \033[48;2;R;G;B;38;2;R;G;Bm
 *   - 48;2;R;G;B = baggrundsfarve (RGB)
 *   - 38;2;R;G;B = tekstfarve/forgrund (RGB)
 *   - \u001B[0m   = nulstil alle farver (RESET)
 *
 * Hver entity i spillet har sin egen fg+bg farve, som gør det nemt
 * at skelne mellem vægge, fjender, items osv. i terminalen.
 */
public class Color {

    // RGB-værdier (0-255 for hver kanal)
    public int r;  // Rød
    public int g;  // Grøn
    public int b;  // Blå

    // ===== Farvekonstanter =====
    // static final = delt mellem alle instanser, kan ikke ændres.
    // Bruges som f.eks. Color.RED i stedet for at skrive new Color(255,80,80) overalt.

    // Basiskonstanter (fra Assignment 4)
    public static final Color RED    = new Color(255, 80, 80);
    public static final Color BLUE   = new Color(80, 80, 255);
    public static final Color GREEN  = new Color(80, 255, 80);
    public static final Color YELLOW = new Color(255, 255, 80);

    // Nye konstanter til SheepEscape
    public static final Color WHITE      = new Color(255, 255, 255);  // Fåret (@)
    public static final Color BROWN      = new Color(139, 90, 43);   // Hegn (|), hund (D), godbid (t)
    public static final Color DARK_GRAY  = new Color(80, 80, 80);    // Stenmur baggrund (#)
    public static final Color LIGHT_GRAY = new Color(160, 160, 160); // Stenmur forgrund, deaktiverede ting
    public static final Color CYAN       = new Color(80, 200, 255);  // Vandtrug (w)
    public static final Color ORANGE     = new Color(255, 165, 0);   // Elektrisk hegn (~), aktiv bombe
    public static final Color DARK_GREEN = new Color(30, 100, 30);   // Græs-baggrund (bruges overalt)
    public static final Color PURPLE     = new Color(180, 60, 180);  // AlarmSensor (!)
    public static final Color BLACK      = new Color(0, 0, 0);       // Default baggrund for Canvas

    // RESET-kode slukker alle farver — skal tilføjes efter farvet tekst
    public static final String RESET = "\u001B[0m";

    /** Constructor — opret en farve med RGB-værdier. */
    public Color(int r, int g, int b) {
        this.r = r;
        this.g = g;
        this.b = b;
    }

    /**
     * colorCode() — ANSI-kode med BÅDE baggrund og forgrund.
     * Bruges af Canvas.render() til at farvelægge hver celle.
     *
     * Eksempel: colorCode(DARK_GREEN, WHITE) giver hvid tekst på grøn baggrund.
     */
    public static String colorCode(Color bg, Color fg) {
        return String.format(
            "\033[48;2;%d;%d;%d;38;2;%d;%d;%dm",
            bg.r, bg.g, bg.b, fg.r, fg.g, fg.b);
    }

    /**
     * fgCode() — ANSI-kode med KUN tekstfarve (ingen baggrund).
     * Bruges til at farve tekst i UI-elementer (HP-bar, inventory osv.).
     */
    public static String fgCode(Color fg) {
        return String.format("\033[38;2;%d;%d;%dm", fg.r, fg.g, fg.b);
    }

    /**
     * bgCode() — ANSI-kode med KUN baggrundsfarve.
     * Kan bruges til at highlight bestemte områder.
     */
    public static String bgCode(Color bg) {
        return String.format("\033[48;2;%d;%d;%dm", bg.r, bg.g, bg.b);
    }
}

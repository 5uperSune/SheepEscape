package core;

/**
 * Entity — Abstrakt baseklasse for ALT der kan stå på brættet.
 *
 * Erstatter BoardElement fra Assignment 4.
 *
 * ALLE ting i spillet arver fra Entity — vægge, fjender, items, gulv, spilleren.
 * Dette er et eksempel på ARV (inheritance) og POLYMORFI:
 *   - Arv: StoneWall extends Entity, Player extends Entity osv.
 *   - Polymorfi: Dungeon kalder entity.onStep() uden at vide hvilken type det er.
 *     Hvis det er en ElectricFence, kører ElectricFence.onStep().
 *     Hvis det er en HayItem, kører HayItem.onStep().
 *     Java vælger automatisk den rigtige metode (dynamic dispatch).
 *
 * Klassen er ABSTRACT — man kan ikke skrive "new Entity(...)".
 * Man SKAL bruge en konkret subklasse som StoneWall, Player osv.
 *
 * De tre vigtigste metoder som subklasser kan override:
 *   renderOn()  — tegn entity'en på Canvas
 *   onStep()    — hvad sker når spilleren træder på denne entity
 *   onTurn()    — hvad sker hver tur (bruges til enemy AI og timers)
 */
public abstract class Entity {

    // Protected = synlig for subklasser, men ikke udefra.
    // Disse felter er fælles for ALLE entity-typer.
    protected Position position;   // Hvor på brættet entity'en befinder sig
    protected char symbol;         // Tegn brugt i level-filer og intern logik (f.eks. '#', '@', 'D')
    protected String sprite;       // 2-kolonne visuel repræsentation (emoji eller 2 tegn, f.eks. "🐑", "██")
    protected Color fgColor;       // Forgrundsfarve (virker på ASCII-sprites, emojis har egne farver)
    protected Color bgColor;       // Baggrundsfarve (virker på alt — vises bag sprite)
    protected boolean solid;       // Blokerer bevægelse? (true = man kan IKKE gå igennem)
    protected boolean visible;     // Synlig på canvas? (kan sættes til false for skjulte ting)

    /**
     * Constructor — initialiserer de fælles felter.
     * Alle subklasser kalder denne via super() i deres egen constructor.
     *
     * Eksempel fra StoneWall:
     *   super(position, '#', Color.LIGHT_GRAY, Color.DARK_GRAY, true);
     *   // symbol=#, grå farver, solid=true (blokerer)
     */
    public Entity(Position position, char symbol, Color fgColor, Color bgColor, boolean solid) {
        this.position = position;
        this.symbol = symbol;
        // Default sprite: symbolet + mellemrum (2 kolonner bred).
        // Subklasser overskriver dette med emojis i deres constructor.
        this.sprite = symbol + " ";
        this.fgColor = fgColor;
        this.bgColor = bgColor;
        this.solid = solid;
        this.visible = true;  // De fleste entities er synlige fra start
    }

    /**
     * renderOn() — tegn denne entity på Canvas.
     * Sætter sit symbol og sine farver på sin position.
     * De fleste subklasser behøver IKKE override denne metode,
     * da default-implementeringen er tilstrækkelig.
     */
    public void renderOn(Canvas canvas) {
        if (visible) {
            canvas.set(position.getX(), position.getY(), sprite, fgColor, bgColor);
        }
    }

    /**
     * onStep() — kaldes når spilleren træder på denne entity's position.
     * Default: ingen effekt. Overrides af subklasser som har en effekt:
     *   - ElectricFence: giver skade
     *   - HayItem: samles op i inventory
     *   - Gate: trigger level-skift
     *   - osv.
     */
    public void onStep(Player player, Dungeon dungeon) {
        // Default: ingen effekt — subklasser overrider denne
    }

    /**
     * onTurn() — kaldes HVER tur for alle entities (efter spillerens træk).
     * Default: ingen effekt. Overrides af subklasser med AI eller timers:
     *   - Farmer: jager spilleren
     *   - Sheepdog: flytter til næste punkt i ruten
     *   - BombItem: tæller ned til eksplosion
     *   - AlarmSensor: tjekker line-of-sight
     */
    public void onTurn(Dungeon dungeon) {
        // Default: ingen effekt — subklasser overrider denne
    }

    /** Er denne entity solid? (blokerer den bevægelse?) */
    public boolean isSolid() {
        return solid;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public char getSymbol() {
        return symbol;
    }

    public String getSprite() {
        return sprite;
    }
}

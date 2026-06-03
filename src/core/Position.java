package core;

/**
 * Position — Repræsenterer et (x, y) koordinat på spilbrættet.
 *
 * x = kolonne (vandret), y = række (lodret), begge 0-baseret.
 * (0,0) er øverste venstre hjørne — ligesom i et 2D-array.
 *
 * Denne klasse er genbrugt fra Assignment 4 (Ricochet Robots),
 * med tilføjelse af distanceTo() til brug i Sheepdog-AI.
 *
 * Vigtigt: equals() og hashCode() er overridet, så to Position-objekter
 * med samme (x,y) anses for at være ens. Uden dette ville Java kun
 * tjekke om det er det SAMME objekt i hukommelsen (==).
 */
public class Position {

    // Private felter — x og y er indkapslet (encapsulation)
    private int x;  // Kolonne (vandret position)
    private int y;  // Række (lodret position)

    /**
     * Constructor — opretter en ny Position med givne koordinater.
     * Eksempel: new Position(3, 5) = kolonne 3, række 5
     */
    public Position(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /**
     * moved() — returnerer en NY Position ét felt i den givne retning.
     * Den originale Position ændres IKKE (immutable mønster).
     *
     * Eksempel:
     *   Position p = new Position(3, 5);
     *   Position q = p.moved(Direction.NORTH);  // q = (3, 4), p er stadig (3, 5)
     */
    public Position moved(Direction dir) {
        return new Position(x + dir.getDx(), y + dir.getDy());
    }

    /**
     * distanceTo() — beregner Manhattan-afstanden til en anden position.
     * Manhattan-afstand = |x1-x2| + |y1-y2| (som at gå i et gitter, ingen diagonaler).
     * Bruges af Sheepdog til at tjekke om fåret er inden for rækkevidde.
     */
    public int distanceTo(Position other) {
        return Math.abs(x - other.x) + Math.abs(y - other.y);
    }

    // --- Getters og setters (standard encapsulation) ---

    public int getX() { return x; }
    public int getY() { return y; }
    public void setX(int x) { this.x = x; }
    public void setY(int y) { this.y = y; }

    /**
     * equals() — sammenligner to Position-objekter baseret på deres koordinater.
     * Overrider Object.equals() så vi kan bruge .equals() i stedet for ==.
     * Dette er nødvendigt for at f.eks. getEntityAt() virker korrekt.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;                              // Samme objekt? Ja.
        if (obj == null || getClass() != obj.getClass()) return false; // Null eller forkert type? Nej.
        Position other = (Position) obj;
        return x == other.x && y == other.y;                       // Samme koordinater? Ja.
    }

    /**
     * hashCode() — returnerer en hash baseret på (x,y).
     * MÅ overrides når equals() overrides, ellers virker HashMap/HashSet ikke.
     */
    @Override
    public int hashCode() {
        return 31 * x + y;
    }

    /** toString() — pæn udskrift til debugging, f.eks. "(3, 5)". */
    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }
}

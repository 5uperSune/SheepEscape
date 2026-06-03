package core;

/**
 * Direction — De fire bevægelsesretninger i spillet.
 *
 * Hver retning har en bevægelsesvektor (dx, dy):
 *   NORTH = op        (0, -1)   y falder (op i konsollen)
 *   SOUTH = ned       (0, +1)   y stiger (ned i konsollen)
 *   EAST  = højre     (+1, 0)   x stiger
 *   WEST  = venstre   (-1, 0)   x falder
 *
 * Genbrugt uændret fra Assignment 4.
 *
 * Bemærk: Vi bruger et enum her i stedet for en klasse, fordi der
 * kun er præcis 4 mulige retninger — enum sikrer at man ikke kan
 * oprette andre retninger ved en fejl.
 */
public enum Direction {

    NORTH(0, -1),   // Op — y bliver mindre
    SOUTH(0, 1),    // Ned — y bliver større
    EAST(1, 0),     // Højre — x bliver større
    WEST(-1, 0);    // Venstre — x bliver mindre

    private int dx;  // Ændring i x (kolonne)
    private int dy;  // Ændring i y (række)

    /**
     * Enum-constructor — kaldes automatisk for hver enum-værdi ovenfor.
     * F.eks. NORTH(0, -1) kalder denne med dx=0, dy=-1.
     */
    Direction(int dx, int dy) {
        this.dx = dx;
        this.dy = dy;
    }

    /** Hent ændring i x-retningen. Bruges af Position.moved(). */
    public int getDx() { return dx; }

    /** Hent ændring i y-retningen. Bruges af Position.moved(). */
    public int getDy() { return dy; }
}

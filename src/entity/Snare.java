package entity;

import core.*;

/**
 * Snare — Skjult fælde der aktiveres når spilleren træder på den.
 *
 * VIGTIGT DESIGN: Snaren ser ud som et gulv '.' indtil spilleren træder på den!
 * Derefter skifter den symbol til 'x' og farve til rød, så man kan se
 * den er aktiveret. Den kan kun gøre skade ÉN gang.
 *
 * Dette er et eksempel på tilstandsændring:
 *   triggered = false → symbol '.', usynlig, kan skade
 *   triggered = true  → symbol 'x', synlig, kan IKKE skade igen
 *
 * Symbol: '.' (skjult) → 'x' (aktiveret)
 * Farve: mørkegrøn (skjult) → rød (aktiveret)
 * Solid: false (spilleren kan gå ind)
 * Skade: 25 HP
 */
public class Snare extends Entity {

    private static final int DAMAGE = 25;
    private boolean triggered;  // Er fælden allerede aktiveret?

    public Snare(Position position) {
        // Starter med '.' og grøn farve — ser IDENTISK ud med Floor!
        super(position, '.', Color.DARK_GREEN, Color.DARK_GREEN, false);
        this.sprite = "  ";  // Skjult — identisk med Floor (to mellemrum, grøn bg)
        this.triggered = false;
    }

    /**
     * onStep() — spilleren træder på fælden.
     * Kun aktiv første gang — derefter er triggered = true og den gør ingenting.
     */
    @Override
    public void onStep(Player player, Dungeon dungeon) {
        if (!triggered) {
            triggered = true;
            player.damage(DAMAGE);
            dungeon.beep();
            // Skift udseende så man kan se fælden nu
            symbol = 'x';
            sprite = "💀";
            fgColor = Color.RED;
            dungeon.addMessage("Du trådte i en snare! -" + DAMAGE + " HP");
        }
    }

    /** Er fælden allerede aktiveret? (bruges i tests) */
    public boolean isTriggered() {
        return triggered;
    }
}

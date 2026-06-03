package entity;

import core.*;

/**
 * WaterTrough — Vandtrug (w).
 *
 * Et helende felt: spilleren healer 15 HP hver gang de træder på det.
 * Til forskel fra HayItem forsvinder vandtruget IKKE — det kan bruges igen og igen.
 *
 * Symbol: 'w'
 * Farve: cyan tekst på blå baggrund (vandfarve)
 * Solid: false
 */
public class WaterTrough extends Entity {

    private static final int HEAL_AMOUNT = 15;

    public WaterTrough(Position position) {
        super(position, 'w', Color.CYAN, Color.BLUE, false);
        this.sprite = "💧";
    }

    /** Hver gang spilleren træder på vandtruget, healer de 15 HP. */
    @Override
    public void onStep(Player player, Dungeon dungeon) {
        player.heal(HEAL_AMOUNT);
        dungeon.addMessage("Du drak vand! +" + HEAL_AMOUNT + " HP");
    }
}

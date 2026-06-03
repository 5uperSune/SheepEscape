package entity;

import core.*;

/**
 * StoneWall — Uødelæggelig stenmur (#).
 *
 * Den simpleste entity i spillet — bare en mur der blokerer bevægelse.
 * Kan ALDRIG fjernes eller ødelægges (til forskel fra Fence).
 *
 * Symbol: '#'
 * Farve: lysegrå tekst på mørkegrå baggrund
 * Solid: true (blokerer al bevægelse)
 *
 * Ingen override af onStep() eller onTurn() — muren gør ingenting.
 * Alt logik arves fra Entity.
 */
public class StoneWall extends Entity {

    public StoneWall(Position position) {
        // solid = true: spilleren kan ikke gå igennem
        super(position, '#', Color.LIGHT_GRAY, Color.DARK_GRAY, true);
        this.sprite = "🧱";
    }
}

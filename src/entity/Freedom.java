package entity;

import core.*;

/**
 * Freedom — Friheden / spillets mål (*).
 *
 * Kun på det sidste level. Når spilleren når hertil, er hele spillet vundet!
 * Til forskel fra Gate (som bare går til næste level) afslutter Freedom spillet.
 *
 * Symbol: '*'
 * Farve: gul tekst på grøn baggrund (skinner!)
 * Solid: false
 */
public class Freedom extends Entity {

    public Freedom(Position position) {
        super(position, '*', Color.YELLOW, Color.GREEN, false);
        this.sprite = "⭐";
    }

    /** Spilleren nåede friheden — spillet er vundet! */
    @Override
    public void onStep(Player player, Dungeon dungeon) {
        dungeon.setGameWon(true);
        dungeon.addMessage("FRIHED! Du slap væk fra folden!");
    }
}

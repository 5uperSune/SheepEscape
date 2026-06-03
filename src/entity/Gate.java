package entity;

import core.*;

/**
 * Gate — Udgangen til næste level (>).
 *
 * Når spilleren træder på denne entity, markeres level'et som gennemført.
 * Game-klassen tjekker derefter om der er flere levels, og loader i så fald det næste.
 *
 * Symbol: '>'
 * Farve: grøn tekst på mørkegrøn baggrund
 * Solid: false (spilleren kan gå ind)
 */
public class Gate extends Entity {

    public Gate(Position position) {
        super(position, '>', Color.GREEN, Color.DARK_GREEN, false);
        this.sprite = "🏁";
    }

    /** Spilleren nåede udgangen — marker level som gennemført. */
    @Override
    public void onStep(Player player, Dungeon dungeon) {
        dungeon.setLevelComplete(true);
        dungeon.addMessage("Du fandt udgangen!");
    }
}

package entity;

import core.*;

/**
 * HayItem — Hø / health potion (h).
 *
 * Et item der kan samles op og gemmes i inventory.
 * Når det bruges, healer det spilleren med 20 HP.
 *
 * Livscyklus:
 *   1. HayItem ligger på brættet som en entity
 *   2. Spilleren træder på det → onStep() fjerner det fra brættet og tilføjer til inventory
 *   3. Spilleren trykker 1-9 for at bruge det → use() healer spilleren
 *
 * Symbol: 'h'
 * Farve: gul tekst på mørkegrøn baggrund
 * Solid: false
 */
public class HayItem extends Entity {

    private static final int HEAL_AMOUNT = 20;  // Hvor meget HP det healer

    public HayItem(Position position) {
        super(position, 'h', Color.YELLOW, Color.DARK_GREEN, false);
        this.sprite = "💛";
    }

    /**
     * onStep() — spilleren samlede høet op.
     * Fjern fra brættet (dungeon) og tilføj til spillerens inventory.
     */
    @Override
    public void onStep(Player player, Dungeon dungeon) {
        player.addItem(this);          // Tilføj til inventory
        dungeon.removeEntity(this);    // Fjern fra brættet
        dungeon.beep();
        dungeon.addMessage("Du samlede hø op! (brug med 1-9)");
    }

    /**
     * use() — spilleren bruger høet fra inventory.
     * Kaldes fra Game.useItem() når spilleren trykker det rigtige tal.
     */
    public void use(Player player, Dungeon dungeon) {
        player.heal(HEAL_AMOUNT);
        dungeon.addMessage("Du spiste hø og fik " + HEAL_AMOUNT + " HP!");
    }

    /** toString() — vises i inventory-listen i UI. */
    @Override
    public String toString() {
        return "Hø (+" + HEAL_AMOUNT + " HP)";
    }
}

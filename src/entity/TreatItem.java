package entity;

import core.*;

/**
 * TreatItem — Godbid der afleder farmere (t).
 *
 * Et item der kan samles op og kastes i en retning.
 * Når en Farmer ser en kastet treat, går den mod treat'en
 * i stedet for at jage spilleren.
 *
 * Livscyklus:
 *   1. TreatItem ligger på brættet → spilleren samler den op
 *   2. Spilleren bruger den → den kastes i en valgt retning
 *   3. Farmer ser den → bevæger sig mod treat i stedet for spilleren
 *   4. Farmer når treat → spiser den (fjernes fra brættet)
 *
 * thrown-flaget skelner mellem en treat der ligger på brættet (kan samles op)
 * og en treat der er kastet (tiltrækker farmere).
 *
 * Symbol: 't'
 * Farve: brun tekst på mørkegrøn baggrund
 * Solid: false
 */
public class TreatItem extends Entity {

    private boolean thrown;  // Er treat'en kastet? (true = aktiv lokkemad)

    public TreatItem(Position position) {
        super(position, 't', Color.BROWN, Color.DARK_GREEN, false);
        this.sprite = "🍖";
        this.thrown = false;
    }

    /**
     * onStep() — spilleren samler treat'en op (kun hvis den ikke allerede er kastet).
     */
    @Override
    public void onStep(Player player, Dungeon dungeon) {
        if (!thrown) {
            player.addItem(this);
            dungeon.removeEntity(this);
            dungeon.beep();
            dungeon.addMessage("Du samlede en hundegodbid op!");
        }
    }

    /**
     * use() — kast treat'en i en retning fra spillerens position.
     * Finder det første frie felt i den retning (springer over solide entities).
     */
    public void use(Player player, Dungeon dungeon, Direction dir) {
        Position target = player.getPosition().moved(dir);

        // Find første frie felt i retningen
        while (true) {
            Entity at = dungeon.getEntityAt(target);
            if (at == null || !at.isSolid()) {
                break;  // Frit felt fundet — placer treat her
            }
            target = target.moved(dir);
            // Sikkerhed: stop hvis vi rammer kanten af brættet
            if (target.getX() < 0 || target.getX() >= dungeon.getCols()
                || target.getY() < 0 || target.getY() >= dungeon.getRows()) {
                target = player.getPosition().moved(dir);
                break;
            }
        }

        // Placer treat'en på det fundne felt
        this.position = target;
        this.thrown = true;   // Markér som kastet (Farmer kigger efter dette)
        this.visible = true;
        dungeon.addEntity(this);
        dungeon.addMessage("Du kastede en godbid mod " + dir.name().toLowerCase() + "!");
    }

    /** Overload: brug treat med default retning NORTH (backup). */
    public void use(Player player, Dungeon dungeon) {
        use(player, dungeon, Direction.NORTH);
    }

    /** Er treat'en kastet? Bruges af Farmer.findTreat(). */
    public boolean isThrown() {
        return thrown;
    }

    @Override
    public String toString() {
        return "Godbid";
    }
}

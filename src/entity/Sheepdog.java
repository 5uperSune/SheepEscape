package entity;

import core.*;
import java.util.List;
import java.util.ArrayList;

/**
 * Sheepdog — Patruljerende fjende (D).
 *
 * Hunden følger en fast rute defineret som en liste af positioner.
 * Hver tur flytter den til næste punkt i ruten.
 * Når den når enden af ruten, starter den forfra (cyklisk med modulo).
 *
 * Hunden er solid — spilleren kan ikke gå igennem den.
 * Hvis hunden lander på spillerens felt, giver den 15 skade.
 *
 * Ruter defineres i level-filerne med ROUTE-linjer.
 * Eksempel: "ROUTE 0 5,3 6,3 7,3 6,3" = hund #0 patruljer vandret
 *
 * Symbol: 'D'
 * Farve: brun tekst på mørkegrøn baggrund
 * Solid: true (blokerer spillerens bevægelse)
 * Skade: 15 HP ved kontakt
 */
public class Sheepdog extends Entity {

    private static final int DAMAGE = 15;
    private List<Position> route;   // Listen af positioner hunden følger
    private int routeIndex;          // Nuværende index i ruten
    private int bombHits = 0;        // Antal gange ramt af bombe

    public Sheepdog(Position position) {
        super(position, 'D', Color.BROWN, Color.DARK_GREEN, true);
        this.sprite = "🐕";
        // Default rute = bare startpositionen (hunden står stille)
        this.route = new ArrayList<>();
        this.route.add(new Position(position.getX(), position.getY()));
        this.routeIndex = 0;
    }

    /** Sæt hele ruten (kaldes af DungeonLoader.parseRoute()). */
    public void setRoute(List<Position> route) {
        this.route = route;
        this.routeIndex = 0;
    }

    /** Tilføj ét punkt til ruten. */
    public void addRoutePoint(Position pos) {
        route.add(pos);
    }

    /**
     * onTurn() — flyt til næste punkt i ruten.
     *
     * Modulo-operatoren (%) sørger for at ruten er cyklisk:
     *   index 0 → 1 → 2 → 0 → 1 → 2 → ...
     *
     * Tjekker også om hunden lander på spillerens felt.
     */
    @Override
    public void onTurn(Dungeon dungeon) {
        // Hvis ruten kun har ét punkt, står hunden stille
        if (route.size() <= 1) return;

        // Gå til næste punkt (cyklisk med modulo)
        routeIndex = (routeIndex + 1) % route.size();
        Position next = route.get(routeIndex);
        this.position = next;

        // Tjek om hunden rammer spilleren
        Player player = dungeon.getPlayer();
        if (player != null && position.equals(player.getPosition())) {
            player.damage(DAMAGE);
            dungeon.beep();
            dungeon.addMessage("Hunden fangede dig! -" + DAMAGE + " HP");
        }
    }

    public void bombHit(Dungeon dungeon) {
        bombHits++;
        if (bombHits >= 2) {
            dungeon.removeEntity(this);
            dungeon.addMessage("Hunden blev sprængt væk!");
        } else {
            dungeon.addMessage("Hunden er ramt af eksplosionen!");
        }
    }

    public List<Position> getRoute() {
        return route;
    }
}

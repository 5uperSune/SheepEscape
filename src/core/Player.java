package core;

import java.util.ArrayList;
import java.util.List;

/**
 * Player — Fåret (@) som spilleren styrer.
 *
 * Extends Entity — arver position, symbol, farver og renderOn().
 * Player har derudover health (liv), maxHealth og et inventory (liste af items).
 *
 * Symbol: '@'
 * Farve: hvid tekst på mørkegrøn baggrund
 * Solid: false (spilleren blokerer ikke sig selv)
 *
 * Inventory-systemet bruger en List<Entity> fordi alle items (HayItem, BombItem,
 * TreatItem) arver fra Entity. Det er polymorfi i praksis — listen kan holde
 * forskellige typer, men de deler et fælles interface.
 */
public class Player extends Entity {

    private int health;             // Nuværende liv (0 = død)
    private int maxHealth;          // Maksimalt liv (kan ikke heale over dette)
    private List<Entity> inventory; // Items spilleren bærer på
    private Position startPosition; // Startposition (gemt til evt. respawn)

    /**
     * Constructor — opret en ny spiller på den givne position.
     * Starter med 100 HP og tomt inventory.
     */
    public Player(Position position) {
        // super() kalder Entity's constructor:
        //   symbol='@', hvid forgrund, grøn baggrund, ikke solid
        super(position, '@', Color.WHITE, Color.DARK_GREEN, false);
        this.sprite = "🐑";
        this.health = 100;
        this.maxHealth = 100;
        this.inventory = new ArrayList<>();
        // Gem startpositionen som en KOPI (så den ikke ændres hvis spilleren flytter)
        this.startPosition = new Position(position.getX(), position.getY());
    }

    /**
     * heal() — giv spilleren HP tilbage.
     * Math.min() sikrer at vi ikke overstiger maxHealth.
     * Eksempel: health=70, heal(50) → health = min(120, 100) = 100
     */
    public void heal(int amount) {
        health = Math.min(health + amount, maxHealth);
    }

    /**
     * damage() — træk HP fra spilleren.
     * Math.max() sikrer at health aldrig går under 0.
     * Eksempel: health=20, damage(50) → health = max(-30, 0) = 0
     */
    public void damage(int amount) {
        health = Math.max(health - amount, 0);
    }

    /** isDead() — er spilleren død? (health er nået 0) */
    public boolean isDead() {
        return health <= 0;
    }

    /** moveTo() — flyt spilleren til en ny position. */
    public void moveTo(Position newPos) {
        this.position = newPos;
    }

    /** addItem() — tilføj et item til inventory (når spilleren samler noget op). */
    public void addItem(Entity item) {
        inventory.add(item);
    }

    /**
     * useItem() — brug et item fra inventory baseret på index.
     * Fjerner item'et fra listen og returnerer det (så Game kan kalde .use()).
     * Returnerer null hvis index er ugyldigt.
     */
    public Entity useItem(int index) {
        if (index >= 0 && index < inventory.size()) {
            return inventory.remove(index);  // remove() returnerer det fjernede element
        }
        return null;
    }

    /** Hent hele inventory-listen (brugt af Game til at vise inventory i UI). */
    public List<Entity> getInventory() {
        return inventory;
    }

    // --- Getters ---
    public int getHealth() { return health; }
    public int getMaxHealth() { return maxHealth; }
    public Position getStartPosition() { return startPosition; }
}

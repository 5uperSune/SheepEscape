package entity;

import core.*;

/**
 * ElectricFence — Elektrisk hegn (~).
 *
 * Til forskel fra Fence er ElectricFence IKKE solid — spilleren KAN gå ind,
 * men tager 10 skade hver gang. Efter 3 interaktioner slukker hegnet
 * og fjernes fra dungeon.
 *
 * Dette er et eksempel på "tilstandsbaseret adfærd":
 * interactions-feltet holder styr på hvor mange gange hegnet er rørt.
 * Når det når 0, fjernes entity'en fra spillet.
 *
 * Symbol: '~'
 * Farve: orange tekst på mørkegrøn baggrund
 * Solid: false (man KAN gå ind, men det gør ondt!)
 */
public class ElectricFence extends Entity {

    private static final int DAMAGE = 10;  // Skade per interaktion
    private int interactions;               // Antal gange det kan ramme (starter på 3)

    public ElectricFence(Position position) {
        super(position, '~', Color.ORANGE, Color.DARK_GREEN, false);
        this.sprite = "⚡";
        this.interactions = 3;
    }

    /**
     * onStep() — kaldes når spilleren går ind i det elektriske hegn.
     * Giver skade og tæller ned. Når interactions rammer 0, fjernes hegnet.
     */
    @Override
    public void onStep(Player player, Dungeon dungeon) {
        if (interactions > 0) {
            player.damage(DAMAGE);
            interactions--;
            dungeon.beep();
            dungeon.addMessage("Elektrisk hegn! -" + DAMAGE + " HP (" + interactions + " opladninger tilbage)");

            // Sluk hegnet når det er brugt op
            if (interactions <= 0) {
                dungeon.removeEntity(this);
                dungeon.addMessage("Det elektriske hegn slukkede!");
            }
        }
    }

    /** Hent antal tilbageværende interaktioner (bruges i tests). */
    public int getInteractions() {
        return interactions;
    }
}

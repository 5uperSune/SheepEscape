package entity;

import core.*;
import java.util.ArrayList;
import java.util.List;

/**
 * BombItem — Bombe der sprænger hegn (b / B).
 *
 * Et item med to faser:
 *   Fase 1 (på brættet): symbol 'b', kan samles op af spilleren
 *   Fase 2 (placeret):   symbol 'B', tæller ned og eksploderer
 *
 * Livscyklus:
 *   1. BombItem ligger på brættet → spilleren samler den op (onStep)
 *   2. Spilleren bruger den → placeres på spillerens position (use)
 *   3. Hvert tur tæller timer ned (onTurn)
 *   4. Når timer = 0 → BOOM! Alle Fence-entities i radius 1 fjernes
 *
 * Symbol: 'b' (ikke placeret) / 'B' (placeret)
 * Farve: rød (ikke placeret) / orange (placeret)
 * Solid: false
 */
public class BombItem extends Entity {

    private int timer;       // Ture til eksplosion (starter på 3 når placeret)
    private boolean placed;  // Er bomben placeret på brættet?

    public BombItem(Position position) {
        super(position, 'b', Color.RED, Color.DARK_GREEN, false);
        this.sprite = "💣";
        this.timer = 3;
        this.placed = false;
    }

    /**
     * onStep() — spilleren træder på en ikke-placeret bombe og samler den op.
     * Tjekker placed-flag, så man ikke samler en allerede aktiv bombe op.
     */
    @Override
    public void onStep(Player player, Dungeon dungeon) {
        if (!placed) {
            player.addItem(this);
            dungeon.removeEntity(this);
            dungeon.beep();
            dungeon.addMessage("Du samlede en bombe op!");
        }
    }

    /**
     * use() — spilleren placerer bomben på sin nuværende position.
     * Bomben tilføjes til dungeon igen som en aktiv entity med timer.
     */
    public void use(Player player, Dungeon dungeon) {
        // Placer bomben på spillerens position
        this.position = new Position(player.getPosition().getX(), player.getPosition().getY());
        this.placed = true;
        this.visible = true;
        this.symbol = 'B';           // Skift symbol så man kan se den er aktiv
        this.sprite = "💥";          // Eksplosions-emoji for aktiv bombe
        this.fgColor = Color.ORANGE; // Orange = advarsel!
        this.timer = 3;
        dungeon.addEntity(this);     // Tilføj tilbage til brættet
        dungeon.addMessage("Bombe placeret! Eksploderer om " + timer + " ture.");
    }

    /**
     * onTurn() — tæl timer ned. Eksplodér når den rammer 0.
     * Denne metode kaldes af Dungeon.tick() for ALLE entities hvert tur.
     */
    @Override
    public void onTurn(Dungeon dungeon) {
        if (placed) {
            timer--;
            if (timer <= 0) {
                explode(dungeon);
            }
        }
    }

    /**
     * explode() — BOOM! Fjern alle Fence-entities inden for radius 1 af bomben.
     *
     * Radius 1 = de 8 felter rundt om bomben + bombens eget felt:
     *   XXX
     *   XBX   (B = bomben)
     *   XXX
     *
     * Vi bruger en midlertidig liste (toRemove) fordi man ikke kan fjerne
     * elementer fra en liste mens man itererer over den (ConcurrentModificationException).
     */
    private void explode(Dungeon dungeon) {
        dungeon.beep();
        dungeon.addMessage("BOOM! Bomben eksploderede!");
        int px = position.getX();
        int py = position.getY();

        // Find alle Fence-entities i radius 1
        List<Entity> toRemove = new ArrayList<>();
        for (Entity e : dungeon.getEntities()) {
            if (e instanceof Fence) {
                int ex = e.getPosition().getX();
                int ey = e.getPosition().getY();
                // Math.abs() tjekker afstand i begge akser — maks 1 felt væk
                if (Math.abs(ex - px) <= 1 && Math.abs(ey - py) <= 1) {
                    toRemove.add(e);
                }
            }
        }

        // Fjern de fundne Fence-entities
        for (Entity e : toRemove) {
            dungeon.removeEntity(e);
        }

        // Find fjender i radius 1 og ram dem
        List<Entity> snapshot = new ArrayList<>(dungeon.getEntities());
        for (Entity e : snapshot) {
            int ex = e.getPosition().getX();
            int ey = e.getPosition().getY();
            if (Math.abs(ex - px) <= 1 && Math.abs(ey - py) <= 1) {
                if (e instanceof Farmer) ((Farmer) e).bombHit(dungeon);
                else if (e instanceof Sheepdog) ((Sheepdog) e).bombHit(dungeon);
            }
        }

        // Hvis hegn blev ødelagt og der er mørke, oplys banen!
        if (!toRemove.isEmpty() && dungeon.getDarkFromX() >= 0) {
            dungeon.setDarkFromX(-1);
            dungeon.addMessage("Mørket forsvinder! Banen er oplyst!");
        }

        // Fjern bomben selv
        dungeon.removeEntity(this);
    }

    /** toString() — vises i inventory-listen. */
    @Override
    public String toString() {
        return "Bombe";
    }
}

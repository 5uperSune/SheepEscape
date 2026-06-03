package entity;

import core.*;

/**
 * Farmer — Jagende fjende (F).
 *
 * Farmeren har simpel AI:
 *   1. Tjek om der er en kastet TreatItem på kortet → gå mod den i stedet
 *   2. Ellers: er spilleren inden for 5 felter? → jag spilleren
 *   3. Hvis ingen af delene: stå stille
 *
 * Bevægelse bruger "greedy chase" — farmeren flytter sig ét felt
 * mod målet ad den akse med størst afstand. Hvis den akse er blokeret,
 * prøver den den anden akse.
 *
 * Farmeren kan distrahere med TreatItem, som er spillerens vigtigste
 * forsvar mod farmere.
 *
 * Symbol: 'F'
 * Farve: rød tekst på mørkegrøn baggrund
 * Solid: true (blokerer spillerens bevægelse)
 * Skade: 30 HP per tur ved kontakt
 */
public class Farmer extends Entity {

    private static final int DAMAGE = 30;          // Skade per tur ved kontakt
    private static final int SIGHT_RANGE = 5;      // Hvor langt farmeren kan "se" (Manhattan-afstand)
    private int bombHits = 0;                       // Antal gange ramt af bombe

    public Farmer(Position position) {
        super(position, 'F', Color.RED, Color.DARK_GREEN, true);
        this.sprite = "👨";
    }

    /**
     * onTurn() — farmerens AI. Kaldes hver tur af Dungeon.tick().
     *
     * Prioritet:
     *   1. Gå mod kastet treat (hvis der er en)
     *   2. Jag spilleren (hvis inden for rækkevidde)
     *   3. Stå stille
     */
    @Override
    public void onTurn(Dungeon dungeon) {
        Player player = dungeon.getPlayer();
        if (player == null) return;

        // PRIORITET 1: Er der en kastet treat? Gå mod den i stedet for spilleren
        TreatItem treat = findTreat(dungeon);
        if (treat != null) {
            moveTowards(treat.getPosition(), dungeon);
            // Hvis farmeren når treat'en, spis den og fjern den fra brættet
            if (position.equals(treat.getPosition())) {
                dungeon.removeEntity(treat);
                dungeon.addMessage("Farmeren spiste godbiden!");
            }
            return;  // Farmeren er optaget af treat — jager IKKE spilleren
        }

        // PRIORITET 2: Er spilleren inden for rækkevidde?
        int dist = position.distanceTo(player.getPosition());
        if (dist <= SIGHT_RANGE) {
            // Hvis farmeren allerede er ved siden af spilleren (afstand 1): slå!
            if (dist <= 1) {
                player.damage(DAMAGE);
                dungeon.beep();
                dungeon.addMessage("Farmeren fangede dig! -" + DAMAGE + " HP");
            } else {
                // Ellers: bevæg mod spilleren
                moveTowards(player.getPosition(), dungeon);
            }
        }
        // PRIORITET 3: Stå stille (spilleren er for langt væk)
    }

    /**
     * findTreat() — søg efter en kastet TreatItem i dungeon.
     * Returnerer den første fundne, eller null.
     * Bruger instanceof til at finde den rigtige type (polymorfi).
     */
    private TreatItem findTreat(Dungeon dungeon) {
        for (Entity e : dungeon.getEntities()) {
            if (e instanceof TreatItem && ((TreatItem) e).isThrown()) {
                return (TreatItem) e;
            }
        }
        return null;
    }

    public void bombHit(Dungeon dungeon) {
        bombHits++;
        if (bombHits >= 2) {
            dungeon.removeEntity(this);
            dungeon.addMessage("Farmeren blev sprængt væk!");
        } else {
            dungeon.addMessage("Farmeren er ramt af eksplosionen!");
        }
    }

    /**
     * moveTowards() — flyt ét felt mod en mål-position (greedy chase).
     *
     * Algoritme:
     *   1. Beregn afstand i x og y til målet
     *   2. Vælg den akse med størst afstand (for at nærme sig hurtigst)
     *   3. Prøv at flytte ét felt i den retning
     *   4. Hvis blokeret → prøv den anden akse i stedet
     *
     * Integer.signum() returnerer -1, 0 eller +1 alt efter fortegn,
     * så vi altid kun flytter ét felt ad gangen.
     */
    private void moveTowards(Position target, Dungeon dungeon) {
        int dx = target.getX() - position.getX();  // Positiv = mål er til højre
        int dy = target.getY() - position.getY();  // Positiv = mål er nedenunder

        Position next;

        // Prøv primær akse (den med størst afstand)
        if (Math.abs(dx) >= Math.abs(dy)) {
            // Horisontalt er størst — prøv at flytte i x-retningen
            next = new Position(position.getX() + Integer.signum(dx), position.getY());
        } else {
            // Vertikalt er størst — prøv at flytte i y-retningen
            next = new Position(position.getX(), position.getY() + Integer.signum(dy));
        }

        // Tjek om det felt er frit (ikke blokeret af mur osv.)
        if (!dungeon.isSolidAt(next)) {
            this.position = next;
        } else {
            // Blokeret! Prøv den anden akse som alternativ
            if (Math.abs(dx) >= Math.abs(dy)) {
                next = new Position(position.getX(), position.getY() + Integer.signum(dy));
            } else {
                next = new Position(position.getX() + Integer.signum(dx), position.getY());
            }
            if (!dungeon.isSolidAt(next)) {
                this.position = next;
            }
            // Hvis begge akser er blokeret → farmeren kan ikke bevæge sig
        }
    }
}

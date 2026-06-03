package entity;

import core.*;

/**
 * AlarmSensor — Line-of-sight fælde (!).
 *
 * Sidder langs en mur og tjekker HVER TUR om spilleren er i dens synsfelt.
 * Sensoren "kigger" i én retning (facing) og scanner alle felter i den
 * retning indtil den rammer en solid entity (mur) eller kanten af brættet.
 *
 * Hvis spilleren er i line-of-sight: giver 20 skade og deaktiveres.
 * Deaktiveret sensor skifter udseende til '.' i grå.
 *
 * Dette bruger onTurn() i stedet for onStep() — forskellen er at
 * onTurn() kører HVER tur uanset om spilleren er ved siden af,
 * mens onStep() kun kører når spilleren træder direkte PÅ feltet.
 *
 * Symbol: '!' (aktiv) → '.' (deaktiveret)
 * Farve: lilla (aktiv) → grå (deaktiveret)
 * Solid: false
 * Skade: 20 HP
 */
public class AlarmSensor extends Entity {

    private static final int DAMAGE = 20;
    private Direction facing;  // Retningen sensoren kigger (NORTH/SOUTH/EAST/WEST)
    private boolean active;    // Er sensoren stadig aktiv?

    /** Constructor med specifik retning. */
    public AlarmSensor(Position position, Direction facing) {
        super(position, '!', Color.PURPLE, Color.DARK_GREEN, false);
        this.sprite = "🚨";
        this.facing = facing;
        this.active = true;
    }

    /** Default constructor — vender mod SOUTH. */
    public AlarmSensor(Position position) {
        this(position, Direction.SOUTH);  // this() kalder den anden constructor
    }

    /**
     * onTurn() — tjek line-of-sight mod spilleren.
     *
     * Algoritme:
     *   1. Start fra sensorens position, gå ét felt i facing-retningen
     *   2. For hvert felt: er der en solid entity? → stop (blokeret)
     *   3. Er spilleren der? → giv skade, deaktivér
     *   4. Fortsæt til kanten af brættet
     */
    @Override
    public void onTurn(Dungeon dungeon) {
        if (!active) return;  // Deaktiveret — gør ingenting

        Player player = dungeon.getPlayer();
        if (player == null) return;

        // Start scanning fra feltet FORAN sensoren
        Position check = position.moved(facing);

        // Fortsæt i facing-retningen indtil vi rammer kanten
        while (check.getX() >= 0 && check.getX() < dungeon.getCols()
            && check.getY() >= 0 && check.getY() < dungeon.getRows()) {

            // Er der en solid entity i vejen? (mur, hegn osv.)
            Entity blocker = dungeon.getSolidAt(check);
            if (blocker != null) break;  // Blokeret — kan ikke se længere

            // Er spilleren på dette felt?
            if (check.equals(player.getPosition())) {
                player.damage(DAMAGE);
                dungeon.beep();
                active = false;          // Éngangsbrug — deaktivér
                symbol = '.';            // Skift udseende til "brugt"
                sprite = "  ";           // Usynlig — ser ud som gulv
                fgColor = Color.LIGHT_GRAY;
                dungeon.addMessage("ALARM! Sensoren udsender et stød! -" + DAMAGE + " HP");
                break;
            }

            // Gå videre til næste felt i retningen
            check = check.moved(facing);
        }
    }

    public boolean isActive() { return active; }
    public Direction getFacing() { return facing; }
}

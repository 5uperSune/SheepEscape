import core.*;
import entity.*;
import app.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Farmer — jager player, distraheres af treat.
 */
public class FarmerTest {

    private Dungeon dungeon;
    private Player player;
    private Farmer farmer;

    @BeforeEach
    void setUp() {
        dungeon = new Dungeon(20, 20, 1);
        // Fyld med gulv
        for (int x = 0; x < 20; x++) {
            for (int y = 0; y < 20; y++) {
                dungeon.addEntity(new Floor(new Position(x, y)));
            }
        }
        player = new Player(new Position(5, 5));
        dungeon.addEntity(player);
        farmer = new Farmer(new Position(8, 5));
        dungeon.addEntity(farmer);
    }

    @Test
    void testChasesPlayerInRange() {
        Position startPos = farmer.getPosition();
        farmer.onTurn(dungeon);
        // Farmeren skal have bevaaget sig mod spilleren
        Position newPos = farmer.getPosition();
        assertTrue(newPos.distanceTo(player.getPosition())
            < startPos.distanceTo(player.getPosition()));
    }

    @Test
    void testDoesNotChaseOutOfRange() {
        // Flyt spilleren langt vaek (mere end 5 felter)
        player.moveTo(new Position(15, 15));
        Position startPos = new Position(farmer.getPosition().getX(), farmer.getPosition().getY());
        farmer.onTurn(dungeon);
        // Farmeren skal ikke have bevaaget sig
        assertEquals(startPos, farmer.getPosition());
    }

    @Test
    void testDamagesPlayerOnContact() {
        // Placer farmeren lige ved siden af spilleren
        farmer.setPosition(new Position(6, 5));
        farmer.onTurn(dungeon);
        // Farmeren burde ramme spilleren med 30 skade
        assertEquals(70, player.getHealth());
    }

    @Test
    void testDistractedByTreat() {
        // Placer en kastet treat langt fra spilleren
        TreatItem treat = new TreatItem(new Position(15, 5));
        treat.setPosition(new Position(15, 5));
        player.addItem(treat);
        Entity used = player.useItem(0);
        if (used instanceof TreatItem) {
            ((TreatItem) used).use(player, dungeon, Direction.EAST);
        }

        Position farmerStart = new Position(farmer.getPosition().getX(), farmer.getPosition().getY());
        farmer.onTurn(dungeon);
        // Farmeren skal bevaege sig mod treat, ikke mod spilleren
        Position farmerNew = farmer.getPosition();
        assertNotEquals(farmerStart, farmerNew);
    }

    @Test
    void testIsSolid() {
        assertTrue(farmer.isSolid());
    }

    @Test
    void testSymbol() {
        assertEquals('F', farmer.getSymbol());
    }
}

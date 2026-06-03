import core.*;
import entity.*;
import app.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Snare — skjult, aktivering, deaktivering.
 */
public class SnareTest {

    private Dungeon dungeon;
    private Player player;
    private Snare snare;

    @BeforeEach
    void setUp() {
        dungeon = new Dungeon(10, 10, 1);
        player = new Player(new Position(5, 5));
        dungeon.addEntity(player);
        snare = new Snare(new Position(6, 5));
        dungeon.addEntity(snare);
    }

    @Test
    void testStartsHidden() {
        // Snare ser ud som gulv foer aktivering
        assertEquals('.', snare.getSymbol());
        assertFalse(snare.isTriggered());
    }

    @Test
    void testDamageOnStep() {
        snare.onStep(player, dungeon);
        assertEquals(75, player.getHealth());
    }

    @Test
    void testBecomesVisibleAfterTriggered() {
        snare.onStep(player, dungeon);
        assertTrue(snare.isTriggered());
        assertEquals('x', snare.getSymbol());
    }

    @Test
    void testDoesNotDamageTwice() {
        snare.onStep(player, dungeon);
        snare.onStep(player, dungeon);
        // Kun 25 skade, ikke 50
        assertEquals(75, player.getHealth());
    }

    @Test
    void testIsNotSolid() {
        assertFalse(snare.isSolid());
    }
}

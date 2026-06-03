import core.*;
import entity.*;
import app.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

/**
 * Tests for Player — heal, damage, isDead, inventory.
 */
public class PlayerTest {

    private Player player;

    @BeforeEach
    void setUp() {
        player = new Player(new Position(5, 5));
    }

    @Test
    void testInitialHealth() {
        assertEquals(100, player.getHealth());
        assertEquals(100, player.getMaxHealth());
    }

    @Test
    void testDamage() {
        player.damage(30);
        assertEquals(70, player.getHealth());
    }

    @Test
    void testDamageDoesNotGoBelowZero() {
        player.damage(150);
        assertEquals(0, player.getHealth());
    }

    @Test
    void testHeal() {
        player.damage(50);
        player.heal(20);
        assertEquals(70, player.getHealth());
    }

    @Test
    void testHealDoesNotExceedMax() {
        player.damage(10);
        player.heal(50);
        assertEquals(100, player.getHealth());
    }

    @Test
    void testIsDead() {
        assertFalse(player.isDead());
        player.damage(100);
        assertTrue(player.isDead());
    }

    @Test
    void testMoveTo() {
        Position newPos = new Position(7, 3);
        player.moveTo(newPos);
        assertEquals(newPos, player.getPosition());
    }

    @Test
    void testInventory() {
        HayItem hay = new HayItem(new Position(0, 0));
        player.addItem(hay);
        assertEquals(1, player.getInventory().size());
        assertEquals(hay, player.getInventory().get(0));
    }

    @Test
    void testUseItem() {
        HayItem hay = new HayItem(new Position(0, 0));
        player.addItem(hay);
        Entity used = player.useItem(0);
        assertEquals(hay, used);
        assertTrue(player.getInventory().isEmpty());
    }

    @Test
    void testUseItemInvalidIndex() {
        assertNull(player.useItem(0));
        assertNull(player.useItem(-1));
    }

    @Test
    void testStartPosition() {
        assertEquals(new Position(5, 5), player.getStartPosition());
        player.moveTo(new Position(10, 10));
        // Start position should remain unchanged
        assertEquals(new Position(5, 5), player.getStartPosition());
    }

    @Test
    void testSymbol() {
        assertEquals('@', player.getSymbol());
    }
}

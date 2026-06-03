import core.*;
import entity.*;
import app.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Dungeon — move, getEntityAt, collision.
 */
public class DungeonTest {

    private Dungeon dungeon;
    private Player player;

    @BeforeEach
    void setUp() {
        dungeon = new Dungeon(10, 10, 1);
        // Fyld med gulv
        for (int x = 0; x < 10; x++) {
            for (int y = 0; y < 10; y++) {
                dungeon.addEntity(new Floor(new Position(x, y)));
            }
        }
        player = new Player(new Position(5, 5));
        dungeon.addEntity(player);
    }

    @Test
    void testPlayerExists() {
        assertNotNull(dungeon.getPlayer());
        assertEquals(new Position(5, 5), dungeon.getPlayer().getPosition());
    }

    @Test
    void testMoveNorth() {
        dungeon.move(Direction.NORTH);
        assertEquals(new Position(5, 4), player.getPosition());
    }

    @Test
    void testMoveSouth() {
        dungeon.move(Direction.SOUTH);
        assertEquals(new Position(5, 6), player.getPosition());
    }

    @Test
    void testMoveEast() {
        dungeon.move(Direction.EAST);
        assertEquals(new Position(6, 5), player.getPosition());
    }

    @Test
    void testMoveWest() {
        dungeon.move(Direction.WEST);
        assertEquals(new Position(4, 5), player.getPosition());
    }

    @Test
    void testMoveBlockedByWall() {
        dungeon.addEntity(new StoneWall(new Position(6, 5)));
        dungeon.move(Direction.EAST);
        // Spilleren maa ikke bevaege sig ind i muren
        assertEquals(new Position(5, 5), player.getPosition());
    }

    @Test
    void testMoveBlockedByBoundary() {
        Player edgePlayer = new Player(new Position(0, 0));
        Dungeon d = new Dungeon(5, 5, 1);
        d.addEntity(edgePlayer);
        d.move(Direction.NORTH);
        assertEquals(new Position(0, 0), edgePlayer.getPosition());
        d.move(Direction.WEST);
        assertEquals(new Position(0, 0), edgePlayer.getPosition());
    }

    @Test
    void testGetEntityAt() {
        StoneWall wall = new StoneWall(new Position(3, 3));
        dungeon.addEntity(wall);
        Entity found = dungeon.getEntityAt(new Position(3, 3));
        assertEquals(wall, found);
    }

    @Test
    void testGetEntityAtReturnsNullForEmptyPosition() {
        // Position (1,1) has only Floor, which is ignored by getEntityAt
        assertNull(dungeon.getEntityAt(new Position(1, 1)));
    }

    @Test
    void testIsSolidAt() {
        dungeon.addEntity(new StoneWall(new Position(2, 2)));
        assertTrue(dungeon.isSolidAt(new Position(2, 2)));
        assertFalse(dungeon.isSolidAt(new Position(3, 3)));
    }

    @Test
    void testRemoveEntity() {
        StoneWall wall = new StoneWall(new Position(4, 4));
        dungeon.addEntity(wall);
        assertTrue(dungeon.isSolidAt(new Position(4, 4)));
        dungeon.removeEntity(wall);
        assertFalse(dungeon.isSolidAt(new Position(4, 4)));
    }

    @Test
    void testIsGameLost() {
        assertFalse(dungeon.isGameLost());
        player.damage(100);
        assertTrue(dungeon.isGameLost());
    }

    @Test
    void testIsGameWon() {
        assertFalse(dungeon.isGameWon());
        dungeon.setGameWon(true);
        assertTrue(dungeon.isGameWon());
    }

    @Test
    void testMessages() {
        dungeon.addMessage("Test besked");
        assertEquals(1, dungeon.getMessages().size());
        assertEquals("Test besked", dungeon.getMessages().get(0));
    }
}

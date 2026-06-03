import core.*;
import entity.*;
import app.*;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;

/**
 * Tests for DungeonLoader — loader level korrekt fra fil.
 */
public class DungeonLoaderTest {

    @Test
    void testLoadLevel1() throws IOException {
        Dungeon dungeon = DungeonLoader.load("levels/level1.txt", 1);
        assertNotNull(dungeon);
        assertNotNull(dungeon.getPlayer());
        assertEquals(1, dungeon.getLevelNumber());
    }

    @Test
    void testPlayerPosition() throws IOException {
        Dungeon dungeon = DungeonLoader.load("levels/level1.txt", 1);
        Player player = dungeon.getPlayer();
        assertNotNull(player);
        // @ er paa position (3, 2) i level1
        assertEquals(new Position(3, 2), player.getPosition());
    }

    @Test
    void testWallsPresent() throws IOException {
        Dungeon dungeon = DungeonLoader.load("levels/level1.txt", 1);
        // Oeverste venstre hjoerne skal vaere en StoneWall
        assertTrue(dungeon.isSolidAt(new Position(0, 0)));
    }

    @Test
    void testGatePresent() throws IOException {
        Dungeon dungeon = DungeonLoader.load("levels/level1.txt", 1);
        // > er paa position (15, 6) i level1
        Entity gate = dungeon.getEntityAt(new Position(15, 6));
        assertNotNull(gate);
        assertTrue(gate instanceof Gate);
    }

    @Test
    void testFencePresent() throws IOException {
        Dungeon dungeon = DungeonLoader.load("levels/level1.txt", 1);
        // Fence | starter paa position (5, 4) i level1
        Entity fence = dungeon.getEntityAt(new Position(5, 4));
        assertNotNull(fence);
        assertTrue(fence instanceof Fence);
    }

    @Test
    void testHayItemPresent() throws IOException {
        Dungeon dungeon = DungeonLoader.load("levels/level1.txt", 1);
        // h er paa position (15, 2) i level1
        Entity hay = dungeon.getEntityAt(new Position(15, 2));
        assertNotNull(hay);
        assertTrue(hay instanceof HayItem);
    }

    @Test
    void testDungeonDimensions() throws IOException {
        Dungeon dungeon = DungeonLoader.load("levels/level1.txt", 1);
        assertTrue(dungeon.getCols() > 0);
        assertTrue(dungeon.getRows() > 0);
    }

    @Test
    void testInvalidFileThrowsException() {
        assertThrows(IOException.class, () -> {
            DungeonLoader.load("levels/nonexistent.txt", 1);
        });
    }

    @Test
    void testLevel2HasSheepdog() throws IOException {
        Dungeon dungeon = DungeonLoader.load("levels/level2.txt", 2);
        boolean hasDog = false;
        for (Entity e : dungeon.getEntities()) {
            if (e instanceof Sheepdog) {
                hasDog = true;
                break;
            }
        }
        assertTrue(hasDog);
    }

    @Test
    void testLevel3HasSheepdog() throws IOException {
        Dungeon dungeon = DungeonLoader.load("levels/level3.txt", 3);
        boolean hasDog = false;
        for (Entity e : dungeon.getEntities()) {
            if (e instanceof Sheepdog) {
                hasDog = true;
                break;
            }
        }
        assertTrue(hasDog);
    }
}

import core.*;
import entity.*;
import app.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for ElectricFence — skade + slukker efter 3 interaktioner.
 */
public class ElectricFenceTest {

    private Dungeon dungeon;
    private Player player;
    private ElectricFence fence;

    @BeforeEach
    void setUp() {
        dungeon = new Dungeon(10, 10, 1);
        player = new Player(new Position(5, 5));
        dungeon.addEntity(player);
        fence = new ElectricFence(new Position(6, 5));
        dungeon.addEntity(fence);
    }

    @Test
    void testIsNotSolid() {
        assertFalse(fence.isSolid());
    }

    @Test
    void testDamageOnStep() {
        fence.onStep(player, dungeon);
        assertEquals(90, player.getHealth());
    }

    @Test
    void testInteractionsDecrease() {
        assertEquals(3, fence.getInteractions());
        fence.onStep(player, dungeon);
        assertEquals(2, fence.getInteractions());
    }

    @Test
    void testDeactivatesAfterThreeHits() {
        fence.onStep(player, dungeon);
        fence.onStep(player, dungeon);
        fence.onStep(player, dungeon);
        assertEquals(0, fence.getInteractions());
        // Fence should be removed from dungeon
        assertFalse(dungeon.getEntities().contains(fence));
    }

    @Test
    void testTotalDamageFromThreeHits() {
        fence.onStep(player, dungeon);
        fence.onStep(player, dungeon);
        fence.onStep(player, dungeon);
        // 3 x 10 = 30 damage
        assertEquals(70, player.getHealth());
    }

    @Test
    void testSymbol() {
        assertEquals('~', fence.getSymbol());
    }
}

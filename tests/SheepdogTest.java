import core.*;
import entity.*;
import app.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.List;

/**
 * Tests for Sheepdog — foelger rute, giver skade.
 */
public class SheepdogTest {

    private Dungeon dungeon;
    private Player player;
    private Sheepdog dog;

    @BeforeEach
    void setUp() {
        dungeon = new Dungeon(10, 10, 1);
        player = new Player(new Position(1, 1));
        dungeon.addEntity(player);
        dog = new Sheepdog(new Position(5, 5));
        dungeon.addEntity(dog);
    }

    @Test
    void testStartPosition() {
        assertEquals(new Position(5, 5), dog.getPosition());
    }

    @Test
    void testFollowsRoute() {
        List<Position> route = Arrays.asList(
            new Position(5, 5),
            new Position(6, 5),
            new Position(7, 5),
            new Position(6, 5)
        );
        dog.setRoute(route);

        dog.onTurn(dungeon);
        assertEquals(new Position(6, 5), dog.getPosition());

        dog.onTurn(dungeon);
        assertEquals(new Position(7, 5), dog.getPosition());

        dog.onTurn(dungeon);
        assertEquals(new Position(6, 5), dog.getPosition());
    }

    @Test
    void testRouteWrapsAround() {
        List<Position> route = Arrays.asList(
            new Position(5, 5),
            new Position(6, 5)
        );
        dog.setRoute(route);

        dog.onTurn(dungeon);
        assertEquals(new Position(6, 5), dog.getPosition());

        dog.onTurn(dungeon);
        assertEquals(new Position(5, 5), dog.getPosition());

        dog.onTurn(dungeon);
        assertEquals(new Position(6, 5), dog.getPosition());
    }

    @Test
    void testDamagesPlayerOnContact() {
        // Placer hund rute saa den rammer spilleren
        List<Position> route = Arrays.asList(
            new Position(5, 5),
            new Position(1, 1)  // Spillerens position
        );
        dog.setRoute(route);

        dog.onTurn(dungeon);
        // Hunden er nu paa (1,1) = spillerens position
        assertEquals(85, player.getHealth());
    }

    @Test
    void testIsSolid() {
        assertTrue(dog.isSolid());
    }

    @Test
    void testSymbol() {
        assertEquals('D', dog.getSymbol());
    }
}

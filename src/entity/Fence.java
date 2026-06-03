package entity;

import core.*;

/**
 * Fence — Trähegn der kan ødelægges med en bombe (|).
 *
 * Ligesom StoneWall blokerer Fence bevægelse (solid = true).
 * Forskellen er at BombItem kan fjerne Fence-entities når den eksploderer.
 * Se BombItem.explode() for den logik.
 *
 * Symbol: '|'
 * Farve: brun tekst på mørkegrøn baggrund
 * Solid: true
 */
public class Fence extends Entity {

    public Fence(Position position) {
        super(position, '|', Color.BROWN, Color.DARK_GREEN, true);
        this.sprite = "🚧";
    }
}

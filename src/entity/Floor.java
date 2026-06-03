package entity;

import core.*;

/**
 * Floor — Tomt gulv/græs (.).
 *
 * Repræsenterer et tomt felt man frit kan gå på.
 * Har ingen interaktion — eksisterer kun for at tegne grøn baggrund.
 *
 * I DungeonLoader lægges et Floor UNDER alle andre entities,
 * så der altid er en grøn baggrund hvis en entity fjernes.
 *
 * Symbol: '.'
 * Farve: mørkegrøn tekst på mørkegrøn baggrund (usynlig prik)
 * Solid: false
 */
public class Floor extends Entity {

    public Floor(Position position) {
        super(position, '.', Color.DARK_GREEN, Color.DARK_GREEN, false);
        this.sprite = "  ";  // To mellemrum — viser kun grøn baggrund
    }
}

# -*- coding: utf-8 -*-
"""Genererer Laeseguide.pdf med korrekt dansk via fpdf2."""

from fpdf import FPDF

pdf = FPDF()
pdf.set_auto_page_break(auto=True, margin=20)
pdf.add_page()

def title(text):
    pdf.set_font("Helvetica", "B", 18)
    pdf.cell(0, 10, text, new_x="LMARGIN", new_y="NEXT", align="C")

def subtitle(text):
    pdf.set_font("Helvetica", "", 12)
    pdf.cell(0, 8, text, new_x="LMARGIN", new_y="NEXT", align="C")
    pdf.ln(4)

def heading(text):
    pdf.ln(4)
    pdf.set_font("Helvetica", "B", 14)
    pdf.cell(0, 9, text, new_x="LMARGIN", new_y="NEXT")
    pdf.ln(1)

def entry(filename, desc):
    pdf.set_font("Courier", "B", 10)
    pdf.set_x(pdf.l_margin)
    pdf.cell(0, 7, "  " + filename, new_x="LMARGIN", new_y="NEXT")
    pdf.set_font("Helvetica", "", 10)
    pdf.set_x(pdf.l_margin)
    pdf.multi_cell(0, 6, "  " + desc)
    pdf.ln(1)

def text(t):
    pdf.set_font("Helvetica", "", 10)
    pdf.set_x(pdf.l_margin)
    pdf.multi_cell(0, 6, t)
    pdf.ln(1)

def bullet(t):
    pdf.set_font("Helvetica", "", 10)
    pdf.set_x(pdf.l_margin)
    pdf.multi_cell(0, 6, "  - " + t)


# -- Indhold --

title("Sheep Escape")
subtitle("L\u00e6seguide til kildekoden")

text(
    "Denne guide beskriver i hvilken r\u00e6kkef\u00f8lge projektets Java-filer "
    "b\u00f8r l\u00e6ses og forst\u00e5s. Filerne er ordnet fra simple byggeklodser "
    "til det komplette system. N\u00f8gleklassen er Entity.java -- n\u00e5r man "
    "forst\u00e5r den, falder resten p\u00e5 plads."
)

heading("Lag 1: Byggeklodser")
entry("Position.java",
    "Simpel (x, y) koordinat-klasse med equals(), hashCode() og moved(). "
    "Bruges overalt til at angive placering p\u00e5 br\u00e6ttet.")
entry("Direction.java",
    "Enum med NORTH, SOUTH, EAST, WEST. Hver retning har en dx/dy-vektor. "
    "Bruges af move-logik og enemy AI.")
entry("Color.java",
    "ANSI-farvekoder til terminalen. Definerer RGB-konstanter (RED, GREEN osv.) "
    "og metoder til at generere escape-sekvenser for forgrund og baggrund.")

heading("Lag 2: Rendering")
entry("Canvas.java",
    "Tegner et 2D-grid med sprites og farver. Hver celle er 2 terminal-kolonner "
    "bred s\u00e5 emojis passer. render() bygger hele outputtet som \u00e9n StringBuilder "
    "og returnerer det som en String. Game.printFrame() sender det til terminalen "
    "i \u00e9t write()-kald for at undg\u00e5 flimmer.")

heading("Lag 3: Entity-hierarkiet")
entry("Entity.java",
    "Abstrakt baseklasse for ALT p\u00e5 br\u00e6ttet. Definerer position, symbol, "
    "sprite, farver, solid-flag og visibility. De vigtigste metoder: renderOn(Canvas), "
    "onStep(Player, Dungeon), onTurn(Dungeon), isSolid(). Alle 15 subklasser arver fra Entity.")
entry("Floor.java",
    "Den simpleste subklasse -- tomt gulv. Ikke solid, ingen interaktion. "
    "God til at forst\u00e5 hvordan Entity-arv virker i praksis.")
entry("StoneWall.java",
    "U\u00f8del\u00e6ggelig mur. solid=true, ingen onStep/onTurn. "
    "Viser den simpleste form for blokerende entity.")

heading("Lag 4: Interaktivt terr\u00e6n")
entry("Fence.java",
    "Hegn der kan \u00f8del\u00e6gges af bomber. Introducerer konceptet entities "
    "der fjernes fra spillet via removeEntity().")
entry("ElectricFence.java",
    "Giver skade n\u00e5r spilleren tr\u00e6der p\u00e5. Slukker efter 3 interaktioner. "
    "Viser onStep() med tilstands\u00e6ndring.")
entry("WaterTrough.java",
    "Healer spilleren via onStep(). Simpelt eksempel p\u00e5 positiv terr\u00e6n-interaktion.")
entry("Snare.java",
    "Skjult f\u00e6lde (usynlig indtil aktiveret). Giver skade \u00e9n gang. "
    "Viser visible-flag og sprite-skift ved aktivering.")
entry("AlarmSensor.java",
    "Line-of-sight f\u00e6lde med retning. Skanner i sin retning p\u00e5 onTurn(). "
    "Viser l\u00f8kke-baseret detektion og deaktivering.")

heading("Lag 5: Items (inventory-systemet)")
entry("HayItem.java",
    "Samles op via onStep() og tilf\u00f8jes til inventory. use() healer spilleren. "
    "Viser hele item-livscyklussen.")
entry("BombItem.java",
    "Komplekst item med to faser: onStep() samler op, use() placerer, onTurn() "
    "t\u00e6ller ned, explode() fjerner Fence i radius 1.")
entry("TreatItem.java",
    "Kastes i en retning med use(). Afleder Farmer AI. "
    "Viser entity-til-entity interaktion.")

heading("Lag 6: Enemies (AI)")
entry("Farmer.java",
    "Jagende fjende. Bev\u00e6ger sig mod spilleren hvis inden for SIGHT_RANGE (5 felter). "
    "Kan distraheres af TreatItem. Bruger greedy chase med moveTowards(). "
    "Mest komplekse AI i spillet.")
entry("Sheepdog.java",
    "Patruljerende fjende med fast rute. onTurn() bruger modulo til cyklisk bev\u00e6gelse. "
    "Viser simpel AI og kollisionsdetektion.")

heading("Lag 7: Objectives + Player")
entry("Gate.java",
    "Level-udgang. onStep() s\u00e6tter levelComplete=true i Dungeon. "
    "Trigger level-skift i Game.")
entry("Freedom.java",
    "Spillets m\u00e5l (sidste level). onStep() s\u00e6tter gameWon=true. "
    "N\u00e5r spilleren tr\u00e6der p\u00e5 Freedom, er spillet vundet.")
entry("Player.java",
    "Spillerens avatar med health, maxHealth og inventory (List<Entity>). "
    "Metoder: heal(), damage(), isDead(), addItem(), useItem(). "
    "Placeret sent i guiden fordi den afh\u00e6nger af item-koncepter.")

heading("Lag 8: System-klasser")
entry("Dungeon.java",
    "Hjertet af spillet. Holder alle entities, h\u00e5ndterer bev\u00e6gelse, kollision, "
    "interaktioner og enemy AI (tick()). move() er n\u00f8glemetoden: tjek solid, "
    "flyt player, kald onStep() p\u00e5 m\u00e5l-entity.")
entry("DungeonLoader.java",
    "Parser level-filer (.txt) til Dungeon-objekter. Mapper tegn til entities: "
    "# = StoneWall, @ = Player, F = Farmer osv. Parser ogs\u00e5 ROUTE-linjer til Sheepdog-ruter.")
entry("MazeGenerator.java",
    "Genererer tilf\u00e6ldige labyrinter for levels 2-4. Bruger recursive backtracking (DFS), "
    "\u00e5bner ca. 75% v\u00e6gge op, og placerer entities baseret p\u00e5 level-nummer.")
entry("Game.java",
    "Main-klasse med spil-loop. H\u00e5ndterer: Scanner (w/a/s/d + Enter), "
    "rendering, HP-bar, inventory-visning, level-skift, game over og sejr.")

heading("Opsummering")
text(
    "L\u00e6s filerne i denne r\u00e6kkef\u00f8lge for at bygge forst\u00e5elsen op gradvist. "
    "Hvert lag tilf\u00f8jer et nyt koncept oven p\u00e5 det forrige:")

for s in [
    "Lag 1-2: Grundl\u00e6ggende datatyper og rendering",
    "Lag 3: Entity-arv og polymorfi",
    "Lag 4: Interaktive entities (onStep, onTurn)",
    "Lag 5: Inventory og item-brug",
    "Lag 6: Enemy AI med pathfinding",
    "Lag 7: Spillerens avatar og m\u00e5l",
    "Lag 8: Alt samles i Dungeon og Game",
]:
    bullet(s)

pdf.ln(4)
text("N\u00f8glekoncepter at holde \u00f8je med:")
for c in [
    "Arv: alle entities arver fra Entity.java",
    "Polymorfi: onStep() og onTurn() opf\u00f8rer sig forskelligt per subklasse",
    "Encapsulation: private felter med getters/setters",
    "Separation of Concerns: Game (UI) vs. Dungeon (logik) vs. Entity (adf\u00e6rd)",
]:
    bullet(c)

pdf.output("Laeseguide.pdf")
print("Laeseguide.pdf genereret!")

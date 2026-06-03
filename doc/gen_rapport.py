# -*- coding: utf-8 -*-
"""Generate RAPPORT.pdf -- concise, LaTeX-style academic layout."""
from fpdf import FPDF


class ReportPDF(FPDF):
    """LaTeX article-style PDF: wide margins, centered, clean."""

    def __init__(self):
        super().__init__()
        # Wide margins like LaTeX default (~38mm sides)
        self.set_margins(left=38, top=30, right=38)
        self.set_auto_page_break(auto=True, margin=30)

    def footer(self):
        if self.page_no() == 1:
            return
        self.set_y(-25)
        self.set_font("Helvetica", "", 10)
        self.set_text_color(0, 0, 0)
        self.cell(0, 10, str(self.page_no() - 1), align="C")

    # --- Title page ---
    def title_page(self):
        self.add_page()
        self.ln(55)
        self.set_font("Helvetica", "", 15)
        self.cell(0, 10, "Object Oriented Programming with Java 1",
                  align="C", new_x="LMARGIN", new_y="NEXT")
        self.set_font("Helvetica", "B", 22)
        self.cell(0, 14, "Exam Project", align="C", new_x="LMARGIN", new_y="NEXT")
        self.ln(20)
        self.set_font("Helvetica", "", 14)
        self.cell(0, 10, "Sheep Escape", align="C", new_x="LMARGIN", new_y="NEXT")
        self.ln(30)
        self.set_font("Helvetica", "", 12)
        self.cell(0, 8, "Sune Groennebaek", align="C", new_x="LMARGIN", new_y="NEXT")
        self.ln(1)
        self.set_font("Helvetica", "I", 10)
        self.cell(0, 8, "SETUR", align="C", new_x="LMARGIN", new_y="NEXT")
        self.ln(8)
        self.set_font("Helvetica", "", 11)
        self.cell(0, 8, "May 2026", align="C", new_x="LMARGIN", new_y="NEXT")

    # --- Formatting helpers ---
    def section(self, num, title):
        self.ln(5)
        self.set_font("Helvetica", "B", 15)
        self.cell(0, 10, f"{num}   {title}", new_x="LMARGIN", new_y="NEXT")
        self.ln(2)

    def subsection(self, num, title):
        self.ln(3)
        self.set_font("Helvetica", "B", 12)
        self.cell(0, 8, f"{num}   {title}", new_x="LMARGIN", new_y="NEXT")
        self.ln(2)

    def body(self, text):
        self.set_font("Helvetica", "", 11)
        self.set_x(self.l_margin)
        self.multi_cell(0, 6, text)
        self.ln(2)

    def bullet(self, text):
        self.set_font("Helvetica", "", 11)
        indent = self.l_margin + 6
        self.set_x(indent)
        # Use a long dash as bullet
        self.multi_cell(0, 6, "-- " + text)
        self.ln(1)

    def code(self, text):
        self.set_font("Courier", "", 9)
        self.set_fill_color(245, 245, 245)
        self.set_x(self.l_margin + 6)
        self.multi_cell(0, 5, text, fill=True)
        self.ln(2)

    def bold_inline(self, label, text):
        """Bold label followed by normal text on same logical block."""
        self.set_font("Helvetica", "B", 11)
        self.set_x(self.l_margin)
        self.cell(0, 6, label, new_x="LMARGIN", new_y="NEXT")
        self.set_font("Helvetica", "", 11)
        self.set_x(self.l_margin)
        self.multi_cell(0, 6, text)
        self.ln(2)


# =====================================================================
pdf = ReportPDF()

# --- TITLE PAGE ---
pdf.title_page()

# --- 1. INTRODUCTION ---
pdf.add_page()
pdf.section("1", "Introduction")
pdf.body(
    "Sheep Escape is a terminal-based game written in Java. The player controls a "
    "sheep escaping a farm through 4 levels, avoiding enemies, collecting items and "
    "disarming traps. It is rendered with ANSI escape codes and emoji sprites."
)
pdf.body(
    "This report describes the OOP concepts used, how they appear in the code, and "
    "the testing strategy. Each section includes a code example."
)

# --- 2. APPLICATION DESCRIPTION ---
pdf.section("2", "Application Description")
pdf.body(
    "The game has 4 levels. Level 1 is loaded from a text file; levels 2-4 are "
    "randomly generated mazes. The player moves with w/a/s/d + Enter, collects items "
    "(hay, bombs, treats) and avoids two enemy types: Farmer (chases the player) and "
    "Sheepdog (patrols a fixed route). The goal is to reach the gate on each level "
    "and ultimately the Freedom tile on the last level."
)
pdf.body(
    "The codebase has 20 Java classes organized in three packages: core (Position, "
    "Direction, Color, Canvas, Entity, Player, Dungeon), entity (15 Entity subclasses), "
    "and app (Game, DungeonLoader, MazeGenerator)."
)

# --- 3. IMPLEMENTATION ---
pdf.section("3", "Implementation")

# --- 3.1 INHERITANCE ---
pdf.subsection("3.1", "Inheritance")
pdf.body(
    "Inheritance lets a class reuse fields and methods from a parent class. In this "
    "project, the abstract class Entity defines position, sprite, colors, solidity, "
    "and default methods (renderOn, onStep, onTurn). All 15 game objects inherit from "
    "Entity and only override what they need."
)
pdf.body(
    "For example, ElectricFence extends Entity and overrides onStep() to deal damage. "
    "Everything else -- rendering, position, solidity -- is inherited from Entity:"
)
pdf.code(
    "public class ElectricFence extends Entity {\n"
    "    private int interactions = 3;\n"
    "\n"
    "    public ElectricFence(Position position) {\n"
    "        super(position, '~', Color.ORANGE, Color.DARK_GREEN, false);\n"
    "        this.sprite = \"electric-emoji\";\n"
    "    }\n"
    "\n"
    "    @Override\n"
    "    public void onStep(Player player, Dungeon dungeon) {\n"
    "        player.damage(10);\n"
    "        interactions--;\n"
    "        if (interactions <= 0) {\n"
    "            dungeon.removeEntity(this);\n"
    "        }\n"
    "    }\n"
    "}"
)
pdf.body(
    "The super() call passes values to Entity's constructor. ElectricFence only adds "
    "what is unique to it: a hit counter and damage logic. This pattern is used by "
    "all 15 subclasses -- StoneWall, Farmer, HayItem, Gate, Player, etc."
)

# --- 3.2 POLYMORPHISM ---
pdf.subsection("3.2", "Polymorphism")
pdf.body(
    "Polymorphism means calling a method on a variable of type Entity, and Java "
    "automatically runs the correct subclass version. The Dungeon class uses this "
    "everywhere -- it stores all game objects in a single List<Entity> and calls "
    "onStep() or onTurn() without knowing the concrete type."
)
pdf.body(
    "The key example is Dungeon.move(). When the player steps onto a new tile, "
    "the method calls e.onStep() on every entity at that position:"
)
pdf.code(
    "// From Dungeon.move()\n"
    "player.moveTo(newPos);\n"
    "\n"
    "List<Entity> atPosition = getEntitiesAt(newPos);\n"
    "for (Entity e : atPosition) {\n"
    "    if (e != player) {\n"
    "        e.onStep(player, this);\n"
    "    }\n"
    "}"
)
pdf.body(
    "If the entity is an ElectricFence, Java runs ElectricFence.onStep() which deals "
    "damage. If it is a HayItem, it runs HayItem.onStep() which adds the item to "
    "inventory. If it is a Gate, it triggers a level transition. The Dungeon code "
    "does not contain a single if/else or instanceof check -- dynamic dispatch "
    "handles it all."
)

# --- 3.3 ENCAPSULATION ---
pdf.subsection("3.3", "Encapsulation")
pdf.body(
    "Encapsulation means hiding internal state behind methods. Fields are private "
    "and can only be changed through controlled methods that enforce rules."
)
pdf.body(
    "The Player class is a good example. Health is private, and the damage() and "
    "heal() methods ensure it never goes below 0 or above maxHealth:"
)
pdf.code(
    "public class Player extends Entity {\n"
    "    private int health = 100;\n"
    "    private int maxHealth = 100;\n"
    "    private List<Entity> inventory = new ArrayList<>();\n"
    "\n"
    "    public void damage(int amount) {\n"
    "        health = Math.max(health - amount, 0);\n"
    "    }\n"
    "\n"
    "    public void heal(int amount) {\n"
    "        health = Math.min(health + amount, maxHealth);\n"
    "    }\n"
    "\n"
    "    public boolean isDead() { return health <= 0; }\n"
    "}"
)
pdf.body(
    "External code cannot set health to -50 or 999 -- it must go through damage() "
    "and heal(). The same principle applies to Dungeon, where the entity list is "
    "private and only accessible through methods like getEntityAt() and isSolidAt()."
)

# --- 3.4 COLLECTIONS ---
pdf.subsection("3.4", "Collections")
pdf.body(
    "Java Collections (ArrayList, List) are used in two central places: Dungeon "
    "stores all game objects in a List<Entity>, and Player stores picked-up items "
    "in a List<Entity> inventory."
)
pdf.body(
    "The inventory system shows polymorphism and collections working together. "
    "useItem() removes an item from the list and returns it, so Game can call the "
    "type-specific use() method:"
)
pdf.code(
    "public Entity useItem(int index) {\n"
    "    if (index >= 0 && index < inventory.size()) {\n"
    "        return inventory.remove(index);\n"
    "    }\n"
    "    return null;\n"
    "}"
)
pdf.body(
    "Because inventory is a List<Entity>, it can hold HayItem, BombItem and TreatItem "
    "in the same list. Game then uses instanceof to determine which use() method to call."
)

# --- 3.5 SEPARATION OF CONCERNS ---
pdf.subsection("3.5", "Separation of Concerns")
pdf.body(
    "The project separates UI from game logic. Game handles all I/O: reading input "
    "with Scanner, building the frame with StringBuilder, and writing to the terminal. "
    "Dungeon handles all mechanics: movement, collision, entity interactions, and "
    "enemy AI. Dungeon has no imports of Scanner, System.out, or any I/O class."
)
pdf.body(
    "This means the input and rendering system could be replaced -- for example with "
    "a GUI -- without changing any game logic. The same separation exists between "
    "Entity subclasses: each entity defines its own behavior (onStep, onTurn), so "
    "adding a new entity type does not require changes to Dungeon or Game."
)

# --- 3.6 EMOJI SPRITE SYSTEM ---
pdf.subsection("3.6", "Emoji Sprite System")
pdf.body(
    "The assignment uses single characters for entities: '@' for the player, '#' for "
    "walls, etc. This project replaces them with emoji sprites -- the player is a "
    "sheep emoji, the farmer is a person emoji, walls are block characters, and so on. "
    "This gives the game a more visual and distinctive look compared to plain ASCII."
)
pdf.body(
    "The challenge is that emojis occupy 2 terminal columns while a normal character "
    "occupies 1. If some cells are 1 column and others are 2, the grid becomes "
    "misaligned. The solution is to make every cell exactly 2 columns wide. Canvas "
    "stores a String per cell (not a char), and each entity sets a 2-column sprite "
    "in its constructor:"
)
pdf.code(
    "// Entity constructor stores a 2-column default\n"
    "this.sprite = symbol + \" \";\n"
    "\n"
    "// Subclasses override with emojis (also 2 columns)\n"
    "this.sprite = \"sheep-emoji\";   // Player\n"
    "this.sprite = \"farmer-emoji\";  // Farmer\n"
    "this.sprite = \"dog-emoji\";     // Sheepdog\n"
    "this.sprite = \"flag-emoji\";    // Gate"
)
pdf.body(
    "Canvas.render() then outputs each cell's sprite directly, producing a grid where "
    "every column is exactly 2 terminal characters wide. This also means the board is "
    "visually twice as wide as the number of logical columns -- a 17-column level "
    "occupies 34 terminal columns -- which gives more room for the gameplay to breathe."
)
pdf.body(
    "Background color is set per cell using ANSI 24-bit RGB escape codes. This "
    "matters even for emoji cells, because emojis do not fully cover the pixel area "
    "-- the background color shows through at the edges. Floor entities use only a "
    "green background with two spaces as their sprite, creating a solid-colored tile."
)

# --- 3.7 FLICKER-FREE RENDERING ---
pdf.subsection("3.7", "Flicker-Free Rendering")
pdf.body(
    "The assignment suggests clearing the screen and printing each cell individually. "
    "This causes visible flicker because the terminal redraws after every print call. "
    "Fixing this required several iterations."
)
pdf.body(
    "The first fix was batching: Canvas.render() builds the entire board as one "
    "String, and Game assembles everything (clear-screen, board, stats, messages) "
    "into a single StringBuilder. This fixed levels 1-3 but not level 4."
)
pdf.body(
    "The problem was PrintStream's internal 8 KB buffer. Level 4's frame with ANSI "
    "color codes is roughly 19,000 characters, so PrintStream flushes mid-frame. "
    "The fix was bypassing PrintStream entirely:"
)
pdf.code(
    "private OutputStream rawOut =\n"
    "    new FileOutputStream(FileDescriptor.out);\n"
    "\n"
    "private void printFrame(StringBuilder frame) {\n"
    "    rawOut.write(frame.toString().getBytes(\"UTF-8\"));\n"
    "    rawOut.flush();\n"
    "}"
)
pdf.body(
    "This writes the entire frame as raw bytes in a single OS-level write() call. "
    "The terminal receives the complete frame atomically -- no intermediate states, "
    "no flicker."
)

# --- 4. TESTING ---
pdf.section("4", "Testing")
pdf.body(
    "The project has 59 JUnit 5 tests across 7 test classes. All tests are black-box: "
    "they call public methods and verify observable results."
)
pdf.bullet("PlayerTest (12): health, inventory, movement, edge cases")
pdf.bullet("DungeonTest (14): movement, collision, entity lookup, win/lose")
pdf.bullet("ElectricFenceTest (6): damage, deactivation after 3 hits")
pdf.bullet("SnareTest (5): hidden state, one-time activation")
pdf.bullet("FarmerTest (6): chase range, damage, treat distraction")
pdf.bullet("SheepdogTest (6): route following, cyclic patrol, damage")
pdf.bullet("DungeonLoaderTest (10): file parsing, entity placement")
pdf.body(
    "As an example, this test verifies that ElectricFence deactivates after exactly "
    "3 interactions:"
)
pdf.code(
    "@Test\n"
    "void testDeactivatesAfterThreeHits() {\n"
    "    fence.onStep(player, dungeon);\n"
    "    fence.onStep(player, dungeon);\n"
    "    fence.onStep(player, dungeon);\n"
    "    assertEquals(0, fence.getInteractions());\n"
    "    assertFalse(dungeon.getEntities().contains(fence));\n"
    "}"
)

# --- 5. CONCLUSION ---
pdf.section("5", "Conclusion")
pdf.body(
    "The Entity hierarchy demonstrates inheritance and polymorphism in a practical "
    "setting -- Dungeon.move() and Dungeon.tick() operate on entities uniformly "
    "without type checks. Encapsulation keeps state consistent (Player.damage() "
    "cannot produce negative health). The separation between Game and Dungeon "
    "keeps I/O and logic independent. The flicker-free rendering required going "
    "beyond the standard library (bypassing PrintStream) but resulted in smooth "
    "output on all levels."
)

# --- 6. SHORTCOMINGS ---
pdf.section("6", "Shortcomings")
pdf.bullet("Farmer AI is a greedy one-step chase with no pathfinding")
pdf.bullet("Bombs only destroy Fence entities, not other types")
pdf.bullet("No save/load functionality")
pdf.bullet("AlarmSensor always faces SOUTH")

# --- SAVE ---
pdf.output("RAPPORT.pdf")
print("RAPPORT.pdf generated!")

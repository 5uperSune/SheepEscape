# Sheep Escape — Project Report

**Course:** Object Oriented Programming with Java 1
**Author:** Sune Groennebaek
**Institution:** SETUR
**Date:** May 2026

---

## 1. Introduction

Sheep Escape is a roguelike game written in Java where you control a sheep (@) trying to
escape from a pen. You avoid farmers, dogs and traps, collect items, and work your way
through 4 levels towards freedom.

The game runs in the terminal with ANSI colors and is controlled with w/a/s/d.

---

## 2. Project Structure

```
SheepEscape/
├── src/
│   ├── core/          ← Foundation (package core)
│   │   ├── Position.java       (x,y) coordinate
│   │   ├── Direction.java       NORTH/SOUTH/EAST/WEST enum
│   │   ├── Color.java           RGB colors + ANSI codes
│   │   ├── Canvas.java          Draws grid with colors
│   │   ├── Entity.java          ABSTRACT base class for everything on the board
│   │   ├── Dungeon.java         The board — entities, movement, AI
│   │   └── Player.java          The sheep — health, inventory
│   ├── entity/        ← 14 Entity subclasses (package entity)
│   │   ├── Floor, StoneWall, Fence, ElectricFence, WaterTrough
│   │   ├── Snare, AlarmSensor
│   │   ├── Farmer, Sheepdog
│   │   ├── HayItem, BombItem, TreatItem
│   │   └── Gate, Freedom
│   └── app/           ← Application layer (package app)
│       ├── Game.java            Main + game loop + UI
│       ├── DungeonLoader.java   Loads levels from .txt files
│       └── MazeGenerator.java   Generates random mazes
├── tests/             ← 7 JUnit test files (59 tests)
├── levels/            ← 4 level files (.txt)
├── lib/               ← JUnit jar file
├── web/               ← HTML5 web version (index.html)
├── doc/               ← Report, reading guide, PDF generators
├── bin/               ← Compiled .class files (generated)
└── .gitignore
```

The source code is organized into three Java packages:
- **core** — the foundation classes that everything else depends on
- **entity** — all 14 concrete Entity subclasses (terrain, enemies, items, objectives)
- **app** — the application layer (game loop, file parsing, maze generation)

---

## 3. OOP Design

### 3.1 Inheritance

Everything on the board inherits from the abstract class `Entity`:

```
Entity (abstract)
  ├── Player          — the player's sheep
  ├── StoneWall       — indestructible wall
  ├── Fence           — fence (can be destroyed)
  ├── ElectricFence   — electric fence
  ├── WaterTrough     — water trough
  ├── Floor           — empty floor
  ├── Snare           — hidden trap
  ├── AlarmSensor     — line-of-sight trap
  ├── Farmer          — chasing enemy
  ├── Sheepdog        — patrolling enemy
  ├── HayItem         — hay (heal item)
  ├── BombItem        — bomb (blows up fences)
  ├── TreatItem       — treat
  ├── Gate            — level exit
  └── Freedom         — game goal
```

The advantage: All entities share common fields (position, symbol, colors, solid)
and methods (renderOn, onStep, onTurn), but each has its own specialized behavior.

### 3.2 Polymorphism

Dungeon treats all entities the same — it calls e.g. `entity.onStep(player, this)`
without knowing whether it's an ElectricFence, HayItem or Gate. Java automatically
selects the correct method based on the object's actual type (dynamic dispatch).

Example from `Dungeon.move()`:
```java
List<Entity> atPosition = getEntitiesAt(newPos);
for (Entity e : atPosition) {
    e.onStep(player, this);  // Calls the CORRECT onStep() for each type
}
```

### 3.3 Encapsulation

- All fields in Entity are `protected` — accessible to subclasses, but not from outside
- Position's fields are `private` with getters/setters
- Dungeon's internal state is private — access is through methods like
  `getEntityAt()`, `isSolidAt()`, `addMessage()` etc.

### 3.4 Abstraction

- `Entity` is abstract — you cannot create a `new Entity()`
- `onStep()` and `onTurn()` have default implementations (do nothing),
  so simple entities like StoneWall and Floor don't need to override them

---

## 4. Key Classes and What They Do

### Entity.java — The Abstract Base Class
Everything on the board inherits from this. Defines 3 important methods:
- `renderOn(Canvas)` — draw the entity with its symbol and colors
- `onStep(Player, Dungeon)` — what happens when the player steps on this tile
- `onTurn(Dungeon)` — what happens each turn (enemy AI, timers)

### Dungeon.java — The Game Board
The core of the game. Holds all entities in a `List<Entity>` and has the methods:
- `move(Direction)` — move the player, check collision, call onStep()
- `show()` — clear canvas, draw entities, return board as String
- `tick()` — call onTurn() on all entities (enemy AI)

### DungeonLoader.java — File Parser
Reads .txt files and builds Dungeon objects. Each character in the file maps to an Entity.
Sheepdog routes are read from ROUTE lines below the map.

### Game.java — Game Loop and UI
Controls input (Scanner), rendering and game state.
Separated from Dungeon — Game knows nothing about game mechanics, Dungeon knows nothing about UI.

### Canvas.java — Terminal Rendering
Three parallel 2D arrays (sprite, fgColor, bgColor) are drawn with ANSI escape codes.
render() builds the entire board in a StringBuilder and returns it as a String.
Game.printFrame() writes the complete frame to the terminal in one raw write() call (avoids flicker).

---

## 5. Interesting Design Decisions

### Floor Under Everything
DungeonLoader ALWAYS adds a Floor object under all other entities.
When an entity is removed (e.g. a bomb that explodes), there is still green floor underneath.

### Snapshot in tick()
Dungeon.tick() makes a copy of the entity list before iterating,
because onTurn() can remove entities. Without this we would get
`ConcurrentModificationException`.

### Snare Design
The snare starts with symbol '.' and green color — visually identical to floor.
Only when the player steps on it does it change to 'x' in red.
The player cannot see the trap until it's too late.

### Treat Distraction
Farmer checks FIRST whether there is a thrown TreatItem.
If so, it moves towards the treat instead of the player.
When the farmer reaches the treat, it is removed from the board.

### Render Order in Dungeon.show()
The darkness overlay (level 4) paints black over everything from a certain column.
To ensure enemies are always visible, they are drawn AFTER the darkness:
1. Terrain, items, traps (background)
2. Darkness overlay
3. Farmer and Sheepdog (always visible)
4. The player (on top)
Only Snare is hidden — it uses sprite disguise, not darkness.

### Flicker-Free Rendering (4-step fix)
The assignment suggests `clearConsole()` + `System.out.print()` per cell.
This causes visible flicker. Fixing it required four iterations:

1. **Many print calls → StringBuilder batching**: Canvas.render() builds the entire
   board as one String. Game assembles clear-screen + board + stats + messages into
   one StringBuilder. Fixed levels 1-3, but NOT level 4.

2. **PrintStream's 8 KB buffer**: Level 4's frame is ~19,000 chars with ANSI codes.
   PrintStream's internal BufferedWriter (8192 chars) flushes mid-frame. Fix: bypass
   PrintStream entirely — write UTF-8 bytes directly to `FileOutputStream(FileDescriptor.out)`
   in a single `write()` call via `printFrame()`.

3. **Dual-stream conflict**: `beep()` wrote BEL char to `System.out` while frames
   went via `rawOut`. Same file descriptor, separate buffers → BEL byte splits the
   frame. Fix: `beep()` sets a boolean flag, Game includes `\007` inside the frame
   StringBuilder so it's sent in the same atomic write.

4. **Variable frame height**: Message log had 0-5 lines depending on message count →
   frame height changed → terminal scrolled. Fix: always render exactly 3 message lines.

**Frame height budget** (level 4): 17 (board) + 1 (stats) + 3 (messages) + 1 (inventory)
+ 1 (controls) + 1 (prompt) = 24 lines. Fits in VS Code's terminal panel.

### Emoji Sprite System
The assignment uses single chars ('@', '#'). This project uses emoji sprites — the
player is 🐑, the farmer is 👨, etc. The challenge: emojis occupy 2 terminal columns,
normal chars occupy 1. If mixed, the grid misaligns.

Solution: every Canvas cell is exactly 2 columns wide. Canvas stores a `String` per
cell (not a `char`). Entity's constructor sets `this.sprite = symbol + " "` (2 chars),
and subclasses override with emojis (also 2 columns). This makes the board visually
twice as wide — a 17-column level occupies 34 terminal columns — giving more room.

Background color matters even for emojis because they don't fully cover the pixel area.
Floor uses `"  "` (two spaces) with green background — no foreground needed.

### Enemy Combat Model
The assignment suggests attacks be exchanged when the player moves onto an enemy.
In Sheep Escape, enemies are solid (the player cannot enter their cell). Instead,
Farmer deals damage when adjacent (distance <= 1) and Sheepdog deals damage when
it patrols onto the player's position. This design makes enemies feel like active
threats that hunt the player, rather than passive obstacles.

---

## 6. Test Strategy

### Black-box Tests
We test behavior without knowing the internal implementation:
- Does ElectricFence deal the correct amount of damage?
- Does it deactivate after exactly 3 hits?
- Can Snare only trigger once?

### What Is Tested (59 tests)
| Test Class            | Count | What Is Tested                                     |
|-----------------------|-------|-----------------------------------------------------|
| PlayerTest            | 12    | Health, heal, damage, isDead, inventory, movement    |
| DungeonTest           | 14    | Movement, collision, entity lookup, game status      |
| ElectricFenceTest     | 6     | Damage, countdown, deactivation after 3 hits         |
| SnareTest             | 5     | Hidden state, activation, one-time damage            |
| FarmerTest            | 6     | Chase range, contact damage, treat distraction       |
| SheepdogTest          | 6     | Route following, cyclic route, contact damage        |
| DungeonLoaderTest     | 10    | File loading, entity placement, dimensions           |

### Compilation and Running
```bash
cd SheepEscape
javac -encoding UTF-8 -d bin src/core/*.java src/entity/*.java src/app/*.java
java -cp bin app.Game

# Tests:
javac -encoding UTF-8 -d bin -cp "lib/junit-platform-console-standalone-6.1.0-RC1.jar" src/core/*.java src/entity/*.java src/app/*.java tests/*.java
java -jar lib/junit-platform-console-standalone-6.1.0-RC1.jar execute -cp bin --scan-classpath
```

---

## 7. Limitations and Shortcomings

- AlarmSensor always has default direction SOUTH (can be extended in level files)
- No save/load functionality
- No sound or animations (pure text-based)
- Farmer AI is a simple "greedy chase" — no pathfinding
- Bombs only damage Fence, not other entity types
- Enemies are solid, so the player cannot move onto them to initiate combat (differs from assignment suggestion — see Section 5, Enemy Combat Model)

---

## 8. Reflection

### What Works Well
- **Separation of Concerns**: Game handles UI, Dungeon handles game mechanics — the two classes know nothing about each other's internal logic
- **Classic game loop**: `play()` follows the render → input → update → check pattern, which is standard in game development
- **Section comments**: `// ==== UI METHODS ====` provides a quick overview in longer files
- **Named constants**: `Direction.NORTH`, `Direction.SOUTH` instead of magic numbers
- **MazeGenerator helper methods**: `pickPosition()`, `findCorridor()` etc. make the code reusable and readable

### What a Professional Would Improve
- **Long methods**: `play()` and `placeEntities()` are both ~120 lines — should be broken into smaller methods
- **Broad exceptions**: `throws Exception` and empty `catch (Exception e) {}` silently swallow errors — professionals use specific exception types
- **instanceof chains**: `useItem()` uses `if (item instanceof HayItem) ... else if (item instanceof BombItem)` — a shared `Usable` interface would be cleaner OOP
- **Magic numbers**: MazeGenerator uses numbers like `3/4`, `3`, `4`, `5` directly — should be named constants
- **Inconsistent types**: MazeGenerator uses `int[]` arrays internally but `Position` objects externally

These improvements are "nice to have" — the code is functional, readable and well-structured for a student project.

### What Was Difficult
- **Flicker-free rendering** — the hardest problem. Each fix revealed a deeper issue: many print calls → PrintStream's hidden 8 KB buffer → dual output streams on the same file descriptor → variable frame height. Required understanding Java's I/O stack from PrintStream down to FileDescriptor (see Section 5, "Flicker-Free Rendering")
- **ConcurrentModificationException in tick()** — entities removing themselves during iteration required a snapshot pattern that was not immediately obvious
- **Render ordering for level 4 darkness** — enemies disappeared when drawn before the dark overlay, requiring a multi-layer rendering approach
- **Emoji rendering across platforms** — emoji width varies between terminals, requiring careful 2-column cell sizing in Canvas
- **Balancing the maze generator** — a pure DFS maze was too narrow, requiring the wall-removal step to open up 75% of inner walls
- **Frame height vs terminal size** — total output exceeded VS Code terminal height (~25 lines), pushing the board off-screen. Required condensing stats, messages (5→3) and controls to fit within 24 lines

### What I Learned
- **Inheritance and polymorphism in practice** — Dungeon.move() calling onStep() without knowing the type was the moment OOP "clicked"
- **The importance of encapsulation** — keeping Dungeon's internal state private forced a clean API (getEntityAt, isSolidAt) that made the code easier to reason about
- **StringBuilder as a batching pattern** — the same principle applies to database operations, network calls, and other I/O in real-world Java systems
- **The value of automated testing** — 59 tests gave confidence to refactor entity behavior without breaking existing functionality

---

## 9. Reused from Assignment 4

Three classes are reused:
- `Position.java` — nearly unchanged (added distanceTo())
- `Direction.java` — unchanged
- `Color.java` — extended with new color constants

Canvas replaces BoardDisplay, Entity replaces BoardElement,
Dungeon replaces Board, and Game is rewritten from scratch.

---

## Appendix A: Questions and Answers on Code Comprehension

### A.1 Color.java and Rendering

**What do fg and bg mean?**
Foreground (text color/sprite color) and background (background color). The terminal has two color layers per cell.

**What "text" is referred to in colorCode()?**
Sprites — e.g. "👨", "🐕", "##", "  ". The terminal treats all content as text, including emojis.

**Why a background, if emojis fill two cells?**
Emojis don't cover the entire pixel area of the cell — the background shows through at the edges. Floor uses no foreground at all, only background color ("  " = two spaces).

**What is `\033[48;2;%d;%d;%d;38;2;%d;%d;%dm`?**
An ANSI escape sequence. `\033[` starts the control code, `48;2;R;G;B` sets background, `38;2;R;G;B` sets foreground, `m` terminates. The terminal interprets this as a color instruction for all text that follows.

**Is colorCode() a class?**
No, it is a static method in the Color class. It can be called directly on the class: `Color.colorCode(bg, fg)` — without creating an instance.

### A.2 Canvas.java and StringBuilder

**What is a "sprite"?**
The visual representation of an entity in the terminal. E.g. "👨" for Farmer, "##" for StoneWall, "  " for Floor. Everything on the board has a sprite — it's the string drawn in the cell.

**What is void?**
It means the method does not return anything. It performs its task and is done — no value is sent back.

**Is StringBuilder built into Java?**
Yes, it is a built-in class from java.lang. It is used to build long strings efficiently — instead of many `+` concatenations, it collects everything in one buffer.

**Why StringBuilder instead of System.out.print()?**
With System.out.print() per cell, the terminal would receive hundreds of small calls per frame and draw line by line — visible flicker. StringBuilder builds the entire image in memory, and `System.out.print(sb.toString())` sends it all in one call. The assignment does not mention StringBuilder — it suggests clearConsole() + print. StringBuilder is a deliberate design choice for a better user experience.

**Where is StringBuilder stored?**
It is a buffer in memory — a local variable in show(). It is created, filled with append(), used with toString(), and disappears when the method ends. The garbage collector cleans up.

**How large can the string get?**
For the largest board (29x17): approximately 32 characters per cell x 493 cells = approximately 16,000-17,000 characters per frame. That is nothing for Java.

**What does `append` mean?**
"Add to the end". `sb.append("text")` adds the string to the end of what is already in the buffer. The board is built piece by piece — line 1, then line 2, etc. — and in the end the entire image is collected in one long string.

### A.3 printFrame() and Raw I/O

**Why not just use System.out.print()?**
System.out is a PrintStream with an internal BufferedWriter of 8192 characters. Level 4's frame is ~19,000 characters. When the buffer fills mid-frame, PrintStream auto-flushes and the terminal renders a half-finished frame — visible as a "jump". `printFrame()` bypasses PrintStream entirely by writing raw UTF-8 bytes to `FileOutputStream(FileDescriptor.out)` in a single OS-level `write()` call.

**What is FileDescriptor.out?**
Java's representation of the underlying operating system file descriptor for standard output (fd 1 on Unix). `new FileOutputStream(FileDescriptor.out)` gives a raw output stream with no internal buffering — what you write goes directly to the OS.

**Why did beep() cause flicker?**
`Dungeon.beep()` originally wrote `\007` (BEL character) to `System.out`. But frames were sent via `rawOut`. Both streams share the same file descriptor but have separate buffers. The BEL byte could arrive between frame bytes from the OS's perspective, splitting the frame. The fix was a boolean flag: `pendingBeep`. Game checks it, includes `\007` inside the frame StringBuilder, and it's sent as part of the same atomic `write()`.

**Why exactly 3 message lines?**
If the message section has a variable number of lines (0-5), the total frame height changes each turn. When the frame grows taller, the terminal scrolls down; when it shrinks, there's leftover text. Rendering exactly 3 lines every frame keeps the height constant at 24 lines — fits in VS Code's terminal panel.

### A.4 Scanner and Input

**How does input work in the game?**
Game.java uses `java.util.Scanner` to read input. `scanner.nextLine()` blocks until the user presses Enter. The `readInput()` method reads the full line, trims whitespace, and returns the first character. Each move requires a letter + Enter (e.g. "w" + Enter to move north).

**Why Scanner instead of raw input?**
Java's standard library does not support raw keystroke input (reading keys without Enter). Raw input requires native OS calls via external libraries. Scanner keeps the project dependency-free — only standard Java is needed.

**What is `charToDirection()`?**
A method that converts a character ('w', 'a', 's', 'd') into a Direction enum value (NORTH, WEST, SOUTH, EAST). Returns null for unrecognized characters, which the game loop silently ignores.

### A.5 Exception Handling

**What is `Exception e`?**
Java's way of handling errors — it is called **exception handling**. `try` attempts to run the code, and `catch` catches the error if something goes wrong. `Exception` is a class that represents "something went wrong", and `e` is the variable name for the error object Java creates. In Game.java it is used around `readInput()` — if the input stream fails or something unexpected happens, `catch` catches the error and `continue` skips to the next turn in the game loop, instead of crashing the entire game.

### A.6 Game Loop and Input

**Can you quit the game with Q?**
Yes. `key == 'q' || key == 'Q'` checks whether the player pressed lowercase or uppercase Q (`||` means "or"). If so, `playing = false` is set which stops the game loop, and `"Spillet afsluttet."` is printed in the terminal. `continue` skips to the next loop iteration, which stops because `playing` is now `false`.

### A.7 Snapshot Pattern in tick()

**What is the snapshot in Dungeon.tick()?**
When the game calls onTurn() on all entities, an entity can remove itself from the list (e.g. a bomb that explodes). Iterating over a list while modifying it causes ConcurrentModificationException in Java. The solution is to copy the list before looping: `List<Entity> snapshot = new ArrayList<>(entities)`. The loop runs over the copy, but all changes happen to the original.

**How does it know a bomb already exploded?**
The snapshot is a "memory list" of who existed when the turn started. For each entity the loop checks `entities.contains(e)` against the original list. If the bomb exploded and removed itself + nearby fences, those entities are no longer in the original — so the check returns false and they are skipped. The snapshot is never compared to the original; it is only used as a safe list to iterate over.

### A.8 Concepts

**What is Manhattan distance?**
A way to measure distance in a grid — count tiles horizontally + vertically (no diagonals). The name comes from Manhattan's street grid in New York, where you cannot walk diagonally through buildings. In SheepEscape, the Farmer's chase AI uses it to determine whether the player is within range: `distance = |farmer.x - player.x| + |farmer.y - player.y|`. A farmer 3 tiles to the right and 2 tiles down = distance 5.

### A.9 Java vs. Python

**What is Java used for in practice?**
Large backend systems — banks, insurance, airlines — where many concurrent users and high uptime are critical. Android apps and enterprise software (Spring Boot).

**Would SheepEscape be easier in Python?**
Shorter code, but the OOP structure (inheritance, polymorphism, encapsulation) would be more implicit. Java forces you to show the concepts explicitly — better for an exam in object-oriented programming.

**Is StringBuilder an example of Java thinking?**
Yes. The same principle is used in large server systems: collect operations in one batch instead of many small calls. That is the fundamental idea in the Java world — performance through efficient resource management.

# Sheep Escape

Et roguelike terminal-spil skrevet i Java. Du styrer et får (@) der skal flygte fra en indhegning — undgå farmers, hunde og fælder, saml items, og kæmp dig igennem 4 levels mod friheden.

---

## Rapport

**Projektrapporten ligger her: [`doc/RAPPORT.pdf`](doc/RAPPORT.pdf)**

---

## Mappestruktur

```
SheepEscape/
├── src/
│   ├── core/       Grundklasser: Entity, Dungeon, Player, Position, Direction, Color, Canvas
│   ├── entity/     14 entity-subklasser: Floor, Farmer, Sheepdog, BombItem, Gate osv.
│   └── app/        Applikation: Game, DungeonLoader, MazeGenerator
├── tests/          7 JUnit-testfiler (59 tests)
├── levels/         4 level-filer (.txt)
├── lib/            JUnit jar
├── doc/            Rapport (PDF + Markdown), PDF-generatorer
├── web/            Web-version (index.html)
└── KØRSEL.txt      Detaljerede kørselsinstruktioner
```

## Krav

Java JDK 17 eller nyere (`java -version`)

## Kompilér og kør

```bash
cd SheepEscape
javac -encoding UTF-8 -d bin src/core/*.java src/entity/*.java src/app/*.java
java -cp bin app.Game
```

## Kør tests (59 tests)

```bash
javac -encoding UTF-8 -d bin -cp "lib/junit-platform-console-standalone-6.1.0-RC1.jar" src/core/*.java src/entity/*.java src/app/*.java tests/*.java
java -jar lib/junit-platform-console-standalone-6.1.0-RC1.jar execute -cp bin --scan-classpath
```

## Kontroller

| Tast | Handling |
|------|----------|
| w/a/s/d + Enter | Bevæg dig |
| 1-9 + Enter | Brug item fra inventory |
| q + Enter | Afslut |

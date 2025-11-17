
Adventure Engine – CS2212A base skeleton (no external libs)
===========================================================
How to compile & run (from this folder):

  javac -d out $(find src -name "*.java")
  java -cp out edu.uwo.cs2212.engine.Main

This provides a minimal console runner so each teammate can implement
their own Character/NPC logic without waiting for the UI.

Structure
---------
- edu.uwo.cs2212.engine.model: core immutable data structures (Game, Location, GameObject, GameCharacter, etc.)
- edu.uwo.cs2212.engine.rules: UseRule, GiveRule, Selector, Want
- edu.uwo.cs2212.engine.engine: GameState (mutable), Command, CommandDispatcher
- edu.uwo.cs2212.engine.io: GameLoader (currently builds an in-memory sample game; replace with JSON/XML later)
- edu.uwo.cs2212.engine.sample: Example NPC implementation pattern
- edu.uwo.cs2212.engine.Main: console loop demo (type commands)

Next steps
----------
- Swap GameLoader to load from JSON/XML per course spec.
- Build JavaFX/Swing UI that calls CommandDispatcher.
- Each teammate can add their character & rules under sample/ and
  register them in GameLoader.sampleGame().


Mini-games
----------
- Mini-game API is under edu.uwo.cs2212.engine.minigame (MiniGame, MiniGameResult, MiniGameRegistry).
- Rules can trigger mini-games via MiniGameRule (see GameLoader.sampleGame()).
- Console example included: LockpickMiniGame (id: lockpick_crypt).

To try it:
  - pickup obj_lockpicks
  - use obj_locked_chest with @lockpick

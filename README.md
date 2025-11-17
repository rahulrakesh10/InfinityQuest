Infinity Quest - Group 15
Overview

Infinity Quest is a Marvel-inspired adventure desktop game developed in Java using Java Swing for all graphical user interface components. Players control Dylin as they navigate through New York, Asgard, Sokovia, and the Moon, facing classic Marvel characters turned bosses in a quest to collect all five Infinity Stones. Victory is achieved by defeating Thanos in a multi-phase encounter and restoring reality.
Features

    Java-based desktop application with Swing GUI.

​

Point and click gameplay: movement across locations and cursor-based mini-games for boss battles.

Thematic progression: Each world features a distinct boss—Symbiote Spider-Man, Thor, Wanda, and Thanos.

Item-based gating: Keys and items (like Stormbreaker and the Siren) are required to access and defeat particular bosses.

Hints and guidance: Silver Surfer serves as a non-combat helper, providing gameplay and exploration tips.

Modular, maintainable code: MVC design patterns, thorough Javadoc documentation, object composition, and strategy-focused code architecture.

    ​

Installation

Infinity Quest runs as a standalone desktop application on any system with Java installed:

    Install Java: Ensure Java SE 21 or later is installed on your system.

​

Clone the repository:

text
git clone [your_repo_url]
cd InfinityQuest

Build the project:

    Use your preferred Java IDE (e.g., IntelliJ IDEA, Eclipse) or the command line:

text
javac -cp . src/main/java/**/*.java

No extra dependencies are required except Gson (included or documented for saving/loading game state).

    ​

Run the game:

    text
    java -cp . src/main/java/com/group15/InfinityQuestMain

        The program is self-contained—data is saved within its directory or subfolders.

Gameplay

    Start in Toronto, your central hub. Explore available worlds—each unlocks with progression.

    Face bosses in mini-game/puzzle battles themed to each character and location:

        Symbiote Spider-Man (New York): Dodge webs and defeat with the Siren.

        Thor (Asgard): Reflect lightning using Stormbreaker.

        Wanda (Sokovia): Solve logic puzzles for victory.

        Thanos (Moon): Multi-phase battle for final Infinity Stones and win condition.

    Collect items and stones to unlock new stages. Silver Surfer gives hints if needed.

    All actions have intuitive UI feedback—invalid moves yield clear, professional error messages.

    Optional accessibility features and hints available from Silver Surfer.

Design Principles

    Modular architecture: Core classes include Game, Character (Player/Boss/NPC), Location, Item, and MiniGame.

    Strategy pattern: Boss fights are modular mini-games plugged into encounters.

​

Composition over inheritance: Promotes flexibility and maintainability.

Encapsulation: Unified interfaces and separate responsibilities (GameState, DialogueManager, Save/Load).

Pixel-art interface: Retro visual identity, clear screens, and minimal UI clutter.

Documentation: Code is commented with Javadoc, coding styles and conventions are rigorously followed.

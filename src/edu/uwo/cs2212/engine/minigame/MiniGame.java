
package edu.uwo.cs2212.engine.minigame;

import edu.uwo.cs2212.engine.engine.GameState;
import edu.uwo.cs2212.engine.model.Game;

/** A pluggable mini-game. Implementations can be console or GUI. */
public interface MiniGame {
    String id();                                   // unique id
    MiniGameResult play(Game game, GameState state); // run and return result
}

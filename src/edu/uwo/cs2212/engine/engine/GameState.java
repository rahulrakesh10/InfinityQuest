
package edu.uwo.cs2212.engine.engine;

import java.util.*;

/** Mutable runtime state (separate from immutable Game definition). */
public final class GameState {
    public String currentLocationId;
    public final Set<String> inventory = new LinkedHashSet<>();
    public int turnsTaken = 0;
    public int talkIndex = 0; // simple global cycle; in a full impl use per-character pointer
    public final Deque<String> log = new ArrayDeque<>();

    public void addLog(String line) {
        log.addLast(line);
        if (log.size() > 200) log.removeFirst();
    }
}

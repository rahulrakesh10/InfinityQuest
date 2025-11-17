
package edu.uwo.cs2212.engine.rules;

import java.util.*;

public final class GiveRule {
    public final String characterId;
    public final Selector given;
    public final String resultText;
    public final java.util.List<String> objectsToUser;
    public final boolean endsGame;

    public GiveRule(String characterId, Selector given, String resultText, java.util.List<String> objectsToUser, boolean endsGame) {
        this.characterId = characterId;
        this.given = given;
        this.resultText = resultText;
        this.objectsToUser = Collections.unmodifiableList(new ArrayList<>(objectsToUser));
        this.endsGame = endsGame;
    }
}

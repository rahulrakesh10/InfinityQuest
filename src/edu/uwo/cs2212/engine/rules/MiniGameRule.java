
package edu.uwo.cs2212.engine.rules;

import java.util.List;

/** Data rule: using primary (optionally with 'with') launches a mini-game. */
public final class MiniGameRule {
    public final Selector primary;
    public final Selector with; // nullable
    public final String miniGameId;
    public final List<String> rewardObjectIds;
    public final String successText;
    public final String failureText;

    public MiniGameRule(Selector primary, Selector with, String miniGameId,
                        List<String> rewardObjectIds, String successText, String failureText) {
        this.primary = primary;
        this.with = with;
        this.miniGameId = miniGameId;
        this.rewardObjectIds = java.util.List.copyOf(rewardObjectIds);
        this.successText = successText;
        this.failureText = failureText;
    }
}

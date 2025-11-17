
package edu.uwo.cs2212.engine.rules;

import java.util.*;

public final class UseRule {
    public final Selector primary;
    public final Selector with; // nullable
    public final String resultText;
    public final java.util.List<String> producedObjectIds;

    public UseRule(Selector primary, Selector with, String resultText, java.util.List<String> producedObjectIds) {
        this.primary = primary;
        this.with = with;
        this.resultText = resultText;
        this.producedObjectIds = Collections.unmodifiableList(new ArrayList<>(producedObjectIds));
    }
}

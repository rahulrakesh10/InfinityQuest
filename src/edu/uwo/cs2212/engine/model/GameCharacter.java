
package edu.uwo.cs2212.engine.model;

import java.util.*;

/** Characters are non-pickup entities you can Talk to and Give items to. */
public final class GameCharacter {
    private final String id;
    private final String name;
    private final String description;
    private final List<String> phrases; // talk cycling
    private final List<Want> wants;     // what they accept

    public GameCharacter(String id, String name, String description,
                         List<String> phrases, List<Want> wants) {
        this.id = Objects.requireNonNull(id);
        this.name = Objects.requireNonNull(name);
        this.description = Objects.requireNonNull(description);
        this.phrases = Collections.unmodifiableList(new ArrayList<>(phrases));
        this.wants = Collections.unmodifiableList(new ArrayList<>(wants));
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public List<String> getPhrases() { return phrases; }
    public List<Want> getWants() { return wants; }
}

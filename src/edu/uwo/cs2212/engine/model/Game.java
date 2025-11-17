
package edu.uwo.cs2212.engine.model;

import java.util.*;

/** Immutable game definition (loaded from JSON/XML). */
public final class Game {
    private final String title;
    private final String startMessage;
    private final String startLocationId;
    private final Set<String> endLocationIds;
    private final Integer turnLimit; // nullable

    private final Map<String, Location> locations;
    private final Map<String, GameObject> objects;
    private final Map<String, GameCharacter> characters;

    private final List<edu.uwo.cs2212.engine.rules.UseRule> useRules;
    private final List<edu.uwo.cs2212.engine.rules.GiveRule> giveRules;
    private final List<edu.uwo.cs2212.engine.rules.MiniGameRule> miniGameRules;

    public Game(String title, String startMessage, String startLocationId, Set<String> endLocationIds,
                Integer turnLimit, Map<String, Location> locations, Map<String, GameObject> objects,
                Map<String, GameCharacter> characters,
                List<edu.uwo.cs2212.engine.rules.UseRule> useRules,
                List<edu.uwo.cs2212.engine.rules.GiveRule> giveRules,
                List<edu.uwo.cs2212.engine.rules.MiniGameRule> miniGameRules) {
        this.title = Objects.requireNonNull(title);
        this.startMessage = Objects.requireNonNull(startMessage);
        this.startLocationId = Objects.requireNonNull(startLocationId);
        this.endLocationIds = Collections.unmodifiableSet(new HashSet<>(endLocationIds));
        this.turnLimit = turnLimit;
        this.locations = Collections.unmodifiableMap(new HashMap<>(locations));
        this.objects = Collections.unmodifiableMap(new HashMap<>(objects));
        this.characters = Collections.unmodifiableMap(new HashMap<>(characters));
        this.useRules = Collections.unmodifiableList(new ArrayList<>(useRules));
        this.giveRules = Collections.unmodifiableList(new ArrayList<>(giveRules));
        this.miniGameRules = Collections.unmodifiableList(new ArrayList<>(miniGameRules));
    }

    public String getTitle() { return title; }
    public String getStartMessage() { return startMessage; }
    public String getStartLocationId() { return startLocationId; }
    public Set<String> getEndLocationIds() { return endLocationIds; }
    public Integer getTurnLimit() { return turnLimit; }
    public Map<String, Location> getLocations() { return locations; }
    public Map<String, GameObject> getObjects() { return objects; }
    public Map<String, GameCharacter> getCharacters() { return characters; }
    public List<edu.uwo.cs2212.engine.rules.UseRule> getUseRules() { return useRules; }
    public List<edu.uwo.cs2212.engine.rules.GiveRule> getGiveRules() { return giveRules; }
    public List<edu.uwo.cs2212.engine.rules.MiniGameRule> getMiniGameRules() { return miniGameRules; }
}

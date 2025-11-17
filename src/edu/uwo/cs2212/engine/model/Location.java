
package edu.uwo.cs2212.engine.model;

import java.util.*;

/** Location in the world; holds visible object ids and connections. */
public final class Location {
    private final String id;
    private final String name;
    private final String description;
    private final String imagePath;
    private final List<String> objectIds;  // visible here
    private final List<String> characterIds; // characters present
    private final List<Connection> connections;

    public Location(String id, String name, String description, String imagePath,
                    List<String> objectIds, List<String> characterIds, List<Connection> connections) {
        this.id = Objects.requireNonNull(id);
        this.name = Objects.requireNonNull(name);
        this.description = Objects.requireNonNull(description);
        this.imagePath = imagePath;
        this.objectIds = new ArrayList<>(objectIds);
        this.characterIds = new ArrayList<>(characterIds);
        this.connections = new ArrayList<>(connections);
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getImagePath() { return imagePath; }
    public List<String> getObjectIds() { return Collections.unmodifiableList(objectIds); }
    public List<String> getCharacterIds() { return Collections.unmodifiableList(characterIds); }
    public List<Connection> getConnections() { return Collections.unmodifiableList(connections); }

    // Mutable helpers for runtime reveal/add/drop operations
    public void addObject(String objectId) { this.objectIds.add(objectId); }
    public boolean removeObject(String objectId) { return this.objectIds.remove(objectId); }
    public void addCharacter(String characterId) { this.characterIds.add(characterId); }
    public boolean removeCharacter(String characterId) { return this.characterIds.remove(characterId); }
}

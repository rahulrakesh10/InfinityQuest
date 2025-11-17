
package edu.uwo.cs2212.engine.model;

import java.util.*;

public final class GameObject {
    private final String id;
    private final String name;
    private final String description;
    private final boolean canPickUp;
    private final Set<String> attributes;
    private final List<String> containedObjectIds;

    public GameObject(String id, String name, String description, boolean canPickUp,
                      Set<String> attributes, List<String> containedObjectIds) {
        this.id = Objects.requireNonNull(id);
        this.name = Objects.requireNonNull(name);
        this.description = Objects.requireNonNull(description);
        this.canPickUp = canPickUp;
        this.attributes = Collections.unmodifiableSet(new HashSet<>(attributes));
        this.containedObjectIds = Collections.unmodifiableList(new ArrayList<>(containedObjectIds));
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public boolean canPickUp() { return canPickUp; }
    public Set<String> getAttributes() { return attributes; }
    public List<String> getContainedObjectIds() { return containedObjectIds; }
}

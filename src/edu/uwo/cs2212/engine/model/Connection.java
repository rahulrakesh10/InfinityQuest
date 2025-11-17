
package edu.uwo.cs2212.engine.model;

import java.util.Objects;

/** Connection from a location to another location via a user-visible label. */
public final class Connection {
    private final String label;
    private final String targetLocationId;

    public Connection(String label, String targetLocationId) {
        this.label = Objects.requireNonNull(label);
        this.targetLocationId = Objects.requireNonNull(targetLocationId);
    }

    public String getLabel() { return label; }
    public String getTargetLocationId() { return targetLocationId; }
}

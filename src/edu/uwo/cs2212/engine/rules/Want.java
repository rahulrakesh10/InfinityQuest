
package edu.uwo.cs2212.engine.rules;

/** A character want: accept either specific object id or any object with attribute. */
public final class Want {
    public final String objectId;      // nullable
    public final String attribute;     // nullable

    public Want(String objectId, String attribute) {
        this.objectId = objectId;
        this.attribute = attribute;
    }
}


package edu.uwo.cs2212.engine.rules;

/** Match by exact object id OR by attribute name (one of them non-null). */
public final class Selector {
    public final String objectId;
    public final String attribute;

    public Selector(String objectId, String attribute) {
        this.objectId = objectId;
        this.attribute = attribute;
    }

    public static Selector byId(String id) { return new Selector(id, null); }
    public static Selector byAttr(String attr) { return new Selector(null, attr); }
}


package edu.uwo.cs2212.engine.minigame;

import java.util.List;

public final class MiniGameResult {
    public final boolean success;
    public final String message;
    public final List<String> producedObjectIds;

    public MiniGameResult(boolean success, String message, List<String> producedObjectIds) {
        this.success = success;
        this.message = message;
        this.producedObjectIds = java.util.List.copyOf(producedObjectIds);
    }

    public static MiniGameResult ok(String msg, List<String> items) {
        return new MiniGameResult(true, msg, items);
    }
    public static MiniGameResult fail(String msg) {
        return new MiniGameResult(false, msg, java.util.List.of());
    }
}

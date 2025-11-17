
package edu.uwo.cs2212.engine.engine;

public final class CommandResult {
    public final boolean success;
    public final String message;

    private CommandResult(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public static CommandResult ok(String msg) { return new CommandResult(true, msg); }
    public static CommandResult fail(String msg) { return new CommandResult(false, msg); }
}

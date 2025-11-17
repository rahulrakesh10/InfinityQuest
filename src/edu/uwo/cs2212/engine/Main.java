
package edu.uwo.cs2212.engine;

import edu.uwo.cs2212.engine.engine.*;
import edu.uwo.cs2212.engine.io.GameLoader;
import edu.uwo.cs2212.engine.model.*;
import edu.uwo.cs2212.engine.minigame.*;

import java.util.Scanner;

public final class Main {
    public static void main(String[] args) {
        Game game = GameLoader.sampleGame();
        GameState state = new GameState();
        // Register mini-games
        MiniGameRegistry.register(new LockpickMiniGame("lockpick_crypt", 5));
        state.currentLocationId = game.getStartLocationId();

        System.out.println(game.getTitle());
        System.out.println(game.getStartMessage());
        System.out.println("Type: go <label> | pickup <id> | drop <id> | inv | ex <id> | use <id|@attr> [with <id|@attr>] | talk <charId> | give <objId> <charId> | look");

        CommandDispatcher cd = new CommandDispatcher(game, state);
        Scanner sc = new Scanner(System.in);

        printLocation(game, state);

        while (true) {
            System.out.print("> ");
            if (!sc.hasNextLine()) break;
            String line = sc.nextLine().trim();
            if (line.equalsIgnoreCase("quit") || line.equalsIgnoreCase("exit")) break;
            if (line.equalsIgnoreCase("look")) {
                printLocation(game, state);
                continue;
            }
            try {
                String[] toks = line.split("\s+");
                if (toks.length == 0) continue;
                String cmd = toks[0].toLowerCase();
                CommandResult r;
                switch (cmd) {
                    case "go":
                        r = cd.go(joinFrom(toks, 1));
                        break;
                    case "pickup":
                        r = cd.pickUp(toks[1]);
                        break;
                    case "drop":
                        r = cd.drop(toks[1]);
                        break;
                    case "inv":
                        r = cd.inventory();
                        break;
                    case "ex":
                        r = cd.examineObject(toks[1]); // try as object first
                        break;
                    case "talk":
                        r = cd.talk(toks[1]);
                        break;
                    case "give":
                        r = cd.give(toks[1], toks[2]);
                        break;
                    case "use":
                        if (toks.length >= 4 && toks[2].equalsIgnoreCase("with")) {
                            r = cd.use(toks[1], toks[3]);
                        } else if (toks.length >= 2) {
                            r = cd.use(toks[1], null);
                        } else r = CommandResult.fail("Usage: use <id|@attr> [with <id|@attr>]");
                        break;
                    default:
                        r = CommandResult.fail("Unknown command.");
                }
                System.out.println(r.message);
                if (game.getEndLocationIds().contains(state.currentLocationId)) {
                    System.out.println("Game ended. Turns: " + state.turnsTaken);
                    break;
                }
                if (game.getTurnLimit() != null && state.turnsTaken >= game.getTurnLimit()) {
                    System.out.println("You ran out of time. Turns: " + state.turnsTaken + "/" + game.getTurnLimit());
                    break;
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
        sc.close();
        System.out.println("Bye.");
    }

    private static void printLocation(Game game, GameState state) {
        Location loc = game.getLocations().get(state.currentLocationId);
        System.out.println("== " + loc.getName() + " ==");
        System.out.println(loc.getDescription());
        if (!loc.getConnections().isEmpty()) {
            System.out.print("Connections: ");
            boolean first = true;
            for (var c : loc.getConnections()) {
                if (!first) System.out.print(", ");
                System.out.print(c.getLabel());
                first = false;
            }
            System.out.println();
        }
        if (!loc.getObjectIds().isEmpty()) {
            System.out.print("Here: ");
            boolean first = true;
            for (var oid : loc.getObjectIds()) {
                if (!first) System.out.print(", ");
                System.out.print(game.getObjects().get(oid).getName() + "(" + oid + ")");
                first = false;
            }
            System.out.println();
        }
        if (!loc.getCharacterIds().isEmpty()) {
            System.out.print("You see: ");
            boolean first = true;
            for (var cid : loc.getCharacterIds()) {
                if (!first) System.out.print(", ");
                System.out.print(game.getCharacters().get(cid).getName() + "(" + cid + ")");
                first = false;
            }
            System.out.println();
        }
    }

    private static String joinFrom(String[] arr, int idx) {
        StringBuilder sb = new StringBuilder();
        for (int i = idx; i < arr.length; i++) {
            if (i > idx) sb.append(' ');
            sb.append(arr[i]);
        }
        return sb.toString();
    }
}

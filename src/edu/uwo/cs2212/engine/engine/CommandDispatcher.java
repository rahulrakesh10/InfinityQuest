
package edu.uwo.cs2212.engine.engine;

import edu.uwo.cs2212.engine.model.*;
import edu.uwo.cs2212.engine.rules.*;

import java.util.*;

public final class CommandDispatcher {
    private final Game game;
    private final GameState state;

    public CommandDispatcher(Game game, GameState state) {
        this.game = game;
        this.state = state;
    }

    public CommandResult go(String connectionLabel) {
        Location loc = game.getLocations().get(state.currentLocationId);
        for (Connection c : loc.getConnections()) {
            if (c.getLabel().equalsIgnoreCase(connectionLabel)) {
                state.currentLocationId = c.getTargetLocationId();
                state.turnsTaken++;
                Location nloc = game.getLocations().get(state.currentLocationId);
                String msg = "You arrive at " + nloc.getName() + ": " + nloc.getDescription();
                if (game.getEndLocationIds().contains(state.currentLocationId)) {
                    msg += " [END]";
                }
                state.addLog(msg);
                return CommandResult.ok(msg);
            }
        }
        state.turnsTaken++;
        return CommandResult.fail("You cannot go via '"+connectionLabel+"'.");
    }

    public CommandResult pickUp(String objectId) {
        Location loc = game.getLocations().get(state.currentLocationId);
        if (!loc.getObjectIds().contains(objectId)) {
            state.turnsTaken++;
            return CommandResult.fail("You don't see that here.");
        }
        GameObject obj = game.getObjects().get(objectId);
        if (obj == null || !obj.canPickUp()) {
            state.turnsTaken++;
            return CommandResult.fail("You cannot pick that up.");
        }
        loc.removeObject(objectId);
        state.inventory.add(objectId);
        state.turnsTaken++;
        String msg = "Picked up " + obj.getName() + ".";
        state.addLog(msg);
        return CommandResult.ok(msg);
    }

    public CommandResult drop(String objectId) {
        if (!state.inventory.contains(objectId)) {
            state.turnsTaken++;
            return CommandResult.fail("It's not in your inventory.");
        }
        Location loc = game.getLocations().get(state.currentLocationId);
        state.inventory.remove(objectId);
        loc.addObject(objectId);
        state.turnsTaken++;
        String msg = "Dropped " + game.getObjects().get(objectId).getName() + ".";
        state.addLog(msg);
        return CommandResult.ok(msg);
    }

    public CommandResult inventory() {
        StringBuilder sb = new StringBuilder("Inventory: ");
        if (state.inventory.isEmpty()) sb.append("(empty)");
        else {
            boolean first = true;
            for (String id : state.inventory) {
                if (!first) sb.append(", ");
                sb.append(game.getObjects().get(id).getName());
                first = false;
            }
        }
        state.turnsTaken++;
        String msg = sb.toString();
        state.addLog(msg);
        return CommandResult.ok(msg);
    }

    public CommandResult examineObject(String objectId) {
        Location loc = game.getLocations().get(state.currentLocationId);
        boolean here = loc.getObjectIds().contains(objectId) || state.inventory.contains(objectId);
        if (!here) {
            state.turnsTaken++;
            return CommandResult.fail("You don't have or see that.");
        }
        GameObject obj = game.getObjects().get(objectId);
        // Reveal contained objects into current location:
        for (String cid : obj.getContainedObjectIds()) {
            if (!loc.getObjectIds().contains(cid)) {
                loc.addObject(cid);
            }
        }
        state.turnsTaken++;
        String msg = obj.getDescription();
        state.addLog(msg);
        return CommandResult.ok(msg);
    }

    public CommandResult examineCharacter(String characterId) {
        Location loc = game.getLocations().get(state.currentLocationId);
        if (!loc.getCharacterIds().contains(characterId)) {
            state.turnsTaken++;
            return CommandResult.fail("You don't see them here.");
        }
        GameCharacter ch = game.getCharacters().get(characterId);
        state.turnsTaken++;
        state.addLog(ch.getDescription());
        return CommandResult.ok(ch.getDescription());
    }

    private boolean matches(Selector sel, GameObject obj) {
        if (sel == null) return false;
        if (sel.objectId != null) {
            return sel.objectId.equals(obj.getId());
        }
        if (sel.attribute != null) {
            return obj.getAttributes().contains(sel.attribute);
        }
        return false;
    }

    public CommandResult use(String primaryIdOrAttr, String withIdOrAttrOrNull) {
        // Resolve to selectors
        Selector prim = primaryIdOrAttr.startsWith("@")
                ? Selector.byAttr(primaryIdOrAttr.substring(1))
                : Selector.byId(primaryIdOrAttr);
        Selector with = null;
        if (withIdOrAttrOrNull != null) {
            with = withIdOrAttrOrNull.startsWith("@")
                    ? Selector.byAttr(withIdOrAttrOrNull.substring(1))
                    : Selector.byId(withIdOrAttrOrNull);
        }

        Location loc = game.getLocations().get(state.currentLocationId);

        // Determine scope (if any operand is in location, results go to location)
        boolean anyInLocation = false;
        if (prim.objectId != null && loc.getObjectIds().contains(prim.objectId)) anyInLocation = true;
        if (with != null && with.objectId != null && loc.getObjectIds().contains(with.objectId)) anyInLocation = true;

        // Find matching rule
        for (UseRule r : game.getUseRules()) {
            // Match primary
            GameObject primObj = (prim.objectId != null) ? game.getObjects().get(prim.objectId) : null;
            GameObject withObj = (with != null && with.objectId != null) ? game.getObjects().get(with.objectId) : null;

            boolean primaryMatches = false;
            if (prim.objectId != null && primObj != null) primaryMatches = matches(r.primary, primObj);
            else if (prim.attribute != null) {
                // try to match any visible object/inventory item with that attribute
                for (String id : visibleOrInventory(loc)) {
                    GameObject cand = game.getObjects().get(id);
                    if (cand.getAttributes().contains(prim.attribute) && matches(r.primary, cand)) {
                        primObj = cand;
                        primaryMatches = true;
                        break;
                    }
                }
            }

            if (!primaryMatches) continue;

            boolean withMatches = (r.with == null && with == null);
            if (!withMatches) {
                if (with == null || r.with == null) continue;
                if (with.objectId != null && withObj != null) withMatches = matches(r.with, withObj);
                else if (with.attribute != null) {
                    for (String id : visibleOrInventory(loc)) {
                        GameObject cand = game.getObjects().get(id);
                        if (cand.getAttributes().contains(with.attribute) && matches(r.with, cand)) {
                            withObj = cand;
                            withMatches = true;
                            break;
                        }
                    }
                }
            }

            if (primaryMatches && withMatches) {
                // Remove used objects if present
                if (primObj != null) {
                    loc.removeObject(primObj.getId());
                    state.inventory.remove(primObj.getId());
                }
                if (withObj != null) {
                    loc.removeObject(withObj.getId());
                    state.inventory.remove(withObj.getId());
                }

                // Place produced objects
                for (String pid : r.producedObjectIds) {
                    if (anyInLocation) loc.addObject(pid);
                    else state.inventory.add(pid);
                }

                state.turnsTaken++;
                state.addLog(r.resultText);
                return CommandResult.ok(r.resultText);
            }
        }


            // Mini-game fallback: try matching a MiniGameRule
            for (edu.uwo.cs2212.engine.rules.MiniGameRule mr : game.getMiniGameRules()) {
                GameObject primObj = (prim.objectId != null) ? game.getObjects().get(prim.objectId) : null;
                GameObject withObj = (with != null && with.objectId != null) ? game.getObjects().get(with.objectId) : null;
                GameCharacter withChar = null;
                // Check if "with" is a character that's in the current location
                if (with != null && with.objectId != null && loc.getCharacterIds().contains(with.objectId)) {
                    withChar = game.getCharacters().get(with.objectId);
                }

                boolean primaryMatches = false;
                if (prim.objectId != null && primObj != null) primaryMatches = matches(mr.primary, primObj);
                else if (prim.attribute != null) {
                    for (String id : visibleOrInventory(loc)) {
                        GameObject cand = game.getObjects().get(id);
                        if (cand != null && cand.getAttributes().contains(prim.attribute) && matches(mr.primary, cand)) {
                            primObj = cand; primaryMatches = true; break;
                        }
                    }
                }
                if (!primaryMatches) continue;

                boolean withMatches = (mr.with == null && with == null);
                if (!withMatches) {
                    if (with == null || mr.with == null) continue;
                    // Check if matching by object ID
                    if (with.objectId != null && withObj != null) {
                        withMatches = matches(mr.with, withObj);
                    }
                    // Check if matching by character ID (for minigame rules)
                    else if (with.objectId != null && withChar != null && mr.with != null && mr.with.objectId != null) {
                        // Direct ID match for characters
                        withMatches = mr.with.objectId.equals(with.objectId);
                    }
                    // Check if matching by attribute
                    else if (with.attribute != null) {
                        for (String id : visibleOrInventory(loc)) {
                            GameObject cand = game.getObjects().get(id);
                            if (cand != null && cand.getAttributes().contains(with.attribute) && matches(mr.with, cand)) {
                                withObj = cand; withMatches = true; break;
                            }
                        }
                    }
                }
                if (!withMatches) continue;

                // Launch mini-game
                edu.uwo.cs2212.engine.minigame.MiniGame mg =
                        edu.uwo.cs2212.engine.minigame.MiniGameRegistry.get(mr.miniGameId);
                if (mg == null) {
                    state.turnsTaken++;
                    return CommandResult.fail("Mini-game not found: " + mr.miniGameId);
                }
                edu.uwo.cs2212.engine.minigame.MiniGameResult res;
                try {
                    res = mg.play(game, state);
                } catch (Exception e) {
                    state.turnsTaken++;
                    e.printStackTrace();
                    return CommandResult.fail("Mini-game error: " + e.getMessage());
                }
                // On success, consume inputs and produce rewards
                if (res.success) {
                    // Remove primary object if it exists
                    if (primObj != null) { 
                        loc.removeObject(primObj.getId()); 
                        state.inventory.remove(primObj.getId()); 
                    }
                    // Remove "with" object if it exists (but not characters - they stay)
                    if (withObj != null) { 
                        loc.removeObject(withObj.getId()); 
                        state.inventory.remove(withObj.getId()); 
                    }
                    // Characters are not removed - they stay in the location
                    java.util.List<String> outIds = res.producedObjectIds.isEmpty() ? mr.rewardObjectIds : res.producedObjectIds;
                    for (String pid : outIds) {
                        if (anyInLocation) loc.addObject(pid); else state.inventory.add(pid);
                    }
                    state.turnsTaken++;
                    String msg = (mr.successText == null || mr.successText.isEmpty()) ? res.message : mr.successText;
                    state.addLog(msg);
                    return CommandResult.ok(msg);
                } else {
                    state.turnsTaken++;
                    String msg = (mr.failureText == null || mr.failureText.isEmpty()) ? res.message : mr.failureText;
                    state.addLog(msg);
                    return CommandResult.fail(msg);
                }
            }

    state.turnsTaken++;
    return CommandResult.fail("Nothing happens.");
}

    private List<String> visibleOrInventory(Location loc) {
        List<String> all = new ArrayList<>();
        all.addAll(loc.getObjectIds());
        all.addAll(state.inventory);
        return all;
    }

    public CommandResult talk(String characterId) {
        Location loc = game.getLocations().get(state.currentLocationId);
        if (!loc.getCharacterIds().contains(characterId)) {
            state.turnsTaken++;
            return CommandResult.fail("They're not here.");
        }
        GameCharacter ch = game.getCharacters().get(characterId);
        String msg;
        if (ch.getPhrases().isEmpty()) msg = "They have nothing to say.";
        else {
            int idx = state.talkIndex % ch.getPhrases().size();
            msg = ch.getPhrases().get(idx);
            state.talkIndex++;
        }
        state.turnsTaken++;
        state.addLog(msg);
        return CommandResult.ok(msg);
    }

    public CommandResult give(String objectId, String characterId) {
        Location loc = game.getLocations().get(state.currentLocationId);
        if (!state.inventory.contains(objectId)) {
            state.turnsTaken++;
            return CommandResult.fail("You don't have that.");
        }
        if (!loc.getCharacterIds().contains(characterId)) {
            state.turnsTaken++;
            return CommandResult.fail("They're not here.");
        }
        GameCharacter ch = game.getCharacters().get(characterId);
        GameObject obj = game.getObjects().get(objectId);

        for (GiveRule gr : game.getGiveRules()) {
            if (!gr.characterId.equals(characterId)) continue;
            boolean matches = false;
            if (gr.given.objectId != null) matches = gr.given.objectId.equals(objectId);
            else if (gr.given.attribute != null) matches = obj.getAttributes().contains(gr.given.attribute);

            if (matches) {
                state.inventory.remove(objectId);
                for (String add : gr.objectsToUser) state.inventory.add(add);
                state.turnsTaken++;
                String msg = gr.resultText + (gr.endsGame ? " [END]" : "");
                state.addLog(msg);
                return CommandResult.ok(msg);
            }
        }
        state.turnsTaken++;
        return CommandResult.fail("They don't need that.");
    }
}

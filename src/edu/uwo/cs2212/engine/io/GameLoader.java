
package edu.uwo.cs2212.engine.io;

import edu.uwo.cs2212.engine.model.*;
import edu.uwo.cs2212.engine.rules.*;
import java.util.*;
import java.util.*;

/** Placeholder loader: builds an in-memory sample game. Replace with JSON/XML loader. */
public final class GameLoader {
    public static Game sampleGame() {
        Map<String, GameObject> objects = new LinkedHashMap<>();
        Map<String, GameCharacter> characters = new LinkedHashMap<>();
        Map<String, Location> locations = new LinkedHashMap<>();
        List<UseRule> useRules = new ArrayList<>();
        List<GiveRule> giveRules = new ArrayList<>();
        List<MiniGameRule> miniGameRules = new ArrayList<>();

        // Objects
        GameObject pole = new GameObject("obj_pole", "Pole", "A long wooden pole.", true,
                set("long"), List.of());
        GameObject coffin = new GameObject("obj_coffin", "Coffin",
                "The lid is stuck; something long could pry it open.", false, set(), List.of());
        GameObject garlic = new GameObject("obj_garlic", "Garlic", "Pungent cloves.", true, set("repellent"), List.of());
        GameObject passage = new GameObject("obj_passage_north", "Passage North", "A dark passage heading north.", false, set(), List.of());
        objects.put(pole.getId(), pole);
        objects.put(coffin.getId(), coffin);
        objects.put(garlic.getId(), garlic);
        objects.put(passage.getId(), passage);
        GameObject lockedChest = new GameObject("obj_locked_chest", "Locked Chest", "It has a tricky lock.", false, set(), List.of());
        GameObject openChest = new GameObject("obj_open_chest", "Open Chest", "The lid is open.", false, set(), List.of());
        GameObject picks = new GameObject("obj_lockpicks", "Lockpicks", "A set of delicate picks.", true, set("lockpick"), List.of());
        objects.put(lockedChest.getId(), lockedChest);
        objects.put(openChest.getId(), openChest);
        objects.put(picks.getId(), picks);


        // Character
        GameCharacter patient = new GameCharacter("char_patient", "Bedridden Stranger", "Parched and weak.",
                List.of("I'm so thirsty...", "Do you have water?"), List.of(new Want("obj_cup_water", null)));
        characters.put(patient.getId(), patient);

        // Locations
        Location crypt = new Location("loc_crypt", "Crypt", "A stone room; a sealed coffin rests here.",
                "images/crypt.png", new ArrayList<>(List.of("obj_coffin", "obj_pole", "obj_locked_chest", "obj_lockpicks")), new ArrayList<>(), new ArrayList<>());
        Location tunnel = new Location("loc_tunnel", "Hidden Tunnel", "A narrow passage leading north.",
                "images/tunnel.png", new ArrayList<>(), new ArrayList<>(),
                new ArrayList<>(List.of(new Connection("north", "loc_outside"))));
        Location outside = new Location("loc_outside", "Outside", "Fresh air at last!", "images/outside.png",
                new ArrayList<>(), new ArrayList<>(), new ArrayList<>());

        locations.put(crypt.getId(), crypt);
        locations.put(tunnel.getId(), tunnel);
        locations.put(outside.getId(), outside);

        // Use rules
        useRules.add(new UseRule(Selector.byAttr("long"), Selector.byId("obj_coffin"),
                "You pry open the coffin. A passage is revealed.", List.of("obj_passage_north")));
        // Using passage creates a connection to tunnel (in a complete system, you'd represent connection objects)
        // For this sample, we simulate by letting GO recognize the label "passage" via adding a connection on examine:
        // (Left for students to extend.)

        // Mini-game rule: use lockpicks (@lockpick) with locked chest -> open chest
        miniGameRules.add(new MiniGameRule(
                Selector.byId("obj_locked_chest"),
                Selector.byAttr("lockpick"),
                "lockpick_crypt",
                List.of("obj_open_chest"),
                "The lock yields with a click; the chest is now open.",
                "You fail to pick the lock."
        ));

        // Give rules (example)
        giveRules.add(new GiveRule("char_patient", Selector.byId("obj_cup_water"),
                "“Thank you.” They hand you a brass key.", List.of("obj_brass_key"), false));

        return new Game(
                "Crypt Escape",
                "You awake in a crypt...",
                "loc_crypt",
                new HashSet<>(Set.of("loc_outside")),
                50,
                locations,
                objects,
                characters,
                useRules,
                giveRules,
                miniGameRules
        );
    }

    private static Set<String> set(String... vals) {
        return new LinkedHashSet<>(Arrays.asList(vals));
    }
}

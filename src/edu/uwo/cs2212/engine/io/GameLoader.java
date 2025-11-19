
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
        
        // Thor level objects
        GameObject stormbreakerChest = new GameObject("obj_stormbreaker_chest", "Ancient Chest", 
                "An ornate chest with Asgardian runes. It seems to contain something powerful.", false, set(), 
                List.of("obj_stormbreaker"));
        GameObject stormbreaker = new GameObject("obj_stormbreaker", "Stormbreaker", 
                "The legendary axe of Thor. It crackles with lightning energy. You'll need this to face Thor.", 
                true, set("stormbreaker", "weapon"), List.of());
        objects.put(stormbreakerChest.getId(), stormbreakerChest);
        objects.put(stormbreaker.getId(), stormbreaker);
        
        // Infinity Stone (reward for defeating Thor)
        GameObject infinityStone2 = new GameObject("obj_infinity_stone_2", "Infinity Stone (Power)", 
                "A glowing Infinity Stone. You've collected the second stone!", true, set("infinity_stone"), List.of());
        objects.put(infinityStone2.getId(), infinityStone2);


        // Characters
        // Dylin - The main character (player)
        GameCharacter dylin = new GameCharacter("char_dylin", "Dylin", 
                "You are Dylin, a brave adventurer on a quest to collect the Infinity Stones and defeat Thanos.",
                List.of("I must collect all the Infinity Stones!", 
                        "Thanos must be stopped!",
                        "I'll use my wits and courage to save the universe."),
                List.of());
        characters.put(dylin.getId(), dylin);
        
        // Silver Surfer - helper NPC
        GameCharacter silverSurfer = new GameCharacter("char_silver_surfer", "Silver Surfer", 
                "A cosmic entity who can provide guidance and hints on your quest.",
                List.of("Welcome, Dylin. I can help you on your quest.", 
                        "Click on objects to interact with them.",
                        "Use items together to solve puzzles.",
                        "Collect all the Infinity Stones to defeat Thanos!",
                        "Each world has a boss you must defeat.",
                        "New York, Asgard, Sokovia, and the Moon await you."),
                List.of());
        characters.put(silverSurfer.getId(), silverSurfer);
        
        // Patient (keeping for now, can be removed later)
        GameCharacter patient = new GameCharacter("char_patient", "Bedridden Stranger", "Parched and weak.",
                List.of("I'm so thirsty...", "Do you have water?"), List.of(new Want("obj_cup_water", null)));
        characters.put(patient.getId(), patient);
        
        // Thor - Boss character
        GameCharacter thor = new GameCharacter("char_thor", "Thor", 
                "The God of Thunder. He stands ready for battle, wielding Mjolnir. You must defeat him to proceed.",
                List.of("You dare challenge the God of Thunder?", 
                        "Face me in battle, mortal!",
                        "Only those worthy can pass!",
                        "Let the lightning decide your fate!"),
                List.of());
        characters.put(thor.getId(), thor);

        // Locations
        // New York - Symbiote Spider-Man boss
        Location newYork = new Location("loc_new_york", "New York", 
                "The streets of New York. Symbiote Spider-Man lurks here. You'll need the Siren to defeat him.",
                "images/Stage1.png", 
                new ArrayList<>(), 
                new ArrayList<>(), 
                new ArrayList<>(List.of(new Connection("back", "loc_toronto"))));
        
        // Asgard locations - Thor level
        // Asgard Hall - starting location in Asgard
        Location asgardHall = new Location("loc_asgard_hall", "Asgard Hall", 
                "The grand hall of Asgard. Golden pillars reach to the sky. You can explore to find Stormbreaker.",
                "images/Stage2.png", 
                new ArrayList<>(), 
                new ArrayList<>(), 
                new ArrayList<>(List.of(
                    new Connection("back", "loc_toronto"),
                    new Connection("Explore East", "loc_asgard_chest_room")
                )));
        
        // Chest Room - where Stormbreaker is found
        Location asgardChestRoom = new Location("loc_asgard_chest_room", "Asgard Treasure Room", 
                "A hidden chamber filled with ancient Asgardian artifacts. An ornate chest sits in the center.",
                "images/Stage2.png", 
                new ArrayList<>(List.of("obj_stormbreaker_chest")), 
                new ArrayList<>(), 
                new ArrayList<>(List.of(
                    new Connection("back", "loc_asgard_hall"),
                    new Connection("Enter Boss Chamber", "loc_asgard_boss_room")
                )));
        
        // Boss Room - where Thor is fought (requires Stormbreaker to enter)
        Location asgardBossRoom = new Location("loc_asgard_boss_room", "Thor's Arena", 
                "The arena where Thor awaits. Lightning crackles in the air. You must defeat him in battle!",
                "images/Stage2.png", 
                new ArrayList<>(), 
                new ArrayList<>(List.of("char_thor")), 
                new ArrayList<>(List.of(
                    new Connection("back", "loc_asgard_chest_room")
                )));
        
        // Removed old asgard location - use asgardHall directly
        
        // Sokovia - Wanda boss
        Location sokovia = new Location("loc_sokovia", "Sokovia", 
                "The ruins of Sokovia. Wanda's logic puzzles block your path. Solve them to proceed.",
                "images/Stage3.png", 
                new ArrayList<>(), 
                new ArrayList<>(), 
                new ArrayList<>(List.of(new Connection("back", "loc_toronto"))));
        
        // Moon - Thanos boss (final)
        Location moon = new Location("loc_moon", "Moon", 
                "The Moon. Thanos awaits with the Infinity Gauntlet. This is the final battle!",
                "images/Stage4_V1.png", 
                new ArrayList<>(), 
                new ArrayList<>(), 
                new ArrayList<>(List.of(new Connection("back", "loc_toronto"))));
        
        // Starting location - Toronto (Base/Hub)
        Location toronto = new Location("loc_toronto", "Toronto", 
                "You stand in Toronto, your central hub. The CN Tower looms in the distance. Silver Surfer approaches to offer guidance. " +
                "Choose your destination: New York, Asgard, Sokovia, or the Moon.",
                "images/Base.png", 
                new ArrayList<>(List.of("obj_pole", "obj_lockpicks")), 
                new ArrayList<>(List.of("char_silver_surfer")), // Dylin is the player, not an NPC
                new ArrayList<>(List.of(
                    new Connection("New York", "loc_new_york"),
                    new Connection("Asgard", "loc_asgard_hall"),
                    new Connection("Sokovia", "loc_sokovia"),
                    new Connection("Moon", "loc_moon")
                )));
        
        // Old crypt location (keeping for compatibility)
        Location crypt = new Location("loc_crypt", "Crypt", "A stone room; a sealed coffin rests here.",
                "images/Base.png", new ArrayList<>(List.of("obj_coffin", "obj_pole", "obj_locked_chest", "obj_lockpicks")), 
                new ArrayList<>(List.of("char_silver_surfer")), new ArrayList<>());
        Location tunnel = new Location("loc_tunnel", "Hidden Tunnel", "A narrow passage leading north.",
                "images/Stage1.png", new ArrayList<>(), new ArrayList<>(),
                new ArrayList<>(List.of(new Connection("north", "loc_outside"))));
        Location outside = new Location("loc_outside", "Outside", "Fresh air at last!", "images/Final.png",
                new ArrayList<>(), new ArrayList<>(), new ArrayList<>());

        locations.put(toronto.getId(), toronto);
        locations.put(newYork.getId(), newYork);
        locations.put(asgardHall.getId(), asgardHall);
        locations.put(asgardChestRoom.getId(), asgardChestRoom);
        locations.put(asgardBossRoom.getId(), asgardBossRoom);
        locations.put(sokovia.getId(), sokovia);
        locations.put(moon.getId(), moon);
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
        
        // Thor level rules
        // Rule: Examine chest to reveal Stormbreaker (or use chest to open it)
        useRules.add(new UseRule(
                Selector.byId("obj_stormbreaker_chest"),
                null,
                "You open the chest. Inside, you find Stormbreaker, the legendary axe of Thor!",
                List.of("obj_stormbreaker")
        ));
        
        // Mini-game rule: Use Stormbreaker with Thor to start the lightning dodge battle
        // Note: primary is the object being used, with is what it's used with
        miniGameRules.add(new MiniGameRule(
                Selector.byAttr("stormbreaker"),  // Use Stormbreaker (primary)
                Selector.byId("char_thor"),       // with Thor (secondary)
                "lightning_dodge_thor",
                List.of("obj_infinity_stone_2"),
                "You have defeated Thor! The path to Wanda is now unlocked.",
                "Thor's lightning was too powerful. You must try again."
        ));

        // Give rules (example)
        giveRules.add(new GiveRule("char_patient", Selector.byId("obj_cup_water"),
                "“Thank you.” They hand you a brass key.", List.of("obj_brass_key"), false));

        return new Game(
                "Infinity Quest",
                "Welcome, Dylin! You are on a quest to collect all five Infinity Stones and defeat Thanos. " +
                "Navigate through New York, Asgard, Sokovia, and the Moon. Silver Surfer is here to guide you. " +
                "Click on objects, characters, and navigation buttons to interact. Good luck!",
                "loc_toronto",
                new HashSet<>(Set.of("loc_moon")), // Moon is the final location
                null, // No turn limit - this is an exploration/adventure game
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


package edu.uwo.cs2212.engine.minigame;

import java.util.*;

public final class MiniGameRegistry {
    private static final Map<String, MiniGame> REG = new HashMap<>();
    private MiniGameRegistry(){}
    public static void register(MiniGame mg){ REG.put(mg.id(), mg); }
    public static MiniGame get(String id){ return REG.get(id); }
}

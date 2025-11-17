
package edu.uwo.cs2212.engine.minigame;

import edu.uwo.cs2212.engine.engine.GameState;
import edu.uwo.cs2212.engine.model.Game;

import java.util.Random;
import java.util.Scanner;
import java.util.List;

/** Simple console lockpick: guess a 3-digit code within N tries. */
public final class LockpickMiniGame implements MiniGame {
    private final String id;
    private final int maxTries;

    public LockpickMiniGame(String id, int maxTries){
        this.id = id;
        this.maxTries = maxTries;
    }

    @Override
    public String id(){ return id; }

    @Override
    public MiniGameResult play(Game game, GameState state) {
        Random rng = new Random(2212);
        int code = 100 + rng.nextInt(900); // 100..999
        Scanner sc = new Scanner(System.in);
        System.out.println("[Lockpick] Guess the 3-digit code. You have " + maxTries + " tries.");

        for (int t=1; t<=maxTries; t++){
            System.out.print("Try " + t + "/" + maxTries + ": ");
            if (!sc.hasNextLine()) break;
            String in = sc.nextLine().trim();
            if (!in.matches("\\d{3}")) { System.out.println("Enter exactly 3 digits."); t--; continue; }
            int g = Integer.parseInt(in);
            if (g == code) {
                return MiniGameResult.ok("You hear a satisfying click.", List.of());
            }
            int bulls = bulls(code, g);
            System.out.println("Close... digits in correct position: " + bulls);
        }
        return MiniGameResult.fail("The pick snaps. The lock holds.");
    }

    private int bulls(int code, int guess){
        String a = String.valueOf(code), b = String.valueOf(guess);
        int cnt = 0; for (int i=0;i<3;i++) if (a.charAt(i)==b.charAt(i)) cnt++; return cnt;
    }
}

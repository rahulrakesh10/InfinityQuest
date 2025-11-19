package edu.uwo.cs2212.engine.minigame;

import edu.uwo.cs2212.engine.engine.GameState;
import edu.uwo.cs2212.engine.model.Game;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Lightning Reflection Mini-Game: A rhythm-based battle where the player must
 * reflect Thor's lightning bolts back at him by pressing keys in time.
 * Lightning bolts fall from the top, and the player must press the correct key
 * (W, A, S, D) when the bolt reaches the reflection zone to reflect it back.
 * Successfully reflected bolts damage Thor. Missed bolts damage the player.
 */
public final class LightningReflectionMiniGame implements MiniGame {
    private final String id;
    private final int playerMaxHealth;
    private final int bossMaxHealth;
    private final int lightningDamageToPlayer;
    private final int reflectionDamageToBoss;

    public LightningReflectionMiniGame(String id, int playerMaxHealth, int bossMaxHealth,
                                      int lightningDamageToPlayer, int reflectionDamageToBoss) {
        this.id = id;
        this.playerMaxHealth = playerMaxHealth;
        this.bossMaxHealth = bossMaxHealth;
        this.lightningDamageToPlayer = lightningDamageToPlayer;
        this.reflectionDamageToBoss = reflectionDamageToBoss;
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public MiniGameResult play(Game game, GameState state) {
        final ReflectionGameFrame[] frameRef = new ReflectionGameFrame[1];
        
        try {
            // Check if we're on the EDT
            if (javax.swing.SwingUtilities.isEventDispatchThread()) {
                // Already on EDT, create directly
                frameRef[0] = new ReflectionGameFrame(
                    playerMaxHealth, bossMaxHealth, lightningDamageToPlayer, reflectionDamageToBoss);
                frameRef[0].setVisible(true);
            } else {
                // Not on EDT, use invokeAndWait
                javax.swing.SwingUtilities.invokeAndWait(() -> {
                    frameRef[0] = new ReflectionGameFrame(
                        playerMaxHealth, bossMaxHealth, lightningDamageToPlayer, reflectionDamageToBoss);
                    frameRef[0].setVisible(true);
                });
            }
            
            ReflectionGameFrame frame = frameRef[0];
            
            // Wait for game to complete
            while (!frame.isGameOver()) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    frame.dispose();
                    return MiniGameResult.fail("Game interrupted.");
                }
            }
            
            // Get result before disposing
            boolean won = frame.isPlayerWon();
            List<String> rewards = frame.getRewardObjectIds();
            
            // Dispose on EDT
            if (javax.swing.SwingUtilities.isEventDispatchThread()) {
                frame.dispose();
            } else {
                javax.swing.SwingUtilities.invokeLater(() -> {
                    frame.dispose();
                });
            }
            
            if (won) {
                return MiniGameResult.ok("You have defeated Thor! The path to Wanda is now unlocked.", 
                                        rewards);
            } else {
                return MiniGameResult.fail("Thor's lightning was too powerful. You must try again.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (frameRef[0] != null) {
                if (javax.swing.SwingUtilities.isEventDispatchThread()) {
                    frameRef[0].dispose();
                } else {
                    javax.swing.SwingUtilities.invokeLater(() -> {
                        if (frameRef[0] != null) {
                            frameRef[0].dispose();
                        }
                    });
                }
            }
            return MiniGameResult.fail("Mini-game error: " + e.getMessage());
        }
    }

    /**
     * GUI Frame for the Lightning Reflection game
     */
    private static class ReflectionGameFrame extends JFrame {
        private final int playerMaxHealth;
        private final int bossMaxHealth;
        private final int lightningDamageToPlayer;
        private final int reflectionDamageToBoss;
        
        private int playerHealth;
        private int bossHealth;
        private boolean gameOver = false;
        private boolean playerWon = false;
        private List<String> rewardObjectIds = new ArrayList<>();
        
        private GamePanel gamePanel;
        private Random random = new Random();
        private Timer gameTimer;
        
        private List<LightningBolt> lightningBolts = new ArrayList<>();
        private int frameCount = 0;
        private int nextSpawnFrame = 60; // First bolt after 1 second
        private int spawnInterval = 90; // Spawn every 1.5 seconds
        
        // Reflection zone (where player needs to press keys)
        private final int reflectionZoneY = 450;
        private final int reflectionZoneHeight = 80;
        
        // Key indicators
        private char[] keys = {'W', 'A', 'S', 'D'};
        private boolean[] keyPressed = {false, false, false, false};
        private boolean[] keyJustPressed = {false, false, false, false};
        
        public ReflectionGameFrame(int playerMaxHealth, int bossMaxHealth,
                                  int lightningDamageToPlayer, int reflectionDamageToBoss) {
            this.playerMaxHealth = playerMaxHealth;
            this.bossMaxHealth = bossMaxHealth;
            this.lightningDamageToPlayer = lightningDamageToPlayer;
            this.reflectionDamageToBoss = reflectionDamageToBoss;
            
            playerHealth = playerMaxHealth;
            bossHealth = bossMaxHealth;
            
            setTitle("Lightning Reflection - Battle with Thor!");
            setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            setResizable(false);
            setSize(900, 700);
            setLocationRelativeTo(null);
            
            gamePanel = new GamePanel();
            add(gamePanel);
            
            setupControls();
            startGame();
        }
        
        private void setupControls() {
            // Keyboard controls
            gamePanel.setFocusable(true);
            gamePanel.requestFocusInWindow();
            
            gamePanel.addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    char key = Character.toUpperCase(e.getKeyChar());
                    for (int i = 0; i < keys.length; i++) {
                        if (key == keys[i] && !keyPressed[i]) {
                            keyPressed[i] = true;
                            keyJustPressed[i] = true;
                        }
                    }
                }
                
                @Override
                public void keyReleased(KeyEvent e) {
                    char key = Character.toUpperCase(e.getKeyChar());
                    for (int i = 0; i < keys.length; i++) {
                        if (key == keys[i]) {
                            keyPressed[i] = false;
                            keyJustPressed[i] = false;
                        }
                    }
                }
            });
        }
        
        private void checkReflection() {
            // Check all keys that were just pressed
            for (int i = 0; i < keys.length; i++) {
                if (keyJustPressed[i]) {
                    checkReflectionForKey(i);
                    keyJustPressed[i] = false;
                }
            }
        }
        
        private void checkReflectionForKey(int keyIndex) {
            // Check if any lightning bolt is in the reflection zone with matching key
            List<LightningBolt> toRemove = new ArrayList<>();
            boolean reflected = false;
            
            for (LightningBolt bolt : lightningBolts) {
                // Check if bolt is in reflection zone
                if (bolt.y >= reflectionZoneY - 20 && bolt.y <= reflectionZoneY + reflectionZoneHeight + 20) {
                    // Check if key matches
                    if (bolt.keyIndex == keyIndex) {
                        // Reflect it back!
                        bolt.reflected = true;
                        bolt.speed = -bolt.speed; // Reverse direction
                        reflected = true;
                        toRemove.add(bolt);
                        // Damage boss
                        bossHealth -= reflectionDamageToBoss;
                        if (bossHealth <= 0) {
                            bossHealth = 0;
                            endGame(true);
                            return;
                        }
                    }
                }
            }
            
            // If key was pressed but no matching bolt, small penalty (optional)
            if (!reflected && lightningBolts.stream().anyMatch(b -> 
                b.y >= reflectionZoneY - 20 && b.y <= reflectionZoneY + reflectionZoneHeight + 20)) {
                // Player pressed wrong key - small penalty
                // (This makes the game more challenging)
            }
            
            // Remove reflected bolts
            lightningBolts.removeAll(toRemove);
        }
        
        private void startGame() {
            // Game loop timer (60 FPS)
            gameTimer = new Timer(16, e -> {
                if (gameOver) return;
                
                frameCount++;
                updateGame();
                checkReflection();
                gamePanel.repaint();
            });
            gameTimer.start();
        }
        
        private void updateGame() {
            // Spawn lightning bolts
            if (frameCount >= nextSpawnFrame && bossHealth > 0) {
                spawnLightning();
                nextSpawnFrame = frameCount + spawnInterval;
                // Gradually increase difficulty
                if (spawnInterval > 45) {
                    spawnInterval = Math.max(45, spawnInterval - 2);
                }
            }
            
            // Update lightning bolts
            List<LightningBolt> toRemove = new ArrayList<>();
            for (LightningBolt bolt : lightningBolts) {
                bolt.y += bolt.speed;
                
                if (bolt.reflected) {
                    // Reflected bolt going up - remove when it reaches top
                    if (bolt.y < 0) {
                        toRemove.add(bolt);
                    }
                } else {
                    // Falling bolt - check if it passed the reflection zone without being reflected
                    if (bolt.y > reflectionZoneY + reflectionZoneHeight + 50) {
                        // Player missed - damage player
                        playerHealth -= lightningDamageToPlayer;
                        toRemove.add(bolt);
                        if (playerHealth <= 0) {
                            playerHealth = 0;
                            endGame(false);
                            return;
                        }
                    }
                }
            }
            lightningBolts.removeAll(toRemove);
        }
        
        private void spawnLightning() {
            // Random position and random key
            int x = 150 + random.nextInt(600); // Spawn in middle area
            int keyIndex = random.nextInt(keys.length);
            int speed = 3 + random.nextInt(2); // Speed between 3-4
            lightningBolts.add(new LightningBolt(x, 0, keyIndex, speed));
        }
        
        private void endGame(boolean won) {
            gameOver = true;
            playerWon = won;
            if (gameTimer != null) gameTimer.stop();
            
            if (won) {
                rewardObjectIds.add("obj_infinity_stone_2");
            }
            
            // Show result after brief delay
            Timer delayTimer = new Timer(1000, e -> {
                String message = won ? 
                    "Victory! You have defeated Thor! The path to Wanda is now unlocked." :
                    "Defeat! Thor's lightning was too powerful. Try again!";
                JOptionPane.showMessageDialog(this, message, 
                    won ? "Victory!" : "Defeat", 
                    JOptionPane.INFORMATION_MESSAGE);
            });
            delayTimer.setRepeats(false);
            delayTimer.start();
        }
        
        public boolean isGameOver() {
            return gameOver;
        }
        
        public boolean isPlayerWon() {
            return playerWon;
        }
        
        public List<String> getRewardObjectIds() {
            return rewardObjectIds;
        }
        
        private class LightningBolt {
            int x, y;
            int keyIndex; // Which key (W=0, A=1, S=2, D=3) is needed to reflect
            int speed;
            boolean reflected = false;
            int width = 40;
            int height = 60;
            
            LightningBolt(int x, int y, int keyIndex, int speed) {
                this.x = x;
                this.y = y;
                this.keyIndex = keyIndex;
                this.speed = speed;
            }
        }
        
        private class GamePanel extends JPanel {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Draw background (stormy sky)
                GradientPaint gradient = new GradientPaint(0, 0, new Color(20, 20, 40),
                                                          0, getHeight(), new Color(10, 10, 20));
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
                
                // Draw lightning bolts
                for (LightningBolt bolt : lightningBolts) {
                    if (bolt.reflected) {
                        // Reflected bolt (going up) - green/yellow
                        g2d.setColor(new Color(100, 255, 100));
                    } else {
                        // Falling bolt - color based on key
                        Color[] boltColors = {
                            new Color(255, 255, 100), // W - Yellow
                            new Color(100, 255, 255), // A - Cyan
                            new Color(255, 100, 255), // S - Magenta
                            new Color(255, 150, 100)  // D - Orange
                        };
                        g2d.setColor(boltColors[bolt.keyIndex]);
                    }
                    
                    // Draw lightning bolt shape
                    int[] xPoints = {bolt.x, bolt.x + bolt.width/2, bolt.x, bolt.x + bolt.width/2, bolt.x};
                    int[] yPoints = {bolt.y, bolt.y + bolt.height/4, bolt.y + bolt.height/2, 
                                    bolt.y + 3*bolt.height/4, bolt.y + bolt.height};
                    g2d.fillPolygon(xPoints, yPoints, 5);
                    
                    // Draw key indicator above bolt
                    if (!bolt.reflected) {
                        g2d.setColor(Color.WHITE);
                        g2d.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));
                        String keyText = String.valueOf(keys[bolt.keyIndex]);
                        FontMetrics fm = g2d.getFontMetrics();
                        int textX = bolt.x + (bolt.width - fm.stringWidth(keyText)) / 2;
                        int textY = bolt.y - 10;
                        g2d.drawString(keyText, textX, textY);
                    }
                }
                
                // Draw reflection zone (where player needs to press keys)
                g2d.setColor(new Color(100, 200, 255, 100));
                g2d.fillRect(0, reflectionZoneY, getWidth(), reflectionZoneHeight);
                g2d.setColor(new Color(100, 200, 255));
                g2d.setStroke(new BasicStroke(3));
                g2d.drawRect(0, reflectionZoneY, getWidth(), reflectionZoneHeight);
                
                // Draw key indicators in reflection zone
                g2d.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 24));
                int keySpacing = getWidth() / 5;
                for (int i = 0; i < keys.length; i++) {
                    int keyX = keySpacing * (i + 1);
                    int keyY = reflectionZoneY + reflectionZoneHeight / 2;
                    
                    // Draw key background
                    if (keyPressed[i]) {
                        g2d.setColor(new Color(255, 255, 100, 150));
                        g2d.fillOval(keyX - 30, keyY - 30, 60, 60);
                    }
                    
                    g2d.setColor(Color.WHITE);
                    g2d.setStroke(new BasicStroke(2));
                    g2d.drawOval(keyX - 30, keyY - 30, 60, 60);
                    
                    // Draw key letter
                    FontMetrics fm = g2d.getFontMetrics();
                    String keyText = String.valueOf(keys[i]);
                    int textX = keyX - fm.stringWidth(keyText) / 2;
                    int textY = keyY + fm.getAscent() / 2 - 5;
                    g2d.drawString(keyText, textX, textY);
                }
                
                // Draw instructions
                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
                String instruction = "Press W, A, S, or D when lightning reaches the blue zone!";
                FontMetrics fm = g2d.getFontMetrics();
                int textX = (getWidth() - fm.stringWidth(instruction)) / 2;
                g2d.drawString(instruction, textX, reflectionZoneY - 30);
                
                // Draw health bars
                drawHealthBar(g2d, "Player", playerHealth, playerMaxHealth, 20, 20, Color.GREEN);
                drawHealthBar(g2d, "Thor", bossHealth, bossMaxHealth, getWidth() - 220, 20, Color.RED);
                
                // Draw game over message
                if (gameOver) {
                    g2d.setColor(new Color(0, 0, 0, 180));
                    g2d.fillRect(0, 0, getWidth(), getHeight());
                    g2d.setColor(Color.WHITE);
                    g2d.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 48));
                    String msg = playerWon ? "VICTORY!" : "DEFEAT!";
                    fm = g2d.getFontMetrics();
                    int msgX = (getWidth() - fm.stringWidth(msg)) / 2;
                    int msgY = getHeight() / 2;
                    g2d.drawString(msg, msgX, msgY);
                }
            }
            
            private void drawHealthBar(Graphics2D g2d, String name, int currentHealth, int maxHealth, 
                                      int x, int y, Color color) {
                int barWidth = 200;
                int barHeight = 25;
                
                // Background
                g2d.setColor(Color.BLACK);
                g2d.fillRect(x, y, barWidth, barHeight);
                
                // Health fill
                g2d.setColor(color);
                int currentBarWidth = (int) ((double) currentHealth / maxHealth * barWidth);
                g2d.fillRect(x, y, currentBarWidth, barHeight);
                
                // Border
                g2d.setColor(Color.WHITE);
                g2d.setStroke(new BasicStroke(2));
                g2d.drawRect(x, y, barWidth, barHeight);
                
                // Text
                g2d.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
                String text = name + ": " + currentHealth + "/" + maxHealth;
                FontMetrics fm = g2d.getFontMetrics();
                int textX = x + (barWidth - fm.stringWidth(text)) / 2;
                int textY = y + barHeight / 2 + fm.getAscent() / 2 - 2;
                g2d.drawString(text, textX, textY);
            }
        }
    }
}


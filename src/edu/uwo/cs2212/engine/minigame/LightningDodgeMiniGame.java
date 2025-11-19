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
 * Lightning Dodge Mini-Game: Player must dodge falling lightning bolts.
 * Player has a health bar, and successfully dodging lightning damages the boss.
 * Win by depleting the boss's health bar to zero.
 */
public final class LightningDodgeMiniGame implements MiniGame {
    private final String id;
    private final int playerMaxHealth;
    private final int bossMaxHealth;
    private final int lightningDamage;
    private final int dodgeReward;

    public LightningDodgeMiniGame(String id, int playerMaxHealth, int bossMaxHealth, 
                                  int lightningDamage, int dodgeReward) {
        this.id = id;
        this.playerMaxHealth = playerMaxHealth;
        this.bossMaxHealth = bossMaxHealth;
        this.lightningDamage = lightningDamage;
        this.dodgeReward = dodgeReward;
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public MiniGameResult play(Game game, GameState state) {
        LightningDodgeFrame frame = new LightningDodgeFrame(playerMaxHealth, bossMaxHealth, 
                                                           lightningDamage, dodgeReward);
        frame.setVisible(true);
        
        // Wait for game to complete
        while (!frame.isGameOver()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return MiniGameResult.fail("Game interrupted.");
            }
        }
        
        frame.dispose();
        
        if (frame.isPlayerWon()) {
            return MiniGameResult.ok("You have defeated Thor! The path to Wanda is now unlocked.", 
                                    frame.getRewardObjectIds());
        } else {
            return MiniGameResult.fail("Thor's lightning was too powerful. You must try again.");
        }
    }

    /**
     * GUI Frame for the Lightning Dodge game
     */
    private static class LightningDodgeFrame extends JFrame {
        private final int playerMaxHealth;
        private final int bossMaxHealth;
        private final int lightningDamage;
        private final int dodgeReward;
        
        private int playerHealth;
        private int bossHealth;
        private boolean gameOver = false;
        private boolean playerWon = false;
        private List<String> rewardObjectIds = new ArrayList<>();
        
        private GamePanel gamePanel;
        private Random random = new Random();
        private Timer gameTimer;
        private Timer lightningTimer;
        
        private int playerX = 400; // Player position (center of screen)
        private int playerY = 500; // Player at bottom
        private final int playerSize = 40;
        private final int playerSpeed = 8;
        
        private List<LightningBolt> lightningBolts = new ArrayList<>();
        private int frameCount = 0;
        private int lightningSpawnRate = 60; // Spawn every 60 frames (1 second at 60fps)
        
        public LightningDodgeFrame(int playerMaxHealth, int bossMaxHealth, 
                                   int lightningDamage, int dodgeReward) {
            this.playerMaxHealth = playerMaxHealth;
            this.bossMaxHealth = bossMaxHealth;
            this.lightningDamage = lightningDamage;
            this.dodgeReward = dodgeReward;
            
            this.playerHealth = playerMaxHealth;
            this.bossHealth = bossMaxHealth;
            
            setTitle("Lightning Dodge - Defeat Thor!");
            setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
            setSize(800, 600);
            setLocationRelativeTo(null);
            setResizable(false);
            
            gamePanel = new GamePanel();
            add(gamePanel);
            
            // Keyboard controls
            gamePanel.setFocusable(true);
            gamePanel.requestFocus();
            
            setupControls();
            startGame();
        }
        
        private void setupControls() {
            // Keyboard controls for movement
            gamePanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0, false), "left");
            gamePanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_A, 0, false), "left");
            gamePanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0, false), "right");
            gamePanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_D, 0, false), "right");
            
            gamePanel.getActionMap().put("left", new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (!gameOver) {
                        playerX = Math.max(0, playerX - playerSpeed);
                    }
                }
            });
            
            gamePanel.getActionMap().put("right", new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (!gameOver) {
                        playerX = Math.min(800 - playerSize, playerX + playerSpeed);
                    }
                }
            });
            
            // Mouse controls - click to move player
            gamePanel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (!gameOver) {
                        playerX = e.getX() - playerSize / 2;
                        // Keep player in bounds
                        playerX = Math.max(0, Math.min(800 - playerSize, playerX));
                    }
                }
            });
        }
        
        private void startGame() {
            // Game loop timer (60 FPS)
            gameTimer = new Timer(16, e -> {
                if (gameOver) return;
                
                frameCount++;
                updateGame();
                gamePanel.repaint();
            });
            gameTimer.start();
            
            // Lightning spawn timer
            lightningTimer = new Timer(1000, e -> {
                if (!gameOver && bossHealth > 0) {
                    spawnLightning();
                }
            });
            lightningTimer.start();
        }
        
        private void updateGame() {
            // Player movement is handled via key bindings in setupControls()
            
            // Update lightning bolts
            List<LightningBolt> toRemove = new ArrayList<>();
            for (LightningBolt bolt : lightningBolts) {
                bolt.y += bolt.speed;
                
                // Check collision with player
                if (bolt.y + bolt.height > playerY && 
                    bolt.y < playerY + playerSize &&
                    bolt.x + bolt.width > playerX && 
                    bolt.x < playerX + playerSize) {
                    // Player hit!
                    playerHealth -= lightningDamage;
                    toRemove.add(bolt);
                    if (playerHealth <= 0) {
                        playerHealth = 0;
                        endGame(false);
                        return;
                    }
                }
                
                // Remove if off screen
                if (bolt.y > 600) {
                    // Player successfully dodged - damage boss
                    bossHealth -= dodgeReward;
                    if (bossHealth <= 0) {
                        bossHealth = 0;
                        endGame(true);
                        return;
                    }
                    toRemove.add(bolt);
                }
            }
            lightningBolts.removeAll(toRemove);
            
            // Spawn lightning periodically
            if (frameCount % lightningSpawnRate == 0 && bossHealth > 0) {
                spawnLightning();
            }
        }
        
        private void spawnLightning() {
            int x = random.nextInt(800 - 30);
            lightningBolts.add(new LightningBolt(x, 0, 30, 50, 5 + random.nextInt(3)));
        }
        
        private void endGame(boolean won) {
            gameOver = true;
            playerWon = won;
            if (gameTimer != null) gameTimer.stop();
            if (lightningTimer != null) lightningTimer.stop();
            
            if (won) {
                rewardObjectIds.add("obj_infinity_stone_2"); // Second infinity stone
            }
            
            // Show result dialog after a brief delay
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
            int x, y, width, height, speed;
            LightningBolt(int x, int y, int width, int height, int speed) {
                this.x = x;
                this.y = y;
                this.width = width;
                this.height = height;
                this.speed = speed;
            }
        }
        
        private class GamePanel extends JPanel {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
                                    RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Draw background (dark stormy sky)
                g2d.setColor(new Color(20, 20, 40));
                g2d.fillRect(0, 0, getWidth(), getHeight());
                
                // Draw storm clouds
                g2d.setColor(new Color(40, 40, 60));
                g2d.fillOval(100, 50, 200, 80);
                g2d.fillOval(500, 30, 250, 100);
                g2d.fillOval(300, 80, 180, 70);
                
                // Draw lightning bolts
                g2d.setColor(new Color(200, 200, 255));
                for (LightningBolt bolt : lightningBolts) {
                    // Draw zigzag lightning
                    int[] xPoints = {bolt.x, bolt.x + bolt.width/2, bolt.x, 
                                    bolt.x + bolt.width/2, bolt.x, bolt.x + bolt.width/2};
                    int[] yPoints = {bolt.y, bolt.y + bolt.height/4, bolt.y + bolt.height/2,
                                    bolt.y + 3*bolt.height/4, bolt.y + bolt.height, 
                                    bolt.y + bolt.height};
                    g2d.setStroke(new BasicStroke(3));
                    g2d.drawPolyline(xPoints, yPoints, 6);
                    
                    // Glow effect
                    g2d.setColor(new Color(255, 255, 255, 100));
                    g2d.setStroke(new BasicStroke(5));
                    g2d.drawPolyline(xPoints, yPoints, 6);
                    g2d.setColor(new Color(200, 200, 255));
                }
                
                // Draw player (Dylin as a simple character)
                g2d.setColor(new Color(100, 150, 255));
                g2d.fillOval(playerX, playerY, playerSize, playerSize);
                g2d.setColor(Color.WHITE);
                g2d.setStroke(new BasicStroke(2));
                g2d.drawOval(playerX, playerY, playerSize, playerSize);
                
                // Draw health bars at top
                int barWidth = 300;
                int barHeight = 25;
                int barX = 50;
                int barY = 20;
                
                // Player health bar
                g2d.setColor(new Color(60, 60, 60));
                g2d.fillRect(barX, barY, barWidth, barHeight);
                g2d.setColor(new Color(255, 50, 50));
                int playerHealthWidth = (int) ((double) playerHealth / playerMaxHealth * barWidth);
                g2d.fillRect(barX, barY, playerHealthWidth, barHeight);
                g2d.setColor(Color.WHITE);
                g2d.setStroke(new BasicStroke(2));
                g2d.drawRect(barX, barY, barWidth, barHeight);
                g2d.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
                g2d.drawString("Your Health: " + playerHealth + "/" + playerMaxHealth, 
                              barX, barY - 5);
                
                // Boss health bar
                int bossBarX = 450;
                g2d.setColor(new Color(60, 60, 60));
                g2d.fillRect(bossBarX, barY, barWidth, barHeight);
                g2d.setColor(new Color(255, 200, 0));
                int bossHealthWidth = (int) ((double) bossHealth / bossMaxHealth * barWidth);
                g2d.fillRect(bossBarX, barY, bossHealthWidth, barHeight);
                g2d.setColor(Color.WHITE);
                g2d.drawRect(bossBarX, barY, barWidth, barHeight);
                g2d.drawString("Thor's Health: " + bossHealth + "/" + bossMaxHealth, 
                              bossBarX, barY - 5);
                
                // Draw instructions
                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
                g2d.drawString("Click to move! Dodge the lightning bolts!", 50, 550);
                g2d.drawString("Dodging lightning damages Thor!", 50, 570);
                
                // Draw game over message
                if (gameOver) {
                    g2d.setColor(new Color(0, 0, 0, 180));
                    g2d.fillRect(0, 0, getWidth(), getHeight());
                    g2d.setColor(Color.WHITE);
                    g2d.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 36));
                    String message = playerWon ? "VICTORY!" : "DEFEAT!";
                    FontMetrics fm = g2d.getFontMetrics();
                    int textWidth = fm.stringWidth(message);
                    g2d.drawString(message, (getWidth() - textWidth) / 2, getHeight() / 2);
                }
            }
        }
    }
}


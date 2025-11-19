package edu.uwo.cs2212.engine.gui;

import edu.uwo.cs2212.engine.engine.*;
import edu.uwo.cs2212.engine.io.GameLoader;
import edu.uwo.cs2212.engine.model.*;
import edu.uwo.cs2212.engine.minigame.*;
import edu.uwo.cs2212.engine.minigame.LightningReflectionMiniGame;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Point-and-click GUI for the Adventure Game Engine.
 * Tap/click on objects, characters, and connections to interact.
 */
public class GameGUI extends JFrame {
    private final Game game;
    private final GameState state;
    private final CommandDispatcher dispatcher;
    
    // Main game view
    private GameViewPanel gameViewPanel;
    private JLabel locationNameLabel;
    private JTextArea messageArea;
    private JPanel inventoryPanel;
    
    // Clickable hotspots
    private Map<String, Rectangle> objectHotspots = new HashMap<>();
    private Map<String, Rectangle> characterHotspots = new HashMap<>();
    private Map<String, Rectangle> connectionHotspots = new HashMap<>();
    
    // Dialogue system
    private String[][] conversation;
    private int currentDialogueIndex = -1; // -1 means no dialogue active
    private boolean showingInitialDialogue = false;
    private boolean showingThorDialogue = false;
    private boolean thorDialogueShown = false; // Track if Thor dialogue has been shown to prevent re-triggering

    /**
     * Metadata for villain sprites shown on boss stages.
     */
    private static class VillainConfig {
        final String imagePath;
        final int targetHeight;
        VillainConfig(String imagePath, int targetHeight) {
            this.imagePath = imagePath;
            this.targetHeight = targetHeight;
        }
    }

    private static final Map<String, VillainConfig> VILLAIN_CONFIGS = Map.of(
        "loc_new_york", new VillainConfig("images/Symbiote.png", 180),
        "loc_asgard_boss_room", new VillainConfig("images/Thor.png", 220),
        "loc_sokovia", new VillainConfig("images/Scarlet.png", 180),
        "loc_moon", new VillainConfig("images/ThanosV1.png", 260)
    );
    
    public GameGUI() {
        // Initialize game
        game = GameLoader.sampleGame();
        state = new GameState();
        state.currentLocationId = game.getStartLocationId();
        dispatcher = new CommandDispatcher(game, state);
        
        // Register mini-games
        MiniGameRegistry.register(new LockpickMiniGame("lockpick_crypt", 5));
        MiniGameRegistry.register(new LightningReflectionMiniGame("lightning_dodge_thor", 100, 200, 15, 20));
        
        // Initialize conversation
        conversation = new String[][]{
            {"Silver Surfer", "Welcome, Dylin. I am the Silver Surfer."},
            {"Dylin", "Silver Surfer? What's happening? Where am I?"},
            {"Silver Surfer", "The universe is in grave danger. Thanos has scattered the five Infinity Stones across different worlds, and he plans to use them to destroy reality itself."},
            {"Dylin", "Thanos? The Infinity Stones? What do I need to do?"},
            {"Silver Surfer", "You must collect all five stones before Thanos can use them. Travel to New York and defeat Symbiote Spider-Man to collect the first stone."},
            {"Dylin", "Spider-Man? How do I defeat him?"},
            {"Silver Surfer", "You'll need the Siren. Journey to Asgard next and face Thor - you'll need Stormbreaker to reflect his lightning."},
            {"Dylin", "And then?"},
            {"Silver Surfer", "Venture to Sokovia and solve Wanda's logic puzzles for the third stone. Finally, confront Thanos on the Moon to collect the remaining stones."},
            {"Dylin", "This is a lot to take in... but I'll do it. The universe is counting on me."},
            {"Silver Surfer", "Click on the location buttons to travel. Click on me anytime for hints. The fate of the universe rests in your hands. Good luck, Dylin!"}
        };
        
        initializeGUI();
        updateDisplay();
        
        // Start initial dialogue
        SwingUtilities.invokeLater(() -> {
            startDialogue();
        });
    }
    
    private void startDialogue() {
        currentDialogueIndex = 0;
        showingInitialDialogue = true;
        gameViewPanel.repaint();
    }
    
    private void nextDialogue() {
        if (currentDialogueIndex >= 0 && currentDialogueIndex < conversation.length - 1) {
            currentDialogueIndex++;
            gameViewPanel.repaint();
        } else {
            // End dialogue
            if (showingInitialDialogue) {
                currentDialogueIndex = -1;
                showingInitialDialogue = false;
            } else if (showingThorDialogue) {
                endThorDialogue();
            }
            gameViewPanel.repaint();
        }
    }
    
    private void initializeGUI() {
        setTitle(game.getTitle());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setSize(1000, 700);
        setResizable(true); // Enable resizing/fullscreen
        
        // Top bar with location name and turns
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBorder(new EmptyBorder(10, 10, 10, 10));
        topBar.setBackground(new Color(40, 40, 40));
        
        locationNameLabel = new JLabel("", JLabel.CENTER);
        locationNameLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 24));
        locationNameLabel.setForeground(Color.WHITE);
        
        // Remove turns label - not needed for this adventure game
        // turnsLabel = new JLabel("Turns: 0");
        
        topBar.add(locationNameLabel, BorderLayout.CENTER);
        // topBar.add(turnsLabel, BorderLayout.EAST);
        
        // Main game view (clickable scene)
        gameViewPanel = new GameViewPanel();
        gameViewPanel.setPreferredSize(new Dimension(800, 500));
        gameViewPanel.setBackground(Color.BLACK);
        // Make sure panel can resize properly
        gameViewPanel.setMinimumSize(new Dimension(400, 300));
        
        // Message area (bottom)
        messageArea = new JTextArea(3, 50);
        messageArea.setEditable(false);
        messageArea.setLineWrap(true);
        messageArea.setWrapStyleWord(true);
        messageArea.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        messageArea.setBackground(new Color(30, 30, 30));
        messageArea.setForeground(Color.WHITE);
        messageArea.setBorder(new EmptyBorder(5, 10, 5, 10));
        JScrollPane messageScroll = new JScrollPane(messageArea);
        messageScroll.setPreferredSize(new Dimension(0, 80));
        
        // Inventory bar (bottom)
        inventoryPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        inventoryPanel.setBorder(new EmptyBorder(5, 10, 5, 10));
        inventoryPanel.setBackground(new Color(50, 50, 50));
        inventoryPanel.setPreferredSize(new Dimension(0, 60));
        
        add(topBar, BorderLayout.NORTH);
        add(gameViewPanel, BorderLayout.CENTER);
        add(messageScroll, BorderLayout.SOUTH);
        add(inventoryPanel, BorderLayout.AFTER_LAST_LINE);
        
        setLocationRelativeTo(null);
    }
    
    /**
     * Main game view panel with clickable hotspots
     */
    private class GameViewPanel extends JPanel {
        private Image backgroundImage;
        private String currentImagePath;
        private Image dylinImage;
        private Image silverSurferImage;
        private Map<String, Image> villainImages = new HashMap<>();
        
        public GameViewPanel() {
            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    // Use mousePressed instead of mouseClicked for more responsive button handling
                    
                    // Check if dialogue is active
                    if (showingInitialDialogue || showingThorDialogue) {
                        // Check if clicking on dialogue "Next" button
                        if (currentDialogueIndex >= 0 && currentDialogueIndex < conversation.length) {
                            Rectangle buttonRect = getDialogueButtonRect();
                            if (buttonRect != null && buttonRect.contains(e.getPoint())) {
                                nextDialogue();
                                return;
                            }
                        }
                        // If dialogue is active but not clicking the button, don't process other clicks
                        // (except allow clicking anywhere on initial dialogue to advance)
                        if (showingInitialDialogue) {
                            // Allow clicking anywhere to advance initial dialogue
                            nextDialogue();
                            return;
                        }
                        // For Thor dialogue, allow clicking anywhere to advance (like initial dialogue)
                        if (showingThorDialogue) {
                            nextDialogue();
                            return;
                        }
                        return;
                    }
                    
                    // No dialogue active - handle normal game clicks
                    handleClick(e.getX(), e.getY());
                }
            });
            
            // Handle resize events
            addComponentListener(new ComponentAdapter() {
                @Override
                public void componentResized(ComponentEvent e) {
                    // Recalculate hotspots when panel is resized
                    Location loc = game.getLocations().get(state.currentLocationId);
                    if (loc != null) {
                        updateHotspots(loc);
                        repaint();
                    }
                }
            });
            
            // Load character images
            loadCharacterImages();
            loadVillainImages();
        }
        
        private void loadCharacterImages() {
            // Load Dylin.png
            File dylinFile = new File("images/Dylin.png");
            if (!dylinFile.exists()) {
                dylinFile = new File(System.getProperty("user.dir"), "images/Dylin.png");
            }
            if (dylinFile.exists()) {
                ImageIcon icon = new ImageIcon(dylinFile.getAbsolutePath());
                dylinImage = icon.getImage();
            }
            
            // Load silverSurfer.png
            File surferFile = new File("images/silverSurfer.png");
            if (!surferFile.exists()) {
                surferFile = new File(System.getProperty("user.dir"), "images/silverSurfer.png");
            }
            if (surferFile.exists()) {
                ImageIcon icon = new ImageIcon(surferFile.getAbsolutePath());
                silverSurferImage = icon.getImage();
            }
        }

        private void drawSprite(Graphics2D g2d, Image image, int x, int y, int width, int height, boolean flipHorizontal) {
            if (image == null) {
                return;
            }
            if (!flipHorizontal) {
                g2d.drawImage(image, x, y, width, height, null);
            } else {
                g2d.drawImage(image, x + width, y, -width, height, null);
            }
        }

        private void loadVillainImages() {
            for (Map.Entry<String, VillainConfig> entry : VILLAIN_CONFIGS.entrySet()) {
                String locationId = entry.getKey();
                VillainConfig config = entry.getValue();
                File villainFile = new File(config.imagePath);
                if (!villainFile.exists()) {
                    villainFile = new File(System.getProperty("user.dir"), config.imagePath);
                }
                if (villainFile.exists()) {
                    ImageIcon icon = new ImageIcon(villainFile.getAbsolutePath());
                    villainImages.put(locationId, icon.getImage());
                }
            }
        }
        
        /**
         * Calculate the dialogue button rectangle using the same logic as paintComponent.
         * Returns null if no dialogue is active.
         */
        private Rectangle getDialogueButtonRect() {
            if (!showingInitialDialogue && !showingThorDialogue) {
                return null;
            }
            if (currentDialogueIndex < 0 || currentDialogueIndex >= conversation.length) {
                return null;
            }
            
            int inventoryBarHeight = 60;
            int messageAreaHeight = 80;
            int characterBaseY = getHeight() - inventoryBarHeight - messageAreaHeight - 20;
            int targetHeight = 180; // Increased from 120 to make characters bigger
            
            String[] message = conversation[currentDialogueIndex];
            String speaker = message[0];
            boolean isSilverSurfer = speaker.equals("Silver Surfer");
            boolean isThor = speaker.equals("Thor");
            
            int bubbleX, bubbleY;
            
            if (isThor && showingThorDialogue) {
                // Calculate Thor's position (villain on the right side)
                VillainConfig villainConfig = VILLAIN_CONFIGS.get("loc_asgard_boss_room");
                if (villainConfig != null) {
                    Image villainImage = villainImages.get("loc_asgard_boss_room");
                    if (villainImage != null) {
                        int rawWidth = villainImage.getWidth(null);
                        int rawHeight = villainImage.getHeight(null);
                        double scale = (double) villainConfig.targetHeight / rawHeight;
                        int villainScaledWidth = (int) (rawWidth * scale);
                        int villainX = (getWidth() * 2) / 3 - villainScaledWidth / 2 + 50;
                        int villainY = characterBaseY - villainConfig.targetHeight;
                        
                        bubbleX = villainX + villainScaledWidth / 2 - 200;
                        bubbleY = villainY - 150;
                    } else {
                        // Fallback if image not loaded
                        bubbleX = getWidth() / 2 - 200;
                        bubbleY = 100;
                    }
                } else {
                    // Fallback
                    bubbleX = getWidth() / 2 - 200;
                    bubbleY = 100;
                }
            } else if (isSilverSurfer) {
                int surferWidth = silverSurferImage != null ? silverSurferImage.getWidth(null) : 100;
                int surferHeight = silverSurferImage != null ? silverSurferImage.getHeight(null) : 120;
                double scale = silverSurferImage != null ? (double) targetHeight / surferHeight : 1.0;
                int scaledWidth = (int) (surferWidth * scale);
                int surferX = (getWidth() * 2) / 3 - scaledWidth / 2;
                int surferY = characterBaseY - (int)(surferHeight * scale);
                bubbleX = surferX + scaledWidth / 2 - 200;
                bubbleY = surferY - 150;
            } else {
                // Dylin or other speakers
                int dylinWidth = dylinImage != null ? dylinImage.getWidth(null) : 100;
                int dylinHeight = dylinImage != null ? dylinImage.getHeight(null) : 120;
                double scale = dylinImage != null ? (double) targetHeight / dylinHeight : 1.0;
                int scaledWidth = (int) (dylinWidth * scale);
                int dylinX = getWidth() / 3 - scaledWidth / 2;
                int dylinY = characterBaseY - (int)(dylinHeight * scale);
                bubbleX = dylinX + scaledWidth / 2 - 200;
                bubbleY = dylinY - 150;
            }
            
            int bubbleWidth = 400;
            int bubbleHeight = 120;
            
            // Ensure bubble stays on screen (same logic as paintComponent)
            if (bubbleX < 10) bubbleX = 10;
            if (bubbleX + bubbleWidth > getWidth() - 10) bubbleX = getWidth() - bubbleWidth - 10;
            if (bubbleY < 10) bubbleY = 10;
            
            // Calculate button position (same as paintComponent)
            int buttonX = bubbleX + bubbleWidth - 100;
            int buttonY = bubbleY + bubbleHeight - 35;
            
            return new Rectangle(buttonX, buttonY, 80, 25);
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            
            // Draw background
            g2d.setColor(Color.BLACK);
            g2d.fillRect(0, 0, getWidth(), getHeight());
            
            // Draw location image if available
            if (backgroundImage != null) {
                int imgWidth = backgroundImage.getWidth(null);
                int imgHeight = backgroundImage.getHeight(null);
                int panelWidth = getWidth();
                int panelHeight = getHeight();
                
                // Scale to fit while maintaining aspect ratio
                // For pixel art, use integer scaling when possible
                double scaleX = (double) panelWidth / imgWidth;
                double scaleY = (double) panelHeight / imgHeight;
                double scale = Math.min(scaleX, scaleY);
                
                // For pixel art, prefer integer scaling
                int intScale = Math.max(1, (int) scale);
                if (intScale * imgWidth <= panelWidth && intScale * imgHeight <= panelHeight) {
                    scale = intScale;
                }
                
                int scaledWidth = (int) (imgWidth * scale);
                int scaledHeight = (int) (imgHeight * scale);
                int x = (panelWidth - scaledWidth) / 2;
                int y = (panelHeight - scaledHeight) / 2;
                
                // Use nearest neighbor for pixel art (already set in rendering hints)
                g2d.drawImage(backgroundImage, x, y, scaledWidth, scaledHeight, null);
            } else {
                // Draw placeholder
                g2d.setColor(new Color(60, 60, 60));
                g2d.fillRect(0, 0, getWidth(), getHeight());
                g2d.setColor(Color.GRAY);
                g2d.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 16));
                String msg = currentImagePath != null ? "Image: " + currentImagePath : "No image available";
                FontMetrics fm = g2d.getFontMetrics();
                int textWidth = fm.stringWidth(msg);
                g2d.drawString(msg, (getWidth() - textWidth) / 2, getHeight() / 2);
            }
            
            // Draw connection arrows/buttons
            g2d.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
            for (Map.Entry<String, Rectangle> entry : connectionHotspots.entrySet()) {
                Rectangle rect = entry.getValue();
                String label = entry.getKey();
                
                // Draw semi-transparent background
                g2d.setColor(new Color(0, 150, 255, 150));
                g2d.fillRoundRect(rect.x, rect.y, rect.width, rect.height, 10, 10);
                
                // Draw border
                g2d.setColor(new Color(100, 200, 255));
                g2d.setStroke(new BasicStroke(2));
                g2d.drawRoundRect(rect.x, rect.y, rect.width, rect.height, 10, 10);
                
                // Draw arrow and label
                g2d.setColor(Color.WHITE);
                FontMetrics fm = g2d.getFontMetrics();
                int textWidth = fm.stringWidth(label);
                int textX = rect.x + (rect.width - textWidth) / 2;
                int textY = rect.y + rect.height / 2 + fm.getAscent() / 2 - 2;
                
                // Draw arrow based on position - better alignment
                // Skip arrows for Asgard, Moon, and "back" button
                boolean showArrow = !label.equals("Asgard") && !label.equals("Moon") && !label.equalsIgnoreCase("back");
                
                int centerX = rect.x + rect.width / 2;
                int centerY = rect.y + rect.height / 2;
                
                // Determine arrow position based on button location
                if (showArrow) {
                    if (rect.y < getHeight() / 4) { // Top area
                        g2d.drawString("↑", centerX - 6, rect.y + 18);
                        g2d.drawString(label, textX, textY + 15);
                    } else if (rect.x > getWidth() * 3 / 4) { // Right side
                        g2d.drawString("→", centerX - 6, centerY + 5);
                        g2d.drawString(label, textX, textY);
                    } else if (rect.y > getHeight() * 3 / 4) { // Bottom area
                        g2d.drawString("↓", centerX - 6, rect.y + rect.height - 8);
                        g2d.drawString(label, textX, textY - 15);
                    } else if (rect.x < getWidth() / 4) { // Left side
                        g2d.drawString("←", rect.x + 8, centerY + 5);
                        g2d.drawString(label, textX + 15, textY);
                    } else { // Center area
                        // No arrow for center buttons, just label
                        g2d.drawString(label, textX, textY);
                    }
                } else {
                    // No arrow, just label centered
                    g2d.drawString(label, textX, textY);
                }
            }
            
            // Draw character sprites - align them at the same baseline
            // Calculate base Y position relative to panel height (accounts for resize)
            int inventoryBarHeight = 60; // Height of inventory bar
            int messageAreaHeight = 80; // Height of message area
            int characterBaseY = getHeight() - inventoryBarHeight - messageAreaHeight - 20; // Position above inventory/message bars
            int targetHeight = 180; // Target height for both characters (increased from 120 to make them bigger)
            
            // Only show villains on specific boss locations
            boolean isBossLocation = state.currentLocationId.equals("loc_new_york") ||
                                    state.currentLocationId.equals("loc_asgard_boss_room") ||
                                    state.currentLocationId.equals("loc_sokovia") ||
                                    state.currentLocationId.equals("loc_moon");
            
            // Calculate character positions
            int dylinX = 0, dylinY = 0, dylinScaledWidth = 0, dylinScaledHeight = 0;
            int surferX = 0, surferY = 0, surferScaledWidth = 0, surferScaledHeight = 0;
            int villainX = 0, villainY = 0, villainScaledWidth = 0, villainScaledHeight = 0;
            
            if (dylinImage != null) {
                int dylinWidth = dylinImage.getWidth(null);
                int dylinHeight = dylinImage.getHeight(null);
                double scale = (double) targetHeight / dylinHeight;
                dylinScaledWidth = (int) (dylinWidth * scale);
                dylinScaledHeight = (int) (dylinHeight * scale);
                // Position closer to center - about 1/3 from left edge
                dylinX = getWidth() / 3 - dylinScaledWidth / 2;
                dylinY = characterBaseY - dylinScaledHeight; // Align bottom to same baseline
            }
            
            if (silverSurferImage != null) {
                int surferWidth = silverSurferImage.getWidth(null);
                int surferHeight = silverSurferImage.getHeight(null);
                double scale = (double) targetHeight / surferHeight;
                surferScaledWidth = (int) (surferWidth * scale);
                surferScaledHeight = (int) (surferHeight * scale);
                
                if (isBossLocation) {
                    // In boss locations: Position Silver Surfer behind Dylin, floating above
                    // Position Silver Surfer further back and floating above Dylin
                    surferX = dylinX - 160; // Positioned even further back to avoid surfboard touching Dylin
                    surferY = characterBaseY - surferScaledHeight - 100; // Floating higher above Dylin
                } else {
                    // On main page: Position to the right side
                    surferX = (getWidth() * 2) / 3 - surferScaledWidth / 2;
                    surferY = characterBaseY - surferScaledHeight; // Align bottom to same baseline as Dylin
                }
            }

            VillainConfig villainConfig = VILLAIN_CONFIGS.get(state.currentLocationId);
            Image villainImage = null;
            if (villainConfig != null && isBossLocation) {
                villainImage = villainImages.get(state.currentLocationId);
            }

            if (villainImage != null && villainConfig != null && isBossLocation) {
                int rawWidth = villainImage.getWidth(null);
                int rawHeight = villainImage.getHeight(null);
                double scale = (double) villainConfig.targetHeight / rawHeight;
                villainScaledWidth = (int) (rawWidth * scale);
                villainScaledHeight = (int) (rawHeight * scale);
                villainX = (getWidth() * 2) / 3 - villainScaledWidth / 2 + 50;
                villainY = characterBaseY - villainScaledHeight;
            }
            
            // Draw characters in correct order based on location
            // In boss locations: Silver Surfer behind (drawn first), Dylin in front (drawn second), villain opposite side
            // On main page: Dylin first, Silver Surfer second (original order)
            boolean isBaseStage = state.currentLocationId.equals("loc_toronto");
            boolean flipDylin = false;          // Dylin always faces right
            boolean flipSurfer = isBaseStage;   // Silver Surfer faces left only on base stage
            boolean flipVillain = true;         // Villains always face left (toward heroes)

            if (isBossLocation) {
                // Fight stage: Silver Surfer behind, Dylin in front
                if (silverSurferImage != null) {
                    drawSprite(g2d, silverSurferImage, surferX, surferY, surferScaledWidth, surferScaledHeight, flipSurfer);
                    // Update Silver Surfer hotspot to match image position
                    characterHotspots.put("char_silver_surfer", 
                        new Rectangle(surferX, surferY, surferScaledWidth, surferScaledHeight));
                }
                if (dylinImage != null) {
                    drawSprite(g2d, dylinImage, dylinX, dylinY, dylinScaledWidth, dylinScaledHeight, flipDylin);
                }
                if (villainImage != null) {
                    drawSprite(g2d, villainImage, villainX, villainY, villainScaledWidth, villainScaledHeight, flipVillain);
                }
            } else {
                // Main page: Original order (Dylin first, Silver Surfer second)
                if (dylinImage != null) {
                    drawSprite(g2d, dylinImage, dylinX, dylinY, dylinScaledWidth, dylinScaledHeight, flipDylin);
                }
                if (silverSurferImage != null) {
                    drawSprite(g2d, silverSurferImage, surferX, surferY, surferScaledWidth, surferScaledHeight, flipSurfer);
                    // Update Silver Surfer hotspot to match image position
                    characterHotspots.put("char_silver_surfer", 
                        new Rectangle(surferX, surferY, surferScaledWidth, surferScaledHeight));
                }
            }
            
            // Draw dialogue speech bubbles if conversation is active
            if ((showingInitialDialogue || showingThorDialogue) && 
                currentDialogueIndex >= 0 && currentDialogueIndex < conversation.length) {
                String[] message = conversation[currentDialogueIndex];
                String speaker = message[0];
                String text = message[1];
                
                // Determine which character is speaking and their position
                boolean isSilverSurfer = speaker.equals("Silver Surfer");
                boolean isThor = speaker.equals("Thor");
                
                // Use already calculated character positions
                int bubbleX, bubbleY;
                int tailX, tailY;
                
                if (isSilverSurfer) {
                    // Speech bubble above Silver Surfer, pointing down (higher up)
                    bubbleX = surferX + surferScaledWidth / 2 - 200;
                    bubbleY = surferY - 150; // Moved higher above character
                    tailX = surferX + surferScaledWidth / 2;
                    tailY = surferY - 10; // Tail still points to character head
                } else if (isThor && villainImage != null) {
                    // Speech bubble above Thor (villain), pointing down
                    bubbleX = villainX + villainScaledWidth / 2 - 200;
                    bubbleY = villainY - 150;
                    tailX = villainX + villainScaledWidth / 2;
                    tailY = villainY - 10;
                } else {
                    // Speech bubble above Dylin, pointing down (higher up)
                    bubbleX = dylinX + dylinScaledWidth / 2 - 200;
                    bubbleY = dylinY - 150; // Moved higher above character
                    tailX = dylinX + dylinScaledWidth / 2;
                    tailY = dylinY - 10; // Tail still points to character head
                }
                
                // Draw speech bubble
                int bubbleWidth = 400;
                int bubbleHeight = 120;
                
                // Ensure bubble stays on screen
                if (bubbleX < 10) bubbleX = 10;
                if (bubbleX + bubbleWidth > getWidth() - 10) bubbleX = getWidth() - bubbleWidth - 10;
                if (bubbleY < 10) bubbleY = 10;
                
                // Bubble background
                Color bubbleColor;
                if (isSilverSurfer) {
                    bubbleColor = new Color(60, 80, 120);
                } else if (isThor) {
                    bubbleColor = new Color(100, 50, 50); // Reddish for Thor
                } else {
                    bubbleColor = new Color(120, 80, 60); // Brownish for Dylin
                }
                g2d.setColor(bubbleColor);
                g2d.fillRoundRect(bubbleX, bubbleY, bubbleWidth, bubbleHeight, 15, 15);
                
                // Bubble border
                g2d.setColor(Color.WHITE);
                g2d.setStroke(new BasicStroke(3));
                g2d.drawRoundRect(bubbleX, bubbleY, bubbleWidth, bubbleHeight, 15, 15);
                
                // Draw tail pointing to character (smaller arrow)
                int tailSize = 8; // Smaller tail size
                int[] tailXPoints = {tailX, tailX - tailSize, tailX + tailSize};
                int[] tailYPoints = {tailY, tailY - tailSize, tailY - tailSize};
                g2d.fillPolygon(tailXPoints, tailYPoints, 3);
                g2d.setStroke(new BasicStroke(2));
                g2d.drawPolygon(tailXPoints, tailYPoints, 3);
                
                // Draw text
                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
                FontMetrics fm = g2d.getFontMetrics();
                
                // Speaker name
                String speakerText = speaker + ":";
                int nameX = bubbleX + 15;
                int nameY = bubbleY + 25;
                g2d.drawString(speakerText, nameX, nameY);
                
                // Dialogue text (wrap if needed)
                g2d.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
                fm = g2d.getFontMetrics();
                int textX = bubbleX + 15;
                int textY = nameY + 25;
                int maxWidth = bubbleWidth - 30;
                
                // Simple word wrapping
                String[] words = text.split(" ");
                StringBuilder line = new StringBuilder();
                int currentY = textY;
                
                for (String word : words) {
                    String testLine = line.length() == 0 ? word : line + " " + word;
                    int width = fm.stringWidth(testLine);
                    if (width > maxWidth && line.length() > 0) {
                        g2d.drawString(line.toString(), textX, currentY);
                        line = new StringBuilder(word);
                        currentY += 20;
                    } else {
                        line = new StringBuilder(testLine);
                    }
                }
                if (line.length() > 0) {
                    g2d.drawString(line.toString(), textX, currentY);
                }
                
                // Draw "Next" button
                int buttonX = bubbleX + bubbleWidth - 100;
                int buttonY = bubbleY + bubbleHeight - 35;
                g2d.setColor(new Color(100, 150, 255));
                g2d.fillRoundRect(buttonX, buttonY, 80, 25, 5, 5);
                g2d.setColor(Color.WHITE);
                g2d.setStroke(new BasicStroke(2));
                g2d.drawRoundRect(buttonX, buttonY, 80, 25, 5, 5);
                g2d.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
                String buttonText = currentDialogueIndex < conversation.length - 1 ? "Next" : "Begin!";
                int btnTextWidth = g2d.getFontMetrics().stringWidth(buttonText);
                g2d.drawString(buttonText, buttonX + (80 - btnTextWidth) / 2, buttonY + 17);
            }
            
            // Draw objects as visible, clickable items on screen
            g2d.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
            for (Map.Entry<String, Rectangle> entry : objectHotspots.entrySet()) {
                String objId = entry.getKey();
                Rectangle rect = entry.getValue();
                GameObject obj = game.getObjects().get(objId);
                
                if (obj != null) {
                    // Draw object background (semi-transparent box)
                    g2d.setColor(new Color(100, 100, 150, 180));
                    g2d.fillRoundRect(rect.x, rect.y, rect.width, rect.height, 10, 10);
                    
                    // Draw border
                    g2d.setColor(new Color(150, 150, 255));
                    g2d.setStroke(new BasicStroke(2));
                    g2d.drawRoundRect(rect.x, rect.y, rect.width, rect.height, 10, 10);
                    
                    // Draw object name
                    g2d.setColor(Color.WHITE);
                    FontMetrics fm = g2d.getFontMetrics();
                    String name = obj.getName();
                    int textWidth = fm.stringWidth(name);
                    int textX = rect.x + (rect.width - textWidth) / 2;
                    int textY = rect.y + rect.height / 2 + fm.getAscent() / 2 - 2;
                    
                    // Wrap text if too long
                    if (textWidth > rect.width - 10) {
                        // Split into multiple lines
                        String[] words = name.split(" ");
                        StringBuilder line = new StringBuilder();
                        int currentY = rect.y + 20;
                        for (String word : words) {
                            String testLine = line.length() == 0 ? word : line + " " + word;
                            int width = fm.stringWidth(testLine);
                            if (width > rect.width - 10 && line.length() > 0) {
                                g2d.drawString(line.toString(), rect.x + 5, currentY);
                                line = new StringBuilder(word);
                                currentY += 15;
                            } else {
                                line = new StringBuilder(testLine);
                            }
                        }
                        if (line.length() > 0) {
                            g2d.drawString(line.toString(), rect.x + 5, currentY);
                        }
                    } else {
                        g2d.drawString(name, textX, textY);
                    }
                }
            }
        }
    }
    
    private void handleClick(int x, int y) {
        Point clickPoint = new Point(x, y);
        
        // Check connections first (for navigation)
        for (Map.Entry<String, Rectangle> entry : connectionHotspots.entrySet()) {
            if (entry.getValue().contains(clickPoint)) {
                String label = entry.getKey();
                executeCommand("go " + label);
                return;
            }
        }
        
        // Check characters - directly talk when clicked
        for (Map.Entry<String, Rectangle> entry : characterHotspots.entrySet()) {
            if (entry.getValue().contains(clickPoint)) {
                String charId = entry.getKey();
                executeCommand("talk " + charId);
                return;
            }
        }
        
        // Check objects
        for (Map.Entry<String, Rectangle> entry : objectHotspots.entrySet()) {
            if (entry.getValue().contains(clickPoint)) {
                String objId = entry.getKey();
                showObjectMenu(objId, x, y);
                return;
            }
        }
        
        // Click on empty space - do nothing (no popup)
    }
    
    private void showObjectMenu(String objId, int x, int y) {
        GameObject obj = game.getObjects().get(objId);
        if (obj == null) return;
        
        JPopupMenu menu = new JPopupMenu();
        menu.setBackground(new Color(50, 50, 50));
        menu.setForeground(Color.WHITE);
        
        // Examine
        JMenuItem examineItem = new JMenuItem("Examine");
        examineItem.addActionListener(e -> executeCommand("ex " + objId));
        menu.add(examineItem);
        
        // Pickup (if in location and can pickup)
        Location loc = game.getLocations().get(state.currentLocationId);
        if (loc.getObjectIds().contains(objId) && obj.canPickUp()) {
            JMenuItem pickupItem = new JMenuItem("Pickup");
            pickupItem.addActionListener(e -> executeCommand("pickup " + objId));
            menu.add(pickupItem);
        }
        
        // Use
        JMenuItem useItem = new JMenuItem("Use");
        useItem.addActionListener(e -> showUseMenu(objId));
        menu.add(useItem);
        
        // Drop (if in inventory)
        if (state.inventory.contains(objId)) {
            JMenuItem dropItem = new JMenuItem("Drop");
            dropItem.addActionListener(e -> executeCommand("drop " + objId));
            menu.add(dropItem);
        }
        
        menu.show(gameViewPanel, x, y);
    }
    
    // Removed showCharacterMenu - characters now talk directly when clicked
    
    private void showUseMenu(String objId) {
        // Show dialog to select what to use with
        Location loc = game.getLocations().get(state.currentLocationId);
        List<String> available = new ArrayList<>();
        available.addAll(loc.getObjectIds());
        available.addAll(state.inventory);
        available.addAll(loc.getCharacterIds()); // Include characters too!
        available.remove(objId); // Remove self
        
        if (available.isEmpty()) {
            executeCommand("use " + objId);
            return;
        }
        
        String[] options = new String[available.size() + 1];
        options[0] = "Use alone";
        for (int i = 0; i < available.size(); i++) {
            String id = available.get(i);
            GameObject obj = game.getObjects().get(id);
            GameCharacter ch = game.getCharacters().get(id);
            if (obj != null) {
                options[i + 1] = "Use with " + obj.getName();
            } else if (ch != null) {
                options[i + 1] = "Use with " + ch.getName();
            } else {
                options[i + 1] = "Use with " + id;
            }
        }
        
        int choice = JOptionPane.showOptionDialog(this,
            "Use " + game.getObjects().get(objId).getName() + " with:",
            "Use Item",
            JOptionPane.DEFAULT_OPTION,
            JOptionPane.QUESTION_MESSAGE,
            null,
            options,
            options[0]);
        
        if (choice == 0) {
            executeCommand("use " + objId);
        } else if (choice > 0) {
            String withId = available.get(choice - 1);
            executeCommand("use " + objId + " with " + withId);
        }
    }
    
    // Removed showLocationInfo - no popup boxes
    
    private void executeCommand(String command) {
        try {
            String[] toks = command.split("\\s+");
            if (toks.length == 0) return;
            
            String cmd = toks[0].toLowerCase();
            CommandResult result;
            
            switch (cmd) {
                case "go":
                    // Reset Thor dialogue flag when leaving boss room
                    if (state.currentLocationId.equals("loc_asgard_boss_room")) {
                        thorDialogueShown = false;
                    }
                    result = dispatcher.go(joinFrom(toks, 1));
                    break;
                case "pickup":
                    if (toks.length < 2) {
                        result = CommandResult.fail("Usage: pickup <id>");
                    } else {
                        result = dispatcher.pickUp(toks[1]);
                    }
                    break;
                case "drop":
                    if (toks.length < 2) {
                        result = CommandResult.fail("Usage: drop <id>");
                    } else {
                        result = dispatcher.drop(toks[1]);
                    }
                    break;
                case "inv":
                    result = dispatcher.inventory();
                    break;
                case "ex":
                case "examine":
                    if (toks.length < 2) {
                        result = CommandResult.fail("Usage: ex <id>");
                    } else {
                        result = dispatcher.examineObject(toks[1]);
                    }
                    break;
                case "talk":
                    if (toks.length < 2) {
                        result = CommandResult.fail("Usage: talk <charId>");
                    } else {
                        result = dispatcher.talk(toks[1]);
                    }
                    break;
                case "give":
                    if (toks.length < 3) {
                        result = CommandResult.fail("Usage: give <objId> <charId>");
                    } else {
                        result = dispatcher.give(toks[1], toks[2]);
                    }
                    break;
                case "use":
                    // Run use command in background thread to avoid blocking EDT if minigame starts
                    if (toks.length >= 4 && toks[2].equalsIgnoreCase("with")) {
                        String primaryId = toks[1];
                        String withId = toks[3];
                        // Check if this might trigger a minigame by checking if "with" is a character
                        Location currentLoc = game.getLocations().get(state.currentLocationId);
                        boolean mightBeMinigame = currentLoc != null && currentLoc.getCharacterIds().contains(withId);
                        
                        if (mightBeMinigame) {
                            // Run in background thread to avoid blocking EDT
                            new Thread(() -> {
                                CommandResult bgResult = dispatcher.use(primaryId, withId);
                                SwingUtilities.invokeLater(() -> {
                                    appendMessage(bgResult.message);
                                    handleCommandResult(bgResult);
                                });
                            }).start();
                            return; // Return early, result will be handled in background thread
                        } else {
                            result = dispatcher.use(primaryId, withId);
                        }
                    } else if (toks.length >= 2) {
                        result = dispatcher.use(toks[1], null);
                    } else {
                        result = CommandResult.fail("Usage: use <id|@attr> [with <id|@attr>]");
                    }
                    break;
                default:
                    result = CommandResult.fail("Unknown command: " + cmd);
            }
            
            handleCommandResult(result);
        } catch (Exception e) {
            appendMessage("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void handleCommandResult(CommandResult result) {
        appendMessage(result.message);
        
        // Check if Thor was defeated - unlock Sokovia connection
        if (result.message.contains("defeated Thor") || result.message.contains("path to Wanda")) {
            Location asgardBossRoom = game.getLocations().get("loc_asgard_boss_room");
            if (asgardBossRoom != null) {
                // Check if Sokovia connection doesn't exist yet
                boolean hasSokoviaConnection = false;
                for (Connection c : asgardBossRoom.getConnections()) {
                    if (c.getTargetLocationId().equals("loc_sokovia")) {
                        hasSokoviaConnection = true;
                        break;
                    }
                }
                if (!hasSokoviaConnection) {
                    asgardBossRoom.addConnection(new Connection("To Sokovia", "loc_sokovia"));
                    appendMessage("The path to Sokovia has been unlocked!");
                }
            }
        }
        
        updateDisplay();
        
        // Check win condition
        if (game.getEndLocationIds().contains(state.currentLocationId)) {
            appendMessage("*** VICTORY! You have completed your quest! ***");
        }
    }
    
    private String joinFrom(String[] arr, int idx) {
        StringBuilder sb = new StringBuilder();
        for (int i = idx; i < arr.length; i++) {
            if (i > idx) sb.append(' ');
            sb.append(arr[i]);
        }
        return sb.toString();
    }
    
    private void updateDisplay() {
        Location loc = game.getLocations().get(state.currentLocationId);
        
        // Safety check - if location doesn't exist, reset to Toronto
        if (loc == null) {
            appendMessage("Error: Location not found! Returning to Toronto.");
            state.currentLocationId = "loc_toronto";
            loc = game.getLocations().get(state.currentLocationId);
            if (loc == null) {
                appendMessage("Fatal error: Toronto location not found!");
                return;
            }
        }
        
        // Update location name
        locationNameLabel.setText(loc.getName());
        
        // Update location image
        updateLocationImage(loc.getImagePath());
        
        // Check if we entered Thor's boss room - show dialogue (only once)
        if (loc.getId().equals("loc_asgard_boss_room") && !thorDialogueShown && !showingThorDialogue && !showingInitialDialogue) {
            thorDialogueShown = true;
            // Delay dialogue slightly to allow display to update first
            SwingUtilities.invokeLater(() -> {
                startThorDialogue();
            });
        }
        
        // Turns removed - not needed for adventure game
        // turnsLabel.setText("Turns: " + state.turnsTaken + 
        //     (game.getTurnLimit() != null ? "/" + game.getTurnLimit() : ""));
        
        // Update hotspots - recalculate based on current panel size
        updateHotspots(loc);
        
        // Update inventory
        updateInventory();
        
        // Repaint - ensure it happens on EDT
        SwingUtilities.invokeLater(() -> {
            gameViewPanel.repaint();
        });
    }
    
    private void startThorDialogue() {
        showingThorDialogue = true;
        conversation = new String[][]{
            {"Thor", "You dare enter my arena, mortal?"},
            {"Dylin", "I need to pass through, Thor. I'm on a quest to save the universe."},
            {"Thor", "Only those worthy may pass! Face me in battle!"},
            {"Dylin", "I have Stormbreaker. I'm ready to face your lightning."},
            {"Thor", "Then let the lightning decide your fate! Use Stormbreaker against me!"}
        };
        currentDialogueIndex = 0;
        gameViewPanel.repaint();
    }
    
    private void endThorDialogue() {
        showingThorDialogue = false;
        currentDialogueIndex = -1;
        gameViewPanel.repaint();
    }
    
    private void updateLocationImage(String imagePath) {
        if (imagePath != null && !imagePath.isEmpty()) {
            File imgFile = new File(imagePath);
            // Try relative path first, then try from project root
            if (!imgFile.exists()) {
                imgFile = new File(System.getProperty("user.dir"), imagePath);
            }
            if (imgFile.exists()) {
                ImageIcon icon = new ImageIcon(imgFile.getAbsolutePath());
                if (icon.getIconWidth() > 0) {
                    gameViewPanel.backgroundImage = icon.getImage();
                    gameViewPanel.currentImagePath = imagePath;
                    return;
                }
            }
            // Image not found
            gameViewPanel.backgroundImage = null;
            gameViewPanel.currentImagePath = imagePath;
        } else {
            gameViewPanel.backgroundImage = null;
            gameViewPanel.currentImagePath = null;
        }
    }
    
    private void updateHotspots(Location loc) {
        objectHotspots.clear();
        characterHotspots.clear();
        connectionHotspots.clear();
        
        // Get actual panel dimensions, with fallback to preferred size
        int panelWidth = gameViewPanel.getWidth();
        int panelHeight = gameViewPanel.getHeight();
        
        // If panel hasn't been sized yet, use preferred size
        if (panelWidth <= 0) {
            Dimension prefSize = gameViewPanel.getPreferredSize();
            panelWidth = prefSize.width > 0 ? prefSize.width : 800;
        }
        if (panelHeight <= 0) {
            Dimension prefSize = gameViewPanel.getPreferredSize();
            panelHeight = prefSize.height > 0 ? prefSize.height : 500;
        }
        
        // Create hotspots for objects (simple grid layout)
        int objCount = loc.getObjectIds().size();
        int objIndex = 0;
        for (String oid : loc.getObjectIds()) {
            int x = 50 + (objIndex % 3) * 200;
            int y = 100 + (objIndex / 3) * 150;
            objectHotspots.put(oid, new Rectangle(x, y, 150, 100));
            objIndex++;
        }
        
        // Create hotspots for characters (exclude Dylin - he's the player)
        int charIndex = 0;
        for (String cid : loc.getCharacterIds()) {
            // Skip Dylin - he's the player character, not an NPC
            if (cid.equals("char_dylin")) continue;
            
            // Position Silver Surfer hotspot to match where his image is drawn
            if (cid.equals("char_silver_surfer")) {
                // Will be updated dynamically in paintComponent when image is drawn
                // Use placeholder that matches new positioning (closer together)
                int inventoryBarHeight = 60;
                int messageAreaHeight = 80;
                int characterBaseY = panelHeight - inventoryBarHeight - messageAreaHeight - 20;
                int targetHeight = 180; // Match the new character size
                int surferX = (panelWidth * 2) / 3 - 90; // Adjusted for larger character (was 60)
                int surferY = characterBaseY - targetHeight;
                characterHotspots.put(cid, new Rectangle(surferX, surferY, 180, 180)); // Updated to match new size
            } else {
                // Position other characters on the right side, vertically stacked
                int x = panelWidth - 180;
                int y = 100 + (charIndex * 60);
                characterHotspots.put(cid, new Rectangle(x, y, 160, 50));
            }
            charIndex++;
        }
        
        // Create hotspots for connections - position as overlay buttons on scene
        int connCount = loc.getConnections().size();
        int connIndex = 0;
        for (Connection c : loc.getConnections()) {
            Rectangle rect;
            int buttonWidth = 130;
            int buttonHeight = 35;
            
            // For Toronto hub, position location buttons aligned with characters
            if (loc.getId().equals("loc_toronto")) {
                // Calculate character positions (same as in paintComponent)
                int inventoryBarHeight = 60;
                int messageAreaHeight = 80;
                int characterBaseY = panelHeight - inventoryBarHeight - messageAreaHeight - 20;
                int targetHeight = 120;
                
                // Estimate character X positions (matching paintComponent logic)
                int dylinX = panelWidth / 3; // Dylin is at 1/3
                int surferX = (panelWidth * 2) / 3; // Silver Surfer is at 2/3
                
                // Position buttons above characters at midway point
                int buttonY = characterBaseY / 2; // Midway point vertically
                
                switch (connIndex) {
                    case 0: // New York - above Dylin (left character)
                        rect = new Rectangle(dylinX - buttonWidth / 2, buttonY, buttonWidth, buttonHeight);
                        break;
                    case 1: // Asgard - top center
                        rect = new Rectangle(panelWidth / 2 - buttonWidth / 2, 50, buttonWidth, buttonHeight);
                        break;
                    case 2: // Sokovia - above Silver Surfer (right character)
                        rect = new Rectangle(surferX - buttonWidth / 2, buttonY, buttonWidth, buttonHeight);
                        break;
                    case 3: // Moon - bottom center (above characters)
                        rect = new Rectangle(panelWidth / 2 - buttonWidth / 2, characterBaseY - 30, buttonWidth, buttonHeight);
                        break;
                    default:
                        rect = new Rectangle(panelWidth / 2 - buttonWidth / 2, 50 + (connIndex * 50), buttonWidth, buttonHeight);
                        break;
                }
            } else {
                // For other locations (including Asgard locations)
                // First pass: count non-back buttons to position them correctly
                int nonBackCount = 0;
                for (Connection conn : loc.getConnections()) {
                    if (!conn.getLabel().equalsIgnoreCase("back")) {
                        nonBackCount++;
                    }
                }
                
                // Position navigation buttons above the back button
                String label = c.getLabel();
                if (label.equalsIgnoreCase("back")) {
                    // Back button at bottom center (above inventory)
                    rect = new Rectangle(panelWidth / 2 - buttonWidth / 2, panelHeight - 110, buttonWidth, buttonHeight);
                } else {
                    // Other navigation buttons (like "Explore East", "Enter Boss Chamber") positioned above back button
                    // Count how many non-back buttons come before this one
                    int navButtonIndex = 0;
                    for (int i = 0; i < connIndex; i++) {
                        if (!loc.getConnections().get(i).getLabel().equalsIgnoreCase("back")) {
                            navButtonIndex++;
                        }
                    }
                    
                    // Stack them vertically, starting from above the back button
                    int backButtonY = panelHeight - 110;
                    int navButtonY = backButtonY - ((nonBackCount - navButtonIndex) * (buttonHeight + 10)) - 10;
                    // Make sure buttons don't go off screen
                    if (navButtonY < 50) {
                        navButtonY = 50 + (navButtonIndex * (buttonHeight + 10));
                    }
                    rect = new Rectangle(panelWidth / 2 - buttonWidth / 2, navButtonY, buttonWidth, buttonHeight);
                }
            }
            connectionHotspots.put(c.getLabel(), rect);
            connIndex++;
        }
    }
    
    private void updateInventory() {
        inventoryPanel.removeAll();
        
        JLabel invLabel = new JLabel("Inventory: ");
        invLabel.setForeground(Color.WHITE);
        invLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        inventoryPanel.add(invLabel);
        
        if (state.inventory.isEmpty()) {
            JLabel emptyLabel = new JLabel("(empty)");
            emptyLabel.setForeground(Color.GRAY);
            inventoryPanel.add(emptyLabel);
        } else {
            for (String oid : state.inventory) {
                GameObject obj = game.getObjects().get(oid);
                if (obj != null) {
                    JButton itemBtn = new JButton(obj.getName());
                    itemBtn.setBackground(new Color(70, 70, 70));
                    itemBtn.setForeground(Color.WHITE);
                    itemBtn.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
                    itemBtn.addActionListener(e -> showObjectMenu(oid, 0, 0));
                    inventoryPanel.add(itemBtn);
                }
            }
        }
        
        inventoryPanel.revalidate();
        inventoryPanel.repaint();
    }
    
    private void appendMessage(String message) {
        messageArea.append(message + "\n");
        messageArea.setCaretPosition(messageArea.getDocument().getLength());
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            GameGUI gui = new GameGUI();
            gui.setVisible(true);
            
            // Silver Surfer dialogue will show automatically via showSilverSurferDialogue()
        });
    }
}

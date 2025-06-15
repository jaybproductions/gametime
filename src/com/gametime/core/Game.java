package com.gametime.core;

import com.gametime.input.KeyInput;
import com.gametime.io.GameState;
import com.gametime.io.SaveManager;
import com.gametime.io.SaveMenu;
import com.gametime.LevelManager;
import com.gametime.Reality;
import com.gametime.World;
import com.gametime.objects.Bullet;
import com.gametime.objects.GameObject;
import com.gametime.objects.Player;
import com.gametime.ui.*;
import com.gametime.DualWorld;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferStrategy;
import java.util.List;

public class Game extends Canvas implements Runnable {
    private Thread thread;
    private boolean running = false;
    private HUD hud;
    private DualWorld dualWorld;
    private Player player;
    private KeyInput input;
    private LevelManager levelManager;
    private RealityIndicator realityIndicator;
    private LevelIndicator levelIndicator;
    private SaveMenu saveMenu;
    private boolean inSaveMenu = false;
    private boolean editMode = false;
    private int freezeFrames = 0;
    private int glitchTimer = 0;
    private final int MAX_GLITCH_TIME = 15;

    public static final int WIDTH = 1280, HEIGHT = 720;

    private Handler handler;
    private Camera camera;
    public static Game instance;

    private int currentTile = 1;
    private final int tileCount = 5;
    private int hoverTileX = -1, hoverTileY = -1;
    private int selectedTile = 1;
    private final int MAX_TILE_TYPE = 6;
    
    private int loadedSlot = -1;


    public Game() {
        Game.instance = this;
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setFocusable(true);
        requestFocusInWindow();
        setFocusTraversalKeysEnabled(false);

        dualWorld = new DualWorld();
        levelManager = new LevelManager(dualWorld);
        dualWorld.loadBothWorlds(levelManager.getLevelFilename());

        Point spawn = dualWorld.getActiveWorld().getFirstSolidTileBelow(100, 0);
        handler = new Handler();
        hud = new HUD();
        input = new KeyInput();
        camera = new Camera();
        saveMenu = new SaveMenu();
        


        player = new Player(spawn.x, spawn.y, hud, dualWorld, input, handler);
        levelManager.setPlayer(player);
        hud.setPlayer(player);
        handler.addObject(player);
        camera.update(player);
        


        realityIndicator = new RealityIndicator();
        levelIndicator = new LevelIndicator();

        Bullet bullet = new Bullet(0, 0, player, handler);
        handler.addObject(bullet);
        addKeyListener(input);
        
        GameState state = SaveManager.loadMostRecent();
        System.out.println("state" + state);
        if (state != null) {
            loadState(state);
            loadedSlot = state.slotNumber; // Save this for later
        }

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (!editMode) return;
                int mouseX = e.getX() + (int) camera.getX() - (int) camera.getOffsetX();
                int mouseY = e.getY() + (int) camera.getY() - (int) camera.getOffsetY();
                int tileX = mouseX / dualWorld.getActiveWorld().TILE_SIZE;
                int tileY = mouseY / dualWorld.getActiveWorld().TILE_SIZE;
                dualWorld.getActiveWorld().setTile(tileX, tileY, selectedTile);
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                if (!editMode) return;
                int mouseX = e.getX() + (int) camera.getX() - (int) camera.getOffsetX();
                int mouseY = e.getY() + (int) camera.getY() - (int) camera.getOffsetY();
                hoverTileX = mouseX / dualWorld.getActiveWorld().TILE_SIZE;
                hoverTileY = mouseY / dualWorld.getActiveWorld().TILE_SIZE;
            }
        });

        addMouseWheelListener(e -> {
            if (!editMode) return;
            selectedTile += e.getWheelRotation();
            if (selectedTile < 0) selectedTile = MAX_TILE_TYPE;
            if (selectedTile > MAX_TILE_TYPE) selectedTile = 0;
            System.out.println("Selected Tile: " + selectedTile);
        });
        
        if (loadedSlot != -1) {
            saveMenu.markSlotLoaded(loadedSlot);
        }
    }

    public synchronized void start() {
        running = true;
        thread = new Thread(this);
        thread.start();
    }

    public synchronized void stop() {
        running = false;
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        createBufferStrategy(3);
        long lastTime = System.nanoTime();
        double nsPerUpdate = 1_000_000_000.0 / 60.0;
        double delta = 0;

        while (running) {
            long now = System.nanoTime();
            delta += (now - lastTime) / nsPerUpdate;
            lastTime = now;

            while (delta >= 1) {
                update();
                delta--;
            }

            render();
        }

        stop();
    }

    private void update() {
        if (input.isJustPressed(KeyEvent.VK_ESCAPE)) {
            inSaveMenu = !inSaveMenu;
        }

        if (inSaveMenu) {
            saveMenu.update(input);
            input.update(); // <- ensure input state updates
            return;
        }
        
        if (input.isJustPressed(KeyEvent.VK_ENTER) && inSaveMenu) {
            GameState state = SaveManager.load(saveMenu.getSelectedSlot());
            if (state != null) {
                loadState(state);
                saveMenu.markSlotLoaded(state.getSlotNumber()); // ✅ Track which is loaded
                inSaveMenu = false;
            }
        }

        if (input.isJustPressed(KeyEvent.VK_F5)) {
            int slot = saveMenu.getSelectedSlot();
            SaveManager.save(GameState.fromGame(this, slot), slot);
            hud.showStatus("Game saved to slot " + slot, 120);
            saveMenu.markSlotLoaded(slot);
        }

        if (input.isJustPressed(KeyEvent.VK_E) && glitchTimer == 0) {
            Reality target = (dualWorld.getCurrentReality() == Reality.A) ? Reality.B : Reality.A;
            int[][] targetMap = (target == Reality.A) ? dualWorld.getRealityA().getMap() : dualWorld.getRealityB().getMap();
            if (!isCollidingWithSolidTiles(player.getX(), player.getY(), targetMap)) {
                dualWorld.setReality(target);
                glitchTimer = MAX_GLITCH_TIME;
                camera.shake(6, 6);
                realityIndicator.showSuccess(target);
            } else {
                realityIndicator.showBlocked();
                camera.shake(2, 2);
            }
        }

        if (input.isJustPressed(KeyEvent.VK_TAB)) {
            editMode = !editMode;
            System.out.println("Edit Mode: " + (editMode ? "ON" : "OFF"));
        }

        if (editMode) {
            if (input.isJustPressed(KeyEvent.VK_S)) {
                dualWorld.saveBothWorlds(levelManager.getLevelFilename());
            }
            if (input.isJustPressed(KeyEvent.VK_L)) {
                dualWorld.loadBothWorlds(levelManager.getLevelFilename());
            }
        }

        if (input.isJustPressed(KeyEvent.VK_CLOSE_BRACKET)) {
            nextLevel();
            player.respawnAtStart();
        }

        if (input.isJustPressed(KeyEvent.VK_OPEN_BRACKET)) {
            int prev = levelManager.getCurrentLevel() - 1;
            levelIndicator.showLevel(prev);
            if (prev >= 1) goToLevel(prev);
        }

        if (freezeFrames > 0) {
            freezeFrames--;
            return;
        }

        handler.update();
        hud.update();
        camera.update(player);
        realityIndicator.update();
        levelIndicator.update();

        if (glitchTimer > 0) {
            glitchTimer--;
        }

        input.update();
    }

    private void render() {
        BufferStrategy bs = getBufferStrategy();
        if (bs == null) {
            createBufferStrategy(3);
            return;
        }

        Graphics g = bs.getDrawGraphics();
        Graphics2D g2d = (Graphics2D) g;

        int screenW = getWidth();
        int screenH = getHeight();

        g2d.setColor(Color.BLACK);
        g2d.fillRect(0, 0, screenW, screenH);
        AffineTransform originalTransform = g2d.getTransform();

        g2d.translate(-camera.getX() + camera.getOffsetX(), -camera.getY() + camera.getOffsetY());
        dualWorld.render(g2d, editMode, hoverTileX, hoverTileY, selectedTile);
        handler.render(g2d);
        g2d.setTransform(originalTransform);

        hud.render(g2d, screenH);
        realityIndicator.render(g2d, screenW);
        levelIndicator.render(g2d, screenW);

        if (inSaveMenu) {
            saveMenu.render(g2d, getWidth(), getHeight());
            g2d.dispose();
            bs.show();
            return;
        }

        if (glitchTimer > 0) {
            int shakeIntensity = 10;
            int dx = (int)(Math.random() * shakeIntensity - shakeIntensity / 2);
            int dy = (int)(Math.random() * shakeIntensity - shakeIntensity / 2);
            g2d.translate(dx, dy);

            int alpha = (int)(30.0 * ((float)glitchTimer / MAX_GLITCH_TIME));
            g2d.setColor(new Color(255, 255, 255, alpha));
            g2d.fillRect(0, 0, screenW, screenH);

            Composite original = g2d.getComposite();
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.2f));
            for (int i = 0; i < 10; i++) {
                int y = (int)(Math.random() * screenH);
                int height = (int)(Math.random() * 10 + 5);
                g2d.setColor(new Color(255, 0, 0, 60));
                g2d.fillRect(0, y, screenW, height);
                g2d.setColor(new Color(0, 255, 255, 60));
                g2d.fillRect(5, y + 1, screenW - 10, height);
            }

            for (int i = 0; i < 100; i++) {
                int x = (int)(Math.random() * screenW);
                int y = (int)(Math.random() * screenH);
                int a = (int)(Math.random() * 50 + 50);
                g2d.setColor(new Color(255, 255, 255, a));
                g2d.fillRect(x, y, 2, 2);
            }

            g2d.setComposite(original);
            g2d.setTransform(originalTransform);
        }

        g.dispose();
        bs.show();
    }

    public void freeze(int frames) {
        freezeFrames = frames;
    }

    public void nextLevel() {
        levelManager.loadNextLevel();
        levelIndicator.showLevel(levelManager.getCurrentLevel());
        dualWorld.setReality(Reality.A);

        Point spawn = dualWorld.getActiveWorld().findSpawnPoint();
        if (spawn == null) {
            spawn = new Point(100, 100);
        }
        player.setPosition(spawn.x, spawn.y);
        player.resetState();

        camera.update(player);
    }

    public void goToLevel(int levelIndex) {
        levelManager = new LevelManager(dualWorld);
        levelManager.setPlayer(player);
        levelManager.setLevel(levelIndex);
        levelManager.loadCurrentLevel();
        dualWorld.setReality(Reality.A);

        Point spawn = dualWorld.getActiveWorld().findSpawnPoint();
        player.setPosition(spawn.x, spawn.y);
        player.resetState();

        camera.update(player);
    }

    private boolean isCollidingWithSolidTiles(float x, float y, int[][] map) {
        int tileSize = World.TILE_SIZE;
        int top = (int)((y + 2) / tileSize);
        int bottom = (int)((y + 30) / tileSize);
        int left = (int)(x / tileSize);
        int right = (int)((x + 31) / tileSize);

        for (int row = top; row <= bottom; row++) {
            for (int col = left; col <= right; col++) {
                if (row < 0 || row >= map.length || col < 0 || col >= map[0].length) continue;
                if (map[row][col] == 1) return true;
            }
        }
        return false;
    }

    public void loadState(GameState state) {
        levelManager.setLevel(state.level);
        levelManager.loadCurrentLevel();
        Reality r = state.reality.equals("B") ? Reality.B : Reality.A;
        dualWorld.setReality(r);
        player.setPosition(state.playerX, state.playerY);
        player.setHealth(state.playerHealth);
        player.setBulletCount(state.bullets);
        player.setEnemiesKilled(state.enemiesKilled);
        camera.update(player);
    }

    public Player getPlayer() {
        return player;
    }

    public DualWorld getDualWorld() {
        return dualWorld;
    }

    public LevelManager getLevelManager() {
        return levelManager;
    }

    public Camera getCamera() {
        return camera;
    }
    
    public SaveMenu getSaveMenu() {
        return saveMenu;
    }
} 

package com.gametime.core;

import com.gametime.input.KeyInput;
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
import java.awt.image.BufferStrategy;
import java.io.File;
import java.util.ArrayList;
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

    
    private boolean editMode = false;

    
    private int freezeFrames = 0;


    public static final int WIDTH = 1280, HEIGHT = 720;

    private Handler handler;
    private Camera camera;
    public static Game instance;
    
    private int currentTile = 1;      // The tile type you're about to place
    private final int tileCount = 5;  // However many tile types you support (0 = empty, 1 = solid, etc)
    private int hoverTileX = -1, hoverTileY = -1;
    private int selectedTile = 1;
    private final int MAX_TILE_TYPE = 5;
    
    public Game() {
        Game.instance = this;
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setFocusable(true);
        requestFocusInWindow();
        setFocusTraversalKeysEnabled(false);

        dualWorld = new DualWorld();
        levelManager = new LevelManager(dualWorld); // still ok
        dualWorld.loadBothWorlds(levelManager.getLevelFilename());

        Point spawn = dualWorld.getActiveWorld().getFirstSolidTileBelow(100, 0);
        handler = new Handler();
        hud = new HUD();
        input = new KeyInput();
        camera = new Camera();

        player = new Player(spawn.x, spawn.y, hud, dualWorld, input, handler);
        levelManager.setPlayer(player); // Now that player is created
        handler.addObject(player);
        camera.update(player);
        
        realityIndicator = new RealityIndicator();
        levelIndicator = new LevelIndicator();
        
        //test bullet
        Bullet bullet = new Bullet(0, 0, player, handler);
        handler.addObject(bullet);
        addKeyListener(input);

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

                int mouseX = e.getX() + (int)camera.getX() - (int)camera.getOffsetX();
                int mouseY = e.getY() + (int)camera.getY() - (int)camera.getOffsetY();

                hoverTileX = mouseX / dualWorld.getActiveWorld().TILE_SIZE;
                hoverTileY = mouseY / dualWorld.getActiveWorld().TILE_SIZE;
            }
        });
        
        addMouseWheelListener(e -> {
            if (!editMode) return;

            selectedTile += e.getWheelRotation(); // scroll down = +1, up = -1
            if (selectedTile < 0) selectedTile = MAX_TILE_TYPE;
            if (selectedTile > MAX_TILE_TYPE) selectedTile = 0;

            System.out.println("Selected Tile: " + selectedTile);
        });
    }



    public synchronized void start() {
        running = true;
        thread = new Thread(this);
        thread.start();
    }

    public synchronized void stop() {
        running = false;
        try { thread.join(); } catch (InterruptedException e) { e.printStackTrace(); }
    }

    @Override
    public void run() {
        createBufferStrategy(3); // triple buffering
        long lastTime = System.nanoTime();
        double nsPerUpdate = 1_000_000_000.0 / 60.0;
        double delta = 0;

        while (running) {
            long now = System.nanoTime();
            delta += (now - lastTime) / nsPerUpdate;
            lastTime = now;

            while (delta >= 1) {
                update();   // Fixed-timestep logic update
                delta--;
            }

            render();       // Render as often as possible
        }

        stop();
    }

    private void update() {
    	if (input.isJustPressed(KeyEvent.VK_E)) {
    	    Reality target = (dualWorld.getCurrentReality() == Reality.A) ? Reality.B : Reality.A;
    	    int[][] targetMap = (target == Reality.A)
    	        ? dualWorld.getRealityA().getMap()
    	        : dualWorld.getRealityB().getMap();

    	    if (!isCollidingWithSolidTiles(player.getX(), player.getY(), targetMap)) {
    	        dualWorld.setReality(target);
    	        realityIndicator.showSuccess(target);  // ✅ visual feedback
    	    } else {
    	        realityIndicator.showBlocked();        // ❌ visual blocked cue
    	        Game.instance.getCamera().shake(4, 4); // optional feedback
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
        
        
        if (input.isJustPressed(KeyEvent.VK_CLOSE_BRACKET)) { // ]
            
        	this.nextLevel();
            player.respawnAtStart(); // optional method to reset player
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


        // Call input.update() LAST
        input.update();
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

    private void render() {
        BufferStrategy bs = getBufferStrategy();
        if (bs == null) {
            createBufferStrategy(3);
            return;
        }

        Graphics g = bs.getDrawGraphics();
        Graphics2D g2d = (Graphics2D) g;

        // Clear screen using actual canvas dimensions
        int screenWidth = getWidth();
        int screenHeight = getHeight();
        g2d.setColor(Color.BLACK);
        g2d.fillRect(0, 0, screenWidth, screenHeight);

        // Apply camera offset
        g2d.translate(-camera.getX() + camera.getOffsetX(), -camera.getY() + camera.getOffsetY());

        dualWorld.render(g2d, editMode, hoverTileX, hoverTileY, selectedTile);
        handler.render(g2d);

        // Reset translation
        g2d.translate(camera.getX(), camera.getY());

        hud.render(g2d);
        realityIndicator.render(g2d, getWidth());
        levelIndicator.render(g2d, getWidth());


        g.dispose();
        bs.show();
    }
    
    public void freeze(int frames) {
        freezeFrames = frames;
    }
    
    
    public void nextLevel() {
        levelManager.loadNextLevel(); // ✅ this loads both A and B
        levelIndicator.showLevel(levelManager.getCurrentLevel());


        System.out.println("Now loading: " + levelManager.getLevelFilename() + "_A.txt and _B.txt");
        
        dualWorld.setReality(Reality.A);

        Point spawn = dualWorld.getActiveWorld().findSpawnPoint();
        if (spawn == null) {
            System.err.println("⚠️ No spawn point found! Defaulting to (100, 100)");
            spawn = new Point(100, 100);
        }
        player.setPosition(spawn.x, spawn.y);
        player.resetState();

        camera.update(player);
    }
    
    public Camera getCamera() {
        return camera;
    }
    
    
}

package com.gametime.core;

import com.gametime.input.KeyInput;
import com.gametime.objects.Bullet;
import com.gametime.objects.GameObject;
import com.gametime.objects.Player;
import com.gametime.ui.*;
import com.gametime.World;
import com.gametime.Camera;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferStrategy;
import java.util.ArrayList;
import java.util.List;

public class Game extends Canvas implements Runnable {
    private Thread thread;
    private boolean running = false;
    private HUD hud;
    private World world;
    private Player player;
    private KeyInput input;

    public static final int WIDTH = 800, HEIGHT = 600;
    private Handler handler;
    private Camera camera;
    
    public Game() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));

        world = new World();
        handler = new Handler();
        hud = new HUD();
        
        camera = new Camera();
        
        input = new KeyInput();
        addKeyListener(input);

        player = new Player(100, 100, hud, world, input);
        handler.addObject(player);
        
        camera.update(player);
        
        handler.addObject(new Bullet(600, 400, player, handler)); // simple test bullet

        setFocusable(true);
        requestFocusInWindow();       // 👈 Tries to request focus
        requestFocus();               // 👈 Ensures focus even more aggressively

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
        handler.update();
        hud.update();
        camera.update(player);
        
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

        // Clear screen
        g2d.setColor(Color.BLACK);
        g2d.fillRect(0, 0, WIDTH, HEIGHT);

        // Apply camera offset
        g2d.translate(-camera.getX(), -camera.getY());

        world.render(g2d);
        handler.render(g2d);

        // Reset translation
        g2d.translate(camera.getX(), camera.getY());

        hud.render(g2d);

        g.dispose();
        bs.show();
    }
}

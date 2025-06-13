package com.gametime.objects;

import com.gametime.input.KeyInput;
import com.gametime.ui.HUD;
import com.gametime.World;

import java.awt.*;
import java.awt.event.KeyEvent;

public class Player extends GameObject {
    private float velX = 0, velY = 0;
    private final float gravity = 0.5f;
    private final float maxFallSpeed = 10;
    private final float jumpStrength = -10;
    private final int tileSize = World.TILE_SIZE;

    private boolean jumping = false;
    private boolean falling = true;

    private final KeyInput input;
    private final HUD hud;
    private final World world;

    private int health = 100;

    // 🐺 Coyote Time
    private final int coyoteTimeMax = 6;
    private int coyoteTimer = 0;

    // 🕊️ Jump Buffering
    private final int jumpBufferMax = 6;
    private int jumpBufferTimer = 0;

    public Player(float x, float y, HUD hud, World world, KeyInput input) {
        super(x, y, ID.PLAYER);
        this.hud = hud;
        this.world = world;
        this.input = input;
    }

    @Override
    public void update() {
   
        // -- Jump Buffering --
        if (input.isJustPressed(KeyEvent.VK_SPACE)) {
            jumpBufferTimer = jumpBufferMax;
        } else if (jumpBufferTimer > 0) {
            jumpBufferTimer--;
        }

        // -- Horizontal Movement --
        velX = 0;
        if (input.isPressed(KeyEvent.VK_A)) velX = -4;
        if (input.isPressed(KeyEvent.VK_D)) velX = 4;
        x += velX;

        // -- Vertical Movement --
        y += velY;

        if (falling || jumping) {
            velY += gravity;
            if (velY > maxFallSpeed) velY = maxFallSpeed;
        }

        // -- Ground Detection --
        if (onGround()) {
            y = ((int)((y + 32) / tileSize)) * tileSize - 32;
            velY = 0;
            falling = false;
            jumping = false;
            coyoteTimer = coyoteTimeMax;
        } else {
            coyoteTimer--;
            falling = true;
        }

        // -- Jump Trigger --
        if (jumpBufferTimer > 0 && coyoteTimer > 0 && !jumping) {
            jump();
            jumpBufferTimer = 0;
            coyoteTimer = 0;
        }

        // Sync health to HUD
        hud.setHealth(health);
    }

    @Override
    public void render(Graphics g) {
        g.setColor(Color.WHITE);
        g.fillRect((int) x, (int) y, 32, 32);
    }

    public void jump() {
        jumping = true;
        velY = jumpStrength;
    }

    public void damage(int amount) {
        health -= amount;
        if (health < 0) health = 0;
    }

    public float getX() { return x; }
    public float getY() { return y; }
    public float getVelX() { return velX; }
    public float getVelY() { return velY; }
    public int getHealth() { return health; }

    private boolean onGround() {
        int[][] map = world.getMap();
        int tileRows = map.length;
        int tileCols = map[0].length;

        int playerTileX = (int) ((x + 16) / tileSize);  // center of feet
        int playerTileY = (int) ((y + 32) / tileSize);  // bottom

        return playerTileY < tileRows && playerTileX >= 0 && playerTileX < tileCols
               && map[playerTileY][playerTileX] == 1;
    }
}

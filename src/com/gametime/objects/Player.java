package com.gametime.objects;

import com.gametime.input.KeyInput;
import com.gametime.*;
import com.gametime.core.*;
import com.gametime.ui.HUD;
import com.gametime.World;

import java.awt.*;
import java.awt.event.KeyEvent;

public class Player extends GameObject {
    private float velX = 0, velY = 0;
    private final float gravityUp = 0.4f, gravityDown = 0.8f;
    private final float maxFallSpeed = 10, jumpStrength = -10;
    private final float moveSpeed = 4.0f, acceleration = 0.5f, deceleration = 0.4f, friction = 0.2f;

    private boolean jumping = false, falling = true;
    private boolean jumpKeyHeld = false;
    private boolean slidingLeft = false, slidingRight = false;

    private int health = 100;
    private float scaleX = 1.0f, scaleY = 1.0f;
    private final float scaleRecoverRate = 0.1f;

    private final KeyInput input;
    private final HUD hud;
    private final DualWorld dualWorld;
    private final Handler handler;

    private final int tileSize = World.TILE_SIZE;
    private final float spawnX, spawnY;

    private final int coyoteTimeMax = 6, jumpBufferMax = 6;
    private int coyoteTimer = 0, jumpBufferTimer = 0;

    private boolean canDash = true;
    private int dashCooldown = 0, dashCooldownMax = 40, dashDuration = 0, dashDurationMax = 6, dashDir = 0;
    private final float dashSpeed = 12.0f;

    private int wallJumpCooldown = 0, wallJumpCooldownMax = 12;

    public Player(float x, float y, HUD hud, DualWorld dualWorld, KeyInput input, Handler handler) {
        super(x, y, ID.PLAYER);
        this.spawnX = x;
        this.spawnY = y;
        this.hud = hud;
        this.dualWorld = dualWorld;
        this.input = input;
        this.handler = handler;
    }

    @Override
    public void update() {
        checkWallSlide();
        if (wallJumpCooldown > 0) wallJumpCooldown--;

        if (input.isJustPressed(KeyEvent.VK_SPACE)) {
            jumpBufferTimer = jumpBufferMax;
            jumpKeyHeld = true;
        }
        if (!input.isPressed(KeyEvent.VK_SPACE)) jumpKeyHeld = false;

        float targetVelX = 0;
        if (input.isPressed(KeyEvent.VK_A)) targetVelX = -moveSpeed;
        if (input.isPressed(KeyEvent.VK_D)) targetVelX = moveSpeed;

        if (targetVelX != 0) {
            if (velX < targetVelX) velX = Math.min(velX + acceleration, targetVelX);
            else if (velX > targetVelX) velX = Math.max(velX - acceleration, targetVelX);
        } else {
            float slow = onGround() ? friction : deceleration;
            if (velX > 0) velX = Math.max(velX - slow, 0);
            else if (velX < 0) velX = Math.min(velX + slow, 0);
        }

        // Dash
        if (input.isJustPressed(KeyEvent.VK_SHIFT) && canDash && dashCooldown == 0) {
            dashDir = input.isPressed(KeyEvent.VK_A) ? -1 : input.isPressed(KeyEvent.VK_D) ? 1 : (velX >= 0 ? 1 : -1);
            velX = dashDir * dashSpeed;
            dashDuration = dashDurationMax;
            canDash = false;
            dashCooldown = dashCooldownMax;
            Game.instance.freeze(2);
            Game.instance.getCamera().shake(8, 5);
            for (int i = 0; i < 8; i++) ParticleSpawner.spawnDashTrail(x + 16, y + 16, velX >= 0, handler);
        }

        if (dashDuration > 0) {
            dashDuration--;
            velX = dashDir * dashSpeed;
        } else if (!canDash && onGround()) canDash = true;
        if (dashCooldown > 0) dashCooldown--;

        x += velX;
        if (checkHorizontalCollision()) {
            while (checkHorizontalCollision()) x -= Math.signum(velX);
            velX = 0;
        }

        // Jump buffering
        if (jumpBufferTimer > 0) jumpBufferTimer--;

     // Gravity
        boolean wasOnGround = onGround();  // BEFORE y is updated

        y += velY;

        // Apply gravity AFTER y update
        if (velY < 0) velY += gravityUp;
        else velY += gravityDown;
        if (velY > maxFallSpeed) velY = maxFallSpeed;
        if (jumping && velY < 0 && !jumpKeyHeld) velY += 0.6f;
        
        if ((slidingLeft || slidingRight) && !onGround() && velY > 1.5f) {
            velY = 1.5f;  // limit downward fall speed while sliding
        }

        if (checkVerticalCollision()) {
            boolean landedOnOneWay = false;
            boolean hardLanding = !wasOnGround && velY > 1.5f;

            // Check what tile we're landing on
            int[][] map = dualWorld.getActiveWorld().getMap();
            int feetRow = (int)((y + 32) / tileSize);
            int left = (int)((x + 2) / tileSize);
            int right = (int)((x + 30) / tileSize);

            for (int col = left; col <= right; col++) {
                if (feetRow >= 0 && feetRow < map.length && col >= 0 && col < map[0].length) {
                    if (map[feetRow][col] == 2) {
                        landedOnOneWay = true;
                        break;
                    }
                }
            }

            if (landedOnOneWay && velY >= 0) {
                // Snap to top of one-way platform surface
                y = feetRow * tileSize + tileSize / 2f - 32;
            } else {
                // Normal solid tile collision resolution
                while (checkVerticalCollision()) {
                    y -= Math.signum(velY);
                }
            }

            velY = 0;
            jumping = false;
            falling = false;
            coyoteTimer = coyoteTimeMax;

            if (hardLanding) {
                scaleY = 0.7f;
                scaleX = 1.4f;
                ParticleSpawner.spawnDust(x + 16, y + 32, handler);
                Game.instance.freeze(2);
            }
        } else {
            falling = true;
            coyoteTimer--;
        }

        // Wall Jump
        if (!onGround() && wallJumpCooldown == 0 && jumpBufferTimer > 0 && (slidingLeft || slidingRight)) {
            velX = slidingLeft ? moveSpeed : -moveSpeed;
            velY = jumpStrength;
            wallJumpCooldown = wallJumpCooldownMax;
            jumpBufferTimer = 0;

            ParticleSpawner.spawnWallJumpSparks(x + (slidingRight ? 32 : 0), y + 16, slidingRight, handler);
            return;  // Don't let a ground jump trigger too
        }

        // Ground jump
        if (jumpBufferTimer > 0 && coyoteTimer > 0 && !jumping) {
            jump();
            jumpBufferTimer = 0;
            coyoteTimer = 0;
        }

        // Goal detection
        if (isTouchingGoalTile()) {
            Game.instance.nextLevel();
            return;
        }

        if (y > dualWorld.getActiveWorld().getMap().length * tileSize + 200) respawn();

        hud.setHealth(health);
        scaleX += (1.0f - scaleX) * scaleRecoverRate;
        scaleY += (1.0f - scaleY) * scaleRecoverRate;
    }

    private boolean checkHorizontalCollision() {
        int[][] map = dualWorld.getActiveWorld().getMap();

        int top = (int)((y + 2) / tileSize), bottom = (int)((y + 30) / tileSize);
        int left = (int)((x) / tileSize), right = (int)((x + 31) / tileSize);
        for (int row = top; row <= bottom; row++)
            for (int col = left; col <= right; col++)
                if (inBounds(row, col) && map[row][col] == 1) return true;
        return false;
    }

    private boolean checkVerticalCollision() {
        int[][] map = dualWorld.getActiveWorld().getMap();
        int left = (int)((x + 2) / tileSize);
        int right = (int)((x + 30) / tileSize);
        int top = (int)(y / tileSize);
        int bottom = (int)((y + 31) / tileSize);

        for (int row = top; row <= bottom; row++) {
            for (int col = left; col <= right; col++) {
                if (row < 0 || row >= map.length || col < 0 || col >= map[0].length) continue;

                int tile = map[row][col];

                if (tile == 1) return true;

                if (tile == 2) {
                    // Only collide if falling and just landing on the platform
                    float playerFeet = y + 32;
                    float tileTop = row * tileSize + tileSize / 2f;

                    if (velY >= 0 && playerFeet <= tileTop + 2) return true;
                }
            }
        }

        return false;
    }

    private boolean inBounds(int row, int col) {
        int[][] map = dualWorld.getActiveWorld().getMap();

        return row >= 0 && row < map.length && col >= 0 && col < map[0].length;
    }

    private boolean isTouchingGoalTile() {
        int[][] map = dualWorld.getActiveWorld().getMap();

        int top = (int)(y / tileSize), bottom = (int)((y + 31) / tileSize);
        int left = (int)(x / tileSize), right = (int)((x + 31) / tileSize);
        for (int row = top; row <= bottom; row++)
            for (int col = left; col <= right; col++)
                if (inBounds(row, col) && map[row][col] == 5) return true;
        return false;
    }

    private void checkWallSlide() {
        int[][] map = dualWorld.getActiveWorld().getMap();

        int tileY = (int)((y + 16) / tileSize);
        slidingLeft = slidingRight = false;
        if (tileY < 0 || tileY >= map.length) return;
        if (input.isPressed(KeyEvent.VK_A) && (int)(x / tileSize) > 0 && map[tileY][(int)(x / tileSize) - 1] == 1) slidingLeft = true;
        if (input.isPressed(KeyEvent.VK_D) && (int)((x + 32) / tileSize) < map[0].length && map[tileY][(int)((x + 32) / tileSize)] == 1) slidingRight = true;
    }

    public void jump() {
        jumping = true;
        velY = jumpStrength;
        scaleY = 1.3f;
        scaleX = 0.8f;
    }
    
    private boolean onGround() {
        int[][] map = dualWorld.getActiveWorld().getMap();

        int tileRows = map.length;
        int tileCols = map[0].length;

        int leftFootX = (int) ((x + 4) / tileSize);
        int rightFootX = (int) ((x + 28) / tileSize); // assuming 32px wide player
        int footY = (int) ((y + 32) / tileSize); // bottom of player

        if (footY >= tileRows) return false;

        for (int tx = leftFootX; tx <= rightFootX; tx++) {
            if (tx >= 0 && tx < tileCols) {
                int tile = map[footY][tx];
                if (tile == 1) return true;

                if (tile == 2) {
                    float playerFeet = y + 32;
                    float tileTop = footY * tileSize + tileSize / 2;
                    if (velY >= 0 && playerFeet <= tileTop + 5) return true;
                }
            }
        }

        return false;
    }
    
    public void resetState() {
        velX = 0;
        velY = 0;
        jumping = false;
        falling = true;
        wallJumpCooldown = 0;
        dashCooldown = 0;
        dashDuration = 0;
        canDash = true;
        coyoteTimer = 0;
        jumpBufferTimer = 0;
        scaleX = 1.0f;
        scaleY = 1.0f;
    }


    public void damage(int amt) { health = Math.max(0, health - amt); }
    public float getDashCooldownPercent() { return 1.0f - (dashCooldown / (float) dashCooldownMax); }
    public boolean canDash() { return dashCooldown <= 0; }
    public float getX() { return x; }
    public float getY() { return y; }
    public int getHealth() { return health; }

    @Override
    public void render(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setColor(Color.WHITE);
        int drawX = (int)(x + 16 - (16 * scaleX));
        int drawY = (int)(y + 16 - (16 * scaleY));
        int drawW = (int)(32 * scaleX);
        int drawH = (int)(32 * scaleY);
        g2d.fillRect(drawX, drawY, drawW, drawH);
    }

    public void respawn() {
        x = spawnX;
        y = spawnY;
        velX = velY = 0;
        jumping = false;
        falling = true;
        coyoteTimer = coyoteTimeMax;
        health = 100;
        Game.instance.freeze(10);
        Game.instance.getCamera().shake(10, 3);
    }

    public void respawnAtStart() {
    	Point spawn = dualWorld.getActiveWorld().findSpawnPoint();
    	setPosition(spawn.x, spawn.y);
        velX = velY = 0;
        jumping = false;
        falling = true;
        coyoteTimer = 0;
        jumpBufferTimer = 0;
        scaleX = scaleY = 1.0f;
        health = 100;
    }

    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
    }
}

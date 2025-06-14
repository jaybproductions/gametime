package com.gametime.ui;

import java.awt.*;
import com.gametime.objects.Player;
import com.gametime.core.Game;

public class HUD {
    private int maxHealth = 100;
    private int targetHealth = 100;
    private float displayHealth = 100; // For smooth lerping
    private final float lerpSpeed = 0.1f;

    private Player player; // ✅ Reference to player

    public void setPlayer(Player player) {
        this.player = player;
    }

    public void setHealth(int health) {
        this.targetHealth = Math.max(0, Math.min(maxHealth, health));
    }

    public void update() {
        displayHealth += (targetHealth - displayHealth) * lerpSpeed;
    }

    public void render(Graphics2D g2d) {
        // --- Health Bar ---
        int healthBarWidth = 200;
        int healthBarHeight = 20;
        int healthX = 20;
        int healthY = 20;

        // Background
        g2d.setColor(new Color(60, 0, 0));
        g2d.fillRoundRect(healthX, healthY, healthBarWidth, healthBarHeight, 10, 10);

        // Smooth foreground
        int filledWidth = (int)((displayHealth / maxHealth) * healthBarWidth);
        g2d.setColor(new Color(255, 50, 50));
        g2d.fillRoundRect(healthX, healthY, filledWidth, healthBarHeight, 10, 10);

        // Border
        g2d.setColor(Color.WHITE);
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRoundRect(healthX, healthY, healthBarWidth, healthBarHeight, 10, 10);

        // --- Dash Cooldown Bar ---
        if (player != null) {
            float cooldownPercent = player.getDashCooldownPercent();

            int dashBarWidth = 100;
            int dashBarHeight = 10;
            int dashX = 20;
            int dashY = Game.HEIGHT - 30;

            // Background
            g2d.setColor(Color.DARK_GRAY);
            g2d.fillRoundRect(dashX, dashY, dashBarWidth, dashBarHeight, 6, 6);

            // Fill
            g2d.setColor(Color.CYAN);
            g2d.fillRoundRect(dashX, dashY, (int)(dashBarWidth * cooldownPercent), dashBarHeight, 6, 6);

            // Border
            g2d.setColor(Color.WHITE);
            g2d.drawRoundRect(dashX, dashY, dashBarWidth, dashBarHeight, 6, 6);
        }
    }
}

package com.gametime.ui;

import java.awt.*;

public class LevelIndicator {
    private int level = 1;
    private int timer = 0;
    private final int DURATION = 90; // ~1.5 seconds at 60fps

    public void showLevel(int level) {
        this.level = level;
        this.timer = DURATION;
    }

    public void update() {
        if (timer > 0) timer--;
    }

    public void render(Graphics2D g2d, int screenWidth) {
        if (timer <= 0) return;

        String text = "🗺️  Level " + level;
        int width = 180;
        int height = 40;
        int x = screenWidth / 2 - width / 2;
        int y = 80;

        // Fade effect
        float alpha = Math.min(1.0f, timer / (float) DURATION);
        Composite original = g2d.getComposite();
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));

        g2d.setColor(new Color(0, 0, 0, 160));
        g2d.fillRoundRect(x, y, width, height, 20, 20);

        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 20));
        g2d.drawString(text, x + 20, y + 26);

        g2d.setComposite(original);
    }
}

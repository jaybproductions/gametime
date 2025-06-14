package com.gametime.ui;

import com.gametime.Reality;

import java.awt.*;

public class RealityIndicator {
    private Reality currentReality;
    private boolean switchBlocked = false;
    private int feedbackTimer = 0;
    private final int FEEDBACK_MAX = 30;

    public void showSuccess(Reality reality) {
        currentReality = reality;
        switchBlocked = false;
        feedbackTimer = FEEDBACK_MAX;
    }

    public void showBlocked() {
        switchBlocked = true;
        feedbackTimer = FEEDBACK_MAX;
    }

    public void update() {
        if (feedbackTimer > 0) feedbackTimer--;
    }

    public void render(Graphics2D g2d, int screenWidth) {
        if (feedbackTimer <= 0) return;

        String text = switchBlocked ? "⛔ Switch Blocked" : "Reality " + currentReality;
        Color bgColor = switchBlocked ? new Color(200, 50, 50, 160) : new Color(50, 150, 255, 160);
        Color fgColor = Color.WHITE;

        int width = 200;
        int height = 40;
        int x = screenWidth / 2 - width / 2;
        int y = 40;

        g2d.setColor(bgColor);
        g2d.fillRoundRect(x, y, width, height, 20, 20);
        g2d.setColor(fgColor);
        g2d.setFont(new Font("Arial", Font.BOLD, 18));
        g2d.drawString(text, x + 20, y + 26);
    }
}

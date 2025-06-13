package com.gametime.ui;

import java.awt.*;

public class HUD {
    private int health = 100;  // we'll sync this with the Player class soon

    public void update() {
        // For now this could include animations, timers, etc.
        if (health < 0) health = 0;
        if (health > 100) health = 100;
    }

    public void render(Graphics g) {
        // Health bar background
        g.setColor(Color.DARK_GRAY);
        g.fillRect(15, 15, 200, 20);

        // Health bar (red -> green as health drops)
        g.setColor(new Color(75, (int)(health * 2.55), 0));
        g.fillRect(15, 15, health * 2, 20);

        // Health text
        g.setColor(Color.WHITE);
        g.drawRect(15, 15, 200, 20);
        g.drawString("Health: " + health, 15, 50);
    }

    public void setHealth(int health) {
        this.health = health;
    }

    public int getHealth() {
        return health;
    }
}

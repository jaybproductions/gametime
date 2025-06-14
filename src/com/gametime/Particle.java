package com.gametime;

import com.gametime.objects.GameObject;
import java.awt.*;
import com.gametime.core.Handler;

public class Particle extends GameObject {
    private float velX, velY;
    private float life;
    private Color color;
    private float size;

    private Handler handler;

    public Particle(float x, float y, float velX, float velY, float life, Color color, float size, Handler handler) {
        super(x, y, null);
        this.velX = velX;
        this.velY = velY;
        this.life = life;
        this.color = color;
        this.size = size;
        this.handler = handler;
    }

    @Override
    public void update() {
        x += velX;
        y += velY;
        life -= 0.05f;

        // Optional fade out
        if (life <= 0) {
            handler.removeObject(this);
        }
    }

    @Override
    public void render(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setColor(color);
        g2d.fillOval((int) x, (int) y, (int) size, (int) size);
    }
}
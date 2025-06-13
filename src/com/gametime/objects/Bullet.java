package com.gametime.objects;

import java.awt.*;
import com.gametime.core.Handler;

public class Bullet extends GameObject {
    private Player target;
    private float speed = 2;
    private Handler handler;

    public Bullet(float x, float y, Player target, Handler handler) {
        super(x, y, ID.BULLET);
        this.target = target;
        this.handler = handler;
    }

    @Override
    public void update() {
        float dx = target.getX() - x;
        float dy = target.getY() - y;
        float distance = (float) Math.sqrt(dx * dx + dy * dy);

        if (distance != 0) {
            x += speed * (dx / distance);
            y += speed * (dy / distance);
        }

        if (distance < 16) {
            target.damage(10); // bigger hit for demonstration

            // Remove this bullet
            handler.removeObject(this);

            // Respawn a new bullet at a random edge position
            float spawnX = (float)(Math.random() * 800);
            float spawnY = (float)(Math.random() * 600);
            handler.addObject(new Bullet(spawnX, spawnY, target, handler));
        }
    }

    @Override
    public void render(Graphics g) {
        g.setColor(Color.RED);
        g.fillOval((int) x, (int) y, 16, 16);
    }
}

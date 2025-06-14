package com.gametime;

import com.gametime.core.Handler;

import java.awt.*;
import java.util.Random;

public class ParticleSpawner {
    private static final Random rand = new Random();

    public static void spawnDust(float x, float y, Handler handler) {
        for (int i = 0; i < 6; i++) {
            float velX = (rand.nextFloat() - 0.5f) * 2;
            float velY = -rand.nextFloat() * 2;
            float size = rand.nextFloat() * 3 + 2;
            float life = rand.nextFloat() * 1.2f + 0.8f;

            handler.addObject(new Particle(x, y, velX, velY, life, new Color(200, 200, 200), size, handler));
        }
    }
    
    public static void spawnWallJumpSparks(float x, float y, boolean rightWall, Handler handler) {
        for (int i = 0; i < 6; i++) {
            float velX = rightWall ? -rand.nextFloat() * 2 : rand.nextFloat() * 2;
            float velY = (rand.nextFloat() - 0.5f) * 2;
            float size = rand.nextFloat() * 2 + 1;
            float life = rand.nextFloat() * 1.0f + 0.5f;

            handler.addObject(new Particle(x, y, velX, velY, life, new Color(255, 200, 50), size, handler));
        }
    }
    
    public static void spawnDashTrail(float x, float y, boolean facingRight, Handler handler) {
        for (int i = 0; i < 5; i++) {
            float offsetX = (float)(Math.random() * 6 - 3);  // jittered trail
            float offsetY = (float)(Math.random() * 6 - 3);

            float speedX = (facingRight ? -1 : 1) * (float)(Math.random() * 2 + 1);
            float speedY = (float)(Math.random() - 0.5);

            int life = (int)(Math.random() * 10 + 10); // short lifespan

            handler.addObject(new Particle(x + offsetX, y + offsetY, speedX, speedY, life, new Color(150, 200, 255, 180), 4, handler));
        }
    }
}
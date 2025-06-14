package com.gametime.core;

import com.gametime.objects.GameObject;

public class Camera {
    private float x, y;
    private float smoothing = 0.4f; // smaller = smoother/slower follow
    private int shakeDuration = 0;
    private float shakeIntensity = 0;
    private float offsetX = 0, offsetY = 0;

    public void update(GameObject target) {
        x = (int) (target.getX() - Game.WIDTH / 2.0f + 16);
        y = (int) (target.getY() - Game.HEIGHT / 2.0f + 16);

        if (shakeDuration > 0) {
            offsetX = (float) (Math.random() - 0.5) * shakeIntensity;
            offsetY = (float) (Math.random() - 0.5) * shakeIntensity;
            shakeDuration--;
        } else {
            offsetX = 0;
            offsetY = 0;
        }
    }
    
    public void shake(int duration, float intensity) {
        this.shakeDuration = duration;
        this.shakeIntensity = intensity;
    }

    public float getOffsetX() { return offsetX; }
    public float getOffsetY() { return offsetY; }

    public float getX() { return x; }
    public float getY() { return y; }
}

package com.gametime;

import com.gametime.objects.GameObject;

public class Camera {
    private float x, y;
    private float smoothing = 0.4f; // smaller = smoother/slower follow

    public void update(GameObject target) {
        // Target center position
        float targetX = target.getX() - 400;
        float targetY = target.getY() - 300;

        // Linear interpolation (LERP)
        x += (targetX - x) * smoothing;
        y += (targetY - y) * smoothing;

        // Clamp if needed
        if (x < 0) x = 0;
        if (y < 0) y = 0;
    }

    public float getX() { return x; }
    public float getY() { return y; }
}

package com.gametime.objects;

import java.awt.*;

public abstract class GameObject {
    protected float x, y;
    protected ID id;

    public GameObject(float x, float y, ID id) {
        this.x = x;
        this.y = y;
        this.id = id;
    }

    public abstract void update();
    public abstract void render(Graphics g);

    public float getX() { return x; }
    public float getY() { return y; }
    public ID getId() { return id; }
}

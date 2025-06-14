package com.gametime.core;

import com.gametime.objects.GameObject;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Handler {
    private final List<GameObject> objects = new ArrayList<>();

    public void update() {
    	for (int i = objects.size() - 1; i >= 0; i--) {
    	    GameObject obj = objects.get(i);
    	    obj.update();
    	}
    }

    public void render(Graphics g) {
        for (GameObject obj : objects) {
            obj.render(g);
        }
    }

    public void addObject(GameObject obj) {
        objects.add(obj);
    }

    public void removeObject(GameObject obj) {
        objects.remove(obj);
    }

    public List<GameObject> getObjects() {
        return objects;
    }
}

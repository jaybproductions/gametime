package com.gametime.input;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class KeyInput implements KeyListener {
    private final boolean[] keys = new boolean[256];
    private final boolean[] lastKeys = new boolean[256];

    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();
        if (code < keys.length) keys[code] = true;
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int code = e.getKeyCode();
        if (code < keys.length) keys[code] = false;
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    public void update() {
        // Copy VALUES, not reference
        for (int i = 0; i < keys.length; i++) {
            lastKeys[i] = keys[i];
        }
    }

    public boolean isPressed(int keyCode) {
        return keyCode < keys.length && keys[keyCode];
    }

    public boolean isJustPressed(int keyCode) {
        if (keyCode >= keys.length) return false;
        return keys[keyCode] && !lastKeys[keyCode];
    }
}

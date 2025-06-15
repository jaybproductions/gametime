package com.gametime.io;

import com.gametime.core.Game;

public class GameState {
    public int level;
    public float playerX, playerY;
    public int playerHealth;
    public int bullets;
    public int enemiesKilled;
    public String reality; // "A" or "B"
    public int slotNumber;

    public int getSlotNumber() {
        return slotNumber;
    }

    public void setSlotNumber(int slotNumber) {
        this.slotNumber = slotNumber;
    }

    // Add more fields as needed (e.g., inventory, powerups, abilities, etc.)
    
    public static GameState fromGame(Game game, int slotNumber) {
        GameState state = new GameState();

        state.level = game.getLevelManager().getCurrentLevel();
        state.playerX = game.getPlayer().getX();
        state.playerY = game.getPlayer().getY();
        state.playerHealth = game.getPlayer().getHealth();
        state.bullets = game.getPlayer().getBulletCount(); // ← if tracked
        state.enemiesKilled = game.getPlayer().getEnemiesKilled(); // ← if tracked
        state.reality = game.getDualWorld().getCurrentReality().name(); // "A" or "B"
        state.slotNumber = slotNumber;

        return state;
    }
}

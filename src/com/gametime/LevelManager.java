package com.gametime;

import com.gametime.objects.Player;

public class LevelManager {
    private int currentLevel = 1;
    private final DualWorld dualWorld;
    private Player player;

    public LevelManager(DualWorld dualWorld) {
        this.dualWorld = dualWorld;
    }

    public String getLevelFilename() {
        return "levels/level" + currentLevel;
    }

    public void loadCurrentLevel() {
        dualWorld.loadBothWorlds(getLevelFilename());
        if (player != null) player.respawnAtStart();
    }

    public void loadNextLevel() {
        currentLevel++;
        loadCurrentLevel();
    }

    public void restartLevel() {
        loadCurrentLevel();
    }

    public int getCurrentLevel() {
        return currentLevel;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }
    
    public void setLevel(int level) {
        currentLevel = level;
    }
}

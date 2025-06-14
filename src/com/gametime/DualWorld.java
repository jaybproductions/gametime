package com.gametime;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.File;

public class DualWorld {
    private final World realityA;
    private final World realityB;
    private Reality currentReality;

    public DualWorld() {
        realityA = new World();
        realityB = new World();
        currentReality = Reality.A;
    }

    public void loadLevels(String fileA, String fileB) {
        realityA.loadFromFile(fileA);
        realityB.loadFromFile(fileB);
    }

    public void toggleReality() {
        currentReality = (currentReality == Reality.A) ? Reality.B : Reality.A;
    }

    public Reality getCurrentReality() {
        return currentReality;
    }

    public World getActiveWorld() {
        return (currentReality == Reality.A) ? realityA : realityB;
    }

    public World getInactiveWorld() {
        return (currentReality == Reality.A) ? realityB : realityA;
    }

    public void render(Graphics2D g2d, boolean editMode, int hoverTileX, int hoverTileY, int selectedTile) {
        // Inactive world (faded)
        Composite original = g2d.getComposite();
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f));
        getInactiveWorld().render(g2d, false, -1, -1, 0);
        g2d.setComposite(original);

        // Active world (normal)
        getActiveWorld().render(g2d, editMode, hoverTileX, hoverTileY, selectedTile);
    }
    
    public void saveBothWorlds(String baseName) {
        realityA.saveToFile(baseName + "_A.txt");
        realityB.saveToFile(baseName + "_B.txt");
    }

    public void loadBothWorlds(String baseName) {
        File fileA = new File(baseName + "_A.txt");
        File fileB = new File(baseName + "_B.txt");

        if (fileA.exists()) {
            realityA.loadFromFile(fileA.getPath());
        } else {
            System.out.println("Missing " + fileA.getPath() + " — creating blank Reality A");
            realityA.createBlankLevel();
            realityA.saveToFile(fileA.getPath());  // Optional: auto-save
        }

        if (fileB.exists()) {
            realityB.loadFromFile(fileB.getPath());
        } else {
            System.out.println("Missing " + fileB.getPath() + " — creating blank Reality B");
            realityB.createBlankLevel();
            realityB.saveToFile(fileB.getPath());  // Optional: auto-save
        }
    }
    
    public void setReality(Reality reality) {
        this.currentReality = reality;
    }
    
    public World getRealityA() {
        return realityA;
    }

    public World getRealityB() {
        return realityB;
    }
}

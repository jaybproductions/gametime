package com.gametime.io;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;

import com.gametime.core.Game;
import com.gametime.input.KeyInput;

public class SaveMenu {
    private int selectedSlot = 0;
    private static final int SLOT_COUNT = 3;
    private int currentLoadedSlot = -1;
    private String statusMessage = "";
    private int statusTimer = 0;

    public void showStatus(String message, int duration) {
        this.statusMessage = message;
        this.statusTimer = duration;
    }

    public void render(Graphics2D g2d, int width, int height) {
    	g2d.setFont(new Font("Dialog", Font.BOLD, 16)); // explicitly reset font
    	String[] slots = {"Save Slot 1", "Save Slot 2", "Save Slot 3"};
    	for (int i = 0; i < slots.length; i++) {
    	    int x = width / 2 - 80;
    	    int y = height / 2 - 30 + i * 40;

    	    if (i == selectedSlot) {
    	        g2d.setColor(Color.YELLOW);
    	        g2d.fillRect(x - 10, y - 20, 180, 30);
    	        g2d.setColor(Color.BLACK); // Black text on yellow highlight
    	    } else {
    	        g2d.setColor(Color.WHITE); // White text on black background
    	    }

    	    g2d.drawString(slots[i], x, y);

    	    // Optional checkmark for loaded slot
    	    if (i + 1 == currentLoadedSlot) {
    	    	g2d.setColor(Color.GREEN); // explicitly reset color
    	    	g2d.drawString("✓", x - 40, y); // or wherever your checkmark is
    	    }
    	}
    }

    public void update(KeyInput input) {
        if (input.isJustPressed(KeyEvent.VK_UP)) selectedSlot = (selectedSlot + SLOT_COUNT - 1) % SLOT_COUNT;
        if (input.isJustPressed(KeyEvent.VK_DOWN)) selectedSlot = (selectedSlot + 1) % SLOT_COUNT;
        if (input.isJustPressed(KeyEvent.VK_ENTER)) {
            int slotToLoad = selectedSlot + 1;
            GameState state = SaveManager.load(slotToLoad);
            if (state != null) {
                Game.instance.loadState(state);
                markSlotLoaded(slotToLoad); // <- Update the visual marker
                showStatus("Loaded slot " + slotToLoad, 120);
            }
        }
        
        if (statusTimer > 0) statusTimer--;
    }

    public int getSelectedSlot() {
        return selectedSlot + 1;
    }
    
    public void markSlotLoaded(int slot) {
        System.out.println("Marked loaded slot: " + slot);
        this.currentLoadedSlot = slot;
    }
}

package com.gametime.io;

import java.io.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


public class SaveManager {
    private static final String SAVE_DIR = "saves/";

    public static void save(GameState state, int slot) {
        try {
            File dir = new File(SAVE_DIR);
            if (!dir.exists()) dir.mkdirs();

            FileWriter writer = new FileWriter(SAVE_DIR + "save_slot_" + slot + ".json");
            state.setSlotNumber(slot); // ✅ Track which slot was saved
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(state, writer);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static GameState load(int slot) {
        try (FileReader reader = new FileReader(SAVE_DIR + "save_slot_" + slot + ".json")) {
            return new Gson().fromJson(reader, GameState.class);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean saveExists(int slot) {
        return new File(SAVE_DIR + "save_slot_" + slot + ".json").exists();
    }
    
    public static GameState loadMostRecent() {
        for (int i = 1; i <= 3; i++) {
            File file = getSlotFile(i);
            if (file.exists()) {
                GameState state = load(i);
                if (state != null) return state;
            }
        }
        return null;
    }
    
    private static File getSlotFile(int slot) {
        return new File(SAVE_DIR + "save_slot_" + slot + ".json");
    }
}

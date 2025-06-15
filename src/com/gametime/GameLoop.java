package com.gametime;

import com.gametime.core.Game;
import com.gametime.io.GameState;
import com.gametime.io.SaveManager;

import java.awt.BorderLayout;
import javax.swing.*;

public class GameLoop {
    public static void main(String[] args) {
        JFrame frame = new JFrame("Game Time");
        Game game = new Game();

        frame.setUndecorated(false);
        frame.setLayout(new BorderLayout());
        frame.add(game, BorderLayout.CENTER);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);

        frame.setVisible(true);
        game.start();

        // ✅ Load the most recent save *after* starting the game
        GameState state = SaveManager.loadMostRecent();
        if (state != null) {
            game.loadState(state);
            game.getSaveMenu().markSlotLoaded(state.getSlotNumber());
        }
    }
}

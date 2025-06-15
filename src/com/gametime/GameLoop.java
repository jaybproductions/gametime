package com.gametime;

import com.gametime.core.Game;
import com.gametime.ui.MainMenu;

import javax.swing.*;
import java.awt.*;

public class GameLoop {
    public static void main(String[] args) {
        JFrame frame = new JFrame("Game Time");

        frame.setUndecorated(false);
        frame.setLayout(new BorderLayout());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Set preferred initial size (optional)
        frame.setPreferredSize(new Dimension(Game.WIDTH, Game.HEIGHT));

        // Center the window on screen before making it visible
        frame.pack();
        frame.setLocationRelativeTo(null); // ✅ THIS centers it
        frame.setVisible(true);

        // Load the main menu
        MainMenu menu = new MainMenu(frame);
        frame.add(menu, BorderLayout.CENTER);
        frame.revalidate();
    }
}
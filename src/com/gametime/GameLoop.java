package com.gametime;

import com.gametime.core.Game;

import javax.swing.*;

public class GameLoop {
    public static void main(String[] args) {
        JFrame frame = new JFrame("My Java Game");
        Game game = new Game();

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.add(game);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        game.start();
    }
}

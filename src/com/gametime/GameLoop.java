package com.gametime;

import com.gametime.core.Game;

import java.awt.BorderLayout;

import javax.swing.*;

public class GameLoop {
    public static void main(String[] args) {
    	JFrame frame = new JFrame("Game Time");
    	Game game = new Game();
    	
    	frame.setUndecorated(false); // Set true to remove title bar
    	frame.setLayout(new BorderLayout());
    	frame.add(game, BorderLayout.CENTER);
    	frame.pack();
    	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    	frame.setExtendedState(JFrame.MAXIMIZED_BOTH); // Fullscreen window

    	frame.setVisible(true);
    	

    	game.start();
    }
}

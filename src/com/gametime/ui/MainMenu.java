package com.gametime.ui;

import com.gametime.io.GameState;
import com.gametime.io.SaveManager;
import com.gametime.core.Game;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainMenu extends JPanel {
    private final JFrame frame;

    public MainMenu(JFrame frame) {
        this.frame = frame;
        setLayout(new GridLayout(4, 1, 20, 20));
        setBorder(BorderFactory.createEmptyBorder(100, 300, 100, 300));

        JLabel title = new JLabel("Select Save Slot", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 24));
        add(title);

        for (int i = 1; i <= 3; i++) {
            int slot = i;
            JButton btn = new JButton(buildSlotLabel(slot));
            btn.setFont(new Font("Arial", Font.PLAIN, 18));
            btn.addActionListener(e -> startGameWithSlot(slot));
            add(btn);
        }
    }

    private String buildSlotLabel(int slot) {
        if (SaveManager.saveExists(slot)) {
            GameState state = SaveManager.load(slot);
            String level = (state != null && state.level > 0) ? "Level " + state.level : "Unknown";
            String timestamp = getTimestamp(slot);
            return "Slot " + slot + " - " + level + " - " + timestamp;
        } else {
            return "Slot " + slot + " - New Game";
        }
    }

    private String getTimestamp(int slot) {
        try {
            File f = new File("saves/save_slot_" + slot + ".json");
            long modified = Files.getLastModifiedTime(f.toPath()).toMillis();
            return new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date(modified));
        } catch (Exception e) {
            return "Unknown";
        }
    }

    private void startGameWithSlot(int slot) {
        frame.getContentPane().removeAll();

        Game game = new Game(slot);
        frame.add(game, BorderLayout.CENTER);

        frame.pack();
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.revalidate();
        frame.repaint();

        // ✅ Ensure canvas has focus after it's part of the visible frame
        SwingUtilities.invokeLater(game::requestFocusInWindow);

        game.start();
    }
}

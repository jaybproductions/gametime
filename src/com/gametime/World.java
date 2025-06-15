package com.gametime;

import java.awt.*;
import java.io.*;


//Tile meanings:
//0 = empty
//1 = solid
//2 = one-way platform
//3 = reserved/future
//4 = spawn point
//5 = level endpoint

public class World {
    public static final int TILE_SIZE = 32;
    private int[][] map;

    public World() {
        map = new int[50][100]; // default size (rows x cols)
    }

    public int[][] getMap() {
        return map;
    }

    public void cycleTile(int x, int y) {
        if (y >= 0 && y < map.length && x >= 0 && x < map[0].length) {
        	int nextTile = (map[y][x] + 1) % 7; // 0–6, including tile 6 ✅

            if (nextTile == 4) {
                // Clear any existing spawn tile
                for (int row = 0; row < map.length; row++) {
                    for (int col = 0; col < map[0].length; col++) {
                        if (map[row][col] == 4) map[row][col] = 0;
                    }
                }
            }

            map[y][x] = nextTile;
        }
    }
    
    public void render(Graphics2D g2d, boolean editMode, int hoverTileX, int hoverTileY, int selectedTile) {
        // -- Draw All Tiles --
        for (int row = 0; row < map.length; row++) {
            for (int col = 0; col < map[0].length; col++) {
                int tile = map[row][col];
                int x = col * TILE_SIZE;
                int y = row * TILE_SIZE;

                if (tile == 1) {
                    g2d.setColor(Color.DARK_GRAY);
                    g2d.fillRect(x, y, TILE_SIZE, TILE_SIZE);
                } else if (tile == 2) {
                    g2d.setColor(Color.LIGHT_GRAY);
                    g2d.fillRect(x, y + TILE_SIZE / 2, TILE_SIZE, TILE_SIZE / 2);
                } else if (tile == 6) { // spikes
                    g2d.setColor(Color.RED);
                    int spikePadding = 6;
                    g2d.fillPolygon(
                        new int[]{x + spikePadding, x + TILE_SIZE / 2, x + TILE_SIZE - spikePadding},
                        new int[]{y + TILE_SIZE, y + TILE_SIZE / 2, y + TILE_SIZE},
                        3
                    );
                }

                // Only show outlines in edit mode
                if (editMode) {
                    if (tile == 4) {
                        g2d.setColor(Color.GREEN);
                        g2d.drawRect(x, y, TILE_SIZE, TILE_SIZE);
                    } else if (tile == 5) {
                        g2d.setColor(Color.YELLOW);
                        g2d.drawRect(x, y, TILE_SIZE, TILE_SIZE);
                    } else if (tile == 6) {
                        g2d.setColor(Color.MAGENTA); // Or orange, red, etc.
                        g2d.drawRect(x, y, TILE_SIZE, TILE_SIZE);
                    }
                }
            }
        }

        // -- Hover Preview (Only Once) --
        if (editMode && hoverTileX >= 0 && hoverTileY >= 0) {
            int px = hoverTileX * TILE_SIZE;
            int py = hoverTileY * TILE_SIZE;

            Composite original = g2d.getComposite();
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.4f));

            switch (selectedTile) {
                case 1 -> g2d.setColor(Color.DARK_GRAY);
                case 2 -> g2d.setColor(Color.LIGHT_GRAY);
                case 4 -> g2d.setColor(Color.GREEN);  // Spawn
                case 5 -> g2d.setColor(Color.YELLOW); // Goal
                case 6 -> g2d.setColor(Color.MAGENTA); // for spike preview
                default -> g2d.setColor(Color.RED);
            }

            g2d.fillRect(px, py, TILE_SIZE, TILE_SIZE);
            g2d.setComposite(original);
        }

        // -- Grid Overlay --
        if (editMode) {
            g2d.setColor(new Color(100, 100, 100, 100));
            for (int row = 0; row < map.length; row++) {
                for (int col = 0; col < map[0].length; col++) {
                    int x = col * TILE_SIZE;
                    int y = row * TILE_SIZE;
                    g2d.drawRect(x, y, TILE_SIZE, TILE_SIZE);
                }
            }
        }
    }


    public void saveToFile(String filename) {
        try {
            File file = new File(filename);

            // 🔧 Ensure the parent directory exists
            File parentDir = file.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs(); // create levels/ if it doesn't exist
            }

            PrintWriter writer = new PrintWriter(new FileWriter(file));
            for (int[] row : map) {
                for (int col = 0; col < row.length; col++) {
                    writer.print(row[col]);
                    if (col < row.length - 1) writer.print(",");
                }
                writer.println();
            }
            writer.close();
            System.out.println("Map saved to " + file.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("Failed to save map: " + e.getMessage());
        }
    }

    public void loadFromFile(String filename) {
        File file = new File(filename);

        if (!file.exists()) {
            System.out.println("File not found: " + filename + " — creating a blank level.");
            createBlankLevel(); // initialize blank map
            saveToFile(filename); // save it immediately
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            int row = 0;
            while ((line = reader.readLine()) != null && row < map.length) {
                String[] tokens = line.split(",");
                for (int col = 0; col < tokens.length && col < map[0].length; col++) {
                    map[row][col] = Integer.parseInt(tokens[col].trim());
                }
                row++;
            }
            System.out.println("Map loaded from " + filename);
        } catch (IOException e) {
            System.err.println("Failed to load map: " + e.getMessage());
        }
    }
    
    
    public void createBlankLevel() {
        for (int y = 0; y < map.length; y++) {
            for (int x = 0; x < map[0].length; x++) {
                map[y][x] = 0; // default to air
            }
        }
    }

    
    
    public Point getFirstSolidTileBelow(float x, float y) {
        int col = (int)(x / TILE_SIZE);
        int row = (int)(y / TILE_SIZE);

        for (int r = row; r < map.length; r++) {
            if (map[r][col] == 1) {
                return new Point(col * TILE_SIZE, (r - 1) * TILE_SIZE); // 1 tile above ground
            }
        }

        return new Point((int)x, (int)y); // fallback
    }
    
    
    public Point findSpawnPoint() {
        for (int y = 0; y < map.length; y++) {
            for (int x = 0; x < map[0].length; x++) {
                if (map[y][x] == 4) {
                    return new Point(x * TILE_SIZE, y * TILE_SIZE);
                }
            }
        }
        // Default if not found
        return new Point(100, 100);
    }
    
    public void setTile(int x, int y, int tileType) {
        if (y >= 0 && y < map.length && x >= 0 && x < map[0].length) {
            map[y][x] = tileType;
        }
    }
}

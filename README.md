# Split Reality Platformer

A 2D Java platformer with smooth movement, dual-layer reality switching, save/load functionality, and an in-game level editor.

---

## 🎮 Gameplay Features

- **Reality Switching**: Toggle between two overlapping realities (A & B) — each with its own tilemap, hazards, and paths.
- **Smooth Movement**: Refined jump, wall-slide, and wall-jump mechanics with responsive controls.
- **Save System**:
  - Three save slots
  - Game auto-saves on level transitions
  - In-game Save Menu (ESC) with slot switching
- **Main Menu**:
  - Opens on launch
  - Lets players select or continue a save slot
  - Displays last played level and save time
- **Level System**:
  - Dynamic level loading via `LevelManager`
  - Dual-world tilemaps (`levelX_A.txt` and `levelX_B.txt`)
- **HUD & UI**:
  - Health, bullets, enemies killed
  - Current level and reality indicators
  - Status messages and notifications

---

## 🛠️ Developer Features

- **In-Game Level Editor**:
  - Toggle with `TAB`
  - Scroll to change tile type
  - Click to paint tiles
  - Press `F5` to save both worlds
- **Flexible Spawn System**:
  - Each level auto-detects a spawn point (`findSpawnPoint()`)
- **Respawn Logic**:
  - If player falls off map, they respawn at spawn point with full reset

---

## ⌨️ Controls

| Action              | Key            |
|---------------------|----------------|
| Move                | Arrow keys / A/D |
| Jump                | Space           |
| Switch Reality      | E               |
| Toggle Edit Mode    | TAB             |
| Save Level (Edit)   | F5              |
| Toggle Save Menu    | ESC             |
| Load Selected Slot  | ENTER (while in Save Menu) |
| Next Level          | ]               |
| Previous Level      | [               |

---

## 📁 Project Structure (Key Classes)

- `Game.java`: Core game class with update/render loop
- `MainMenu.java`: Pre-game UI to select save slot
- `SaveManager.java`: GSON-based save/load system
- `GameState.java`: Serializable game state structure
- `DualWorld.java`: Handles two separate tilemaps (Reality A and B)
- `LevelManager.java`: Controls loading and switching levels
- `Player.java`: Handles player physics, state, and controls
- `SaveMenu.java`: In-game save/load interface
- `RealityIndicator.java`, `LevelIndicator.java`, `HUD.java`: UI overlays

---

## 🚧 In Progress / Coming Soon

- Checkpoints and mid-level auto-saves
- Dynamic enemies and bosses that phase between realities
- Puzzle objects tied to layer logic
- Level complete transitions
- Background music and sound FX

---

## 🧠 Requirements

- Java 17+
- Desktop platform with standard graphics support
- No external dependencies beyond standard Java + GSON

---

## 📝 Credits

Built by Chris Blair.

---


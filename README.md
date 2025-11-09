# Plot Plugin for Minecraft 1.21.8

Plugin for plot management with custom world generation.

Join the [discord](https://discord.gg/m3s6Ey3EcM) server to receive any help!

## Features

- **100x100 plot world generation**: Plots are automatically generated when new chunks are created
- **Separation roads**: Each plot is separated by 2 columns of stone slabs in each direction
- **Claim system**: Players can claim random plots near other plots
- **Member management**: Add or remove players from your plots
- **Flag system**: Control who can break and place blocks in your plot
- **Notifications**: When you enter another player's plot, you receive a notification

## Installation

1. Compile the plugin with Maven:
   ```bash
   mvn clean package
   ```

2. Copy the generated JAR file (`target/Plot-1.0.0.jar`) to your server's `plugins` folder

3. **IMPORTANT**: To apply the generator to the main world "world", edit the `bukkit.yml` file in the server root:
   ```yaml
   worlds:
     world:
       generator: Plot
   ```
   
   **NOTE**: If the "world" already exists, you must delete it before restarting the server. The generator only works on new worlds.
   
   To delete the existing world:
   - Stop the server
   - Delete the `world`, `world_nether`, `world_the_end` folders
   - Add the configuration to `bukkit.yml`
   - Restart the server

4. Restart the server - the "world" will be automatically generated with plots!

## Commands

### `/plot claim`
Claims a random plot near other plots. Automatically teleports you to the center of the claimed plot.

**IMPORTANT**: Each player can have **only 1 plot**. If you try to claim a second plot, you will receive an error message.

### `/plot tp`
Teleports to your plot. Useful for quickly returning to your land.

### `/plot info`
Shows information about the plot you are standing in:
- Plot ID
- Owner
- Members
- Active flags

### `/plot add <player>`
Adds a player to your plot. Members can build in the plot.

### `/plot remove <player>`
Removes a player from your plot.

### `/plot flag set <flag> <value>`
Sets plot flags. Available flags:
- `break`: Allows non-members to break blocks (default: false)
- `place`: Allows non-members to place blocks (default: false)

Example: `/plot flag set break true`

### `/plot reset`
Resets your plot, removing the owner, members, and restoring default flags.

### `/plot admin` (Admin Only)
Administrative commands to manage plots:
- `/plot admin reset` - Resets any plot (including other players' plots)
- `/plot admin setowner <player>` - Sets the owner of a plot
- `/plot admin delete` - Deletes and resets a plot

**Note**: Admins with `plot.admin` permission can modify blocks in any plot.

## Permissions

- `plot.claim` - Allows claiming plots (default: true)
- `plot.tp` - Allows teleporting to your own plot (default: true)
- `plot.info` - Allows viewing plot info (default: true)
- `plot.add` - Allows adding members (default: true)
- `plot.remove` - Allows removing members (default: true)
- `plot.flag` - Allows setting flags (default: true)
- `plot.reset` - Allows resetting plots (default: true)
- `plot.admin` - Admin permissions to manage all plots (default: op)

## World Structure

The world is completely **flat** with the following structure:

- **Plots**: 100x100 blocks with grass surface
- **Roads**: 2 blocks wide made of smooth stone slabs (2 columns in height)
- **Vertical levels**:
  - Y=0: Bedrock (indestructible base)
  - Y=1 to Y=62: Dirt
  - Y=63: Grass Block (plots) / Smooth Stone Slab (roads)
  - Y=64: Smooth Stone Slab (roads only, second column)
- **Spawn height**: Y=64 (plot center)

## Protections

- Cannot build on roads
- Only the owner and members can build in a plot
- Flags control non-member actions
- Plot data is automatically saved in `plugins/Plot/plots.yml`

## Development

**Package**: `com.scala`  
**Name**: Plot  
**Version**: 1.0.0  
**Minecraft API**: 1.21  
**Java**: 21

## Project Structure

```
src/main/java/com/scala/plot/
├── PlotPlugin.java              # Main class
├── commands/
│   └── PlotCommand.java         # Command handling
├── generator/
│   └── PlotWorldGenerator.java  # World generator
├── listeners/
│   ├── BlockListener.java       # Block protection
│   └── PlayerMoveListener.java  # Movement notifications
├── managers/
│   └── PlotManager.java         # Plot management and saving
└── model/
    ├── Plot.java                # Plot model
    ├── PlotId.java              # Plot identifier
    └── PlotFlag.java            # Plot flags
```

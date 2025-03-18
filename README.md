<h1 align="center">ResourceWorldResetter</h1>

<p align="center">
  <img src="https://files.catbox.moe/xhfveh.png" alt="project-image">
</p>

## Overview
ResourceWorldResetter is a Spigot/Bukkit plugin designed to automatically manage and reset a designated resource world at scheduled intervals. It's perfect for Minecraft servers that want to provide fresh resources to players without completely resetting the entire server.

## Features
- **Automatic World Creation**: Automatically creates a "Resources" world if it doesn't exist
- **Scheduled Resets**: Resets the resource world on a configurable schedule
- **Warning System**: Warns players before a reset occurs
- **Player Safety**: Automatically teleports players out of the resource world before reset
- **Admin Commands**: Full set of commands to control all aspects of the plugin
- **Multiverse Integration**: Leverages Multiverse-Core for stable world management

## Requirements
- Minecraft 1.21.4
- Java 17 or higher
- Multiverse-Core 4.3.1 or higher

## Installation
1. Download the latest release from the [Releases](https://github.com/yourusername/ResourceWorldResetter/releases) page
2. Place the JAR file in your server's `plugins` folder
3. Start or restart your server
4. The plugin will create a default configuration and a "Resources" world automatically

## Configuration (config.yml)
```yaml
worldName: "Resources"    # The world to reset (defaults to "Resources")
resetWarningTime: 5       # Time (in minutes) to warn players before reset
resetInterval: 86400      # Time (in seconds) between resets (e.g., 24 hours = 86400)
restartTime: 3            # Time of day to reset (3 AM by default)
```

## Commands
| Command | Description | Permission |
|---------|-------------|------------|
| `/setworld <worldname>` | Set the resource world name | resourceworldresetter.admin |
| `/setresetinterval <hours>` | Set the reset interval in hours | resourceworldresetter.admin |
| `/setrestarttime <hour>` | Set the daily reset time (24hr format) | resourceworldresetter.admin |
| `/resetworld` | Force an immediate reset of the resource world | resourceworldresetter.admin |
| `/reloadworldresetter` | Reload the plugin configuration | resourceworldresetter.admin |

## Permissions
- `resourceworldresetter.admin` - Allows access to all plugin commands

## What's New in Version 2.0
- Updated to support Minecraft 1.21.4
- Now requires Java 17
- Automatically creates the resource world named "Resources"
- Improved error handling and user feedback
- Added prefix to all plugin messages
- Better scheduling of reset events
- Improved world reset process

## Building from Source
This project uses Gradle:

```bash
# Clone the repository
git clone https://github.com/yourusername/ResourceWorldResetter.git

# Navigate to the project directory
cd ResourceWorldResetter

# Build with Gradle
./gradlew build
```

## Support
If you encounter any issues or have questions, please create an issue on the GitHub repository.

## License
This project is licensed under BSD 3-Clause License - see the LICENSE file for details.

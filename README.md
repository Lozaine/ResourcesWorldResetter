<h1 align="center">ResourceWorldResetter</h1>

<p align="center">
  <img src="https://files.catbox.moe/xhfveh.png" alt="project-image">
</p>

<p align="center">
  Automatically resets resources in one world regularly for fresh materials without affecting others!
</p>

<p align="center">
  <a href="https://github.com/Lozaine/ResourceWorldResetter">
    <img src="https://img.shields.io/github/v/release/Lozaine/ResourceWorldResetter?style=for-the-badge" alt="GitHub release">
  </a>
  <a href="https://github.com/Lozaine/ResourceWorldResetter/issues">
    <img src="https://img.shields.io/github/issues/Lozaine/ResourceWorldResetter?style=for-the-badge" alt="GitHub issues">
  </a>
  <a href="https://github.com/Lozaine/ResourceWorldResetter/pulls">
    <img src="https://img.shields.io/github/issues-pr/Lozaine/ResourceWorldResetter?style=for-the-badge" alt="GitHub pull requests">
  </a>
  <a href="https://github.com/Lozaine/ResourceWorldResetter/stargazers">
    <img src="https://img.shields.io/github/stars/Lozaine/ResourceWorldResetter?style=for-the-badge" alt="GitHub stars">
  </a>
</p>

## 🚀 Features

Here are some of the project's best features:

- 🔄 **Automatic World Reset**: Resets a specific world at a regular interval (default 24 hours).
- 🌍 **Configurable World**: Admins can choose which world to reset.
- 🕒 **Customizable Timing**: Set reset intervals, restart times, and player warnings.
- ⚠️ **Teleports Players to Safety**: Automatically teleports all players out of the resource world before the reset.

## 🛠️ Installation Steps

1. Download the `ResourceWorldResetter` plugin and place the `.jar` file in the `plugins` folder of your Minecraft server.
2. Ensure [Multiverse-Core](https://dev.bukkit.org/projects/multiverse-core) is also installed and placed in your server's plugins folder.
3. **Start the server** once to generate the default configuration file. You will see an error message similar to this:
   
   ![error-screenshot](https://files.catbox.moe/k6wcnt.png)

   ```yaml
   Error: No world name specified in the config file! Please set 'worldName' in the config.yml.
   ```

4. After the error appears, stop the server and navigate to the `plugins/ResourceWorldResetter` folder. Open the `config.yml` file and modify the following settings:

   ```yaml
   worldName: ""        # The world to reset
   resetWarningTime: 5   # Time (in minutes) to warn players before reset
   resetInterval: 86400  # Time (in seconds) between resets (e.g., 24 hours = 86400)
   restartTime: 21       # Server restart time (24-hour format)
   ```

5. Set the `worldName` to the world you want to reset. This only affects the specified world, not the entire server.
6. Save the `config.yml` file and restart your server.

## 🔧 Usage

- `/setworld <worldname>` to set the resource world that will be reset.
- `/setresetinterval <hours>` to set how often the world resets (in hours).
- `/setrestarttime <hour>` to set the daily restart time (24-hour format).
- `/resetworld` to force an immediate reset of the resource world.

## ⏰ How the World Reset Time Works

The reset time in the **ResourceWorldResetter** plugin is based on the server's **local system time**, meaning it follows the **timezone of the server's operating system**. 

### Key Details:
1. **Local Time Fetching**:  
   The plugin uses the Java method `LocalDateTime.now()`, which captures the current time based on the server’s clock and timezone.

2. **Reset Scheduling**:  
   When you use the `/setrestarttime <hour>` command, the plugin schedules the reset based on the **server's current local time** (e.g., UTC, PST).

3. **Adjusting Timezones**:  
   If you need the reset to happen in a specific timezone, adjust the server’s timezone.

## ⚠️ Critical Warnings

### Important Note for Older Minecraft Versions: [Read Here](https://loz-seas-organization.gitbook.io/resourcesworldresetter/important-note-for-older-minecraft-versions/quickstart)

⚠️ **Critical Warning: No Recovery for Mistaken World Resets**

Once a world is reset, **the world and all data are permanently deleted**, including player structures, inventories, and progress.

- **No built-in recovery** is available for worlds that have been reset. 
- Always **backup** your world files before configuring and using this plugin.

## 📚 Dependencies

Before using **ResourceWorldResetter**, ensure you have installed the following plugins:

- [Multiverse-Core](https://dev.bukkit.org/projects/multiverse-core): Required for managing world creation, unloading, and deletion.


## 📄 License

This project is licensed under the BSD 3-Clause License - see the [LICENSE](LICENSE) file for details.

---

<p align="center">
  <a href="https://github.com/Lozaine/ResourceWorldResetter">
    <img src="https://img.shields.io/github/forks/Lozaine/ResourceWorldResetter?style=social" alt="GitHub forks">
  </a>
  <a href="https://github.com/Lozaine/ResourceWorldResetter">
    <img src="https://img.shields.io/github/watchers/Lozaine/ResourceWorldResetter?style=social" alt="GitHub watchers">
  </a>
  <a href="https://github.com/Lozaine/ResourceWorldResetter/stargazers">
    <img src="https://img.shields.io/github/stars/Lozaine/ResourceWorldResetter?style=social" alt="GitHub stars">
  </a>
</p>

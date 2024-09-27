<h1 align="center" id="title">ResourceWorldResetter</h1>

<p align="center"><img src="https://files.catbox.moe/xhfveh.png" alt="project-image"></p>

<p id="description">Automatically resets resources in one world regularly for fresh materials without affecting others!</p>

  
  
<h2>🧐 Features</h2>

Here're some of the project's best features:

*   Automatic World Reset: Resets a specific world at a regular interval (default 24 hours).
*   Configurable World: Admins can choose which world to reset.
*   Customizable Timing: Set reset intervals restart times and player warnings.
*   Teleports Players to Safety: Teleports all players out of the resource world before the reset.

<h2>🛠️ Installation Steps:</h2>

<p>1. Download the `ResourceWorldResetter` plugin and place the `.jar` file in the `plugins` folder of your Minecraft server.</p>

<p>2. Ensure Multiverse-Core is also installed and placed in your server's plugins folder.</p>

<p>3. *Start the server once to generate the default configuration file. You will see an error message similar to this:</p>

```
Error: No world name specified in the config file! Please set 'worldName' in the config.yml. ​
```

<p>4. After the error appears stop the server and navigate to the plugins/ResourceWorldResetter folder. Open the config.yml file and modify the following settings:</p>

```
Code (YAML): worldName: ""        # The world to reset resetWarningTime: 5   # Time (in minutes) to warn players before reset resetInterval: 86400  # Time (in seconds) between resets (e.g. 24 hours = 86400) restartTime: 21       # Server restart time (24-hour format)
```

<p>5. Set the worldName to the world you want to reset. This only affects the specified world not the entire server.</p>

<p>6. Save the config.yml file and restart your server.</p>

<h2>🍰 Contribution Guidelines:</h2>

This project is actively maintained by Lozaine and contributions are welcome from the community! Whether you're fixing bugs adding new features or improving documentation feel free to open a pull request or suggest changes. You can contribute by: Reporting bugs or suggesting features on the GitHub Issues page. Submitting pull requests for code improvements or new features. Helping with documentation testing or translations.

<h2>🛡️ License:</h2>

This project is licensed under the MIT

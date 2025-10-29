## Setting up TTTool

This app uses [TTTool](https://github.com/entropia/tip-toi-reveng/), so you’ll need to have it installed and available
in your **PATH**:

### 🪟 Windows
1. Download the **tttool-xx.zip** from the [latest TTTool release](https://github.com/entropia/tip-toi-reveng/releases/latest).
2. Extract it somewhere you like, e.g. `C:\Program Files\TTTool\`.
3. Add that folder to your system **PATH** (via *Edit environment variables*).
4. Test it by running:
   ```
   tttool --help
   ```
   If you see a help text with an overview of commands, you’re ready.

### 🐧 Linux
1. Download the **tttool-xx.zip** from the [latest TTTool release](https://github.com/entropia/tip-toi-reveng/releases/latest).
2. Extract it and create a link to the `tttool` binary somewhere in your PATH, e.g.:
   ```
   sudo ln -s linux/tttool /usr/bin/tttool
   ```
3. (Optional) Install extra packages on Debian/Ubuntu:
   ```
   sudo apt install libttspico-utils vorbis-tools
   ```
4. Test your setup:
   ```
   tttool --version
   ```
   If you see a help text with an overview of commands, you’re ready.

### 🍎 macOS
1. Download the **tttool-xx.zip** from the [latest TTTool release](https://github.com/entropia/tip-toi-reveng/releases/latest).
2. Extract it and move the `tttool` binary to a folder in your PATH, e.g.:
   ```
   sudo ln -s osx/tttool /usr/bin/tttool
   ```
3. Test it:
   ```
   tttool --version
   ```
   If you see a help text with an overview of commands, you’re ready.

Once `tttool` runs in your terminal, this frontend will automatically detect and use it as.

<br>

> [!IMPORTANT]
> The executable file has to be named `tttool` (or `tttool.exe` on windows) in order for ttEdit to detect it.
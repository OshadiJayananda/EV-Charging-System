
```markdown
# 📱 EV Charging System – Mobile App Setup Guide

This guide explains how to set up the environment required to build and run the **Mobile Android App (Java + SQLite)** inside this repository.  

Both the **Web App** and **Mobile App** share the same backend (`EvBackend`), so make sure you have the backend running before testing the mobile app.  

---

## ⚙️ 1. Install Java JDK 17

### Windows
- Download Oracle JDK 17 → [Oracle JDK 17 Downloads](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html)  
- Install into:
```

C:\Program Files\Java\jdk-17

````

- Add environment variable:
- `JAVA_HOME = C:\Program Files\Java\jdk-17`
- Add to **Path**:
  ```
  %JAVA_HOME%\bin
  ```

### Linux (Ubuntu/Debian)
```bash
sudo apt update
sudo apt install openjdk-17-jdk -y
````

Add to `~/.bashrc` or `~/.zshrc`:

```bash
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
export PATH=$JAVA_HOME/bin:$PATH
```

### macOS (Homebrew)

```bash
brew install openjdk@17
```

Add to `~/.zshrc`:

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v17)
export PATH=$JAVA_HOME/bin:$PATH
```

✅ Verify everywhere:

```bash
java -version
javac -version
```

---

## ⚙️ 2. Install Android SDK Command-line Tools

* Download → [Android Command-line Tools](https://developer.android.com/studio#command-tools)
* Choose the **zip** for your OS.

### Extract into:

* **Windows**:

  ```
  C:\Android\cmdline-tools\latest\
  ```
* **Linux/macOS**:

  ```
  ~/Android/cmdline-tools/latest/
  ```

Final structure should include:

```
cmdline-tools/latest/bin/sdkmanager
cmdline-tools/latest/lib/...
```

⚠️ Folder **must** be named `latest`.

---

## ⚙️ 3. Set Environment Variables

### Windows

Add system variables:

```
JAVA_HOME = C:\Program Files\Java\jdk-17
ANDROID_HOME = C:\Android
```

Add to Path:

```
%JAVA_HOME%\bin
C:\Android\cmdline-tools\latest\bin
C:\Android\platform-tools
```

### Linux/macOS

Add to `~/.bashrc` or `~/.zshrc`:

```bash
export ANDROID_HOME=$HOME/Android
export PATH=$JAVA_HOME/bin:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools:$PATH
```

✅ Reload shell:

```bash
source ~/.bashrc   # or source ~/.zshrc
```

---

## ⚙️ 4. Install SDK Packages

Run:

```bash
sdkmanager --sdk_root=$ANDROID_HOME "platform-tools" "platforms;android-34" "build-tools;34.0.0"
sdkmanager --licenses
```

On Windows, you can also use a helper batch script `install-android-sdk.bat`:

```bat
@echo off
echo Installing Android SDK packages...
sdkmanager --sdk_root=C:\Android "platform-tools" "platforms;android-34" "build-tools;34.0.0"
echo Accepting all licenses...
yes | sdkmanager --licenses
echo Done! You can now use adb and build Android apps.
pause
```

On Linux/macOS you can run:

```bash
yes | sdkmanager --licenses
```

---

## ⚙️ 5. Verify Installation

```bash
adb --version
sdkmanager --version
```

✅ Expected:

* Android Debug Bridge version
* SDK Manager version

---

## 📱 6. Connect Android Phone

1. Enable **Developer Options → USB Debugging** on your phone.
2. Connect via USB.
3. Run:

   ```bash
   adb devices
   ```

   You should see your phone listed as `device`.

---

## ⚙️ 7. Build & Install App

From the repo root:

```bash
cd EV-CHARGING-SYSTEM/Mobile
gradle assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```


## ⚠️ Common Issues & Fixes

* **`sdkmanager not found`**

  * Check your `PATH` includes:

    ```
    C:\Android\cmdline-tools\latest\bin
    ```

    or

    ```
    $ANDROID_HOME/cmdline-tools/latest/bin
    ```

* **`adb not recognized`**

  * Ensure `platform-tools` is installed and path includes:

    ```
    C:\Android\platform-tools
    ```

* **`findstr not recognized (Windows)`**

  * Add `C:\Windows\System32` to your PATH.

* **`Java version error`**

  * Must be Java 17. Run:

    ```bash
    java -version
    ```


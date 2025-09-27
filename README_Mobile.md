

# 📱 EV-Mobile App (Java + Gradle)

This is the **Mobile Android app** for the EV-Charging System.
It is built with **pure Android (Java + Gradle)**, no frameworks, and runs inside the same repo alongside **Web** and **Backend**.

---

## 🚀 Features

* EV Owner:

  * Create, update, deactivate account
  * Make and cancel bookings
  * View history and QR codes
* Station Operator:

  * Login via mobile
  * Scan QR codes
  * Finalize bookings
* Dashboard with reservations summary
* Local SQLite DB support (to be added later)

---

## 🛠️ Prerequisites

* **Java 17 (JDK)**
* **Android SDK Command Line Tools**
* **Gradle 8.6** (via wrapper)
* **ADB (Android Debug Bridge)**
* **VS Code** with recommended extensions:

  * Extension Pack for Java
  * Gradle for Java
  * Android XML Tools

---

## 🖥️ Setup Instructions

### 1. Install Java 17

#### Windows

* Install to: `C:\Program Files\Java\jdk-17`
* Set environment variables:

  ```text
  JAVA_HOME = C:\Program Files\Java\jdk-17
  PATH += %JAVA_HOME%\bin
  ```
* Verify:

  ```cmd
  java -version
  javac -version
  ```

#### Linux / macOS

```bash
sudo apt update && sudo apt install openjdk-17-jdk -y   # Ubuntu/Debian
brew install openjdk@17                                 # macOS
```

Add to `~/.bashrc` or `~/.zshrc`:

```bash
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
export PATH=$JAVA_HOME/bin:$PATH
```

---

### 2. Install Android SDK CLI Tools

Download from: [Android Command Line Tools](https://developer.android.com/studio#command-tools)

#### Windows

* Extract to:

  ```
  C:\Android\cmdline-tools\latest\
  ```
* Add environment variables:

  ```text
  ANDROID_HOME = C:\Android
  PATH += C:\Android\cmdline-tools\latest\bin
  PATH += C:\Android\platform-tools
  ```

#### Linux / macOS

* Extract to:

  ```
  $HOME/Android/cmdline-tools/latest/
  ```
* Add to `~/.bashrc`:

  ```bash
  export ANDROID_HOME=$HOME/Android
  export PATH=$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools:$PATH
  ```

---

### 3. Install SDK Packages

```bash
sdkmanager --licenses
sdkmanager "platform-tools" "platforms;android-34" "build-tools;34.0.0"
```

---

### 4. Initialize Gradle Project

Inside repo root:

```bash
cd EV-CHARGING-SYSTEM
mkdir Mobile
cd Mobile
gradle init
```

Select:

* Application → Java → Java 17
* Project name → EvMobile
* Single project, DSL = Groovy
* Test framework = JUnit 4
* New APIs = No

---

### 5. Convert to Android Project

Delete:

```
app/src/main/java/org/example/App.java
app/src/test/java/org/example/AppTest.java
```

Create Android files:

```
app/src/main/java/com/evcharging/mobile/MainActivity.java
app/src/main/res/layout/activity_main.xml
app/src/main/AndroidManifest.xml
```

📌 Add content:

* [MainActivity.java](#mainactivityjava)
* [activity_main.xml](#activity_mainxml)
* [AndroidManifest.xml](#androidmanifestxml)

---

### 6. Update Gradle Configs

**settings.gradle**

```gradle
pluginManagement {
    repositories { google(); mavenCentral(); gradlePluginPortal() }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories { google(); mavenCentral() }
}
rootProject.name = "EvMobile"
include(":app")
```

**app/build.gradle**

```gradle
plugins {
    id 'com.android.application' version '8.2.2'
}
android {
    namespace "com.evcharging.mobile"
    compileSdk 34
    defaultConfig {
        applicationId "com.evcharging.mobile"
        minSdk 24
        targetSdk 34
        versionCode 1
        versionName "1.0"
    }
    buildTypes {
        release {
            minifyEnabled false
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
        }
    }
}
dependencies {
    implementation 'androidx.appcompat:appcompat:1.6.1'
    implementation 'com.google.android.material:material:1.9.0'
    implementation 'androidx.constraintlayout:constraintlayout:2.1.4'
}
```

**gradle-wrapper.properties**

```properties
distributionUrl=https\://services.gradle.org/distributions/gradle-8.6-bin.zip
```

---

### 7. Build APK

```bash
cd Mobile
gradlew clean
gradlew build
```

APK output:

```
Mobile/app/build/outputs/apk/debug/app-debug.apk
```

---

### 8. Install APK on Phone

1. Enable **Developer Options** → USB Debugging.
2. Connect phone → verify:

   ```bash
   adb devices
   ```
3. Install:

   ```bash
   adb install -r app/build/outputs/apk/debug/app-debug.apk
   ```

Open app → **Hello EV Mobile World!** 🎉

---

## 📂 Folder Structure

```
Mobile/
 ├── app/
 │   ├── build.gradle
 │   ├── src/
 │   │   └── main/
 │   │       ├── AndroidManifest.xml
 │   │       ├── java/com/evcharging/mobile/MainActivity.java
 │   │       └── res/layout/activity_main.xml
 ├── build.gradle
 ├── settings.gradle
 ├── gradle/wrapper/gradle-wrapper.properties
 ├── gradlew
 ├── gradlew.bat
```

---

## 📌 Source Files

### MainActivity.java

```java
package com.evcharging.mobile;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TextView tv = new TextView(this);
        tv.setText("Hello, EV Mobile World!");
        tv.setTextSize(24);

        setContentView(tv);
    }
}
```

### activity_main.xml

```xml
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:gravity="center"
    android:orientation="vertical">

    <TextView
        android:id="@+id/helloText"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Hello EV Mobile World!"
        android:textSize="24sp"/>
</LinearLayout>
```

### AndroidManifest.xml

```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="com.evcharging.mobile">

    <application
        android:label="EvMobile"
        android:theme="@style/Theme.AppCompat.Light.NoActionBar">
        <activity android:name=".MainActivity">
            <intent-filter>
                <action android:name="android.intent.action.MAIN"/>
                <category android:name="android.intent.category.LAUNCHER"/>
            </intent-filter>
        </activity>
    </application>
</manifest>
```

---


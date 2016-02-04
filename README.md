## Who Can Use the Plugin

To use the plugin, you need to be a SpotX publisher and use the Brightcove Player SDK for Android.

### Become a SpotXchange Publisher

If you are not already a SpotX publisher, click [here](http://www.spotxchange.com/publishers/apply-to-become-a-spotx-publisher/) to apply.

## What the Plugin Does

The plugin allows the SpotX SDK and the Brightcove Player SDK to work together so you can monetize your player's content. To use this new plugin, SpotX publishers will need to integrate the SpotX SDK and the Brightcove Player SDK into their App.


## How to Install the Plugin

There are three ways to install this plugin:

### Gradle Dependency (preferred)

Simply add the following to your build.gradle.

```groovy
compile 'com.spotxchange:spotx-brightcove-android:+'
```

### Maven Dependency

Declare the dependency in Maven:

```xml
<dependency>
    <groupId>com.spotxchange</groupId>
    <artifactId>spotx-brightcove-android</artifactId>
    <version>1.0</version>
</dependency>
```

### As a Library Project

Download the source code and import it as a library project in Android Studio or Eclipse. The project is available from our GitHub repository [here](https://github.com/spotxmobile/spotx-brightcove-android).

Get more information on how to do this [here](http://developer.android.com/tools/projects/index.html#LibraryProjects).

## Set Up

To set up and use the SpotX plugin you need to init the plugin with the required SpotxAdSettings Object.
```java
SpotxBrightcovePlugin plugin = new SpotxBrightcovePlugin(brightcoveVideoView.getEventEmitter(), this, brightcoveVideoView);
SpotxAdSettings adSettings = new SpotxAdSettings(YOUR_CHANNEL_ID, "www.yourdomain.com");
plugin.init(adSettings);
```

### Usage

You can follow [Brightcove's sample app](https://github.com/BrightcoveOS/android-plugin-guide/blob/master/sample/SamplePluginApplication/src/main/java/com/brightcove/player/application/MainActivity.java) on how to configure and use the plugin.

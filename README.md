# KontinuousSpeechRecognizer
[![Release](https://jitpack.io/v/StephenVinouze/KontinuousSpeechRecognizer.svg)](https://jitpack.io/#StephenVinouze/KontinuousSpeechRecognizer)
[![Build Status](https://app.bitrise.io/app/43dc200460e9ab60/status.svg?token=QBs7FABHP8s3Whwr0EnZRA)](https://www.bitrise.io/app/43dc200460e9ab60)
[![API](https://img.shields.io/badge/API-14%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=14)
[![Android Arsenal](https://img.shields.io/badge/Android%20Arsenal-KontinuousSpeechRecognizer-brightgreen.svg?style=flat)](https://android-arsenal.com/details/1/5790)
[![GitHub license](http://img.shields.io/badge/license-APACHE2-blue.svg)](https://github.com/StephenVinouze/KontinuousSpeechRecognizer/blob/master/LICENSE)

## Prelude

Speech recognition is designed to listen commands for a brief moment and deactivate on its own. I wanted to mimic the "OK Google" recognition pattern. Use it with care to prevent battery drain.

The recognizer is managed on its own via the **KontinuousRecognitionManager** and exposes methods such as `startRecognition`, `stopRecognition`, `cancelRecognition` and `destroyRecognizer`. It is up to you to manage the lifecycle of this manager from your view.

The recognizer will be listening and be respawned all the time once you call `startRecognition` and until you call `stopRecognition`. It expects an activation keyword, such as "Ok Google", then once detected will yield the result to the client and stop listening. A sound system will be heard once the activation keyword is detected but you can decide to mute it.

The workflow is explained as follow :

![Speech recognition](docs/KontinuousSpeechRecognition.png)

## Getting started

In your *AndroidManifest.xml* file, add the following line if your manifest merger is disabled :

```xml
<uses-permission android:name="android.permission.RECORD_AUDIO" />
 ```
 
 If you are developing an application targeting SDK 23, remember to take care of runtime permission.
 
 In your Activity, create the **KontinuousRecognitionManager** :
 
 ```kotlin
 
 companion object {
    /**
     * Put any keyword that will trigger the speech recognition
     */
    private const val ACTIVATION_KEYWORD = "<YOUR_ACTIVATION_KEYWORD>"
}
 
private val recognitionManager: KontinuousRecognitionManager by lazy {
        KontinuousRecognitionManager(this, activationKeyword = ACTIVATION_KEYWORD, shouldMute = false, callback = this)
    }
 
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    recognitionManager.createRecognizer()
    
    if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), RECORD_AUDIO_REQUEST_CODE)
    }
}

override fun onDestroy() {
    recognitionManager.destroyRecognizer()
    super.onDestroy()
}

override fun onResume() {
    super.onResume()

    if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
        startRecognition()
    }
}

override fun onPause() {
    stopRecognition()
    super.onPause()
}
 ```
 
We also provide an optional **RecognitionCallback** interface that yields all **RecognitionListener** callbacks in addition to a few custom ones corresponding to the keyword detection and the speech recognition availability (it is known to be unsupported on a range of devices).

## Gradle Dependency

Add this in your root `build.gradle` file:

```groovy
allprojects {
	repositories {
		// ... other repositories
		maven { url "https://jitpack.io" }
	}
}
```

Then add the following dependency in your project.

```groovy
dependencies {
    implementation "com.github.StephenVinouze:KontinuousSpeechRecognizer:{latest_version}"
}
```

## Pull requests

I welcome and encourage all pull requests. I might not be able to respond as fast as I would want to but I endeavor to be as responsive as possible.

All PR must:

1. Be written in Kotlin
2. Maintain code style
3. Indicate whether it is a enhancement, bug fix or anything else
4. Provide a clear description of what your PR brings
5. Enjoy coding in Kotlin :)

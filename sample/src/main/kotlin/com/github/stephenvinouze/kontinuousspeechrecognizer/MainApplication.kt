package com.github.stephenvinouze.kontinuousspeechrecognizer

import android.app.Application
import timber.log.Timber

/**
 * Created by stephenvinouze on 16/05/2017.
 */
class MainApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        Timber.plant(Timber.DebugTree())
    }

}
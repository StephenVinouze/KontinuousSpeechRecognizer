package com.github.stephenvinouze.core.managers

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.core.content.getSystemService
import com.github.stephenvinouze.core.interfaces.RecognitionCallback
import com.github.stephenvinouze.core.models.RecognitionStatus

/**
 * Created by stephenvinouze on 16/05/2017.
 */
class KontinuousRecognitionManager(
        private val context: Context,
        private val activationKeyword: String,
        private val shouldMute: Boolean = false,
        private val callback: RecognitionCallback? = null
) : RecognitionListener {

    private var isActivated: Boolean = false
    private val speech: SpeechRecognizer by lazy { SpeechRecognizer.createSpeechRecognizer(context) }
    private val audioManager: AudioManager? = context.getSystemService()

    private val recognizerIntent by lazy {
        Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH)
            putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)
            }
        }
    }

    fun createRecognizer() {
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            speech.setRecognitionListener(this)
            callback?.onPrepared(RecognitionStatus.SUCCESS)
        } else {
            callback?.onPrepared(RecognitionStatus.UNAVAILABLE)
        }
    }

    fun destroyRecognizer() {
        muteRecognition(false)
        speech.destroy()
    }

    fun startRecognition() {
        speech.startListening(recognizerIntent)
    }

    fun stopRecognition() {
        speech.stopListening()
    }

    fun cancelRecognition() {
        speech.cancel()
    }

    @Suppress("DEPRECATION")
    private fun muteRecognition(mute: Boolean) {
        audioManager?.let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val flag = if (mute) AudioManager.ADJUST_MUTE else AudioManager.ADJUST_UNMUTE
                it.adjustStreamVolume(AudioManager.STREAM_NOTIFICATION, flag, 0)
                it.adjustStreamVolume(AudioManager.STREAM_ALARM, flag, 0)
                it.adjustStreamVolume(AudioManager.STREAM_MUSIC, flag, 0)
                it.adjustStreamVolume(AudioManager.STREAM_RING, flag, 0)
                it.adjustStreamVolume(AudioManager.STREAM_SYSTEM, flag, 0)
            } else {
                it.setStreamMute(AudioManager.STREAM_NOTIFICATION, mute)
                it.setStreamMute(AudioManager.STREAM_ALARM, mute)
                it.setStreamMute(AudioManager.STREAM_MUSIC, mute)
                it.setStreamMute(AudioManager.STREAM_RING, mute)
                it.setStreamMute(AudioManager.STREAM_SYSTEM, mute)
            }
        }
    }

    override fun onBeginningOfSpeech() {
        callback?.onBeginningOfSpeech()
    }

    override fun onReadyForSpeech(params: Bundle) {
        muteRecognition(shouldMute || !isActivated)
        callback?.onReadyForSpeech(params)
    }

    override fun onBufferReceived(buffer: ByteArray) {
        callback?.onBufferReceived(buffer)
    }

    override fun onRmsChanged(rmsdB: Float) {
        callback?.onRmsChanged(rmsdB)
    }

    override fun onEndOfSpeech() {
        callback?.onEndOfSpeech()
    }

    override fun onError(errorCode: Int) {
        if (isActivated) {
            callback?.onError(errorCode)
        }
        isActivated = false

        when (errorCode) {
            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> cancelRecognition()
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> {
                destroyRecognizer()
                createRecognizer()
            }
        }

        startRecognition()
    }

    override fun onEvent(eventType: Int, params: Bundle) {
        callback?.onEvent(eventType, params)
    }

    override fun onPartialResults(partialResults: Bundle) {
        val matches = partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        if (isActivated && matches != null) {
            callback?.onPartialResults(matches)
        }
    }

    override fun onResults(results: Bundle) {
        val matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        val scores = results.getFloatArray(SpeechRecognizer.CONFIDENCE_SCORES)
        if (matches != null) {
            if (isActivated) {
                isActivated = false
                callback?.onResults(matches, scores)
                stopRecognition()
            } else {
                matches.firstOrNull { it.contains(other = activationKeyword, ignoreCase = true) }
                        ?.let {
                            isActivated = true
                            callback?.onKeywordDetected()
                        }
                startRecognition()
            }
        }
    }

}
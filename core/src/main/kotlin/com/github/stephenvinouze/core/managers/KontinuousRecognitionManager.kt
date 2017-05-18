package com.github.stephenvinouze.core.managers

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer

/**
 * Created by stephenvinouze on 16/05/2017.
 */
class KontinuousRecognitionManager(private val context: Context,
                                   private val activationKeyword: String,
                                   private val callback: RecognitionCallback? = null) : RecognitionListener {

    var recognizerIntent: Intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
    var shouldMute: Boolean = false

    private var isActivated: Boolean = false
    private var speech: SpeechRecognizer? = null
    private var audioManager: AudioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    init {
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH)
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)

        initializeRecognizer()
    }

    fun startRecognition() {
        speech?.startListening(recognizerIntent)
    }

    fun stopRecognition() {
        speech?.stopListening()
    }

    fun cancelRecognition() {
        speech?.cancel()
    }

    fun destroyRecognizer() {
        muteRecognition(false)
        speech?.destroy()
    }

    private fun initializeRecognizer() {
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            speech = SpeechRecognizer.createSpeechRecognizer(context)
            speech?.setRecognitionListener(this)
            callback?.onPrepared(if (speech != null) RecognitionStatus.SUCCESS else RecognitionStatus.FAILURE)
        } else {
            callback?.onPrepared(RecognitionStatus.UNAVAILABLE)
        }
    }

    private fun muteRecognition(mute: Boolean) {
        val flag = if (mute) AudioManager.ADJUST_MUTE else AudioManager.ADJUST_UNMUTE
        audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, flag, 0)
        audioManager.adjustStreamVolume(AudioManager.STREAM_SYSTEM, flag, 0)
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
                initializeRecognizer()
            }
        }

        startRecognition()
    }

    override fun onEvent(eventType: Int, params: Bundle) {
        callback?.onEvent(eventType, params)
    }

    override fun onPartialResults(partialResults: Bundle) {
        val matches = partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        if (matches != null) {
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
            } else {
                matches.forEach {
                    if (it.contains(other = activationKeyword, ignoreCase = true)) {
                        isActivated = true
                        callback?.onKeywordDetected()
                        return@forEach
                    }
                }
            }
        }

        startRecognition()
    }

    interface RecognitionCallback {
        fun onPrepared(status: RecognitionStatus)
        fun onBeginningOfSpeech()
        fun onKeywordDetected()
        fun onReadyForSpeech(params: Bundle)
        fun onBufferReceived(buffer: ByteArray)
        fun onRmsChanged(rmsdB: Float)
        fun onPartialResults(results: List<String>)
        fun onResults(results: List<String>, scores: FloatArray?)
        fun onError(errorCode: Int)
        fun onEvent(eventType: Int, params: Bundle)
        fun onEndOfSpeech()
    }

    enum class RecognitionStatus {
        SUCCESS, FAILURE, UNAVAILABLE
    }

}
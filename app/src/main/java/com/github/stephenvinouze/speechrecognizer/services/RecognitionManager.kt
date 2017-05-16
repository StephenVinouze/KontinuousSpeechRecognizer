package com.github.stephenvinouze.speechrecognizer.services

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.SpeechRecognizer

/**
 * Created by stephenvinouze on 16/05/2017.
 */
class RecognitionManager(context: Context,
                         private val recognizerIntent: Intent,
                         private val callback: RecognitionCallback? = null): RecognitionListener {

    private var speech: SpeechRecognizer? = null

    init {
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            speech = SpeechRecognizer.createSpeechRecognizer(context)
            speech?.setRecognitionListener(this)
            callback?.onPrepared(if (speech != null) RecognitionStatus.SUCCESS else RecognitionStatus.FAILURE)
        } else {
            callback?.onPrepared(RecognitionStatus.UNAVAILABLE)
        }
    }

    fun startRecognition() {
        speech?.cancel()
        speech?.startListening(recognizerIntent)
    }

    fun stopRecognition() {
        speech?.stopListening()
    }

    fun destroyRecognition() {
        speech?.destroy()
    }

    override fun onBeginningOfSpeech() {
        callback?.onBeginningOfSpeech()
    }

    override fun onBufferReceived(buffer: ByteArray) {
        callback?.onBufferReceived(buffer)
    }

    override fun onEndOfSpeech() {
        callback?.onEndOfSpeech()
    }

    override fun onError(errorCode: Int) {
        callback?.onError(errorCode)

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

    override fun onReadyForSpeech(params: Bundle) {
        callback?.onReadyForSpeech(params)
    }

    override fun onResults(results: Bundle) {
        val matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        val scores = results.getFloatArray(SpeechRecognizer.CONFIDENCE_SCORES)
        if (matches != null) {
            callback?.onResults(matches, scores)
        }

        startRecognition()
    }

    override fun onRmsChanged(rmsdB: Float) {
        callback?.onRmsChanged(rmsdB)
    }

    interface RecognitionCallback {
        fun onPrepared(status: RecognitionStatus)
        fun onBeginningOfSpeech()
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
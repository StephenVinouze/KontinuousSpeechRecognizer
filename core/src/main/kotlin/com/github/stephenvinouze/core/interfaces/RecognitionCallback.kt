package com.github.stephenvinouze.core.interfaces

import android.os.Bundle
import com.github.stephenvinouze.core.models.RecognitionStatus

/**
 * Created by stephenvinouze on 18/05/2017.
 */
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
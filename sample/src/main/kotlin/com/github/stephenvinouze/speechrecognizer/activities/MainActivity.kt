package com.github.stephenvinouze.speechrecognizer.activities

import android.os.Bundle
import android.speech.SpeechRecognizer
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import com.afollestad.materialdialogs.MaterialDialog
import com.github.stephenvinouze.core.managers.KontinuousRecognitionManager
import com.github.stephenvinouze.speechrecognizer.R
import timber.log.Timber

class MainActivity : AppCompatActivity(), KontinuousRecognitionManager.RecognitionCallback {

    @BindView(R.id.textView1)
    lateinit var returnedText: TextView

    @BindView(R.id.progressBar1)
    lateinit var progressBar: ProgressBar

    lateinit var recognitionManager: KontinuousRecognitionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        ButterKnife.bind(this)

        progressBar.visibility = View.INVISIBLE
        progressBar.max = 10

        recognitionManager = KontinuousRecognitionManager(this, "OK chef", this)
    }

    override fun onDestroy() {
        recognitionManager.destroyRecognizer()
        super.onDestroy()
    }

    override fun onResume() {
        super.onResume()
        startRecognition()
    }

    override fun onPause() {
        stopRecognition()
        super.onPause()
    }

    private fun startRecognition() {
        progressBar.visibility = View.VISIBLE
        recognitionManager.startRecognition()
    }

    private fun stopRecognition() {
        progressBar.isIndeterminate = true
        progressBar.visibility = View.INVISIBLE
        recognitionManager.stopRecognition()
    }

    private fun getErrorText(errorCode: Int): String {
        when (errorCode) {
            SpeechRecognizer.ERROR_AUDIO -> return "Audio recording error"
            SpeechRecognizer.ERROR_CLIENT -> return "Client side error"
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> return "Insufficient permissions"
            SpeechRecognizer.ERROR_NETWORK -> return "Network error"
            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> return "Network timeout"
            SpeechRecognizer.ERROR_NO_MATCH -> return "No match"
            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> return "RecognitionService busy"
            SpeechRecognizer.ERROR_SERVER -> return "Error from server"
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> return "No speech input"
            else -> return "Didn't understand, please try again."
        }
    }

    override fun onBeginningOfSpeech() {
        Timber.i("onBeginningOfSpeech")
        progressBar.isIndeterminate = false
    }

    override fun onBufferReceived(buffer: ByteArray) {
        Timber.i("onBufferReceived: %s", buffer)
    }

    override fun onEndOfSpeech() {
        Timber.i("onEndOfSpeech")
    }

    override fun onError(errorCode: Int) {
        val errorMessage = getErrorText(errorCode)
        Timber.d("onError: %s", errorMessage)
        returnedText.text = errorMessage
    }

    override fun onEvent(eventType: Int, params: Bundle) {
        Timber.i("onEvent")
    }

    override fun onReadyForSpeech(params: Bundle) {
        Timber.i("onReadyForSpeech")
    }

    override fun onRmsChanged(rmsdB: Float) {
        progressBar.progress = rmsdB.toInt()
    }

    override fun onPrepared(status: KontinuousRecognitionManager.RecognitionStatus) {
        when (status) {
            KontinuousRecognitionManager.RecognitionStatus.SUCCESS -> {
                Timber.i("onPrepared: Success")
                returnedText.text = "Recognition ready"
            }
            KontinuousRecognitionManager.RecognitionStatus.FAILURE,
            KontinuousRecognitionManager.RecognitionStatus.UNAVAILABLE -> {
                Timber.i("onPrepared: Failure or unavailable")
                MaterialDialog.Builder(this)
                        .title("Speech Recognizer unavailable")
                        .content("Your device does not support Speech Recognition. Sorry!")
                        .positiveText(android.R.string.ok)
                        .show()
            }
        }
    }

    override fun onKeywordDetected() {
        Timber.i("keyword detected !!!")
        returnedText.text = "Keyword detected"
    }

    override fun onPartialResults(results: List<String>) {}

    override fun onResults(results: List<String>, scores: FloatArray?) {
        val text = results.joinToString(separator = "\n")
        Timber.i("onResults : %s", text)
        returnedText.text = text
    }

}

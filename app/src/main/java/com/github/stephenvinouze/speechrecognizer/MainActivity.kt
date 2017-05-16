package com.github.stephenvinouze.speechrecognizer

import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.ToggleButton
import butterknife.BindView
import butterknife.ButterKnife
import com.afollestad.materialdialogs.MaterialDialog
import timber.log.Timber

class MainActivity : AppCompatActivity(), RecognitionListener {

    @BindView(R.id.textView1)
    lateinit var returnedText: TextView

    @BindView(R.id.toggleButton1)
    lateinit var toggleButton: ToggleButton

    @BindView(R.id.progressBar1)
    lateinit var progressBar: ProgressBar

    private var speech: SpeechRecognizer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        ButterKnife.bind(this)

        progressBar.visibility = View.INVISIBLE
        progressBar.max = 10

        if (SpeechRecognizer.isRecognitionAvailable(this)) {
            speech = SpeechRecognizer.createSpeechRecognizer(this)
            speech?.setRecognitionListener(this)

            toggleButton.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    startRecognition()
                } else {
                    stopRecognition()
                }
            }
        } else {
            MaterialDialog.Builder(this)
                    .title("Speech Recognizer unavailable")
                    .content("Your device does not support Speech Recognition. Sorry!")
                    .positiveText(android.R.string.ok)
                    .show()
        }
    }

    override fun onDestroy() {
        speech?.destroy()
        super.onDestroy()
    }

    override fun onPause() {
        stopRecognition()
        super.onPause()
    }

    private fun buildRecognizerIntent(): Intent {
        val recognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH)
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, packageName)
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)
        return recognizerIntent
    }

    private fun startRecognition() {
        toggleButton.isChecked = true
        progressBar.visibility = View.VISIBLE
        progressBar.isIndeterminate = true
        speech?.startListening(buildRecognizerIntent())
    }

    private fun stopRecognition() {
        toggleButton.isChecked = false
        progressBar.isIndeterminate = false
        progressBar.visibility = View.INVISIBLE
        speech?.stopListening()
    }

    private fun getErrorText(errorCode: Int): String {
        when (errorCode) {
            SpeechRecognizer.ERROR_AUDIO -> return  "Audio recording error"
            SpeechRecognizer.ERROR_CLIENT -> return  "Client side error"
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> return  "Insufficient permissions"
            SpeechRecognizer.ERROR_NETWORK -> return  "Network error"
            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> return  "Network timeout"
            SpeechRecognizer.ERROR_NO_MATCH -> return  "No match"
            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> return "RecognitionService busy"
            SpeechRecognizer.ERROR_SERVER -> return  "Error from server"
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> return  "No speech input"
            else -> return  "Didn't understand, please try again."
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
        progressBar.isIndeterminate = true
        toggleButton.isChecked = false
    }

    override fun onError(errorCode: Int) {
        val errorMessage = getErrorText(errorCode)
        Timber.d("FAILED %s", errorMessage)
        returnedText.text = errorMessage
        toggleButton.isChecked = false
    }

    override fun onEvent(eventType: Int, params: Bundle) {
        Timber.i("onEvent")
    }

    override fun onPartialResults(partialResults: Bundle) {
        Timber.i("onPartialResults")
    }

    override fun onReadyForSpeech(params: Bundle) {
        Timber.i("onReadyForSpeech")
    }

    override fun onResults(results: Bundle) {
        val matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        var text = ""
        if (matches != null) {
            for (result in matches)
                text += result + "\n"
        }
        Timber.i("onResults : %s", text)

        returnedText.text = text
    }

    override fun onRmsChanged(rmsdB: Float) {
        Timber.i("onRmsChanged: %f", rmsdB)
        progressBar.progress = rmsdB.toInt()
    }

}

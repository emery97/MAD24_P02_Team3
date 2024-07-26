package sg.edu.np.mad.TicketFinder;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.Locale;

public class SpeechRecognizerHelper {

    // Activity instance for context
    private Activity activity;

    // SpeechRecognizer instance
    private SpeechRecognizer speechRecognizer;

    // Intent for speech recognition
    private Intent speechRecognizerIntent;

    // ImageView for microphone icon
    private ImageView micIcon;

    // Listener for speech result callbacks
    private OnSpeechResultListener listener;


    // Interface for speech result callbacks
    public interface OnSpeechResultListener {
        void onSpeechResult(String text);
    }


    // Constructor to initialize the SpeechRecognizerHelper
    public SpeechRecognizerHelper(Activity activity, ImageView micIcon, OnSpeechResultListener listener) {
        this.activity = activity;
        this.micIcon = micIcon;
        this.listener = listener;
        initSpeechRecognizer(); // Initialize the speech recognizer
    }


    // Method to initialize the speech recognizer
    private void initSpeechRecognizer() {
        // Create a new SpeechRecognizer instance
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(activity);
        // Create an intent for speech recognition
        speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        // Set the language model to free form
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        // Set the default language to the device's locale
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());

        // Set the recognition listener for the speech recognizer
        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {
                micIcon.setImageResource(R.drawable.active_mic_icon); // Change icon to active
            }

            @Override
            public void onBeginningOfSpeech() {}

            @Override
            public void onRmsChanged(float rmsdB) {}

            @Override
            public void onBufferReceived(byte[] buffer) {}

            @Override
            public void onEndOfSpeech() {
                micIcon.setImageResource(R.drawable.mic_icon); // Change icon to inactive
            }

            @Override
            public void onError(int error) {
                micIcon.setImageResource(R.drawable.mic_icon); // Change icon to inactive
            }

            @Override
            public void onResults(Bundle results) {
                // Get the recognized speech results
                ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                // If matches are found, call the listener with the first result
                if (matches != null && !matches.isEmpty()) {
                    listener.onSpeechResult(matches.get(0));
                }
                micIcon.setImageResource(R.drawable.mic_icon); // Change icon to inactive
            }

            @Override
            public void onPartialResults(Bundle partialResults) {}

            @Override
            public void onEvent(int eventType, Bundle params) {}
        });
    }


    // Method to start listening for speech input
    public void startListening() {
        speechRecognizer.startListening(speechRecognizerIntent);
    }

    // Method to stop listening for speech input
    public void stopListening() {
        speechRecognizer.stopListening();
    }

    // Method to destroy the speech recognizer
    public void destroy() {
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
        }
    }
}

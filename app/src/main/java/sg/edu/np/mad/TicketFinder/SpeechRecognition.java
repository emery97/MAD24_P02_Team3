package sg.edu.np.mad.TicketFinder;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;

import java.util.ArrayList;
import java.util.Locale;

public class SpeechRecognition {
    private static final String TAG = "SpeechRecognition";
    private SpeechRecognizer speechRecognizer;
    private Intent recognizerIntent;
    private SpeechRecognitionListener listener;

    public SpeechRecognition(Context context, SpeechRecognitionListener listener) {
        this.listener = listener;
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context);
        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {
                listener.onReadyForSpeech();
            }

            @Override
            public void onBeginningOfSpeech() {
                listener.onBeginningOfSpeech();
            }

            @Override
            public void onRmsChanged(float rmsdB) {
                listener.onRmsChanged(rmsdB);
            }

            @Override
            public void onBufferReceived(byte[] buffer) {
                listener.onBufferReceived(buffer);
            }

            @Override
            public void onEndOfSpeech() {
                listener.onEndOfSpeech();
            }

            @Override
            public void onError(int error) {
                listener.onError(error);
                Log.e(TAG, "Speech recognition error: " + error);
            }

            @Override
            public void onResults(Bundle results) {
                ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null && matches.size() > 0) {
                    Log.d(TAG, "Recognized results: " + matches.get(0));
                    listener.onResults(matches.get(0));
                }
            }

            @Override
            public void onPartialResults(Bundle partialResults) {
                ArrayList<String> matches = partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null && matches.size() > 0) {
                    Log.d(TAG, "Partial results: " + matches.get(0));
                    listener.onPartialResults(matches.get(0));
                }
            }

            @Override
            public void onEvent(int eventType, Bundle params) {
                listener.onEvent(eventType, params);
            }
        });

        recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
    }

    public void startListening() {
        Log.d(TAG, "Starting to listen");
        speechRecognizer.startListening(recognizerIntent);
    }

    public void destroy() {
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
        }
    }

    public interface SpeechRecognitionListener {
        void onReadyForSpeech();
        void onBeginningOfSpeech();
        void onRmsChanged(float rmsdB);
        void onBufferReceived(byte[] buffer);
        void onEndOfSpeech();
        void onError(int error);
        void onResults(String result);
        void onPartialResults(String partialResult);
        void onEvent(int eventType, Bundle params);
    }

    public static String getNavigationCommand(String result) {
        result = result.toLowerCase();
        if (result.contains("home")) {
            return "homepage";
        } else if (result.contains("explore")) {
            return "exploreEvents";
        } else if (result.contains("history")) {
            return "bookingHistory";
        } else if (result.contains("profile")) {
            return "profilePage";
        }
        return null;
    }
}

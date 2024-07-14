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

    private Activity activity;
    private SpeechRecognizer speechRecognizer;
    private Intent speechRecognizerIntent;
    private ImageView micIcon;
    private OnSpeechResultListener listener;

    public interface OnSpeechResultListener {
        void onSpeechResult(String text);
    }

    public SpeechRecognizerHelper(Activity activity, ImageView micIcon, OnSpeechResultListener listener) {
        this.activity = activity;
        this.micIcon = micIcon;
        this.listener = listener;
        initSpeechRecognizer();
    }

    private void initSpeechRecognizer() {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(activity);
        speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());

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
                ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
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

    public void startListening() {
        speechRecognizer.startListening(speechRecognizerIntent);
    }

    public void stopListening() {
        speechRecognizer.stopListening();
    }

    public void destroy() {
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
        }
    }
}

package sg.edu.np.mad.TicketFinder;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class SpeechRecognitionNav {
    private static final String TAG = "SpeechRecognition";
    private SpeechRecognizer speechRecognizer;
    private Intent recognizerIntent;
    private SpeechRecognitionListener listener;
    private ArrayList<Event> eventList;
    private Context context;

    public SpeechRecognitionNav(Context context, SpeechRecognitionListener listener, ArrayList<Event> eventList) {
        this.context = context;
        this.listener = listener;
        this.eventList = eventList;
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

    public String getNavigationCommand(String result) {
        result = result.toLowerCase().trim();
        Log.d(TAG, "Recognized text: " + result); // Debug log for recognized text

        // Words to ignore in speech input
        Set<String> ignoreWords = new HashSet<>(Arrays.asList("i", "want", "to", "go", "open", "the", "a", "an", "on", "for", "and"));

        // Extract relevant keywords
        String[] words = result.split("\\s+");
        StringBuilder filteredResult = new StringBuilder();
        for (String word : words) {
            if (!ignoreWords.contains(word)) {
                filteredResult.append(word).append(" ");
            }
        }
        result = filteredResult.toString().trim();

        Log.d(TAG, "Filtered text: " + result); // Debug log for filtered text

        ArrayList<String> matchingEvents = new ArrayList<>();
        for (Event event : eventList) {
            String eventTitle = event.getTitle().toLowerCase().trim();
            String eventArtist = event.getArtist().toLowerCase().trim();
            Log.d(TAG, "Comparing with event title: " + eventTitle + " and artist: " + eventArtist); // Debug log for event titles and artists

            if (result.contains(eventTitle) || result.contains(eventArtist)) {
                matchingEvents.add(event.getTitle());
            }
        }

        if (matchingEvents.size() > 1) {
            // If multiple events are found, navigate to the ExploreEvents activity with the search filter
            Intent intent = new Intent(context, ExploreEvents.class);
            intent.putExtra("searchArtist", result);
            context.startActivity(intent);
            return null;
        } else if (matchingEvents.size() == 1) {
            return matchingEvents.get(0);
        }

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

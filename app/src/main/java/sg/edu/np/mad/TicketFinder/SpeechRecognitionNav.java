package sg.edu.np.mad.TicketFinder;

import android.content.Context; // Import for context
import android.content.Intent; // Import for intent handling
import android.os.Bundle; // Import for bundle handling
import android.speech.RecognitionListener; // Import for speech recognition listener
import android.speech.RecognizerIntent; // Import for recognizer intent
import android.speech.SpeechRecognizer; // Import for speech recognizer
import android.util.Log; // Import for logging

import java.util.ArrayList; // Import for ArrayList
import java.util.Arrays; // Import for Arrays
import java.util.HashSet; // Import for HashSet
import java.util.Locale; // Import for Locale
import java.util.Set; // Import for Set

public class SpeechRecognitionNav {
    private static final String TAG = "SpeechRecognition"; // Tag for logging
    private SpeechRecognizer speechRecognizer; // Speech recognizer instance
    private Intent recognizerIntent; // Intent for speech recognition
    private SpeechRecognitionListener listener; // Listener for speech recognition events
    private ArrayList<Event> eventList; // List of events to match against speech input
    private Context context; // Context of the activity

    public SpeechRecognitionNav(Context context, SpeechRecognitionListener listener, ArrayList<Event> eventList) {
        this.context = context; // Initialize context
        this.listener = listener; // Initialize listener
        this.eventList = eventList; // Initialize event list
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context); // Create speech recognizer instance
        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {
                listener.onReadyForSpeech(); // Notify listener that speech recognizer is ready
            }

            @Override
            public void onBeginningOfSpeech() {
                listener.onBeginningOfSpeech(); // Notify listener that speech input has started
            }

            @Override
            public void onRmsChanged(float rmsdB) {
                listener.onRmsChanged(rmsdB); // Notify listener of RMS change
            }

            @Override
            public void onBufferReceived(byte[] buffer) {
                listener.onBufferReceived(buffer); // Notify listener that buffer is received
            }

            @Override
            public void onEndOfSpeech() {
                listener.onEndOfSpeech(); // Notify listener that speech input has ended
            }

            @Override
            public void onError(int error) {
                listener.onError(error); // Notify listener of error
                Log.e(TAG, "Speech recognition error: " + error); // Log the error
            }

            @Override
            public void onResults(Bundle results) {
                ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION); // Get recognition results
                if (matches != null && matches.size() > 0) {
                    Log.d(TAG, "Recognized results: " + matches.get(0)); // Log recognized results
                    listener.onResults(matches.get(0)); // Notify listener of recognized results
                }
            }

            @Override
            public void onPartialResults(Bundle partialResults) {
                ArrayList<String> matches = partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION); // Get partial recognition results
                if (matches != null && matches.size() > 0) {
                    Log.d(TAG, "Partial results: " + matches.get(0)); // Log partial results
                    listener.onPartialResults(matches.get(0)); // Notify listener of partial results
                }
            }

            @Override
            public void onEvent(int eventType, Bundle params) {
                listener.onEvent(eventType, params); // Notify listener of events
            }
        });

        recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH); // Create recognizer intent
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM); // Set language model
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault()); // Set language to default locale
    }

    public void startListening() {
        Log.d(TAG, "Starting to listen"); // Log start of listening
        speechRecognizer.startListening(recognizerIntent); // Start listening for speech input
    }

    public void destroy() {
        if (speechRecognizer != null) {
            speechRecognizer.destroy(); // Destroy the speech recognizer if not null
        }
    }

    public interface SpeechRecognitionListener {
        void onReadyForSpeech(); // Callback for ready for speech event
        void onBeginningOfSpeech(); // Callback for beginning of speech event
        void onRmsChanged(float rmsdB); // Callback for RMS change
        void onBufferReceived(byte[] buffer); // Callback for buffer received
        void onEndOfSpeech(); // Callback for end of speech
        void onError(int error); // Callback for error event
        void onResults(String result); // Callback for results event
        void onPartialResults(String partialResult); // Callback for partial results event
        void onEvent(int eventType, Bundle params); // Callback for other events
    }

    public String getNavigationCommand(String result) {
        result = result.toLowerCase().trim(); // Convert result to lower case and trim whitespaces
        Log.d(TAG, "Recognized text: " + result); // Log recognized text

        // Words to ignore in speech input
        Set<String> ignoreWords = new HashSet<>(Arrays.asList("i", "want", "to", "go", "open", "the", "a", "an", "on", "for", "and"));

        // Extract relevant keywords
        String[] words = result.split("\\s+"); // Split the result into words
        StringBuilder filteredResult = new StringBuilder(); // Initialize StringBuilder for filtered result
        for (String word : words) {
            if (!ignoreWords.contains(word)) {
                filteredResult.append(word).append(" "); // Append relevant words to filtered result
            }
        }
        result = filteredResult.toString().trim(); // Convert StringBuilder to String and trim

        Log.d(TAG, "Filtered text: " + result); // Log filtered text

        ArrayList<String> matchingEvents = new ArrayList<>(); // Initialize list for matching events
        for (Event event : eventList) {
            String eventTitle = event.getTitle().toLowerCase().trim(); // Get event title in lower case and trim
            String eventArtist = event.getArtist().toLowerCase().trim(); // Get event artist in lower case and trim
            Log.d(TAG, "Comparing with event title: " + eventTitle + " and artist: " + eventArtist); // Log event title and artist

            if (result.contains(eventTitle) || result.contains(eventArtist)) {
                matchingEvents.add(event.getTitle()); // Add matching events to the list
            }
        }

        if (matchingEvents.size() > 1) {
            // If multiple events are found, navigate to the ExploreEvents activity with the search filter
            Intent intent = new Intent(context, ExploreEvents.class); // Create intent for ExploreEvents
            intent.putExtra("searchArtist", result); // Add search filter to the intent
            context.startActivity(intent); // Start ExploreEvents activity
            return null; // Return null since navigation is handled by the intent
        } else if (matchingEvents.size() == 1) {
            return matchingEvents.get(0); // Return the single matching event
        }

        // Check for navigation commands in the result
        if (result.contains("home")) {
            return "homepage"; // Navigate to homepage
        } else if (result.contains("explore")) {
            return "exploreEvents"; // Navigate to explore events
        } else if (result.contains("history")) {
            return "bookingHistory"; // Navigate to booking history
        } else if (result.contains("profile")) {
            return "profilePage"; // Navigate to profile page
        }
        return null; // Return null if no commands matched
    }
}

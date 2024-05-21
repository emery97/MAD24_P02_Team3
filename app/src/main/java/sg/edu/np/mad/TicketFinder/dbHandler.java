package sg.edu.np.mad.TicketFinder;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class dbHandler extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // Initialize Firebase
        FirebaseApp.initializeApp(this);

        FirebaseFirestore.setLoggingEnabled(true);
    }

    public void getData(FirestoreCallback firestoreCallback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference eventsCollection = db.collection("Events");

        eventsCollection.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        ArrayList<Event> eventList = new ArrayList<>();
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Event event = new Event();

                                if (document.contains("Name")) {
                                    event.setTitle(document.getString("Name"));
                                }
                                if (document.contains("Caption")) {
                                    event.setCaption(document.getString("Caption"));
                                }
                                if (document.contains("Price")) {
                                    event.setPrice(document.getDouble("Price"));
                                }
                                if (document.contains("Description")) {
                                    event.setDescription(document.getString("Description"));
                                }
                                if (document.contains("Artist")) {
                                    event.setArtist(document.getString("Artist"));
                                }
                                if (document.contains("Genre")) {
                                    event.setGenre(document.getString("Genre"));
                                }
                                if (document.contains("Venue")) {
                                    event.setVenue(document.getString("Venue"));
                                }
                                if (document.contains("Date")) {
                                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                                    String inputDate = document.getString("Date").substring(0, 10);
                                    LocalDate date = LocalDate.parse(inputDate, formatter);
                                    event.setDate(date);

                                }
                                if (document.contains("Time")) {
                                    event.setTime(document.getString("Time").substring(11, 16));
                                }

                                eventList.add(event);
                            }
                            firestoreCallback.onCallback(eventList);
                            Log.d("dbHandler", "getData: Callback invoked with eventList size: " + eventList.size());
                        } else {
                            Log.w("eventError", "Error getting documents.", task.getException());
                        }
                    }
                });
    }
}
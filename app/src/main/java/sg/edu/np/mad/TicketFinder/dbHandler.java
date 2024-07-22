package sg.edu.np.mad.TicketFinder;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class dbHandler extends Application {
    private Set<String> dictionary = new HashSet<>(); // Initialize the dictionary here

    @Override
    public void onCreate() {
        super.onCreate();
        // Initialize Firebase
        FirebaseApp.initializeApp(this);
        FirebaseFirestore.setLoggingEnabled(true);
        dictionary = new HashSet<>(); // Initialize dictionary set
    }


    //get event data (coded with the help of ChatGPT)
    public void getData(FirestoreCallback firestoreCallback) {
        // connect to firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        // reference events collections
        CollectionReference eventsCollection = db.collection("Events");

        eventsCollection.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        // init event list
                        ArrayList<Event> eventList = new ArrayList<>();

                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) { // for each event
                                // create new event object and set values
                                Event event = new Event();
                                if (document.contains("EventImage")) {
                                    ArrayList<String> eventImages = (ArrayList<String>) document.get("EventImage");
                                    if (eventImages != null && !eventImages.isEmpty()) {
                                        event.setImgUrl(eventImages.get(0));
                                    }
                                }
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
                                if (document.contains("Date")) { //format date as LocalDate
                                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                                    String inputDate = document.getString("Date").substring(0, 10);
                                    LocalDate date = LocalDate.parse(inputDate, formatter);
                                    event.setDate(date);

                                }
                                if (document.contains("Time")) {
                                    event.setTime(document.getString("Time").substring(11, 16));
                                }
                                //add to eventlist
                                eventList.add(event);
                            }
                            firestoreCallback.onCallback(eventList); // return eventlist
                            Log.d("dbHandler", "getData: Callback invoked with eventList size: " + eventList.size());
                        } else {
                            Log.w("eventError", "Error getting documents.", task.getException());
                        }
                    }
                });
    }

    public void getVenues(FirestoreCallback<String> firestoreCallback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference eventsCollection = db.collection("Events");

        eventsCollection.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                ArrayList<String> venues = new ArrayList<>();
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        if (document.contains("Venue")) {
                            String venue = document.getString("Venue");
                            if (!venues.contains(venue)) {
                                venues.add(venue);
                            }
                        }
                    }
                    firestoreCallback.onCallback(venues);
                } else { //
                    Log.w("venueError", "Error getting documents.", task.getException());
                }
            }
        });
    }

    public void getSeatCategoryData(FirestoreCallback<SeatCategory> firestoreCallback) {

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        CollectionReference seatCategoryCollection = db.collection("SeatCategory");

        seatCategoryCollection.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {

            @Override

            public void onComplete(@NonNull Task<QuerySnapshot> task) {

                Log.d("dbHandler", "getSeatCategoryData: Fetching seat category data from Firestore");

                ArrayList<SeatCategory> seatCategoryList = new ArrayList<>();

                if (task.isSuccessful()) {

                    for (QueryDocumentSnapshot document : task.getResult()) {

                        SeatCategory seatCategory = new SeatCategory();

                        if (document.contains("Category")) {

                            seatCategory.setCategory(document.getString("Category"));

                        }

                        // USED CHATGPT FOR THIS

                        if (document.contains("Price")) {

                            Object priceObject = document.get("Price");

                            if (priceObject instanceof Long) {

                                // Convert integer to double

                                Long priceLong = (Long) priceObject;

                                double priceDouble = priceLong.doubleValue();

                                seatCategory.setSeatCategoryPrice(priceDouble);

                                Log.d("PRICE", String.valueOf(priceDouble));

                            } else if (priceObject instanceof Double) {

                                // Price is already a double

                                double price = (Double) priceObject;

                                seatCategory.setSeatCategoryPrice(price);

                                Log.d("PRICE", String.valueOf(price));

                            } else {

                                // Handle unexpected data type

                                Log.e("PRICE", "Unexpected data type for 'Price' field: " + priceObject.getClass().getSimpleName());

                            }

                        }


                        if (document.contains("Seats")) {

                            ArrayList<String> seatList = (ArrayList<String>) document.get("Seats");

                            if (seatList != null && !seatList.isEmpty()) {

                                seatCategory.setSeats(seatList);

                            }

                        }

                        seatCategoryList.add(seatCategory);

                    }

                    firestoreCallback.onCallback(seatCategoryList);

                    Log.d("seatCategorySize", "getData: Callback invoked with seatCategory size: " + seatCategoryList.size());

                } else {

                    Log.w("seatCategoryError", "Error getting documents.", task.getException());

                }


            }

        });
    }

    public void getFaq(FirestoreCallback<Faq> firestoreCallback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference faqCollection = db.collection("FAQ");

        faqCollection.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                ArrayList<Faq> faqList = new ArrayList<>();
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Faq faq = document.toObject(Faq.class);
                        faqList.add(faq);
                    }
                    firestoreCallback.onCallback(faqList);
                } else {
                    Log.w("faqError", "Error getting documents.", task.getException());
                }
            }
        });
    }

    public void getGreetings(FirestoreCallback<Greeting> firestoreCallback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference greetingsCollection = db.collection("Greetings");

        greetingsCollection.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                ArrayList<Greeting> greetingList = new ArrayList<>();
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Greeting greeting = new Greeting();
                        if (document.contains("Greeting")) {
                            greeting.setGreeting(document.getString("Greeting"));
                        }
                        if (document.contains("Response")) {
                            greeting.setResponse(document.getString("Response"));
                        }
                        greetingList.add(greeting);
                    }
                    firestoreCallback.onCallback(greetingList);
                    Log.d("dbHandler", "getGreetings: Callback invoked with greetingList size: " + greetingList.size());
                } else {
                    Log.w("greetingError", "Error getting documents.", task.getException());
                }
            }
        });
    }

}
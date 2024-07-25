package sg.edu.np.mad.TicketFinder;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.github.chrisbanes.photoview.PhotoView;
import com.google.common.reflect.TypeToken;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.gson.Gson;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BuyTicket extends AppCompatActivity implements SeatSelectionFragment.OnInputValidListener {

    // List to hold seat categories
    private ArrayList<SeatCategory> seatCategoryList = new ArrayList<>();

    // Counter to track runtime
    private int runTime = 0;
    // Button to book tickets
    private Button booked;
    private List<Boolean> fragmentValidStates = new ArrayList<>();

    // for populating dropdown through fragments
    private EditText quantityInput;
    private int quantityEntered = 0;
    private LinearLayout dynamicContainer;
    // to get the seat cat:seat num
    private SeatSelectionViewModel viewModel;
    private HashMap<String, ArrayList<String>> latestSeatMap = new HashMap<>();
    private double totalPrice = 0.0;
    private int remainingPricesToFetch;

    private static String TAG = "buyTicket";
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Initialize runtime counter
        runTime = 0;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.buy_ticket);

        // Retrieve Event object from intent
        Intent eventIntent = getIntent();
        Event eventObj = (Event) eventIntent.getSerializableExtra("event");

        // Initialize views
        booked = findViewById(R.id.button_booked);
        booked.setVisibility(View.INVISIBLE);  // Initially hide the button


        quantityInput = findViewById(R.id.quantityInput);
        dynamicContainer = findViewById(R.id.dynamic_container);

        // set chosenConcertTitle to concert user choose in event details page
        // Get chosen concert title
        TextView chosenConcertTitle = findViewById(R.id.concertTitleChosen);
        String concertTitle = getIntent().getStringExtra("eventTitle");
        chosenConcertTitle.setText(concertTitle);

        // Initialize PhotoView
        PhotoView imageMap = findViewById(R.id.imageMap);
        imageMap.setImageResource(R.drawable.bigger_font_data);


        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(SeatSelectionViewModel.class);

        // Observe seatMap changes
        viewModel.getSeatMap().observe(this, new Observer<HashMap<String, ArrayList<String>>>() {
            @Override
            public void onChanged(HashMap<String, ArrayList<String>> updatedSeatMap) {
                Log.d(TAG, "onCreate: " + updatedSeatMap);
                latestSeatMap = updatedSeatMap;
            }
        });


        // Handle click on the booked button
        booked.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String concertName = chosenConcertTitle.getText().toString().trim();

                // Get selected seat number
                //String selectedSeatNumber = seatAutoCompleteTextView.getText().toString().trim();

                // reset variables
                totalPrice = 0.0;
                remainingPricesToFetch = 0;
                // Find price of selected seat
                for (Map.Entry<String, ArrayList<String>> entry : latestSeatMap.entrySet()) {
                    String seatCategoryKey = entry.getKey();
                    ArrayList<String> seatNumbers = entry.getValue();

                    for (String seatNumber : seatNumbers) {
                        remainingPricesToFetch++; // Increment the count of remaining prices to fetch

                        findSeatPrice(seatCategoryKey, new PriceCallback() {
                            @Override
                            public void onPriceFetched(double seatPrice) {
                                // Check if all prices have been fetched
                                remainingPricesToFetch--;
                                if (remainingPricesToFetch == 0) {
                                    // All prices have been fetched, now proceed to the next step
                                    onAllPricesFetched(concertName, eventObj, latestSeatMap);
                                }
                            }
                        });
                    }
                }
            }
        });

        // referenced from chatgpt
        // Adding texwatcher for quantity input
        quantityInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                String quantityText = quantityInput.getText().toString();
                try {
                    quantityEntered = Integer.parseInt(quantityText);
                    handleQuantityChange(quantityText);
                } catch (NumberFormatException e) {
                    Toast.makeText(BuyTicket.this, "Please enter a valid number", Toast.LENGTH_SHORT).show();
                }
                return true;
            }
            return false;
        });

        // Set up footer
        Footer.setUpFooter(this);
    }


    private void onAllPricesFetched(String concertName, Event eventObj, HashMap seatMap){
        // Get quantity entered by the user
        String quantityText = quantityInput.getText().toString().trim();
        int quantity = Integer.parseInt(quantityText);

        String eventTiming = getIntent().getStringExtra("eventTiming");

        // Create intent to start payment activity
        Intent intent = new Intent(BuyTicket.this, payment.class);
        intent.putExtra("event", eventObj); // Pass the Event object
        intent.putExtra("concertName", concertName);
        intent.putExtra("totalPrice", totalPrice);
        intent.putExtra("seatMap", seatMap);
        intent.putExtra("quantity", quantity);
        intent.putExtra("eventTiming", eventTiming);
        startActivity(intent);

    }

    // asking if quantity entered is final
    private void showConfirmationDialog(int quantity) {
        // Building dialogue
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure with this quantity? ")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Toast.makeText(BuyTicket.this, "Continue entering your seat number and seat category at the bottom", Toast.LENGTH_SHORT).show();
                        addDynamicFragments(quantity);
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {

                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();

        // Get the buttons and set their custom styles
        Button positiveButton = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
        Button negativeButton = dialog.getButton(DialogInterface.BUTTON_NEGATIVE);

        // Apply the custom styles
        positiveButton.setTextColor(Color.parseColor("#976954"));
        negativeButton.setTextColor(Color.parseColor("#976954"));
    }

    // handling the quantity input
    private void handleQuantityChange(String quantityText) {
        try {
            int quantity = Integer.parseInt(quantityText);
            showConfirmationDialog(quantity);
        } catch (NumberFormatException e) {
            Toast.makeText(BuyTicket.this, "Please enter a valid number", Toast.LENGTH_SHORT).show();
        }
    }

    // adding seat cat and seat num drop down fragment
    private void addDynamicFragments(int quantity) {
        // Clear existing fragments
        for (Fragment fragment : getSupportFragmentManager().getFragments()) {
            getSupportFragmentManager().beginTransaction().remove(fragment).commit();
        }

        // Initialize the list based on the quantity
        initializeFragmentValidStates(quantity);

        // Add new fragments based on quantity
        for (int i = 0; i < quantity; i++) {
            Fragment fragment = SeatSelectionFragment.newInstance(i);
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.add(dynamicContainer.getId(), fragment, "fragment" + i).commit();
        }
    }


    private void initializeFragmentValidStates(int numberOfFragments) {
        fragmentValidStates = new ArrayList<>(Collections.nCopies(numberOfFragments, false));
        Log.d(TAG, "Initialized fragmentValidStates: " + fragmentValidStates.toString());
    }

    @Override
    public void onInputValid(boolean isValid, int fragmentIndex) {
        if (fragmentIndex >= 0 && fragmentIndex < fragmentValidStates.size()) {
            fragmentValidStates.set(fragmentIndex, isValid);
            Log.d(TAG, "Fragment index: " + fragmentIndex + ", Valid: " + isValid);
            Log.d(TAG, "Current fragmentValidStates: " + fragmentValidStates.toString());
            updateBookedButtonVisibility();
        } else {
            Log.e(TAG, "Invalid fragment index: " + fragmentIndex);
        }
    }

    private void updateBookedButtonVisibility() {
        boolean allValid = !fragmentValidStates.contains(false);
        Log.d(TAG, "All fragments valid: " + allValid);
        booked.setVisibility(allValid ? View.VISIBLE : View.INVISIBLE);
        Log.d(TAG, "Button visibility: " + (allValid ? "VISIBLE" : "INVISIBLE"));
    }
    public interface PriceCallback {
        void onPriceFetched(double price);
    }


    // Method to find the price of a seat
    private void findSeatPrice(String seatCategoryKey, PriceCallback callback) {
        db.collection("SeatCategory")
                .whereEqualTo("Category", seatCategoryKey)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            double price = document.getDouble("Price");
                            Log.d(TAG, "findSeatPrice: "+seatCategoryKey+" "+ price);
                            totalPrice+=price;
                            callback.onPriceFetched(price);
                            return;
                        }
                    } else {
                        Log.e("BuyTicket", "Error fetching seat price", task.getException());
                        callback.onPriceFetched(0.0); // Return 0.0 in case of an error
                    }
                });
    }

}


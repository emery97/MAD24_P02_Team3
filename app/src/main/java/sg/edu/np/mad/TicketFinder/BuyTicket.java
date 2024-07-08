package sg.edu.np.mad.TicketFinder;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.github.chrisbanes.photoview.PhotoView;

import java.util.ArrayList;

public class BuyTicket extends AppCompatActivity {

    // List to hold seat categories
    private ArrayList<SeatCategory> seatCategoryList = new ArrayList<>();
    // AutoCompleteTextView for selecting seat category
    private AutoCompleteTextView autoCompleteTextView;
    // AutoCompleteTextView for selecting seat number
    private AutoCompleteTextView seatAutoCompleteTextView;
    // Selected seat category
    private String chosenSeatCategory;
    // Counter to track runtime
    private int runTime = 0;
    // Button to book tickets
    private Button booked;
    // EditText for entering quantity
    private EditText quantityEditText;

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
        autoCompleteTextView = findViewById(R.id.auto_complete_txt);
        seatAutoCompleteTextView = findViewById(R.id.auto_complete_txt2);
        booked = findViewById(R.id.button_booked);
        quantityEditText = findViewById(R.id.quantity);

        // Initially disable the dropdowns
        autoCompleteTextView.setEnabled(false);
        seatAutoCompleteTextView.setEnabled(false);

        // set chosenConcertTitle to concert user choose in event details page
        // Get chosen concert title
        TextView chosenConcertTitle = findViewById(R.id.concertTitleChosen);
        String concertTitle = getIntent().getStringExtra("eventTitle");
        chosenConcertTitle.setText(concertTitle);

        // Get references for conclusion data
        TextView selectedSeatCat = findViewById(R.id.selectedSeatCat);
        TextView selectedSeatNum = findViewById(R.id.selectedSeatNum);

        // Initialize PhotoView
        PhotoView imageMap = findViewById(R.id.imageMap);
        imageMap.setImageResource(R.drawable.bigger_font_data);

        // Fetch seat category data from Firestore
        dbHandler handler = new dbHandler();
        handler.getSeatCategoryData(new FirestoreCallback<SeatCategory>() {
            @Override
            public void onCallback(ArrayList<SeatCategory> retrievedSeatCategoryList) {
                // Add retrieved seat categories to the list
                seatCategoryList.addAll(retrievedSeatCategoryList);

                // Extract seat category names
                ArrayList<String> seatCategoryNames = new ArrayList<>();
                for (SeatCategory seatCategory : seatCategoryList) {
                    if (seatCategory.getCategory() != null) {
                        seatCategoryNames.add(seatCategory.getCategory());
                    }
                }

                // referenced from chatgpt
                // Update the adapter with the new data
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // Create ArrayAdapter for seat categories
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(BuyTicket.this, android.R.layout.simple_dropdown_item_1line, seatCategoryNames);
                        autoCompleteTextView.setAdapter(adapter);
                        // Enable the dropdown after setting the adapter
                        autoCompleteTextView.setEnabled(true);
                    }
                });
            }
        });

        // Handle item selection in the seat category dropdown
        autoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Get the selected item
                String selectedItem = (String) parent.getItemAtPosition(position);
                // Set the chosen seat category
                chosenSeatCategory = selectedItem;
                // Show a toast message indicating the selection
                Toast.makeText(BuyTicket.this, "You've chosen: " + selectedItem, Toast.LENGTH_SHORT).show();
                // Filter seat numbers based on the selected category
                filterSeatsByCategory(selectedItem);
                // Increment the runtime counter
                runTime++;
                // Update selected seat category text
                selectedSeatCat.setText(selectedItem);
                // Log the runtime check
                Log.d("RUNTIME CHECK!!", String.valueOf(runTime));
            }
        });

        // Handle item selection in the seat number dropdown
        seatAutoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Get the selected item
                String selectedItem = (String) parent.getItemAtPosition(position);
                // Update the selected seat number text
                selectedSeatNum.setText(selectedItem);
                // Show a toast message indicating the selection
                Toast.makeText(BuyTicket.this, "You've chosen: " + selectedItem, Toast.LENGTH_SHORT).show();
                // Log the selected seat number
                Log.d("seatNumberToastMessage", "DONE " + selectedItem);
                // Update button visibility
                bookedAppear();
            }
        });

        // Log the quantity edit text value
        Log.d("QUANTITY EDIT TEXT", "onCreate: " + quantityEditText.getText().toString());
        // Update button visibility
        bookedAppear();

        // Handle click on the booked button
        booked.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String concertName = chosenConcertTitle.getText().toString().trim();
                // Get selected seat number
                String selectedSeatNumber = seatAutoCompleteTextView.getText().toString().trim();
                // Find price of selected seat
                double seatPrice = findSeatPrice(selectedSeatNumber);
                // Get selected seat category
                String selectedSeatCategory = chosenSeatCategory;
                // Get quantity entered by the user
                String quantityText = quantityEditText.getText().toString().trim();
                int quantity = Integer.parseInt(quantityText);
                // Calculate total price
                double totalPrice = seatPrice * quantity;
                String eventTiming = getIntent().getStringExtra("eventTiming");
                // Create intent to start payment activity
                Intent intent = new Intent(BuyTicket.this, payment.class);
                intent.putExtra("event", eventObj); // Pass the Event object
                intent.putExtra("concertName", concertName);
                intent.putExtra("totalPrice", totalPrice);
                intent.putExtra("seatCategory", selectedSeatCategory);
                intent.putExtra("seatNumber", selectedSeatNumber);
                intent.putExtra("quantity", quantity);
                intent.putExtra("eventTiming", eventTiming);
                startActivity(intent);
            }
        });

        // Set up footer
        Footer.setUpFooter(this);
    }

    // Method to filter seats by category
    private void filterSeatsByCategory(String category) {
        // List to hold filtered seat numbers
        ArrayList<String> seatNumbers = new ArrayList<>();
        for (SeatCategory seatCategory : seatCategoryList) {
            if (seatCategory.getCategory().equals(category)) {
                seatNumbers.addAll(seatCategory.getSeats());
                break;
            }
        }
        // referenced from chatgpt
        // Update the seat number dropdown adapter
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView selectedSeatNum = findViewById(R.id.selectedSeatNum);
                ArrayAdapter<String> adapter = new ArrayAdapter<>(BuyTicket.this, android.R.layout.simple_dropdown_item_1line, seatNumbers);
                seatAutoCompleteTextView.setAdapter(adapter);
                // Enable the seat dropdown after setting the adapter
                seatAutoCompleteTextView.setEnabled(true);
                // Reset seat number text if user chooses a new seat category
                if (runTime >= 1) {
                    seatAutoCompleteTextView.setText("");
                    selectedSeatNum.setText("");
                }
            }
        });
    }

    // Method to find the price of a seat
    private double findSeatPrice(String seatNumber) {
        for (SeatCategory seatCategory : seatCategoryList) {
            if (seatCategory.getSeats().contains(seatNumber)) {
                return seatCategory.getSeatCategoryPrice();
            }
        }
        return 0.0;
    }

    // Method to manage the visibility of the booked button
    private void bookedAppear() {
        // References to conclusion data
        TextView selectedSeatCat = findViewById(R.id.selectedSeatCat);
        TextView selectedSeatNum = findViewById(R.id.selectedSeatNum);

        // referenced from chatgpt
        // Add a TextWatcher to quantityEditText
        quantityEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Not required
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    // Update button visibility based on all conditions here
                    if (selectedSeatCat.getText().toString().trim().length() > 0 &&
                            selectedSeatNum.getText().toString().trim().length() > 0 &&
                            Integer.parseInt(String.valueOf(quantityEditText.getText())) > 0) {
                        // Ensure user enters a quantity greater than 0
                        booked.setVisibility(View.VISIBLE);
                    }
                    if (Integer.parseInt(String.valueOf(quantityEditText.getText())) == 0) {
                        booked.setVisibility(View.INVISIBLE);
                    }
                } catch (Exception e) {
                    Toast.makeText(BuyTicket.this, "Please enter a number more than 0 ", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Not required
            }
        });

        // referenced from chatgpt
        // Add a TextWatcher to seatAutoCompleteTextView
        seatAutoCompleteTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Not required
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Update selected seat number text
                Log.d("selected seat num change", "selectedSeatNum: " + selectedSeatNum.getText());
                selectedSeatNum.setText(s.toString().trim());
                // Hide booked button if no seat number is selected
                if (selectedSeatNum == null || selectedSeatNum.getText().toString().trim().length() == 0) {
                    booked.setVisibility(View.INVISIBLE);
                }
                try {
                    // Update button visibility based on all conditions here
                    if (selectedSeatCat.getText().toString().trim().length() > 0 &&
                            selectedSeatNum.getText().toString().trim().length() > 0 &&
                            Integer.parseInt(String.valueOf(quantityEditText.getText())) > 0) {
                        // Ensure user enters a quantity greater than 0
                        booked.setVisibility(View.VISIBLE);
                    }
                } catch (Exception e) {
                    Toast.makeText(BuyTicket.this, "Please enter a valid quantity (numbers only)", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Not required
            }
        });
    }
}


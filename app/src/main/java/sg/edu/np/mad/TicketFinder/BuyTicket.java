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
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class BuyTicket extends AppCompatActivity {

    private ArrayList<SeatCategory> seatCategoryList = new ArrayList<>();
    private AutoCompleteTextView autoCompleteTextView;
    private AutoCompleteTextView seatAutoCompleteTextView;

    private String chosenSeatCategory;
    private int runTime =0;
    private Button booked;
    private EditText quantityEditText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        runTime = 0;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.buy_ticket);

        autoCompleteTextView = findViewById(R.id.auto_complete_txt);
        seatAutoCompleteTextView = findViewById(R.id.auto_complete_txt2);
        booked = findViewById(R.id.button_booked);
        quantityEditText = findViewById(R.id.quantity);

        // Initially disable the dropdowns
        autoCompleteTextView.setEnabled(false);
        seatAutoCompleteTextView.setEnabled(false);

        // conclusion data
        TextView selectedSeatCat= findViewById(R.id.selectedSeatCat);
        TextView selectedSeatNum= findViewById(R.id.selectedSeatNum);

        dbHandler handler = new dbHandler();
        handler.getSeatCategoryData(new FirestoreCallback<SeatCategory>() {
            @Override
            public void onCallback(ArrayList<SeatCategory> retrievedSeatCategoryList) {
                seatCategoryList.addAll(retrievedSeatCategoryList);
                Log.d("onCallback", "Retrieved seat category list size: " + retrievedSeatCategoryList.size());


                ArrayList<String> seatCategoryNames = new ArrayList<>();
                for (SeatCategory seatCategory : seatCategoryList) {
                    if (seatCategory.getCategory() != null) {
                        seatCategoryNames.add(seatCategory.getCategory());
                    }
                }

                Log.d("seatCategoryLength", String.valueOf(seatCategoryNames.size()));

                // loading the drop down data
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // Update the adapter with the new data
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(BuyTicket.this, android.R.layout.simple_dropdown_item_1line, seatCategoryNames);
                        autoCompleteTextView.setAdapter(adapter);
                        autoCompleteTextView.setEnabled(true); // Enable the dropdown after setting the adapter
                    }
                });
            }
        });
        autoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedItem = (String) parent.getItemAtPosition(position);
                chosenSeatCategory = selectedItem;
                Toast.makeText(BuyTicket.this, "You've chosen: " + selectedItem, Toast.LENGTH_SHORT).show();
                filterSeatsByCategory(selectedItem);
                runTime++;
                selectedSeatCat.setText(selectedItem);
                Log.d("RUNTIME CHECK!!",String.valueOf(runTime));
            }
        });

        seatAutoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedItem = (String) parent.getItemAtPosition(position);
                selectedSeatNum.setText(selectedItem);
                Toast.makeText(BuyTicket.this, "You've chosen: " + selectedItem, Toast.LENGTH_SHORT).show();
                Log.d("seatNumberToastMessage","DONE " + selectedItem);
                bookedAppear();
            }
        });

        Log.d("QUANTITY EDIT TEXT", "onCreate: " + quantityEditText.getText().toString() );
        bookedAppear();
        booked.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String selectedSeatNumber = seatAutoCompleteTextView.getText().toString().trim();
                double seatPrice = findSeatPrice(selectedSeatNumber);
                String selectedSeatCategory = chosenSeatCategory;

                String quantityText = quantityEditText.getText().toString().trim();
                int quantity = Integer.parseInt(quantityText);

                double totalPrice = seatPrice * quantity;

                Intent intent = new Intent(BuyTicket.this, payment.class);
                intent.putExtra("totalPrice", totalPrice);
                intent.putExtra("seatCategory", selectedSeatCategory);
                intent.putExtra("seatNumber", selectedSeatNumber);
                intent.putExtra("quantity", quantity);
                startActivity(intent);
            }
        });

        Footer.setUpFooter(this);
    }
    private void filterSeatsByCategory(String category) {
        ArrayList<String> seatNumbers = new ArrayList<>();
        for (SeatCategory seatCategory : seatCategoryList) {
            Log.d("SUCCESS", String.valueOf(seatCategoryList.size()));
            if (seatCategory.getCategory().equals(category)) {
                seatNumbers.addAll(seatCategory.getSeats());
                Log.d("SUCCESS", "SIZE OF FILTERED SEATS: " + String.valueOf(seatCategory.getSeats().size()));
                break;
            }
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView selectedSeatNum= findViewById(R.id.selectedSeatNum);
                ArrayAdapter<String> adapter = new ArrayAdapter<>(BuyTicket.this, android.R.layout.simple_dropdown_item_1line, seatNumbers);
                seatAutoCompleteTextView.setAdapter(adapter);
                seatAutoCompleteTextView.setEnabled(true); // Enable the seat dropdown after setting the adapter
                Log.d("RUNTIME CHECK AT RUN","before if: "+String.valueOf(runTime) );
                // reset seat number text if user chooses a new seat category
                if (runTime >= 1){
                    seatAutoCompleteTextView.setText("");
                    selectedSeatNum.setText("");
                    Log.d("runTime value at run", String.valueOf(runTime));
                }

            }
        });
    }

    private double findSeatPrice(String seatNumber) {
        for (SeatCategory seatCategory : seatCategoryList) {
            if (seatCategory.getSeats().contains(seatNumber)) {
                return seatCategory.getSeatCategoryPrice();
            }
        }
        return 0.0;
    }

    private void bookedAppear(){
        // conclusion data
        TextView selectedSeatCat= findViewById(R.id.selectedSeatCat);
        TextView selectedSeatNum= findViewById(R.id.selectedSeatNum);
        quantityEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Not required
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try{
                    // Update button visibility based on all conditions here
                    if (selectedSeatCat.getText().toString().trim().length() > 0 &&
                            selectedSeatNum.getText().toString().trim().length() > 0 &&
                            Integer.parseInt(String.valueOf(quantityEditText.getText())) > 0) {
                        // need to ensure user enter quantity > 0
                        booked.setVisibility(View.VISIBLE);
                    }
                    if(Integer.parseInt(String.valueOf(quantityEditText.getText())) == 0){
                        booked.setVisibility(View.INVISIBLE);
                    }

                }catch(Exception e){
                    Toast.makeText(BuyTicket.this, "Please enter a number more than 0 ", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Not required
            }
        });
        seatAutoCompleteTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Not required
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.d("selected seat num change", "selectedSeatNum: " + selectedSeatNum.getText());
                selectedSeatNum.setText(s.toString().trim());
                if (selectedSeatNum == null || selectedSeatNum.getText().toString().trim().length() == 0){
                    booked.setVisibility(View.INVISIBLE);
                }
                try{
                    // Update button visibility based on all conditions here
                    if (selectedSeatCat.getText().toString().trim().length() > 0 &&
                            selectedSeatNum.getText().toString().trim().length() > 0 &&
                            Integer.parseInt(String.valueOf(quantityEditText.getText())) > 0) {
                        // need to ensure user enter quantity > 0
                        booked.setVisibility(View.VISIBLE);
                    }

                }catch(Exception e){
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

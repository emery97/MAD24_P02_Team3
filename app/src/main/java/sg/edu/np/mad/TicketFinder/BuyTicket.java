package sg.edu.np.mad.TicketFinder;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        runTime = 0;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.buy_ticket);

        autoCompleteTextView = findViewById(R.id.auto_complete_txt);
        seatAutoCompleteTextView = findViewById(R.id.auto_complete_txt2);
        booked = findViewById(R.id.button_booked);

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
                Log.d("RUNTIME CHECK!!",String.valueOf(runTime));
            }
        });

        seatAutoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedItem = (String) parent.getItemAtPosition(position);
                Toast.makeText(BuyTicket.this, "You've chosen: " + selectedItem, Toast.LENGTH_SHORT).show();
                Log.d("seatNumberToastMessage","DONE " + selectedItem);
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
}
//
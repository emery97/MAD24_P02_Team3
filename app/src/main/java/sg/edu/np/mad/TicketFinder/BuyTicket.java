// File: BuyTicket.java
package sg.edu.np.mad.TicketFinder;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class BuyTicket extends AppCompatActivity {

    private ArrayList<SeatCategory> seatCategoryList = new ArrayList<>();
    private ArrayAdapter<String> adapterItems;
    private AutoCompleteTextView autoCompleteTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.buy_ticket);

        autoCompleteTextView = findViewById(R.id.auto_complete_txt);

        dbHandler handler = new dbHandler();
        handler.getSeatCategoryData(new FirestoreCallback<SeatCategory>() {
            @Override
            public void onCallback(ArrayList<SeatCategory> retrievedSeatCategoryList) {
                seatCategoryList.addAll(retrievedSeatCategoryList);
                Log.d("onCallback", "Retrieved seat category list size: " + retrievedSeatCategoryList.size());

                // Convert SeatCategory list to a list of Strings for the dropdown
                ArrayList<String> seatCategoryNames = new ArrayList<>();
                for (SeatCategory seatCategory : seatCategoryList) {
                    seatCategoryNames.add(seatCategory.getCategory());
                }
                Log.d("seatCategoryLength", String.valueOf(seatCategoryNames.size()));
                // Update the adapter with the new data
                ArrayAdapter<String> adapter = new ArrayAdapter<>(BuyTicket.this, android.R.layout.simple_dropdown_item_1line,seatCategoryNames);
                autoCompleteTextView.setAdapter(adapter);
            }
        });

        autoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String item = parent.getItemAtPosition(position).toString();
                Toast.makeText(BuyTicket.this, item, Toast.LENGTH_SHORT).show();
            }
        });

        Footer.setUpFooter(this);
    }
}

package sg.edu.np.mad.TicketFinder;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class BuyTicket extends AppCompatActivity {

    String[] seatCategory = {"VIP Standing Priority","VIP Seated", "CAT 1 Standing","CAT 2","CAT 3","CAT 4"};

    AutoCompleteTextView autoCompleteTextView;
    ArrayAdapter<String> adapterItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.buy_ticket);

        autoCompleteTextView = findViewById(R.id.auto_complete_txt);
        adapterItems = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, seatCategory);

        autoCompleteTextView.setAdapter(adapterItems);

        autoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String item = parent.getItemAtPosition(position).toString();
                Toast.makeText(BuyTicket.this, item, Toast.LENGTH_SHORT).show();
            }
        });
    }
}

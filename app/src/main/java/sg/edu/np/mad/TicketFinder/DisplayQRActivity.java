package sg.edu.np.mad.TicketFinder;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import org.json.JSONException;
import org.json.JSONObject;

public class DisplayQRActivity extends AppCompatActivity {
    private ImageView qrCodeImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.display_qractivity);

        qrCodeImageView = findViewById(R.id.imageViewQRCode);

        // Retrieve JSON data from intent
        String eventDetailsJsonString = getIntent().getStringExtra("eventDetailsJson");
        try {
            JSONObject eventDetailsJson = new JSONObject(eventDetailsJsonString);

            // Generate QR code using eventDetailsJson
            Bitmap qrBitmap = generateQRCode(eventDetailsJson);
            qrCodeImageView.setImageBitmap(qrBitmap);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // Method to generate QR code from JSON object
    private Bitmap generateQRCode(JSONObject jsonObject) {
        // Implement QR code generation logic here
        // Example implementation using BarcodeEncoder and MultiFormatWriter
        Bitmap bitmap = null;
        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
        try {
            BitMatrix bitMatrix = multiFormatWriter.encode(jsonObject.toString(), BarcodeFormat.QR_CODE, 400, 400);
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            bitmap = barcodeEncoder.createBitmap(bitMatrix);
        } catch (WriterException e) {
            e.printStackTrace();
        }
        return bitmap;
    }
}

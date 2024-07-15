package sg.edu.np.mad.TicketFinder;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class DisplayQRActivity extends AppCompatActivity {
    private static final String TAG = DisplayQRActivity.class.getSimpleName();
    private ImageView qrCodeImageView;
    private FirebaseFirestore db;
    private String qrCodeDocumentId;
    private ListenerRegistration qrCodeListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.display_qractivity);

        qrCodeImageView = findViewById(R.id.imageViewQRCode);
        db = FirebaseFirestore.getInstance();

        // Retrieve JSON data from intent
        String eventDetailsJsonString = getIntent().getStringExtra("eventDetailsJson");
        try {
            JSONObject eventDetailsJson = new JSONObject(eventDetailsJsonString);

            // Generate QR code using eventDetailsJson
            Bitmap qrBitmap = generateQRCode(eventDetailsJson);
            qrCodeImageView.setImageBitmap(qrBitmap);

            // Store QR code data in Firestore and start listening for status changes
            storeQRCodeDataInFirestore(eventDetailsJson);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // Method to generate QR code from JSON object
    private Bitmap generateQRCode(JSONObject jsonObject) {
        // Add expiration time to JSON object
        long currentTimeMillis = System.currentTimeMillis();
        long expiryTimeMillis = currentTimeMillis + (10 * 60 * 1000); // Expiry in 10 minutes

        try {
            jsonObject.put("expiryTimeMillis", expiryTimeMillis);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Generate QR code from JSON object
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

    private void storeQRCodeDataInFirestore(JSONObject jsonObject) {
        Map<String, Object> qrCodeData = new HashMap<>();
        try {
            qrCodeData.put("data", jsonObject.toString());
            qrCodeData.put("status", "on-hold");

            db.collection("QrCodes")
                    .add(qrCodeData)
                    .addOnSuccessListener(documentReference -> {
                        qrCodeDocumentId = documentReference.getId();
                        Log.d(TAG, "DocumentSnapshot added with ID: " + qrCodeDocumentId);
                        // Start listening for changes in the QR code document
                        startListeningForStatusChanges();
                    })
                    .addOnFailureListener(e -> Log.w(TAG, "Error adding document", e));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startListeningForStatusChanges() {
        qrCodeListener = db.collection("QrCodes")
                .document(qrCodeDocumentId)
                .addSnapshotListener((documentSnapshot, e) -> {
                    if (e != null) {
                        Log.w(TAG, "Listen failed.", e);
                        return;
                    }

                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        String status = documentSnapshot.getString("status");
                        if ("waiting".equals(status)) {
                            showAlert("Waiting for approval");
                        }
                    } else {
                        Log.d(TAG, "Current data: null");
                    }
                });
    }

    private void showAlert(String message) {
        new AlertDialog.Builder(this)
                .setTitle("Status")
                .setMessage(message)
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Remove the Firestore listener when the activity is destroyed
        if (qrCodeListener != null) {
            qrCodeListener.remove();
        }
    }
}

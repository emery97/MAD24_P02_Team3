package sg.edu.np.mad.TicketFinder;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

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
    private AlertDialog alertDialog;

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
            qrCodeData.put("usage", "Not Used");
            qrCodeData.put("verify", "Not Verified");

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

    // Live listener for the status
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
                        String verifyStatus = documentSnapshot.getString("verify");

                        // Check if the verify status is already "Verified"
                        if ("waiting".equals(status) && !"Verified".equals(verifyStatus)) {
                            // Prompt for email before showing the alert
                            promptForEmailAndValidate(documentSnapshot);
                        } else if ("approved".equals(status)) {
                            dismissAlert();
                            showApprovalSuccessAlert();
                        } else if ("rejected".equals(status)){
                            Intent intent = new Intent(DisplayQRActivity.this, BookingHistoryDetails.class);
                            startActivity(intent);
                            finish();
                        }
                    } else {
                        Log.d(TAG, "Current data: null");
                    }
                });
    }


    private void promptForEmailAndValidate(DocumentSnapshot documentSnapshot) {
        // Create an EditText input field
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);

        new AlertDialog.Builder(this, R.style.CustomAlertDialogTheme)
                .setTitle("Enter Email")
                .setMessage("Please enter your email to proceed:")
                .setView(input)
                .setPositiveButton("OK", (dialog, which) -> {
                    String enteredEmail = input.getText().toString().trim();
                    try {
                        // Retrieve the email from the QR code JSON data
                        String qrCodeEmail = new JSONObject(documentSnapshot.getString("data")).getString("email");

                        // Compare the entered email with the email in the QR code JSON
                        if (enteredEmail.equals(qrCodeEmail)) {
                            // Update the "verify" field to "Verified" in Firestore
                            documentSnapshot.getReference().update("verify", "Verified")
                                    .addOnSuccessListener(aVoid -> {
                                        showAlert("Email verified. Waiting for approval.");
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.w(TAG, "Error updating document", e);
                                        Toast.makeText(this, "Failed to update verification status.", Toast.LENGTH_SHORT).show();
                                    });
                        } else {
                            Toast.makeText(this, "Email does not match. Please try again.", Toast.LENGTH_SHORT).show();
                            promptForEmailAndValidate(documentSnapshot);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Error retrieving email from QR code data.", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.cancel())
                .show();
    }

    private void showAlert(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Status")
                .setMessage(message);

        // Create custom AlertDialog without any buttons
        alertDialog = builder.create();

        // Disable outside touch and back button to dismiss
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.setCancelable(false);

        // Show the dialog
        alertDialog.show();
    }


    private void dismissAlert() {
        // Dismiss any existing alert dialog if shown
        if (alertDialog != null && alertDialog.isShowing()) {
            alertDialog.dismiss();
        }
    }

    private void showApprovalSuccessAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("QR Code Approved")
                .setMessage("The QR Code has been approved.")
                .setPositiveButton("OK", (dialog, which) -> {
                    Intent intent = new Intent(DisplayQRActivity.this, BookingHistoryDetails.class);
                    startActivity(intent);
                    finish();
                })
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

package sg.edu.np.mad.TicketFinder;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;

public class profilePage extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 1; // CHANGED
    private static final int CAMERA_REQUEST_CODE = 2; // CHANGED
    private static final int CAMERA_PERMISSION_CODE = 3; // CHANGED

    // UI components
    private TextView username, password, email, regPassword;
    private EditText editUsername, editPassword;
    private ImageView editingIcon, profilePicture;
    private CheckBox showPassword;
    private Button saveButton, logoutButton, feedbackbutton, deleteAccountButton, uploadProfilePicButton; // CHANGED
    private String actualPassword;
    private SharedPreferences sharedPreferences;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseUser firebaseUser;
    private FirebaseStorage storage; // CHANGED
    private StorageReference storageReference; // CHANGED
    private String userId;
    private static final String TAG = "ProfilePage";
    private boolean isEditMode = false;

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_page);

        // Set up footer
        Footer.setUpFooter(this);

        // Initialize UI components
        username = findViewById(R.id.regUsername);
        password = findViewById(R.id.regPassword);
        email = findViewById(R.id.regEmail);
        editUsername = findViewById(R.id.editUsername);
        editPassword = findViewById(R.id.editPassword);
        regPassword = findViewById(R.id.regPassword);
        editingIcon = findViewById(R.id.editingIcon);
        profilePicture = findViewById(R.id.profilePicture);
        showPassword = findViewById(R.id.showPassword);
        saveButton = findViewById(R.id.saveButton);
        logoutButton = findViewById(R.id.logoutButton);
        feedbackbutton = findViewById(R.id.Viewfeedbackbtn);
        deleteAccountButton = findViewById(R.id.deleteAccountButton);
        uploadProfilePicButton = findViewById(R.id.uploadProfilePicButton); // CHANGED

        sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);

        // Initialise database components
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        firebaseUser = mAuth.getCurrentUser();
        storage = FirebaseStorage.getInstance(); // CHANGED
        storageReference = storage.getReference(); // CHANGED

        // Check if user is authenticated
        if (firebaseUser == null) {
            // Redirect to login page if user is not authenticated
            Log.e(TAG, "FirebaseUser is null, redirecting to login");
            Intent intent = new Intent(profilePage.this, MainActivity.class);
            startActivity(intent);
            finish();
        }

        // Get userId from SharedPreferences
        userId = sharedPreferences.getString("userId", "N/A");
        if (userId.equals("N/A")) {
            // Attempt to fetch the userId from Firestore using the email
            fetchUserId();
        } else {
            // Load user data
            loadUserData();
        }

        // OnClickListener for show password checkbox
        showPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isEditMode) {
                    togglePasswordVisibility();
                } else {
                    togglePasswordVisibilityNonEditMode();
                }
            }
        });

        // OnClickListener for editing icon
        editingIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isEditMode = true;
                AllowEditing();
                saveButton.setVisibility(View.VISIBLE); // Ensure save button is visible when editing
                deleteAccountButton.setVisibility(View.VISIBLE); // Show delete button in edit mode
                feedbackbutton.setVisibility(View.INVISIBLE);
                logoutButton.setVisibility(View.INVISIBLE);
                uploadProfilePicButton.setVisibility(View.VISIBLE); // CHANGED
            }
        });

        // OnClickListener for save button
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get password from EditText
                actualPassword = editPassword.getText().toString();
                // Disable editing mode
                UnallowEditing();
                isEditMode = false;
                // Update user information
                updateUserInformation();
            }
        });

        // OnClickListener for logout button
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Perform logout
                logout();
            }
        });

        // OnClickListener for feedback button
        feedbackbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to feedback activity
                Intent intent = new Intent(profilePage.this, ViewFeedback.class);
                startActivity(intent);
            }
        });

        // OnClickListener for delete account button
        deleteAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show confirmation dialog
                new AlertDialog.Builder(profilePage.this)
                        .setTitle("Delete Account")
                        .setMessage("Are you sure you want to delete your account? This action cannot be undone.")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // User confirmed to delete the account
                                deleteAccount();
                            }
                        })
                        .setNegativeButton(android.R.string.no, null)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }
        });

        // OnClickListener for upload profile picture button // CHANGED
        uploadProfilePicButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showImageOptions();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clear SharedPreferences if "RememberMe" is false
        if (!sharedPreferences.getBoolean("RememberMe", false)) {
            sharedPreferences.edit().clear().apply();
        }
    }

    // Method to fetch userId from Firestore using email
    private void fetchUserId() {
        // Match account email to entry in database
        db.collection("Account").whereEqualTo("Email", firebaseUser.getEmail()).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        // Get userId
                        DocumentSnapshot documentSnapshot = queryDocumentSnapshots.getDocuments().get(0);
                        userId = documentSnapshot.getId();

                        // Save userId to SharedPreferences
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("userId", userId);
                        editor.apply();

                        // Load user data
                        loadUserData();
                    } else { // No matches in database
                        Log.e(TAG, "No matching document found in Firestore");
                        Toast.makeText(this, "No user data found. Please log in again.", Toast.LENGTH_SHORT).show();
                        mAuth.signOut();
                        Intent intent = new Intent(profilePage.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    }
                })
                .addOnFailureListener(e -> { // Error handling
                    Log.e(TAG, "Error fetching userId from Firestore", e);
                    Toast.makeText(this, "Error fetching user data. Please try again later.", Toast.LENGTH_SHORT).show();
                });
    }

    // Method to load user data
    private void loadUserData() {
        if (firebaseUser != null) {
            // Set username and email from SharedPreferences
            username.setText(sharedPreferences.getString("Name", "N/A"));
            email.setText(firebaseUser.getEmail());
            // Set actual password from SharedPreferences
            actualPassword = sharedPreferences.getString("Password", "N/A");
            password.setText(actualPassword);

            Log.d(TAG, "Loaded user data: " + firebaseUser.getDisplayName() + ", " + firebaseUser.getEmail() + ", userId: " + userId);

            // Fetch the latest user data from Firestore
            db.collection("Account").document(userId).get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    // Set username and email from Firestore
                    username.setText(documentSnapshot.getString("Name"));
                    email.setText(documentSnapshot.getString("Email"));
                    // Set actual password from Firestore
                    actualPassword = documentSnapshot.getString("Password");
                    password.setText(actualPassword);

                    // Save the latest data to SharedPreferences
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("Name", documentSnapshot.getString("Name"));
                    editor.putString("Email", documentSnapshot.getString("Email"));
                    editor.putString("Password", documentSnapshot.getString("Password"));
                    editor.apply();

                    Log.d(TAG, "User data refreshed from Firestore");

                    // Load profile picture if available
                    String profilePicUrl = documentSnapshot.getString("ProfilePicUrl"); // CHANGED
                    if (profilePicUrl != null && !profilePicUrl.isEmpty()) {
                        // Load profile picture using a library like Glide or Picasso
                        Glide.with(this)
                                .load(profilePicUrl)
                                .circleCrop() // CHANGED
                                .into(profilePicture);
                    }
                } else { // No user found
                    Log.e(TAG, "No such document in Firestore");
                }
            }).addOnFailureListener(e -> Log.e(TAG, "Failed to fetch user data from Firestore", e));
        } else {
            Log.e(TAG, "FirebaseUser is null in loadUserData");
        }
    }

    // Method to toggle password visibility in edit mode
    private void togglePasswordVisibility() {
        if (showPassword.isChecked()) {
            Log.d("SHOW PASSWORD", "togglePasswordVisibility: checked");
            editPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
        } else {
            editPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        }
        editPassword.setSelection(editPassword.getText().length());
    }

    // Method to toggle password visibility in non-edit mode
    private void togglePasswordVisibilityNonEditMode() {
        if (showPassword.isChecked()) {
            Log.d("SHOW PASSWORD", "togglePasswordVisibilityNonEditMode: checked");
            regPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
        } else {
            regPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        }
        regPassword.requestLayout();
    }

    // Method to allow editing user information
    private void AllowEditing() {
        // Regular fields not shown
        username.setVisibility(View.GONE);
        password.setVisibility(View.GONE);

        // Replaced by editing fields
        editUsername.setVisibility(View.VISIBLE);
        editPassword.setVisibility(View.VISIBLE);
        // Setting account data in editing fields
        editUsername.setText(username.getText());
        editPassword.setText(password.getText());

        // Display save button
        saveButton.setVisibility(View.VISIBLE);
        deleteAccountButton.setVisibility(View.VISIBLE); // Show delete button in edit mode
        uploadProfilePicButton.setVisibility(View.VISIBLE); // CHANGED
    }

    // Method to disable editing user information
    private void UnallowEditing() {
        // Regular fields shown
        username.setVisibility(View.VISIBLE);
        password.setVisibility(View.VISIBLE);

        // Editing fields and save button not shown
        editUsername.setVisibility(View.GONE);
        editPassword.setVisibility(View.GONE);
        saveButton.setVisibility(View.GONE);
        deleteAccountButton.setVisibility(View.GONE); // Hide delete button after editing
        uploadProfilePicButton.setVisibility(View.GONE); // CHANGED
        feedbackbutton.setVisibility(View.VISIBLE);
        logoutButton.setVisibility(View.VISIBLE);
    }

    // Method to logout user
    private void logout() {
        mAuth.signOut();
        sharedPreferences.edit().clear().apply();
        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();

        // Navigate to login/register page
        Intent intent = new Intent(profilePage.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    // Method to update user information in Firestore
    private void updateFirestore(String updatedName, String updatedEmail, String updatedPassword, String profilePicUrl) { // CHANGED
        // Post to database
        db.collection("Account").document(userId)
                .update("Name", updatedName, "Password", updatedPassword, "ProfilePicUrl", profilePicUrl) // CHANGED
                .addOnSuccessListener(aVoid -> {
                    // Upon posting
                    // Update SharedPreferences
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("Name", updatedName);
                    editor.putString("Password", updatedPassword);
                    editor.putString("ProfilePicUrl", profilePicUrl); // CHANGED
                    editor.apply();

                    Log.d(TAG, "Profile updated successfully in Firestore.");
                    Toast.makeText(profilePage.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();

                    // Reload user data to reflect changes
                    loadUserData();
                })
                .addOnFailureListener(e -> {
                    // Error handling
                    Log.e(TAG, "Failed to update profile in Firestore", e);
                    Toast.makeText(profilePage.this, "Failed to update profile in Firestore", Toast.LENGTH_SHORT).show();
                });
    }

    // Method to update user information
    private void updateUserInformation() {
        // Get updated name and password
        String updatedName = editUsername.getText().toString();
        String updatedPassword = editPassword.getText().toString();
        String profilePicUrl = sharedPreferences.getString("ProfilePicUrl", ""); // CHANGED

        Log.d(TAG, "Updating user information with updatedName: " + updatedName + ", updatedPassword: " + updatedPassword);

        if (firebaseUser != null) {
            // Check if user exists
            // Get email and password of user
            String currentEmail = firebaseUser.getEmail();
            String currentPassword = sharedPreferences.getString("Password", "N/A");

            if (currentEmail != null && !currentPassword.equals("N/A")) {
                // Check if user has both email and password
                Log.d(TAG, "Current email: " + currentEmail + ", Current password: " + currentPassword);

                AuthCredential credential = EmailAuthProvider.getCredential(currentEmail, currentPassword);

                firebaseUser.reauthenticate(credential).addOnCompleteListener(task -> {
                    // Reauthenticate user
                    if (task.isSuccessful()) {
                        Log.d(TAG, "User re-authenticated.");
                        Toast.makeText(this, "Re-Authentication success.", Toast.LENGTH_SHORT).show();

                        // Update password in Firebase Auth
                        firebaseUser.updatePassword(updatedPassword).addOnCompleteListener(passwordUpdateTask -> {
                            if (passwordUpdateTask.isSuccessful()) {
                                Log.d(TAG, "User password updated.");

                                // Update profile in Firestore
                                updateFirestore(updatedName, currentEmail, updatedPassword, profilePicUrl);
                            } else {
                                Log.e(TAG, "Error updating password in Firebase Auth", passwordUpdateTask.getException());
                                Toast.makeText(profilePage.this, "Failed to update password in Firebase Auth", Toast.LENGTH_SHORT).show();
                            }
                        });

                    } else {
                        Log.e(TAG, "User re-authentication failed", task.getException());
                        Toast.makeText(profilePage.this, "User re-authentication failed", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Log.e(TAG, "Current email or password is missing");
                Toast.makeText(profilePage.this, "Current email or password is missing", Toast.LENGTH_SHORT).show();
            }
        } else {
            Log.e(TAG, "FirebaseUser is null");
            Toast.makeText(profilePage.this, "Failed to update profile: FirebaseUser not found", Toast.LENGTH_SHORT).show();
        }
    }

    // Method to delete user account
    private void deleteAccount() {
        if (firebaseUser != null) {
            // Get user ID from SharedPreferences
            String userId = sharedPreferences.getString("userId", "N/A");

            // Delete user from Firestore
            db.collection("Account").document(userId)
                    .delete()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            // Delete user from Firebase Authentication
                            firebaseUser.delete()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                // Clear SharedPreferences and logout user
                                                sharedPreferences.edit().clear().apply();
                                                Toast.makeText(profilePage.this, "Account deleted successfully", Toast.LENGTH_SHORT).show();

                                                // Redirect to login page
                                                Intent intent = new Intent(profilePage.this, MainActivity.class);
                                                startActivity(intent);
                                                finish();
                                            } else {
                                                // Error handling for Firebase Authentication deletion
                                                Log.e(TAG, "Error deleting user from Firebase Authentication", task.getException());
                                                Toast.makeText(profilePage.this, "Error deleting account: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Error handling for Firestore deletion
                            Log.e(TAG, "Error deleting user from Firestore", e);
                            Toast.makeText(profilePage.this, "Error deleting account: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Log.e(TAG, "FirebaseUser is null");
            Toast.makeText(this, "User is not authenticated", Toast.LENGTH_SHORT).show();
        }
    }

    // Method to show image options // CHANGED
    private void showImageOptions() {
        final CharSequence[] options = {"Take Photo", "Choose from Gallery", "Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(profilePage.this);
        builder.setTitle("Upload Profile Picture");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (options[item].equals("Take Photo")) {
                    if (ContextCompat.checkSelfPermission(profilePage.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(profilePage.this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
                    } else {
                        Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        startActivityForResult(takePicture, CAMERA_REQUEST_CODE);
                    }
                } else if (options[item].equals("Choose from Gallery")) {
                    Intent pickPhoto = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(pickPhoto, PICK_IMAGE_REQUEST);
                } else if (options[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) { // CHANGED
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(takePicture, CAMERA_REQUEST_CODE);
            } else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) { // CHANGED
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == PICK_IMAGE_REQUEST) {
                Uri selectedImage = data.getData();
                uploadImageToFirebase(selectedImage);
            } else if (requestCode == CAMERA_REQUEST_CODE) {
                Bitmap photo = (Bitmap) data.getExtras().get("data");
                uploadImageToFirebase(getImageUri(this, photo));
            }
        }
    }

    // Method to get URI from bitmap // CHANGED
    private Uri getImageUri(Context context, Bitmap photo) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        photo.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), photo, "Title", null);
        return Uri.parse(path);
    }

    // Method to upload image to Firebase Storage // CHANGED
    private void uploadImageToFirebase(Uri imageUri) {
        if (imageUri != null) {
            StorageReference ref = storageReference.child("profile_pictures/" + userId + ".jpg");
            ref.putFile(imageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    String profilePicUrl = uri.toString();
                                    SharedPreferences.Editor editor = sharedPreferences.edit();
                                    editor.putString("ProfilePicUrl", profilePicUrl);
                                    editor.apply();

                                    // Update Firestore with the new profile picture URL
                                    updateFirestore(username.getText().toString(), firebaseUser.getEmail(), actualPassword, profilePicUrl);

                                    // Load profile picture using a library like Glide or Picasso
                                    Glide.with(profilePage.this)
                                            .load(profilePicUrl)
                                            .circleCrop() // CHANGED
                                            .into(profilePicture);

                                    profilePicture.setVisibility(View.VISIBLE); // CHANGED

                                    Toast.makeText(profilePage.this, "Profile picture uploaded successfully", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(profilePage.this, "Failed to upload profile picture", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }
}

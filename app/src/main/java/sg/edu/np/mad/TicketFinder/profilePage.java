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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
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
import java.util.concurrent.Executor;

public class profilePage extends AppCompatActivity {
    // Request codes for image picking and camera
    private static final int PICK_IMAGE_REQUEST = 1; // Request code for picking an image from the gallery
    private static final int CAMERA_REQUEST_CODE = 2; // Request code for taking a photo with the camera
    private static final int CAMERA_PERMISSION_CODE = 3; // Request code for camera permission

    // UI components
    private TextView username, password, email, regPassword; // TextView components for displaying user info
    private EditText editUsername, editPassword; // EditText components for editing user info
    private ImageView editingIcon, profilePicture; // ImageView components for editing icon and profile picture
    private CheckBox showPassword; // CheckBox component for toggling password visibility
    private Button saveButton, logoutButton, feedbackbutton, deleteAccountButton, uploadProfilePicButton; // Button components for various actions
    private ImageButton forumButton; // ImageButton for navigating to ForumPage

    private String actualPassword; // String to store the actual password
    private SharedPreferences sharedPreferences; // SharedPreferences for storing user data
    private FirebaseFirestore db; // Firestore database instance
    private FirebaseAuth mAuth; // Firebase Authentication instance
    private FirebaseUser firebaseUser; // Current authenticated user
    private FirebaseStorage storage; // Firebase Storage instance
    private StorageReference storageReference; // Reference to Firebase Storage
    private String userId; // User ID
    private static final String TAG = "ProfilePage"; // Tag for logging
    private boolean isEditMode = false; // Boolean to track if the profile is in edit mode

    // Biometric authentication components
    private BiometricPrompt biometricPrompt; // Biometric prompt for authentication
    private BiometricPrompt.PromptInfo promptInfo; // Information for biometric prompt
    private Executor executor; // Executor for handling biometric authentication callbacks

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_page); // Set the layout for the profile page

        // Set up footer
        Footer.setUpFooter(this); // Set up footer view

        // Initialize UI components
        username = findViewById(R.id.regUsername); // Initialize TextView for username
        password = findViewById(R.id.regPassword); // Initialize TextView for password
        email = findViewById(R.id.regEmail); // Initialize TextView for email
        editUsername = findViewById(R.id.editUsername); // Initialize EditText for editing username
        editPassword = findViewById(R.id.editPassword); // Initialize EditText for editing password
        regPassword = findViewById(R.id.regPassword); // Initialize TextView for registering password
        editingIcon = findViewById(R.id.editingIcon); // Initialize ImageView for editing icon
        profilePicture = findViewById(R.id.profilePicture); // Initialize ImageView for profile picture
        showPassword = findViewById(R.id.showPassword); // Initialize CheckBox for showing password
        saveButton = findViewById(R.id.saveButton); // Initialize Button for saving changes
        logoutButton = findViewById(R.id.logoutButton); // Initialize Button for logging out
        feedbackbutton = findViewById(R.id.Viewfeedbackbtn); // Initialize Button for viewing feedback
        deleteAccountButton = findViewById(R.id.deleteAccountButton); // Initialize Button for deleting account
        uploadProfilePicButton = findViewById(R.id.uploadProfilePicButton); // Initialize Button for uploading profile picture
        forumButton = findViewById(R.id.forumButton); // Initialize ImageButton for navigating to ForumPage

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE); // Get SharedPreferences instance

        // Initialize Firebase components
        db = FirebaseFirestore.getInstance(); // Initialize Firestore database
        mAuth = FirebaseAuth.getInstance(); // Initialize Firebase Authentication
        firebaseUser = mAuth.getCurrentUser(); // Get current authenticated user
        storage = FirebaseStorage.getInstance(); // Initialize Firebase Storage
        storageReference = storage.getReference(); // Get reference to Firebase Storage

        // Redirect to login if user is not authenticated
        redirectToLoginIfNoUser(); // Check if user is authenticated, if not redirect to login

        // Get userId from SharedPreferences
        userId = sharedPreferences.getString("userId", "N/A"); // Get userId from SharedPreferences
        if (userId.equals("N/A")) {
            // Attempt to fetch the userId from Firestore using the email
            fetchUserId(); // Fetch userId from Firestore
        } else {
            // Load user data
            loadUserData(); // Load user data from SharedPreferences
        }

        // OnClickListener for show password checkbox
        showPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isEditMode) {
                    togglePasswordVisibility(); // Toggle password visibility in edit mode
                } else {
                    togglePasswordVisibilityNonEditMode(); // Toggle password visibility in non-edit mode
                }
            }
        });

        // OnClickListener for editing icon
        editingIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                authenticateUser(); // Authenticate user before allowing editing
            }
        });

        // OnClickListener for save button
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actualPassword = editPassword.getText().toString(); // Get password from EditText
                UnallowEditing(); // Disable editing mode
                isEditMode = false; // Set edit mode to false
                updateUserInformation(); // Update user information
            }
        });

        // OnClickListener for logout button
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout(); // Perform logout
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
                new AlertDialog.Builder(profilePage.this, R.style.CustomAlertDialogTheme)
                        .setTitle("Delete Account")
                        .setMessage("Are you sure you want to delete your account? This action cannot be undone.")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                deleteAccount(); // User confirmed to delete the account
                            }
                        })
                        .setNegativeButton(android.R.string.no, null)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show(); // Show confirmation dialog
            }
        });

        // OnClickListener for upload profile picture button
        uploadProfilePicButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showImageOptions(); // Show options for uploading profile picture
            }
        });
        // OnClickListener for forum button
        forumButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to ForumPage
                Intent intent = new Intent(profilePage.this, ForumPage.class);
                startActivity(intent);
            }
        });
        // Initialize biometric prompt
        executor = ContextCompat.getMainExecutor(this); // Get executor for biometric authentication
        biometricPrompt = new BiometricPrompt(profilePage.this, executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                Toast.makeText(profilePage.this, "Authentication error: " + errString, Toast.LENGTH_SHORT).show(); // Show error message
            }

            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                Toast.makeText(profilePage.this, "Authentication succeeded!", Toast.LENGTH_SHORT).show(); // Show success message
                isEditMode = true; // Set edit mode to true
                AllowEditing(); // Allow editing of user information
                saveButton.setVisibility(View.VISIBLE); // Ensure save button is visible when editing
                deleteAccountButton.setVisibility(View.VISIBLE); // Show delete button in edit mode
                feedbackbutton.setVisibility(View.INVISIBLE); // Hide feedback button in edit mode
                logoutButton.setVisibility(View.INVISIBLE); // Hide logout button in edit mode
                uploadProfilePicButton.setVisibility(View.VISIBLE); // Show upload profile picture button in edit mode
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                Toast.makeText(profilePage.this, "Authentication failed", Toast.LENGTH_SHORT).show(); // Show failure message
            }
        });

        // Set up biometric prompt info
        BiometricPrompt.PromptInfo.Builder promptInfoBuilder = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Biometric login for TicketFinder")
                .setSubtitle("Log in using your biometric credential");

        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.R) {
            // For devices running Android 10 (API 29) or lower, use setNegativeButtonText()
            promptInfoBuilder.setNegativeButtonText("Use account password");
        } else {
            // For devices running Android 11 (API 30) or higher, do not set negative button text
            promptInfoBuilder.setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG | BiometricManager.Authenticators.DEVICE_CREDENTIAL);
        }

        promptInfo = promptInfoBuilder.build(); // Build the biometric prompt info
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clear SharedPreferences if "RememberMe" is false
        if (!sharedPreferences.getBoolean("RememberMe", false)) {
            sharedPreferences.edit().clear().apply(); // Clear SharedPreferences
        }
    }

    // Method to fetch userId from Firestore using email
    private void fetchUserId() {
        if (firebaseUser != null) {
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
                            redirectToLoginIfNoUser();
                        }
                    })
                    .addOnFailureListener(e -> { // Error handling
                        Log.e(TAG, "Error fetching userId from Firestore", e);
                        Toast.makeText(this, "Error fetching user data. Please try again later.", Toast.LENGTH_SHORT).show();
                    });
        } else {
            Log.e(TAG, "FirebaseUser is null in fetchUserId");
            redirectToLoginIfNoUser();
        }
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
                    editor.putString("ProfilePicUrl", documentSnapshot.getString("ProfilePicUrl")); // Save ProfilePicUrl
                    editor.apply();

                    Log.d(TAG, "User data refreshed from Firestore");

                    // Load profile picture if available
                    String profilePicUrl = documentSnapshot.getString("ProfilePicUrl");
                    if (profilePicUrl != null && !profilePicUrl.isEmpty()) {
                        Log.d(TAG, "Loading profile picture from URL: " + profilePicUrl);
                        // Load profile picture using a library like Glide or Picasso
                        Glide.with(this)
                                .load(profilePicUrl)
                                .circleCrop()
                                .into(profilePicture);
                    }
                } else { // No user found
                    Log.e(TAG, "No such document in Firestore");
                }
            }).addOnFailureListener(e -> Log.e(TAG, "Failed to fetch user data from Firestore", e));
        } else {
            Log.e(TAG, "FirebaseUser is null in loadUserData");
            redirectToLoginIfNoUser();
        }
    }

    // Method to toggle password visibility in edit mode
    private void togglePasswordVisibility() {
        if (showPassword.isChecked()) {
            Log.d("SHOW PASSWORD", "togglePasswordVisibility: checked");
            editPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD); // Show password
        } else {
            editPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD); // Hide password
        }
        editPassword.setSelection(editPassword.getText().length()); // Set cursor position
    }

    // Method to toggle password visibility in non-edit mode
    private void togglePasswordVisibilityNonEditMode() {
        if (showPassword.isChecked()) {
            Log.d("SHOW PASSWORD", "togglePasswordVisibilityNonEditMode: checked");
            regPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD); // Show password
        } else {
            regPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD); // Hide password
        }
        regPassword.requestLayout(); // Refresh layout
    }

    // Method to allow editing user information
    private void AllowEditing() {
        // Regular fields not shown
        username.setVisibility(View.GONE); // Hide username TextView
        password.setVisibility(View.GONE); // Hide password TextView
        editingIcon.setVisibility(View.GONE); // Hide editing icon

        // Replaced by editing fields
        editUsername.setVisibility(View.VISIBLE); // Show EditText for username
        editPassword.setVisibility(View.VISIBLE); // Show EditText for password
        // Setting account data in editing fields
        editUsername.setText(username.getText()); // Set username in EditText
        editPassword.setText(password.getText()); // Set password in EditText

        // Display save button
        saveButton.setVisibility(View.VISIBLE); // Show save button
        deleteAccountButton.setVisibility(View.VISIBLE); // Show delete button in edit mode
        uploadProfilePicButton.setVisibility(View.VISIBLE); // Show upload profile picture button
    }

    // Method to disable editing user information
    private void UnallowEditing() {
        // Regular fields shown
        username.setVisibility(View.VISIBLE); // Show username TextView
        password.setVisibility(View.VISIBLE); // Show password TextView
        editingIcon.setVisibility(View.VISIBLE); // Show editing icon

        // Editing fields and save button not shown
        editUsername.setVisibility(View.GONE); // Hide EditText for username
        editPassword.setVisibility(View.GONE); // Hide EditText for password
        saveButton.setVisibility(View.GONE); // Hide save button
        deleteAccountButton.setVisibility(View.GONE); // Hide delete button after editing
        uploadProfilePicButton.setVisibility(View.GONE); // Hide upload profile picture button
        feedbackbutton.setVisibility(View.VISIBLE); // Show feedback button
        logoutButton.setVisibility(View.VISIBLE); // Show logout button
    }

    // Method to logout user
    private void logout() {
        mAuth.signOut(); // Sign out from Firebase Authentication
        sharedPreferences.edit().clear().apply(); // Clear SharedPreferences
        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show(); // Show success message

        // Navigate to login/register page
        Intent intent = new Intent(profilePage.this, MainActivity.class); // Create intent for MainActivity
        startActivity(intent); // Start MainActivity
        finish(); // Finish current activity
    }

    // Method to update user information in Firestore
    private void updateFirestore(String updatedName, String updatedEmail, String updatedPassword, String profilePicUrl) {
        // Post to database
        db.collection("Account").document(userId)
                .update("Name", updatedName, "Password", updatedPassword, "ProfilePicUrl", profilePicUrl)
                .addOnSuccessListener(aVoid -> {
                    // Upon posting
                    // Update SharedPreferences
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("Name", updatedName); // Save updated name
                    editor.putString("Password", updatedPassword); // Save updated password
                    editor.putString("ProfilePicUrl", profilePicUrl); // Save profile picture URL
                    editor.apply();

                    Log.d(TAG, "Profile updated successfully in Firestore.");
                    Toast.makeText(profilePage.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();

                    // Reload user data to reflect changes
                    loadUserData(); // Load user data from Firestore
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
        String updatedName = editUsername.getText().toString(); // Get updated username
        String updatedPassword = editPassword.getText().toString(); // Get updated password
        String profilePicUrl = sharedPreferences.getString("ProfilePicUrl", ""); // Get profile picture URL

        Log.d(TAG, "Updating user information with updatedName: " + updatedName + ", updatedPassword: " + updatedPassword);

        if (firebaseUser != null) {
            // Check if user exists
            // Get email and password of user
            String currentEmail = firebaseUser.getEmail(); // Get current email
            String currentPassword = sharedPreferences.getString("Password", "N/A"); // Get current password

            if (currentEmail != null && !currentPassword.equals("N/A")) {
                // Check if user has both email and password
                Log.d(TAG, "Current email: " + currentEmail + ", Current password: " + currentPassword);

                AuthCredential credential = EmailAuthProvider.getCredential(currentEmail, currentPassword); // Create authentication credential

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
            redirectToLoginIfNoUser();
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
                                                redirectToLoginIfNoUser();
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
            redirectToLoginIfNoUser();
        }
    }

    // Method to show image options for uploading profile picture
    private void showImageOptions() {
        final CharSequence[] options = {"Take Photo", "Choose from Gallery", "Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(profilePage.this);
        builder.setTitle("Upload Profile Picture");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (options[item].equals("Take Photo")) {
                    if (ContextCompat.checkSelfPermission(profilePage.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(profilePage.this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE); // Request camera permission
                    } else {
                        Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE); // Create intent to take a photo
                        startActivityForResult(takePicture, CAMERA_REQUEST_CODE); // Start camera activity
                    }
                } else if (options[item].equals("Choose from Gallery")) {
                    Intent pickPhoto = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI); // Create intent to pick an image from the gallery
                    startActivityForResult(pickPhoto, PICK_IMAGE_REQUEST); // Start gallery activity
                } else if (options[item].equals("Cancel")) {
                    dialog.dismiss(); // Dismiss dialog
                }
            }
        });
        builder.show(); // Show dialog
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE); // Create intent to take a photo
                startActivityForResult(takePicture, CAMERA_REQUEST_CODE); // Start camera activity
            } else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show(); // Show permission denied message
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == PICK_IMAGE_REQUEST) {
                Uri selectedImage = data.getData(); // Get selected image URI
                uploadImageToFirebase(selectedImage); // Upload image to Firebase
            } else if (requestCode == CAMERA_REQUEST_CODE) {
                Bitmap photo = (Bitmap) data.getExtras().get("data"); // Get photo bitmap
                uploadImageToFirebase(getImageUri(this, photo)); // Upload image to Firebase
            }
        }
    }

    // Method to get URI from bitmap
    private Uri getImageUri(Context context, Bitmap photo) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream(); // Create ByteArrayOutputStream
        photo.compress(Bitmap.CompressFormat.JPEG, 100, bytes); // Compress bitmap to JPEG
        String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), photo, "Title", null); // Insert image into MediaStore
        return Uri.parse(path); // Return URI of the inserted image
    }

    // Method to upload image to Firebase Storage
    private void uploadImageToFirebase(Uri imageUri) {
        if (imageUri != null) {
            StorageReference ref = storageReference.child("profile_pictures/" + userId + ".jpg"); // Get reference to profile picture in Firebase Storage
            ref.putFile(imageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    String profilePicUrl = uri.toString(); // Get profile picture URL
                                    SharedPreferences.Editor editor = sharedPreferences.edit();
                                    editor.putString("ProfilePicUrl", profilePicUrl); // Save profile picture URL to SharedPreferences
                                    editor.apply();

                                    // Update Firestore with the new profile picture URL
                                    updateFirestore(username.getText().toString(), firebaseUser.getEmail(), actualPassword, profilePicUrl);

                                    // Load profile picture using a library like Glide or Picasso
                                    Glide.with(profilePage.this)
                                            .load(profilePicUrl)
                                            .circleCrop()
                                            .into(profilePicture);

                                    profilePicture.setVisibility(View.VISIBLE); // Show profile picture

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

    // Method to authenticate user with biometric or password
    private void authenticateUser() {
        BiometricManager biometricManager = BiometricManager.from(this); // Get BiometricManager instance
        switch (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG | BiometricManager.Authenticators.DEVICE_CREDENTIAL)) {
            case BiometricManager.BIOMETRIC_SUCCESS:
                // Show biometric prompt
                biometricPrompt.authenticate(promptInfo); // Authenticate using biometric prompt
                break;
            case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
            case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
            case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                // Show password dialog
                showPasswordDialog(); // Authenticate using password dialog
                break;
        }
    }

    // Method to show password dialog for authentication
    private void showPasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomAlertDialogTheme);
        builder.setTitle("Enter Account Password");

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_enter_password, null); // Inflate dialog view
        final EditText input = dialogView.findViewById(R.id.passwordInput); // Get password input EditText

        builder.setView(dialogView); // Set dialog view

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String password = input.getText().toString(); // Get password from EditText
                if (password.equals(actualPassword)) {
                    isEditMode = true; // Set edit mode to true
                    AllowEditing(); // Allow editing of user information
                    saveButton.setVisibility(View.VISIBLE); // Show save button
                    deleteAccountButton.setVisibility(View.VISIBLE); // Show delete button
                    feedbackbutton.setVisibility(View.INVISIBLE); // Hide feedback button
                    logoutButton.setVisibility(View.INVISIBLE); // Hide logout button
                    uploadProfilePicButton.setVisibility(View.VISIBLE); // Show upload profile picture button
                } else {
                    Toast.makeText(profilePage.this, "Incorrect password", Toast.LENGTH_SHORT).show(); // Show incorrect password message
                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel(); // Cancel dialog
            }
        });

        builder.show(); // Show dialog
    }

    // Method to redirect to MainActivity if no user is authenticated
    private void redirectToLoginIfNoUser() {
        if (firebaseUser == null) {
            Log.e(TAG, "No authenticated user, redirecting to login");
            // Clear SharedPreferences
            sharedPreferences.edit().clear().apply(); // Clear SharedPreferences
            Intent intent = new Intent(profilePage.this, MainActivity.class); // Create intent for MainActivity
            startActivity(intent); // Start MainActivity
            finish(); // Finish current activity
        }
    }
}

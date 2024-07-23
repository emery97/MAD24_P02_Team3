package sg.edu.np.mad.TicketFinder;

import android.app.Notification;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class CommentsBottomSheetFragment extends BottomSheetDialogFragment {
    private String documentId;
    private String name;
    private String userIdString;
    private String profilePicUrl;
    private FirebaseFirestore db;
    private ListenerRegistration commentsListener;
    private List<ForumComment> commentList;
    private ForumCommentAdapter commentAdapter;
    private SharedPreferences sharedPreferences;

    public CommentsBottomSheetFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_comments_bottom_sheet, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Initialize Firebase Firestore
        db = FirebaseFirestore.getInstance();
        if (db == null) {
            Log.e("CommentsBottomSheetFragment", "FirebaseFirestore instance is null");
            return; // Exit early if db is not initialized
        }

        if (getArguments() != null) {
            documentId = getArguments().getString("documentId");
        }
        sharedPreferences = getActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        name = sharedPreferences.getString("Name", null);
        userIdString = sharedPreferences.getString("UserId", null);
        if (userIdString != null) {
            fetchProfilePicUrl(userIdString);
        }

        // Set up BottomSheetBehavior
        View bottomSheet = (View) view.getParent();
        BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(bottomSheet);
        behavior.setPeekHeight(BottomSheetBehavior.PEEK_HEIGHT_AUTO);
        behavior.setState(BottomSheetBehavior.STATE_EXPANDED);


        // Set up RecyclerView
        RecyclerView commentsRecyclerView = view.findViewById(R.id.comments_recycler_view);
        commentsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        commentList = new ArrayList<>();
        commentAdapter = new ForumCommentAdapter(commentList);
        commentsRecyclerView.setAdapter(commentAdapter);

        // Set up EditText and SendButton
        EditText commentsEditText = view.findViewById(R.id.comments_edit_text);
        ImageButton sendButton = view.findViewById(R.id.send_button);

        sendButton.setOnClickListener(v -> {
            String comment = commentsEditText.getText().toString();
            if (!comment.isEmpty() && documentId != null) {
                if (profilePicUrl == null) {
                    profilePicUrl = ""; // Default to empty if not yet fetched
                }
                ForumComment forumComment = new ForumComment(comment, documentId, System.currentTimeMillis(),name,profilePicUrl);

                db.collection("forumComments")
                        .add(forumComment)
                        .addOnSuccessListener(documentReference -> {
                            // Comment added successfully
                            commentsEditText.setText(""); // Clear the EditText
                        })
                        .addOnFailureListener(e -> {
                            // Handle error
                        });
            }
        });
        // Start listening for comments
        startListeningForComments();
    }

    private void startListeningForComments() {
        if (documentId != null) {
            commentsListener = db.collection("forumComments")
                    .whereEqualTo("documentId", documentId)
                    .addSnapshotListener((snapshot, e) -> {
                        if (e != null) {
                            Log.w("CommentsBottomSheetFragment", "Listen failed.", e);
                            return;
                        }

                        if (snapshot != null) {
                            List<ForumComment> tempList = new ArrayList<>();
                            for (DocumentSnapshot doc : snapshot.getDocuments()) {
                                ForumComment comment = doc.toObject(ForumComment.class);
                                if (comment != null) {
                                    tempList.add(comment);
                                }
                            }
                            // Sort locally
                            tempList.sort((c1, c2) -> Long.compare(c2.getTimestamp(), c1.getTimestamp()));
                            commentList.clear();
                            commentList.addAll(tempList);
                            commentAdapter.notifyDataSetChanged();
                        }
                    });
        }
    }

    private void fetchProfilePicUrl(String userIdString) {
        if (userIdString == null || userIdString.isEmpty()) {
            Log.d("Firestore", "User ID is null or empty");
            profilePicUrl = ""; // Default to empty if user ID is null
            return;
        }

        int userId;
        try {
            userId = Integer.parseInt(userIdString);
        } catch (NumberFormatException e) {
            Log.d("Firestore", "Invalid user ID format: " + userIdString);
            profilePicUrl = ""; // Default to empty if conversion fails
            return;
        }

        Log.d("Firestore", "Fetching profile picture URL for User ID: " + userId);

        db.collection("Account")
                .whereEqualTo("userId", userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot != null && !querySnapshot.isEmpty()) {
                            DocumentSnapshot document = querySnapshot.getDocuments().get(0);
                            if (document.exists()) {
                                profilePicUrl = document.getString("ProfilePicUrl");
                                Log.d("Firestore", "ProfilePicUrl: " + profilePicUrl);
                            } else {
                                Log.d("Firestore", "Document does not exist for userId: " + userId);
                                profilePicUrl = ""; // Default to empty if not found
                            }
                        } else {
                            Log.d("Firestore", "No documents found for userId: " + userId);
                            profilePicUrl = ""; // Default to empty if not found
                        }
                    } else {
                        Log.d("Firestore", "Error fetching profile picture URL", task.getException());
                        profilePicUrl = ""; // Default to empty on failure
                    }
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Remove the Firestore listener to avoid memory leaks
        if (commentsListener != null) {
            commentsListener.remove();
        }
    }
}

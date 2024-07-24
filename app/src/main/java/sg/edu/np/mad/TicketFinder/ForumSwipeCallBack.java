package sg.edu.np.mad.TicketFinder;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ForumSwipeCallBack extends ItemTouchHelper.SimpleCallback {

    private final ForumAdapter mAdapter;
    private final Paint deleteBackgroundPaint;
    private final Paint editBackgroundPaint;
    private final Paint deleteTextPaint;
    private final Paint editTextPaint;
    private float deleteButtonStartX;
    private float deleteButtonEndX;
    private float editButtonStartX;
    private float editButtonEndX;
    private float deleteButtonTop;
    private float deleteButtonBottom;
    private float editButtonTop;
    private float editButtonBottom;
    private RecyclerView.ViewHolder currentViewHolder;
    private boolean isDeleteButtonVisible = false;
    private boolean isEditButtonVisible = false;
    private boolean isSwipeActive = false; // Flag to track swipe state
    private static final float TEXT_OFFSET = 30;
    private FirebaseFirestore db;
    private boolean isSwipeEnabled = true; // Flag to enable/disable swipe
    private static final int REQUEST_IMAGE_PICK = 1;
    private static final int MAX_IMAGE_COUNT = 3;

    public ForumSwipeCallBack(ForumAdapter adapter) {
        super(0, ItemTouchHelper.RIGHT); // Handle right swipe
        mAdapter = adapter;
        db = FirebaseFirestore.getInstance();

        // Setup paint for delete and edit buttons
        deleteBackgroundPaint = new Paint();
        deleteBackgroundPaint.setColor(Color.parseColor("#E0847C"));// Set delete button background color
        deleteBackgroundPaint.setAntiAlias(true);

        editBackgroundPaint = new Paint();
        editBackgroundPaint.setColor(Color.parseColor("#126da4")); // Set edit button background color
        editBackgroundPaint.setAntiAlias(true);

        deleteTextPaint = new Paint();
        deleteTextPaint.setColor(Color.WHITE); // Set delete text color to white
        deleteTextPaint.setTextSize(48);
        deleteTextPaint.setAntiAlias(true);

        editTextPaint = new Paint();
        editTextPaint.setColor(Color.WHITE); // Set edit text color to white
        editTextPaint.setTextSize(48);
        editTextPaint.setAntiAlias(true);
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
        return false; // We don't want to handle move actions
    }

    @Override
    public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
            View itemView = viewHolder.itemView;
            float width = itemView.getWidth();
            float swipeThreshold = width / 2.4f; // Threshold for showing buttons

            // Draw the buttons when swiping right
            if (dX > swipeThreshold) {
                drawButtons(c, itemView, dX);
                isDeleteButtonVisible = true;
                isEditButtonVisible = true;
                currentViewHolder = viewHolder;
                isSwipeActive = true;
            } else {
                isDeleteButtonVisible = false;
                isEditButtonVisible = false;
                currentViewHolder = null;
                isSwipeActive = false;
            }

            // Limit the swipe distance to show buttons
            if (dX > swipeThreshold) {
                dX = swipeThreshold;
            }
            itemView.setTranslationX(dX); // Move item view based on swipe distance
        } else {
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
        }
    }

    private void drawButtons(Canvas c, View itemView, float dX) {
        float buttonWidth = itemView.getWidth() / 5;
        float buttonHeight = itemView.getHeight() / 1.3f; // Reduced height for shorter buttons
        float itemHeight = itemView.getHeight();
        float left = itemView.getLeft();
        float top = itemView.getTop();
        float radius = buttonHeight / 2; // Radius for rounded corners based on the new height

        // Define the gap between the buttons
        float buttonGap = 10; // Adjust this value to increase or decrease the gap

        // Adjust button positions based on the new height and gap
        editButtonTop = top + (itemHeight - buttonHeight) / 2; // Center vertically
        editButtonBottom = editButtonTop + buttonHeight;
        editButtonStartX = left;
        editButtonEndX = editButtonStartX + buttonWidth;

        deleteButtonTop = editButtonTop;
        deleteButtonBottom = editButtonBottom;
        deleteButtonStartX = editButtonEndX + buttonGap; // Add gap between buttons
        deleteButtonEndX = deleteButtonStartX + buttonWidth;

        // Draw the edit button background
        RectF editBackground = new RectF(editButtonStartX, editButtonTop, editButtonEndX, editButtonBottom);
        c.drawRoundRect(editBackground, radius, radius, editBackgroundPaint); // Button background

        // Calculate text position for the edit button
        String editText = "EDIT";
        float editTextWidth = editTextPaint.measureText(editText);
        float editTextX = editButtonStartX + (buttonWidth - editTextWidth) / 2; // Center horizontally within button
        float editTextY = editButtonTop + (buttonHeight / 2) - ((editTextPaint.descent() + editTextPaint.ascent()) / 2); // Center vertically within button

        // Draw the text for edit button
        c.drawText(editText, editTextX, editTextY, editTextPaint);

        // Draw the delete button background
        RectF deleteBackground = new RectF(deleteButtonStartX, deleteButtonTop, deleteButtonEndX, deleteButtonBottom);
        c.drawRoundRect(deleteBackground, radius, radius, deleteBackgroundPaint); // Button background

        // Calculate text position for the delete button
        String deleteText = "DELETE";
        float deleteTextWidth = deleteTextPaint.measureText(deleteText);
        float deleteTextX = deleteButtonStartX + (buttonWidth - deleteTextWidth) / 2; // Center horizontally within button
        float deleteTextY = deleteButtonTop + (buttonHeight / 2) - ((deleteTextPaint.descent() + deleteTextPaint.ascent()) / 2); // Center vertically within button

        // Draw the text for delete button
        c.drawText(deleteText, deleteTextX, deleteTextY, deleteTextPaint);
    }


    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        // Do nothing here; handle actions on button clicks
    }

    @Override
    public void onSelectedChanged(@Nullable RecyclerView.ViewHolder viewHolder, int actionState) {
        super.onSelectedChanged(viewHolder, actionState);
        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
            // Get the view being swiped
            View itemView = viewHolder.itemView;
            itemView.setAlpha(0.5f); // Make it semi-transparent to indicate it's being swiped
        }
    }

    @Override
    public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        super.clearView(recyclerView, viewHolder);
        // Reset item view alpha and translation
        viewHolder.itemView.setAlpha(1.0f);
        viewHolder.itemView.setTranslationX(0); // Reset item translation
        isDeleteButtonVisible = false;
        isEditButtonVisible = false;
        isSwipeActive = false;
        // Clear the touch listener
        recyclerView.setOnTouchListener(null);
    }

    @Override
    public void onChildDrawOver(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        super.onChildDrawOver(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

        if (isSwipeActive && currentViewHolder == viewHolder) {
            recyclerView.setOnTouchListener((v, event) -> {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    if (isDeleteButtonVisible && event.getX() >= deleteButtonStartX && event.getX() <= deleteButtonEndX &&
                            event.getY() >= deleteButtonTop && event.getY() <= deleteButtonBottom) {
                        showDeleteConfirmationDialog(v.getContext(), viewHolder.getAdapterPosition());
                        return true;
                    } else if (isEditButtonVisible && event.getX() >= editButtonStartX && event.getX() <= editButtonEndX &&
                            event.getY() >= editButtonTop && event.getY() <= editButtonBottom) {
                        handleEditAction(v.getContext(), viewHolder.getAdapterPosition());
                        return true;
                    }
                }
                return false; // Allow nested RecyclerView to handle the touch event
            });
        } else {
            recyclerView.setOnTouchListener(null);
        }
    }

    private void showDeleteConfirmationDialog(Context context, int position) {
        new AlertDialog.Builder(context)
                .setTitle("Delete Confirmation")
                .setMessage("Are you sure you want to delete this item?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    // Handle deletion logic here
                    mAdapter.onDeleteClicked(position);
                })
                .setNegativeButton("No", (dialog, which) -> {
                    // Reset the item view if deletion is cancelled
                    mAdapter.notifyItemChanged(position);
                })
                .show();
    }

    private void handleEditAction(Context context, int position) {
        // Inflate the dialog view
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_edit_forum, null);

        // Get the views from the dialog
        EditText messageEditText = dialogView.findViewById(R.id.edit_message);
        RecyclerView imagesRecyclerView = dialogView.findViewById(R.id.image_recycler_view);
        Button saveButton = dialogView.findViewById(R.id.save_button);

        // Set up the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Edit Forum");
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();
        dialog.show();

        // Load current forum data into the dialog
        Forum forumToEdit = mAdapter.getForumList().get(position);
        messageEditText.setText(forumToEdit.getMessage());

        ForumImageAdapter imageAdapter = new ForumImageAdapter(forumToEdit.getImageUrls());
        imagesRecyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
        imagesRecyclerView.setAdapter(imageAdapter);

        saveButton.setOnClickListener(v -> {
            // Logic to save changes
            String newMessage = messageEditText.getText().toString();
            List<String> updatedImageUrls = imageAdapter.getImageUrls();
            updateForumInDatabase(position, newMessage, updatedImageUrls);
            dialog.dismiss();
        });
    }

    private void updateForumInDatabase(int position, String newMessage, List<String> updatedImageUrls) {
        Forum forumToUpdate = mAdapter.getForumList().get(position);
        String documentId = forumToUpdate.getDocumentId();

        Map<String, Object> updates = new HashMap<>();
        updates.put("message", newMessage);
        updates.put("imageUrls", updatedImageUrls);

        db.collection("Forum").document(documentId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Log.d("ForumAdapter", "DocumentSnapshot successfully updated!");
                    forumToUpdate.setMessage(newMessage);
                    forumToUpdate.setImageUrls(updatedImageUrls);
                    mAdapter.notifyItemChanged(position);
                })
                .addOnFailureListener(e -> Log.w("ForumAdapter", "Error updating document", e));
    }
}
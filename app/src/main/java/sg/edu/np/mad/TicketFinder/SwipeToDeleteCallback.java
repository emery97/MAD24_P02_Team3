package sg.edu.np.mad.TicketFinder;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

public class SwipeToDeleteCallback extends ItemTouchHelper.SimpleCallback {
    private final BookingDetailsAdapter mAdapter;
    private Paint mPaint;
    private boolean isDeleteButtonVisible = false;
    private float deleteButtonStartX;
    private float deleteButtonEndX;
    private RecyclerView.ViewHolder currentViewHolder;
    private FirebaseFirestore db;

    public SwipeToDeleteCallback(BookingDetailsAdapter adapter) {
        super(0, ItemTouchHelper.LEFT);
        mAdapter = adapter;
        mPaint = new Paint();
        db = FirebaseFirestore.getInstance();
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
            float swipeThreshold = width / 2; // Threshold for showing delete button

            // Draw the delete button when swiping left
            if (dX < -swipeThreshold) {
                drawDeleteButton(c, itemView, dX);
                isDeleteButtonVisible = true;
                currentViewHolder = viewHolder; // Keep track of the current view holder
            } else {
                isDeleteButtonVisible = false;
                currentViewHolder = null;
            }

            // Adjust item position
            if (dX < -swipeThreshold) {
                dX = -swipeThreshold; // Limit the swipe distance to the left
            }
            itemView.setTranslationX(dX); // Move item view based on swipe distance
        } else {
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
        }
    }

    private void drawDeleteButton(Canvas c, View itemView, float dX) {
        float buttonWidth = itemView.getWidth() / 2;
        float left = itemView.getRight() - buttonWidth;
        float right = itemView.getRight();
        float top = itemView.getTop();
        float bottom = itemView.getBottom();

        deleteButtonStartX = left;
        deleteButtonEndX = right;

        // Draw the background of the delete button
        mPaint.setColor(Color.RED);
        RectF background = new RectF(left, top, right, bottom);
        c.drawRect(background, mPaint);

        // Draw the text of the delete button
        mPaint.setColor(Color.WHITE);
        mPaint.setTextSize(48);
        mPaint.setTextAlign(Paint.Align.CENTER);
        float textX = left + (right - left) / 2;
        float textY = top + (bottom - top) / 2 - (mPaint.descent() + mPaint.ascent()) / 2;
        c.drawText("DELETE", textX, textY, mPaint);
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        // Do nothing here; handle deletion on user action
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
    }


    @Override
    public void onChildDrawOver(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        super.onChildDrawOver(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

        if (isDeleteButtonVisible && currentViewHolder == viewHolder) {
            recyclerView.setOnTouchListener((v, event) -> {
                if (event.getAction() == MotionEvent.ACTION_DOWN && isDeleteButtonVisible) {
                    if (event.getX() >= deleteButtonStartX && event.getX() <= deleteButtonEndX) {
                        // Show confirmation dialog
                        showDeleteConfirmationDialog(v.getContext(), viewHolder.getAdapterPosition());
                    }
                }
                return false;
            });
        } else {
            // Remove the touch listener if the delete button is not visible or swipe is reversed
            recyclerView.setOnTouchListener(null);
        }
    }

    private void showDeleteConfirmationDialog(Context context, int position) {
        Log.d("SwipeToDelete", "Confirming deletion for position: " + position);

        new AlertDialog.Builder(context)
                .setTitle("Delete Confirmation")
                .setMessage("Are you sure you want to delete this item?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    // Remove item from Firestore
                    BookingDetails bookingDetails = mAdapter.getBookingDetailsAtPosition(position);
                    String documentId = bookingDetails.getDocumentId();

                    db.collection("BookingDetails").document(documentId)
                            .delete()
                            .addOnSuccessListener(aVoid -> {
                                Log.d("SwipeToDelete", "DocumentSnapshot successfully deleted!");
                                // Remove item from adapter
                                mAdapter.removeItem(position);
                            })
                            .addOnFailureListener(e -> {
                                Log.w("SwipeToDelete", "Error deleting document", e);
                                // Notify failure
                                mAdapter.notifyItemChanged(position);
                            });
                })
                .setNegativeButton("No", (dialog, which) -> {
                    // Reset the item view if deletion is cancelled
                    mAdapter.notifyItemChanged(position);
                })
                .show();
    }
}

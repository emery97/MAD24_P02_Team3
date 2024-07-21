package sg.edu.np.mad.TicketFinder;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.text.BreakIterator;
import java.util.List;

public class UnfriendAdapter extends RecyclerView.Adapter<UnfriendAdapter.ViewHolder> {
    private final Context context;
    private final LayoutInflater mInflater;
    private FirebaseFirestore db;
    private List<User> friendList;
    private static String TAG = "unfriendAdapter";

    //Constructor to initialize data
    public UnfriendAdapter(Context context, List<User> friendList){
        this.context = context;
        this.mInflater = LayoutInflater.from(context);
        this.friendList = friendList;
        db = FirebaseFirestore.getInstance();
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position){
        User friend = friendList.get(position);
        holder.name.setText(friend.getName());
    }
    @Override
    public int getItemCount(){
        return friendList.size();
    }
    // Inflate the row layout when needed
    @NonNull
    @Override
    public UnfriendAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
        View view = mInflater.inflate(R.layout.unfriend_item,parent,false);
        return new ViewHolder(view);
    }

    public class ViewHolder  extends RecyclerView.ViewHolder{
        public BreakIterator textViewName;
        ImageView profilePicture;
        TextView name;
        Button unfriendButton;
        public ViewHolder(View itemView) {
            super(itemView);
            profilePicture = itemView.findViewById(R.id.profilePicture);
            name = itemView.findViewById(R.id.friendName);
            unfriendButton = itemView.findViewById(R.id.unfriendButton);
        }
    }
}

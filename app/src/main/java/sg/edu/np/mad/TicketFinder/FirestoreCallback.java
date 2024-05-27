package sg.edu.np.mad.TicketFinder;

import java.util.ArrayList;

public interface FirestoreCallback<T> {
    void onCallback(ArrayList<T> list);
}


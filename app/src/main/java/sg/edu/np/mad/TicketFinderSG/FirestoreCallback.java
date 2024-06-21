package sg.edu.np.mad.TicketFinderSG;

import java.util.ArrayList;

public interface FirestoreCallback<T> {
    void onCallback(ArrayList<T> list);
}

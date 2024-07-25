package sg.edu.np.mad.TicketFinder;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import java.util.ArrayList;
import java.util.HashMap;

public class SeatSelectionViewModel extends ViewModel {
    private final MutableLiveData<HashMap<String, ArrayList<String>>> seatMap = new MutableLiveData<>(new HashMap<>());

    public MutableLiveData<HashMap<String, ArrayList<String>>> getSeatMap() {
        return seatMap;
    }

    public void updateSeatMap(String category, String seatNumber) {
        HashMap<String, ArrayList<String>> currentMap = seatMap.getValue();
        if (currentMap != null) {
            if (!currentMap.containsKey(category)) {
                currentMap.put(category, new ArrayList<>());
            }
            currentMap.get(category).add(seatNumber);
            seatMap.setValue(currentMap);
        }
    }
}


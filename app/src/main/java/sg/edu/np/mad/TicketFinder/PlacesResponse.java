package sg.edu.np.mad.TicketFinder;

import java.util.List;

public class PlacesResponse {
    public Result result;

    public static class Result {
        public List<Photo> photos;
        public OpeningHours opening_hours;
    }

    public static class Photo {
        public String photo_reference;
    }

    public static class OpeningHours {
        public List<String> weekday_text;
    }
}
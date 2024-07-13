package sg.edu.np.mad.TicketFinder;

import java.util.List;

public class PlacesResponse {
    public Result result;

    public static class Result {
        public List<Photo> photos;
        public OpeningHours opening_hours;
        public String international_phone_number;
        public String website;
        public boolean wheelchair_accessible_entrance;
        public double rating;
        public int user_ratings_total;
        public List<Review> reviews;

    }

    public static class Photo {
        public String photo_reference;
    }

    public static class OpeningHours {
        public List<String> weekday_text;
    }
    public static class Review {
        public String author_name;
        public int rating;
        public String relative_time_description;
        public String text;

    }
}
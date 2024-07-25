package sg.edu.np.mad.TicketFinder;

import java.util.List;

public class NearbyPlacesResponse {
    public List<Result> results;

    public static class Result {
        public Geometry geometry;
        public String name;

        public static class Geometry {
            public Location location;

            public static class Location {
                public double lat;
                public double lng;
            }
        }
    }
}
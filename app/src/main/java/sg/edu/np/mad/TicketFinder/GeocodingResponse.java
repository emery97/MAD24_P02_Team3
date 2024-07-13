package sg.edu.np.mad.TicketFinder;

import java.util.List;

public class GeocodingResponse {
    public List<Result> results;
    public String status;

    public class Result {
        public Geometry geometry;
        public String formatted_address;
        public String place_id;

        public class Geometry {
            public Location location;

            public class Location {
                public double lat;
                public double lng;
            }
        }
    }
}
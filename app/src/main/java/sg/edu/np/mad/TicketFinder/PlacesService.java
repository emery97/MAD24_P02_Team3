package sg.edu.np.mad.TicketFinder;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface PlacesService {
    @GET("place/details/json")
    Call<PlacesResponse> getPlaceDetails(@Query("place_id") String placeId, @Query("key") String apiKey);

    @GET("place/nearbysearch/json")
    Call<NearbyPlacesResponse> getNearbyPlaces(
            @Query("type") String type,
            @Query("location") String location,
            @Query("radius") int radius,
            @Query("key") String apiKey);
}
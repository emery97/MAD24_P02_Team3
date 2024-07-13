package sg.edu.np.mad.TicketFinder;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface GeocodingService {
    @GET("geocode/json")
    Call<GeocodingResponse> getGeocodingData(@Query("address") String address, @Query("key") String apiKey);

    @GET("geocode/json")
    Call<GeocodingResponse> getReverseGeocodingData(@Query("latlng") String latlng, @Query("key") String apiKey);
}
package sg.edu.np.mad.TicketFinder;

import org.json.JSONArray;
import org.json.JSONObject;
import android.util.Log;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class Translator {
    private static final String API_KEY = "AIzaSyAUxAnDen1vwxL_BanWBwaAewxMzGTrp_g";
    private static final String DETECT_URL = "https://translation.googleapis.com/language/translate/v2/detect";
    private static final String TRANSLATE_URL = "https://translation.googleapis.com/language/translate/v2";

    public String detectLanguage(String text) throws Exception {
        JSONObject jsonObject = new JSONObject();
        JSONArray qArray = new JSONArray();
        qArray.put(text);
        jsonObject.put("q", qArray);

        String response = sendPostRequest(DETECT_URL, jsonObject.toString());

        // Log the raw JSON response
        Log.d("Translator", "Raw JSON response for language detection: " + response);

        // Correct JSON parsing
        JSONObject responseObject = new JSONObject(response);
        JSONArray detectionsArray = responseObject.getJSONObject("data").getJSONArray("detections");
        JSONObject detection = detectionsArray.getJSONArray(0).getJSONObject(0);
        return detection.getString("language");
    }

    public String translate(String sourceLang, String targetLang, String text) throws Exception {
        JSONObject jsonObject = new JSONObject();
        JSONArray qArray = new JSONArray();
        qArray.put(text);
        jsonObject.put("q", qArray);
        jsonObject.put("source", sourceLang);
        jsonObject.put("target", targetLang);
        jsonObject.put("format", "text");

        String response = sendPostRequest(TRANSLATE_URL, jsonObject.toString());

        // Log the raw JSON response
        Log.d("Translator", "Raw JSON response for translation: " + response);

        // Correct JSON parsing
        JSONObject responseObject = new JSONObject(response);
        JSONArray translationsArray = responseObject.getJSONObject("data").getJSONArray("translations");
        return translationsArray.getJSONObject(0).getString("translatedText");
    }

    private String sendPostRequest(String requestUrl, String payload) throws Exception {
        URL url = new URL(requestUrl + "?key=" + API_KEY);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setDoOutput(true);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");

        OutputStream os = conn.getOutputStream();
        os.write(payload.getBytes());
        os.flush();
        os.close();

        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            response.append(line);
        }
        br.close();
        conn.disconnect();
        return response.toString();
    }
}

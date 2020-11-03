package es.fernandopal.yato.core.audio.sources.spotify;

import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.model_objects.credentials.ClientCredentials;
import com.wrapper.spotify.requests.authorization.client_credentials.ClientCredentialsRequest;
import org.apache.hc.core5.http.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Calendar;

public class SpotifyApiManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(SpotifyApiManager.class);

    private static SpotifyApi spotifyApi = null;
    private static String clientSecret;
    private static String clientId;
    private static long expirationDate = 0;

    public static void initialize(String clientId, String clientSecret) {
        SpotifyApiManager.clientId = clientId;
        SpotifyApiManager.clientSecret = clientSecret;
        getAccess();
    }

    public static void getAccess() {
        spotifyApi = new SpotifyApi.Builder().setClientId(clientId).setClientSecret(clientSecret).build();

        ClientCredentialsRequest clientCredentialsRequest = spotifyApi.clientCredentials().build();

        try {
            ClientCredentials clientCredentials = clientCredentialsRequest.execute();

            // Set access token for further "spotifyApi" object usage
            spotifyApi.setAccessToken(clientCredentials.getAccessToken());

            expirationDate = clientCredentials.getExpiresIn() * 1000 + System.currentTimeMillis();
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(expirationDate);
            LOGGER.info("New spotify expiration date: " + calendar.getTime().toString());
        } catch (IOException | SpotifyWebApiException | ParseException ignored) { }
    }

    private static void checkAccess() {
        if (expirationDate < System.currentTimeMillis() - 1000L) { getAccess(); }
    }

    public static SpotifyApi getSpotifyApi() {
        checkAccess();
        return spotifyApi;
    }
}
package es.fernandopal.yato.core.audio;

import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;

import es.fernandopal.yato.core.BotManager;
import es.fernandopal.yato.files.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

public class YouTubeSearchHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(BotManager.class);
    private static final HashMap<String, SearchListResponse> searchCache = new HashMap<>();
    private static final Config config = new Config();

    /**
     * Searches youtube using e.getParameters() and returns the first result.
     * @return youtube video url.
     */
    public static List<SearchResult> search(String searchString, long maxResults) {
        LOGGER.info("Searching '" + searchString + "' ...");
        try {
            SearchListResponse searchResponse = searchCache.getOrDefault(searchString, null);

            if(searchResponse == null) {
                YouTube youtube = new YouTube.Builder(new NetHttpTransport(), new JacksonFactory(), request -> {}).setApplicationName("yato").build();

                YouTube.Search.List search = youtube.search().list("snippet")
                        .setKey(config.getString("google-api-key"))
                        .setQ(searchString)
                        .setType("video")
                        .setFields("items(id/videoId,snippet/title)")
                        .setMaxResults(maxResults);

                searchResponse = search.execute();
                searchCache.put(searchString, searchResponse);
            }

            return searchResponse.getItems();

        } catch (IOException ex) {
        	ex.printStackTrace();
            return null;
        }
    }

    public static HashMap<String, SearchListResponse> getSearchCache() {
        return searchCache;
    }

    public static void clearSearchCache() {
        searchCache.clear();
    }
}
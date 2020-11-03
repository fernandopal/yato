package es.fernandopal.ydba.database;

import ch.qos.logback.core.status.Status;
import com.google.gson.Gson;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.client.*;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import es.fernandopal.yato.files.DBConfig;
import es.fernandopal.ydba.entities.YPlaylist;
import es.fernandopal.ydba.entities.YTrack;
import es.fernandopal.ydba.entities.YUser;
import es.fernandopal.yato.files.Config;
import es.fernandopal.ydba.StaticHolder;
import es.fernandopal.ydba.entities.TrackInfo;
import org.bson.BsonDocument;
import org.bson.Document;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.mongodb.client.model.Filters.*;

public class DatabaseManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseManager.class);
    private final Config config = new Config();
    private MongoDatabase database = null;

    public DatabaseManager() {
        final DBConfig dbConfig = new DBConfig();

        final String username = dbConfig.getString("username");
        final String password = dbConfig.getString("password");
        final String dbUrl = dbConfig.getString("database-url");
        final String dbName = dbConfig.getString("database-name");
        final String urlArgs = dbConfig.getString("url-args");

        if(username == null || password == null || dbUrl == null || dbName == null || urlArgs == null) {
            LOGGER.error("One of the values of your database config returns null, please check them.");
            System.exit(Status.ERROR);
        } else {
            MongoClient mongoClient = MongoClients.create("mongodb+srv://"+username+":"+password+"@"+dbUrl+"/"+dbName+"?"+urlArgs);
            this.database = mongoClient.getDatabase(dbName);
        }
    }

    public String getPrefix(long guildId) {
        String prefix = config.getString("bot-prefix");

        if(StaticHolder.PREFIXES.containsKey(guildId)) {
            prefix = StaticHolder.PREFIXES.get(guildId);
        } else {
//            LOGGER.info("Mapping guild: " + guildId);
            final List<Document> guilds = get("guilds", new BasicDBObject("_id", guildId));
            if(guilds.isEmpty()) {
                StaticHolder.PREFIXES.put(guildId, prefix);
                setPrefix(guildId, prefix);
            } else {
                prefix = guilds.get(0).getString("bot_prefix");
                StaticHolder.PREFIXES.put(guildId, prefix);
            }
        }
        return prefix;
    }

    public Long getMusicChannel(long guildId) {
        Long musicChannel = null;

        final List<Document> guilds = get("guilds", new BasicDBObject("_id", guildId));
        if(!guilds.isEmpty()) { musicChannel = guilds.get(0).getLong("music_channel"); }

        return musicChannel;
    }

    public void setNowPlayingInfo(long guildId, AudioTrack track) {
        update("guilds", new BasicDBObject("_id", guildId), new BasicDBObject("player_now_playing", new Gson().toJson(track)));
    }

    public void updateQueue(long guildId, List<AudioTrack> queue) {
        update("guilds", new BasicDBObject("_id", guildId), new BasicDBObject("player_queue", new Gson().toJson(queue)));
    }

    public long getBotID() {
        long botID = Long.parseLong(config.getString("bot-id"));

        final List<Document> bot = get("bot", BasicDBObject.parse("{ 'client_id': { $ne: '0' } }"));
        if(!bot.isEmpty()) { botID = bot.get(0).getLong("client_id"); }
        return botID;
    }

    public TrackInfo getRandomSong() {
        TrackInfo trackInfo = null;

        final List<Document> songs = get("songs", BasicDBObject.parse("{ 'uri': { $ne: 'NULL' } }"));
        Collections.shuffle(songs);

        if(!songs.isEmpty()) {
            final Document dbTrack = songs.get(0);

            Integer totalPlays = dbTrack.getInteger("totalPlays");
            if (totalPlays == null) { totalPlays = 0; }
            Integer todayPlays = dbTrack.getInteger("todayPlays");
            if (todayPlays == null) { todayPlays = 0; }
            String uri = dbTrack.getString("uri");
            if (uri == null) { uri = ""; }

            trackInfo = new TrackInfo( todayPlays, totalPlays, uri);
        }

        return trackInfo;
    }

    public int getDatabaseSize() {
        return (int) this.database.getCollection("songs").countDocuments();
    }

    public TrackInfo getSong(String uri) {
        TrackInfo trackInfo;

        final Document song = this.database.getCollection("songs").find(eq("uri", uri)).first();

        if(song != null) {
            trackInfo = new TrackInfo(
                    song.getInteger("totalPlays"),
                    song.getInteger("todayPlays"),
                    song.getString("uri")
            );
        } else {
            trackInfo = new TrackInfo(0, 0, uri);
            insertSong(trackInfo);
        }

        return trackInfo;
    }

    public void addPlays(TrackInfo trackInfo) {
        if(trackInfo != null) {
            insertSong(trackInfo);
            final BasicDBObject query = new BasicDBObject("uri", trackInfo.getUri());
            update("songs", query, new BasicDBObject("todayPlays", trackInfo.getTodayPlays() + 1));
            update("songs", query, new BasicDBObject("totalPlays", trackInfo.getTotalPlays() + 1));
        }
    }

    public void insertSong(TrackInfo trackInfo) {
        final String json = new Gson().toJson(trackInfo, TrackInfo.class);
        final MongoCollection<Document> songs = this.database.getCollection("songs");
        final BasicDBObject search = BasicDBObject.parse("{'uri': '" + trackInfo.getUri() + "'}");
        final Document doc = songs.find(search).first();

//        LOGGER.warn("Check for: " + search.toJson());

        if (doc == null) {
            songs.insertOne(dboToDocument(BasicDBObject.parse(json)));
//            LOGGER.warn("-> insertSong: " + json);
        }
    }

    public void insertGuild(@NotNull BasicDBObject guild) { //TODO: insertGuild()
        final MongoCollection<Document> songs = this.database.getCollection("guilds");
        final BasicDBObject search = BasicDBObject.parse("{'_id': " + guild.getLong("_id") + "}");
        final Document doc = songs.find(search).first();

        if (doc == null) {
            songs.insertOne(dboToDocument(guild));
        }
    }

    public boolean createPlaylist(@NotNull YPlaylist yPlaylist) {
        final YPlaylist check = getPlaylist(yPlaylist.getPlaylistName(), yPlaylist.getPlaylistAuthor());
        if(check == null) {
            BasicDBObject doc = new BasicDBObject("_id", yPlaylist.getPlaylistId())
                    .append("name", yPlaylist.getPlaylistName())
                    .append("tracks", yPlaylist.getPlaylistTracks())
                    .append("author", yPlaylist.getPlaylistAuthor())
                    .append("private", yPlaylist.isPrivate());
            this.database.getCollection("yPlaylists").insertOne(dboToDocument(doc));
            return true;
        }
        return false;
    }

    public boolean insertUser(@NotNull YUser yUser) {
        final YUser check = getUser(yUser.getUserId());
        if(check == null) {
            BasicDBObject doc = new BasicDBObject("_id", yUser.getUserId()).append("selectedPlaylist", yUser.getSelectedPlaylist());
            this.database.getCollection("yUsers").insertOne(dboToDocument(doc));
            return true;
        }
        return false;
    }

    public void registerDailyVote(long userId, int points) {
        BasicDBObject doc = new BasicDBObject("uid", userId).append("hasVoted", true).append("pointsObtained", points);
        this.database.getCollection("daily_votes").insertOne(dboToDocument(doc));
    }

    public boolean hasVoted(long userId) {
        boolean result = false;
        final Document yUser = this.database.getCollection("daily_votes").find(
                eq("uid", userId)
        ).first();

        if (yUser != null) { result = true; }

        return result;
    }

    public boolean selectPlaylist(long userId, String yPlaylist) {
        final YUser check = getUser(userId);
        if(check != null) {
            final BasicDBObject query = new BasicDBObject("_id", userId);
            update("yUsers", query, new BasicDBObject("selectedPlaylist", yPlaylist));
            return true;
        }
        return false;
    }

    public String getSelectedPlaylist(long id) {
        String result = null;
        final Document yUser = this.database.getCollection("yUsers").find(
                eq("_id", id)
        ).first();

        if (yUser != null) {
            result = yUser.getString("selectedPlaylist");
        }

        return result;
    }

    public boolean setTotalPoints(long userId, int points) {

        final YUser check = getUser(userId);
        if(check != null) {
            final BasicDBObject query = new BasicDBObject("_id", userId);
            update("yUsers", query, new BasicDBObject("totalPoints", points));
            return true;
        }
        return false;
    }

    public int getTotalPoints(long id) {
        int result = 0;
        final Document yUser = this.database.getCollection("yUsers").find(
                eq("_id", id)
        ).first();

        if (yUser != null) {
            result = yUser.getInteger("totalPoints");
        }

        return result;
    }

    public void resetDailyVotes() {
        final List<Document> songs = get("daily_votes", BasicDBObject.parse("{ 'uid': { $ne: '0' } }"));
        songs.forEach(s -> this.database.getCollection("daily_votes").deleteOne(s));
    }

    public List<YPlaylist> getPlaylists(long id) {
        List<YPlaylist> result = new ArrayList<>();
        final FindIterable<Document> yPlaylists = this.database.getCollection("yPlaylists").find(
                eq("author", id)
        );

        for (Document doc : yPlaylists) {
            result.add(getPlaylist(doc));
        }

        return result;
    }

    public void insertYTrack(@NotNull YTrack yTrack) { //TODO: insertYTrack()
        final YTrack check = getYTrack(yTrack.getTrackUri());
        if(check == null) {
            final BasicDBObject doc = new BasicDBObject("_id", yTrack.getTrackId())
                    .append("uri", yTrack.getTrackUri())
                    .append("b64", yTrack.getTrackB64())
                    .append("track_name", yTrack.getTrackName())
                    .append("total_plays", yTrack.getTotalPlays())
                    .append("today_plays", yTrack.getTodayPlays());
            this.database.getCollection("yTracks").insertOne(dboToDocument(doc));
        }
    }

    public YTrack getYTrack(String trackUri) { //TODO: getYTrack()
        YTrack result = null;
        final Document yTrack = this.database.getCollection("yTracks").find(eq("uri", trackUri)).first();

        if (yTrack != null) {
            result = new YTrack(
                    yTrack.getString("_id"),
                    yTrack.getString("uri"),
                    yTrack.getString("b64"),
                    yTrack.getString("track_name"),
                    yTrack.getInteger("total_plays"),
                    yTrack.getInteger("today_plays")
            );
        }

        return result;
    }

    public YTrack getYTrackById(String trackId) { //TODO: getYTrack()
        YTrack result = null;
        final Document yTrack = this.database.getCollection("yTracks").find(eq("_id", trackId)).first();

        if (yTrack != null) {
            result = new YTrack(
                    yTrack.getString("_id"),
                    yTrack.getString("uri"),
                    yTrack.getString("b64"),
                    yTrack.getString("track_name"),
                    yTrack.getInteger("total_plays"),
                    yTrack.getInteger("today_plays")
            );
        }

        return result;
    }

    public void addYTrackToYPlaylist(YTrack yTrack, @NotNull YPlaylist oldPlaylist) { //TODO: addYTrackToYPlaylist()
        insertYTrack(yTrack);

        final ArrayList<String> playlistTracks = oldPlaylist.getPlaylistTracks();
        playlistTracks.add(yTrack.getTrackId());

        final YPlaylist newPlaylist = new YPlaylist(
                oldPlaylist.getPlaylistId(),
                playlistTracks,
                oldPlaylist.getPlaylistName(),
                oldPlaylist.getPlaylistAuthor(),
                oldPlaylist.isPrivate()
        );

        updatePlaylist(oldPlaylist, newPlaylist);
    }

    public boolean updatePlaylist(YPlaylist oldPlaylist, YPlaylist newPlaylist) { //TODO: updatePlaylist()
        final YPlaylist check = getPlaylist(oldPlaylist.getPlaylistId());
        if(check != null) {
            final BasicDBObject query = new BasicDBObject("_id", oldPlaylist.getPlaylistId());
            update("yPlaylists", query, new BasicDBObject("name", newPlaylist.getPlaylistName()));
            update("yPlaylists", query, new BasicDBObject("tracks", newPlaylist.getPlaylistTracks()));
            update("yPlaylists", query, new BasicDBObject("author", newPlaylist.getPlaylistAuthor()));
            update("yPlaylists", query, new BasicDBObject("private", newPlaylist.isPrivate()));
            return true;
        }
        return false;
    }

    public YPlaylist getPlaylist(String name, long userId) {
        final Document yPlaylist = this.database.getCollection("yPlaylists").find(
                and(
                    eq("name", name),
                    eq("author", userId)
                )
        ).first();

        return getPlaylist(yPlaylist);
    }

    public YPlaylist getPlaylist(String id) {
        final Document yPlaylist = this.database.getCollection("yPlaylists").find(
                eq("_id", id)
        ).first();

        return getPlaylist(yPlaylist);
    }

    private YPlaylist getPlaylist(Document yPlaylist) {
        YPlaylist result = null;

        if (yPlaylist != null) {
            result = new YPlaylist(
                    yPlaylist.getString("_id"),
                    (ArrayList<String>) yPlaylist.get("tracks"),
                    yPlaylist.getString("name"),
                    yPlaylist.getLong("author"),
                    yPlaylist.getBoolean("private")
            );
        }

        return result;
    }

    public YUser getUser(long id) {
        YUser result = null;
        final Document yUser = this.database.getCollection("yUsers").find(
                eq("_id", id)
        ).first();

        LOGGER.debug("Check for: " + yUser);

        if (yUser != null) {
            result = new YUser(
                    yUser.getLong("_id"),
                    yUser.getString("selectedPlaylist"),
                    yUser.getInteger("totalPoints"));
        }

        return result;
    }

    public void deletePlaylist(@NotNull YPlaylist yPlaylist) {
        deletePlaylist(yPlaylist.getPlaylistId());
    }

    public void deletePlaylist(String id) {
        this.database.getCollection("yPlaylists").deleteOne(
                eq("_id", id)
        );
    }

    public void resetDailyPlays(@NotNull TrackInfo trackInfo) {
        final BasicDBObject query = new BasicDBObject("uri", trackInfo.getUri());
        update("songs", query, new BasicDBObject("todayPlays", 0));
        update("songs", query, new BasicDBObject("totalPlays", 0));
    }

    public void resetDailyPlays() {
        final List<Document> songs = get("songs", BasicDBObject.parse("{ 'uri': { $ne: 'NULL' } }"));
        songs.forEach(s -> update("songs", new BasicDBObject("uri", s.getString("uri")), new BasicDBObject("todayPlays", 0)));
    }

    public TrackInfo getSongOfTheDay() {
        TrackInfo trackInfo = null;

        final Document song = this.database.getCollection("songs").find().sort(new BasicDBObject("todayPlays", -1)).first();

        if(song != null) {
            trackInfo = new TrackInfo(
                    song.getInteger("totalPlays"),
                    song.getInteger("todayPlays"),
                    song.getString("uri")
            );
        }

        return trackInfo;
    }

    public String getBotServerInvite() {
        String invite = "https://discord.gg/TSHGVQt";

        final List<Document> bot = get("bot", BasicDBObject.parse("{ 'client_id': { $ne: '0' } }"));
        if(!bot.isEmpty()) { invite = bot.get(0).getString("discord_invite"); }
        return invite;
    }

    public String getBotWebsiteUrl() {
        String web = "https://yato.fernandopal.es";

        final List<Document> bot = get("bot", BasicDBObject.parse("{ 'client_id': { $ne: '0' } }"));
        if(!bot.isEmpty()) { web = bot.get(0).getString("website"); }
        return web;
    }

    public void setPrefix(long guildId, String newPrefix) {
        update("guilds", new BasicDBObject("_id", guildId), new BasicDBObject("bot_prefix", newPrefix));
    }

    public boolean hasQueueMessagesEnabled(long guildId) { //TODO: hasQueueMessagesEnabled()
        boolean queueMessages = true;

        final List<Document> guilds = get("guilds", new BasicDBObject("_id", guildId));
        if(!guilds.isEmpty()) {
            try {
                queueMessages = guilds.get(0).getBoolean("queue_messages");
            } catch (Exception ignored) { }
        }

        return queueMessages;
    }

    public void toggleQueueMessages(long guildId) { //TODO: toggleQueueMessages()
        update("guilds", new BasicDBObject("_id", guildId), new BasicDBObject("queue_messages", !hasQueueMessagesEnabled(guildId)));
    }

    public boolean hasSearchResultsEnabled(long guildId) { //TODO: hasSearchResultsEnabled()
        boolean searchResults = true;

        final List<Document> guilds = get("guilds", new BasicDBObject("_id", guildId));
        if(!guilds.isEmpty()) {
            try {
                searchResults = guilds.get(0).getBoolean("search_results");
            } catch (Exception ignored) { }
        }

        LOGGER.error(searchResults + "");

        return searchResults;
    }

    public void toggleSearchResults(long guildId) { //TODO: toggleSearchResults()
        update("guilds", new BasicDBObject("_id", guildId), new BasicDBObject("search_results", !hasSearchResultsEnabled(guildId)));
    }

    public void updateBotUserStats(int bot_users, int bot_guilds) {
        BasicDBObject query = BasicDBObject.parse("{ 'client_id': { $ne: '0' } }");

        update("bot", query, new BasicDBObject("bot_users", bot_users));
        update("bot", query, new BasicDBObject("bot_guilds", bot_guilds));
    }

    public void updateBotShardsCount(int bot_shards) {
        BasicDBObject query = BasicDBObject.parse("{ 'client_id': { $ne: '0' } }");

        update("bot", query, new BasicDBObject("bot_shards", bot_shards));
    }

    public void insert(String collection, Document document) {
        this.database.getCollection(collection).insertOne(document);
    }

    public void insert(String collection, BasicDBObject dbObject) {
        this.database.getCollection(collection).insertOne(dboToDocument(dbObject));
    }

    public void insert(String collection, List<Document> documents) {
        this.database.getCollection(collection).insertMany(documents);
    }

    public void update(String collection, BasicDBObject query, BasicDBObject newDoc) {
        this.database.getCollection(collection).updateOne(query, new BasicDBObject("$set", newDoc));
    }

    public List<Document> get(String collection, @NotNull BasicDBObject query) {
        FindIterable<Document> cursor = this.database.getCollection(collection).find(query.toBsonDocument(BsonDocument.class, database.getCodecRegistry()));

        List<Document> results = new ArrayList<>();

        for (Document r : cursor) {
            results.add(r);
        }

        return results;
    }

    public List<Document> getAndFilter(String collection, @NotNull BasicDBObject query, BasicDBObject filter) {
        FindIterable<Document> cursor = this.database.getCollection(collection)
                .find(query.toBsonDocument(BsonDocument.class, database.getCodecRegistry()))
                .filter(filter);

        List<Document> results = new ArrayList<>();

        for (Document r : cursor) {
            results.add(r);
        }

        return results;
    }

    public List<Document> getAndSort(String collection, BasicDBObject query, BasicDBObject sort) {
        FindIterable<Document> cursor = this.database.getCollection(collection)
                .find(query)
                .sort(sort);

        List<Document> results = new ArrayList<>();

        for (Document r : cursor) {
            results.add(r);
        }

        return results;
    }

    public AudioTrack getPlayingTrack(long guildId) {
//        AudioTrack player_now_playing = null;
//
//        final List<Document> guilds = get("guilds", new BasicDBObject("GID", guildId));
//        if (!guilds.isEmpty()) {
//            final String json = guilds.get(0).getString("player_queue");
//            if (json == null || json.equals("null")) {
//                final Gson gson = new Gson();
//                final JsonObject jsonObject = new JsonParser().parse(json).getAsJsonObject();
//
//                player_now_playing = gson.fromJson(jsonObject, AudioTrack.class);
//            }
//        }
//        return player_now_playing;
        return null;
    }

    public List<AudioTrack> getSongsQueue(long guildId) {
//        List<AudioTrack> player_queue = new ArrayList<>();
//
//        final List<Document> guilds = get("guilds", new BasicDBObject("GID", guildId));
//        if (!guilds.isEmpty()) {
//            final String json = guilds.get(0).getString("player_queue");
//            if (json == null || json.equals("null")) {
//                final Gson gson = new Gson();
//                final JsonObject jsonObject = new JsonParser().parse(json).getAsJsonObject();
//                final List queue = gson.fromJson(jsonObject, List.class);
//
//                if (!queue.isEmpty()) {
//                    for (Object audioTrack : queue) {
//                        player_queue.add((AudioTrack) audioTrack);
//                    }
//                }
//            }
//
//        }
//        return player_queue;
        return null;
    }

    public int getBotUsers() {
        int count = 0;

        final List<Document> guilds = get("bot", BasicDBObject.parse("{ 'client_id': { $ne: '0' } }"));
        if (!guilds.isEmpty()) {
            count = guilds.get(0).getInteger("bot_users");
        }
        return count;
    }

    public int getBotGuilds() {
        int count = 0;

        final List<Document> guilds = get("bot", BasicDBObject.parse("{ 'client_id': { $ne: '0' } }"));
        if (!guilds.isEmpty()) {
            count = guilds.get(0).getInteger("bot_guilds");
        }
        return count;
    }

    public int getBotShards() {
        int count = 0;

        final List<Document> guilds = get("bot", BasicDBObject.parse("{ 'client_id': { $ne: '0' } }"));
        if (!guilds.isEmpty()) {
            count = guilds.get(0).getInteger("bot_shards");
        }
        return count;
    }

    public int getSongsPlayed() { //TODO: fix
        int count = 0;

        final List<Document> songs = get("songs", BasicDBObject.parse("{ 'client_id': { $ne: '0' } }"));
        if (!songs.isEmpty()) {
            for (Document song : songs) {
                count += song.getInteger("total_plays");
            }
        }
        return count;
    }

    private static Document dboToDocument(DBObject dbo) {
        if(dbo == null) { return null; }
        return new Document(dbo.toMap());
    }
}

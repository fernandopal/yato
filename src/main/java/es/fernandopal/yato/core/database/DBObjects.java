package es.fernandopal.yato.core.database;

import com.mongodb.BasicDBObject;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;

public class DBObjects {
    public static BasicDBObject getGuildObject(Guild guild, long music_channel, boolean dj_enabled, long dj_role, String bot_prefix, boolean queue_messages, boolean search_results) {
        final BasicDBObject gDoc = new BasicDBObject("_id", guild.getIdLong());
        gDoc.append("guild_name", guild.getName());
        gDoc.append("music_channel", music_channel);
        gDoc.append("dj_enabled", dj_enabled);
        gDoc.append("dj_role", dj_role);
        gDoc.append("bot_prefix", bot_prefix);
        gDoc.append("guild_users", guild.getMemberCount());
        gDoc.append("songs_served_today", 0);
        gDoc.append("player_now_playing", null);
        gDoc.append("player_queue", null);
        gDoc.append("queue_messages", queue_messages);
        gDoc.append("search_results", search_results);
        int bots = 0;
        for(Member m : guild.getMembers()) { if (m.getUser().isBot()) { bots++; } }
        gDoc.append("guild_bots", bots);
        return gDoc;
    }

    public static BasicDBObject getBotObject(long client_id, String bot_website, String discord_invite, String discord_oauth, long owner_id, int bot_users, int bot_guilds, int bot_shards) {
        final BasicDBObject gDoc = new BasicDBObject("client_id", client_id);
        gDoc.append("website", bot_website);
        gDoc.append("discord_invite", discord_invite);
        gDoc.append("discord_oauth", discord_oauth);
        gDoc.append("owner_id", owner_id);
        gDoc.append("bot_users", bot_users);
        gDoc.append("bot_guilds", bot_guilds);
        gDoc.append("bot_shards", bot_shards);
        return gDoc;
    }
}

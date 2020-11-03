package es.fernandopal.yato.core.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.bandcamp.BandcampAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.http.HttpAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.soundcloud.SoundCloudAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.twitch.TwitchStreamAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.vimeo.VimeoAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import com.wrapper.spotify.SpotifyApi;
import es.fernandopal.yato.Main;
import java.util.HashMap;

import es.fernandopal.yato.core.BotManager;
import lavalink.client.io.Link;
import net.dv8tion.jda.api.entities.Guild;

public class AudioManagerController {
  private static HashMap<Guild, GuildAudioManager> managers;
  private static AudioPlayerManager playerManager;
  
  public AudioManagerController() {
    managers = new HashMap<>();
    playerManager = new DefaultAudioPlayerManager();
    playerManager.registerSourceManager(new YoutubeAudioSourceManager());
    playerManager.registerSourceManager(SoundCloudAudioSourceManager.createDefault());
    playerManager.registerSourceManager(new BandcampAudioSourceManager());
    playerManager.registerSourceManager(new VimeoAudioSourceManager());
    playerManager.registerSourceManager(new TwitchStreamAudioSourceManager());
    playerManager.registerSourceManager(new HttpAudioSourceManager());
    playerManager.setTrackStuckThreshold(5000L);
  }
  
  public static GuildAudioManager getGuildAudioManager(Guild guild) {
    GuildAudioManager manager;
    if (!managers.containsKey(guild)) {
      synchronized (getGuildAudioManagers()) {
        manager = new GuildAudioManager(guild);
        addGuildAudioManager(guild, manager);
      } 
    } else {
      manager = managers.get(guild);
    } 
    return manager;
  }
  
  public static HashMap<Guild, GuildAudioManager> getGuildAudioManagers() {
    return managers;
  }
  
  private static void addGuildAudioManager(Guild guild, GuildAudioManager manager) {
    managers.put(guild, manager);
  }
  
  public static void removeGuildAudioManager(Guild guild) {
    managers.remove(guild);
  }
  
  public static AudioPlayerManager getPlayerManager() {
    return playerManager;
  }
  
  public static Link getExistingLink(Guild guild) {
    return Main.getBotManager().getLavalink().getExistingLink(guild.getId());
  }
}

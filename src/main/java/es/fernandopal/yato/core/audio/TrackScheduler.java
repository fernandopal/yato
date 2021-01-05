package es.fernandopal.yato.core.audio;

import java.util.*;
import java.util.concurrent.ScheduledFuture;

import es.fernandopal.yato.core.scheduler.tasks.VoiceTimeoutJob;
import es.fernandopal.yato.util.MessageUtil;
import es.fernandopal.ydba.entities.TrackInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;

import es.fernandopal.yato.Main;
import es.fernandopal.yato.core.scheduler.ScheduleHandler;
import lavalink.client.player.IPlayer;
import lavalink.client.player.LavalinkPlayer;
import lavalink.client.player.event.PlayerEventListenerAdapter;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

@SuppressWarnings("rawtypes")
public class TrackScheduler extends PlayerEventListenerAdapter {
	private static final Logger LOGGER = LoggerFactory.getLogger(TrackScheduler.class);
    private final MessageUtil msg = Main.getBotManager().getMsgu();

    private final Guild guild;
    private AudioTrack lastTrack = null;
    private boolean looping = false;
    private final LavalinkPlayer player;
    public final Queue<AudioTrack> queue;
	private ScheduledFuture timeout;
    private TextChannel musicChannel;

    TrackScheduler(Guild guild, LavalinkPlayer player) {
        this.guild = guild;
        this.player = player;
        this.queue = new LinkedList<>();
    }

    public boolean hasNextTrack() {
        return (queue.peek() != null);
    }

    public void queue(AudioTrack track) {
        if(player.getPlayingTrack() == null) {
            queue.add(track);
            nextTrack();
            return;
        }

        queue.offer(track);
    }

    public void clearQueue() {
        queue.clear();
    }

//    public boolean removeFromQueue(int pos) {
//        final List<Object> lq = Arrays.asList(queue.toArray());
//        if(pos > lq.size()) { return false; }
//
//        lq.remove(pos);
//
//        clearQueue();
//        lq.forEach(i -> queue.add((AudioTrack) i));
//
//        return true;
//    }

//    public boolean removeDupes() {
//        final TreeSet<Object> lq = new TreeSet<>(Arrays.asList(queue.toArray()));
//
//        clearQueue();
//        lq.forEach(i -> { queue.add((AudioTrack) i); });
//
//        return true;

    /* java.lang.ClassCastException: com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioTrack cannot be cast to java.base/java.lang.Comparable
	at java.base/java.util.TreeMap.compare(TreeMap.java:1291)
	at java.base/java.util.TreeMap.put(TreeMap.java:536)
	at java.base/java.util.TreeSet.add(TreeSet.java:255)
	at java.base/java.util.AbstractCollection.addAll(AbstractCollection.java:352)
	at java.base/java.util.TreeSet.addAll(TreeSet.java:312)
	at java.base/java.util.TreeSet.<init>(TreeSet.java:160)
	at es.fernandopal.yato.core.audio.TrackScheduler.removeDupes(TrackScheduler.java:72)
	at es.fernandopal.yato.commands.music.RemoveDupesCommand.handle(RemoveDupesCommand.java:31)
	at es.fernandopal.yato.commands.CommandManager.handle(CommandManager.java:129)
	at es.fernandopal.yato.listeners.CommandListener.onGuildMessageReceived(CommandListener.java:28)
	at net.dv8tion.jda.api.hooks.ListenerAdapter.onEvent(ListenerAdapter.java:396)
	at net.dv8tion.jda.api.hooks.InterfacedEventManager.handle(InterfacedEventManager.java:96)
	at net.dv8tion.jda.internal.hooks.EventManagerProxy.handleInternally(EventManagerProxy.java:82)
	at net.dv8tion.jda.internal.hooks.EventManagerProxy.handle(EventManagerProxy.java:69)
	at net.dv8tion.jda.internal.JDAImpl.handleEvent(JDAImpl.java:166)
	at net.dv8tion.jda.internal.handle.MessageCreateHandler.handleInternally(MessageCreateHandler.java:97)
	at net.dv8tion.jda.internal.handle.SocketHandler.handle(SocketHandler.java:36)
	at net.dv8tion.jda.internal.requests.WebSocketClient.onDispatch(WebSocketClient.java:921)
	at net.dv8tion.jda.internal.requests.WebSocketClient.onEvent(WebSocketClient.java:808)
	at net.dv8tion.jda.internal.requests.WebSocketClient.handleEvent(WebSocketClient.java:786)
	at net.dv8tion.jda.internal.requests.WebSocketClient.onBinaryMessage(WebSocketClient.java:959)
	at com.neovisionaries.ws.client.ListenerManager.callOnBinaryMessage(ListenerManager.java:385)
	at com.neovisionaries.ws.client.ReadingThread.callOnBinaryMessage(ReadingThread.java:276)
	at com.neovisionaries.ws.client.ReadingThread.handleBinaryFrame(ReadingThread.java:996)
	at com.neovisionaries.ws.client.ReadingThread.handleFrame(ReadingThread.java:755)
	at com.neovisionaries.ws.client.ReadingThread.main(ReadingThread.java:108)
	at com.neovisionaries.ws.client.ReadingThread.runMain(ReadingThread.java:64)
	at com.neovisionaries.ws.client.WebSocketThread.run(WebSocketThread.java:45) */
//    }

    public void nextTrack() {
        try {
            AudioTrack track = queue.poll();

//            LOGGER.error(queue.toString());

            if(track == null) {
                if(player.getPlayingTrack() != null) {
                    player.stopTrack();
                }

                timeout = ScheduleHandler.registerUniqueJob(new VoiceTimeoutJob(guild));
                //TODO: fix in-discord logs
//                msg.sendWebhookMsg("Queue empty | The bot will leave in 1 minute from `" + guild.toString() + "`", "https://discordapp.com/api/webhooks/733077861639913580/t_5H_mh3Onjgkki24CFTTrfNniPiXCEM-zUH7LFNDarmroQYb9zjLeH78RHSgumbD-p1");
//                LOGGER.info("Queue empty | The bot will leave in 1 minute from " + guild.toString());
                
                try {
    	            GuildMessageReceivedEvent evt = (GuildMessageReceivedEvent) lastTrack.getUserData();
    	            TextChannel tc = evt.getChannel();

                    if(musicChannel == null) { musicChannel = tc; }
                    
                    if(player.getPlayingTrack() == null && queue.isEmpty()) {
                        Main.getBotManager().getMsgu().sendOk(musicChannel, "There is no tracks in queue, the bot will leave the channel in 1 minute.");
                    }
                } catch(Exception ignored) { }
                
                return;
            }

            if(timeout != null) {
                timeout.cancel(true);
                timeout = null;
                //TODO: fix in-discord logs
//                msg.sendWebhookMsg("Queue not empty | Leave job cancelled for `" + guild.toString() + "`", "https://discordapp.com/api/webhooks/733077861639913580/t_5H_mh3Onjgkki24CFTTrfNniPiXCEM-zUH7LFNDarmroQYb9zjLeH78RHSgumbD-p1");
//                LOGGER.info("Queue not empty | Leave job cancelled for " + guild.toString());
            }

            player.playTrack(track);

        	Thread t = new Thread(() -> {
                TrackInfo trackInfo = Main.getDb().getSong(track.getInfo().uri);
                Main.getDb().addPlays(trackInfo);
            });
            t.start();
            
    		
        } catch(Exception ex) { ex.printStackTrace(); }
    }
    
    @Override
    public void onTrackStart(IPlayer player, AudioTrack track) {
    	super.onTrackStart(player, track);
    }

    @Override
    public void onTrackException(IPlayer player, AudioTrack track, Exception exception) {
    	super.onTrackException(player, track, exception);
    }

    @Override
    public void onTrackStuck(IPlayer player, AudioTrack track, long thresholdMs) {
    	super.onTrackStuck(player, track, thresholdMs);
    }

    @Override
    public void onTrackEnd(IPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        this.lastTrack = track;

        // Only start the next track if the end reason is suitable for it (FINISHED or LOAD_FAILED)
        if(endReason.mayStartNext) {
            if(looping) {
                queue.add(track);
                nextTrack();
                return;
            }

            nextTrack();
        }
    }
    
    public boolean isLooping() { return looping; }
    public void setLooping(boolean looping) { this.looping = looping; }
    public AudioTrack getLastTrack() { return lastTrack; }
    public void shuffle() { Collections.shuffle((List<?>) queue); }
    public ScheduledFuture getTimeout() {
        return timeout;
    }
    public void setTimeout(ScheduledFuture timeout) {
        this.timeout = timeout;
    }
}
package es.fernandopal.yato.core.audio.playlists;

import com.google.api.client.util.Base64;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.tools.io.MessageInput;
import com.sedmelluq.discord.lavaplayer.tools.io.MessageOutput;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import es.fernandopal.yato.core.audio.AudioManagerController;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class PlaylistsUtils {
    private final AudioPlayerManager audioPlayerManager = AudioManagerController.getPlayerManager();

    public String encodeTrack(AudioTrack track) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        audioPlayerManager.encodeTrack(new MessageOutput(baos), track);
        return Base64.encodeBase64String(baos.toByteArray());
    }

    public AudioTrack decodeTrack(String trackB64) throws IOException {
        byte[] b64 = Base64.decodeBase64(trackB64);
        ByteArrayInputStream bais = new ByteArrayInputStream(b64);
        return audioPlayerManager.decodeTrack(new MessageInput(bais)).decodedTrack;
    }
}

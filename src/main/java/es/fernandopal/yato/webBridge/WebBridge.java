package es.fernandopal.yato.webBridge;

import com.google.gson.Gson;
import es.fernandopal.yato.Main;
import es.fernandopal.yato.webBridge.entities.GuildInfo;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class WebBridge implements Runnable {
    private static final Logger LOG = LoggerFactory.getLogger(WebBridge.class);
    private ServerSocket serverSocket;

    public void run() {
        LOG.info("Starting WebBridge...");

        try {
            serverSocket = new ServerSocket(7242);
            LOG.info("Waiting for connections...");
            while (true) {
                new EchoClientHandler(serverSocket.accept()).start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            stop();
        }
    }

    public void stop() {
        try {
            serverSocket.close();
        } catch (IOException ignored) { }

    }

    private static class EchoClientHandler extends Thread {
        private final Socket clientSocket;
        private final Gson gson;

        public EchoClientHandler(Socket socket) {
            this.clientSocket = socket;
            this.gson = new Gson();
        }

        public void run() {
            try {
                LOG.info("Client connected: " + clientSocket.getLocalAddress());

                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                final String readLine = in.readLine();
                LOG.info("[" + clientSocket.getLocalAddress() + "][IN]: " + readLine);
                String inputLine;
                while ((inputLine = readLine) != null) {
                    final JSONObject json = new JSONObject(inputLine);
                    final String requestType = json.getString("request_type");

                    if(requestType.equals("GET")) {
                        final String request = json.getString("request");

                        if (request.equals("bot_guild_count")) {
                            final JSONObject response = new JSONObject();
                            response.put("bot_guilds", Main.getShardManager().getGuilds().size());
                            sendMsg(response.toString(), clientSocket, out);
                            break;

                        } else if (request.equals("guild_info")) {
                            final Guild guild = Main.getShardManager().getGuildById(json.getLong("guild_id"));
                            GuildInfo guildInfo;
                            if(guild != null) {
                                int bots = 0;
                                for(Member m : guild.getMembers()) { if(m.getUser().isBot()) { bots++; } }

                                final TextChannel musicChannel = Main.getTextChannelById(Main.getDb().getMusicChannel(guild.getIdLong()));
                                Long musicChannelId = null;
                                if(musicChannel != null) { musicChannelId = musicChannel.getIdLong(); }

                                guildInfo = new GuildInfo(
                                        musicChannelId,
                                        null,
                                        guild.loadMembers().get().size(),
                                        bots,
                                        0,
                                        guild.getRegion().toString()
                                );
                                final String response = gson.toJson(guildInfo);
                                sendMsg(response, clientSocket, out);
                            }
                            break;

                        }
                    }
                    out.println(inputLine);
                }

                LOG.info("Client disconnected: " + clientSocket.getLocalAddress());

                in.close();
                out.close();
                clientSocket.close();

            } catch (IOException e) {
                LOG.debug(e.getMessage());
            }
        }
    }

    private static void sendMsg(String msg, Socket clientSocket, PrintWriter out) {
        LOG.info("[" + clientSocket.getLocalAddress() + "][OUT]: " + msg);
        out.println(msg);
    }
}
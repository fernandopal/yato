package es.fernandopal.yato.webBridge.entities;

public class GuildInfo {
    Long musicChannel, djRole;
    Integer userCount, botUsers, songsServedToday;
    String region;

    public GuildInfo(Long musicChannel, Long djRole, Integer userCount, Integer botUsers, Integer songsServedToday, String region) {
        this.musicChannel = musicChannel;
        this.djRole = djRole;
        this.userCount = userCount;
        this.botUsers = botUsers;
        this.songsServedToday = songsServedToday;
        this.region = region;
    }
}

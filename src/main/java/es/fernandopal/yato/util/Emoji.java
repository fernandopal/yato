package es.fernandopal.yato.util;

import com.vdurmont.emoji.EmojiParser;

public class Emoji {
	public static final String WARNING = EmojiParser.parseToUnicode(":warning:");
	public static final String PING = EmojiParser.parseToUnicode(":ping_pong:");
	public static final String HAND_OK = EmojiParser.parseToUnicode(":ok_hand:");
	public static final String INFORMATION = EmojiParser.parseToUnicode(":information_source:");
	public static final String TEXT = EmojiParser.parseToUnicode(":page_with_curl:");
	public static final String MARK_EXCLAMATION = EmojiParser.parseToUnicode(":exclamation:");
	public static final String SHUFFLE = EmojiParser.parseToUnicode(":twisted_rightwards_arrows:");
	public static final String REPEAT_SINGLE = EmojiParser.parseToUnicode(":repeat_one:");
	public static final String SPEAKER = EmojiParser.parseToUnicode(":speaker:");
	public static final String VOLUME_HIGH = EmojiParser.parseToUnicode(":loud_sound:");
	public static final String VOLUME_LOW = EmojiParser.parseToUnicode(":sound:");
	public static final String NEXT_TRACK = EmojiParser.parseToUnicode(":fast_forward:");
    public final static String STOP = EmojiParser.parseToUnicode(":black_square_for_stop:");
	public static final String PLAY = EmojiParser.parseToUnicode(":arrow_forward:");
	public static final String PAUSE = EmojiParser.parseToUnicode(":pause_button:");
	public static final String REPEAT = EmojiParser.parseToUnicode(":repeat:");
	public static final String NOTES = EmojiParser.parseToUnicode(":notes:");
	public static final String HEART = EmojiParser.parseToUnicode(":heart:");
	public static final String GIFT = EmojiParser.parseToUnicode(":gift:");
	public static final String S_ORANGE_DIAMOND = EmojiParser.parseToUnicode(":small_orange_diamond:");
	public static final String S_BLUE_DIAMOND = EmojiParser.parseToUnicode(":small_blue_diamond:");
	public static final String S_RED_TRIANGLE = EmojiParser.parseToUnicode(":small_red_triangle:");
	public static final String CLIPBOARD = EmojiParser.parseToUnicode(":clipboard:");
	public static final String POINT_RIGHT = EmojiParser.parseToUnicode(":point_right:");
	public static final String CD = EmojiParser.parseToUnicode(":cd:");
	public static final String CROSS = EmojiParser.parseToUnicode(":x:");
	public static final String SEARCH = EmojiParser.parseToUnicode(":mag_right:");
	public static final String ROBOT = EmojiParser.parseToUnicode(":robot:");
	public static final String CHECK_2 = EmojiParser.parseToUnicode(":white_check_mark:");
	public static final String CLOCK1 = EmojiParser.parseToUnicode(":clock1:");
	 
	//CUSTOM EMOJIS//
    public static final String STATUS_ONLINE = "<:online:468787250952405012>";
    public static final String STATUS_IDLE = "<:idle:468787249941839872>";
    public static final String STATUS_DND = "<:dnd:468787250461933568>";
    public static final String STATUS_OFFLINE = "<:offline:468787250675712010>";
    public static final String STATUS_STREAMING = "<:streaming:468787251434749952>";
    public static final String ERROR = "<:xmark:469529569087586314>";
	public static final String CHECK = "<:check:468799153288380417>";
	public static final String ROCK = "<:rock:469523126611607556>";
	public static final String PAPER = "<:paper:469523124866514964>";
	public static final String SCISSORS = "<:cscissors:469528048874815509>";
	public static final String LOADING = "<a:ytloading:468787256254136320>";
	public static final String TOP10 = "<:top10:470640096299974672>";
	public static final String BASS_BOOST = "<:bass_boost:477424901423104031>";
	public static final String JDA = "<:jda:468787255352492042>";
	public static final String JAVA = "<:java:468787254651781151>";
	public static final String DEBIAN = "<:debian:479363575232856064>";
	public static final String TUX = "<:tux:546431513542000650>";
	public static final String RADIO = "<:yatoradio:733053038104674371>";

	public static String NUMBER(int number) {
		String r = null;
		switch (number) {
			case 0: r = EmojiParser.parseToUnicode(":zero:"); break;
			case 1: r = EmojiParser.parseToUnicode(":one:"); break;
			case 2: r = EmojiParser.parseToUnicode(":two:"); break;
			case 3: r = EmojiParser.parseToUnicode(":three:"); break;
			case 4: r = EmojiParser.parseToUnicode(":four:"); break;
			case 5: r = EmojiParser.parseToUnicode(":five:"); break;
			case 6: r = EmojiParser.parseToUnicode(":six:"); break;
			case 7: r = EmojiParser.parseToUnicode(":seven:"); break;
			case 8: r = EmojiParser.parseToUnicode(":eight:"); break;
			case 9: r = EmojiParser.parseToUnicode(":nine:"); break;
		}
		return r;
	};

	public static String reactFromCustom(String emoji) {
		return emoji.replace("<", "").replace(">", "");
	}
}
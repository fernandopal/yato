# Yato | 夜ト
Yato was planned as a closed-source bot but after +6 months of having it running and +1 year of development I decided it was time to make it open to the community so here we are :)<br>
Don't expect the code to have a good quality, I have learned what I know by myself, and I still have a lot of things to improve so if you want to improve something pull requests are there for you.

**Index:**
1. [Self hosting guide](#self-hosting-guide)
2. [Bot features](#bot-features)

**Get help:**
- [Discord](https://discord.gg/aHJXGb3)

**top.gg:** <br>
[![top.gg](https://top.gg/api/widget/454272495114256394.svg)](https://top.gg/bot/454272495114256394)

<br><br>

# self hosting guide
**First of all, a list of what you need to have:**
- An app created at [discord developer portal](https://discord.com/developers)
- A MongoDB database (free tier of [mongodb atlas](https://www.mongodb.com/cloud/atlas) it's ok for small bots)
- A Google application with [youtube data api V3](https://developers.google.com/youtube/v3) enabled
- A [spotify application](https://developer.spotify.com) (not in use by the moment but can be implemented in the future)
- [Download Lavalink.jar](https://github.com/Frederikam/Lavalink/releases) (Lavalink is used to distribute the audio sending load)
- [Download the latest yato release](https://github.com/fernandopal/yato-public/releases) or build yato by yourself using the source code.

**How to set up yato properly?**<br>
1. Put your yato-x.x_x.jar in a folder.
2. Download the example config files: [database_example.json](https://github.com/fernandopal/yato-public/blob/master/database-example.json) and [config_example.json](https://github.com/fernandopal/yato-public/blob/master/config-example.json) and remove the _example from the names.
3. Take a look at the content of the files, there is a few things that you **need** to configure:
````yaml
{
  "bot-token": "", <- Here you need to put your discord app token
  "google-api-key": "", <- The google api key of the google app that you have created
  "spotify-client-id": "", <- The spotify client id of the app that you have created
  "spotify-secret": "", <- The spotify secret of the spotify app that you have created
  "bot-id": "483329776996712468", <- If of your bot user IMPORTANT, If you put a wrong id the audio will not be sent to discord
  "bot-game": "https://yato.fernandopal.es", <- Playing...
  "bot-shards": 1, <- Number of instances of the bot, I recommend you to put 1 for every ~1500 - 2000 guilds
  "bot-prefix": "y:", <- This will be the default prefix for the bot
  "bot-owners": [ <- Id's of all the users with FULL CONTROL over the bot commands and features
    "214829164253937674"
  ],
  "lavalink-nodes": [ <- Add your lavalink nodes here, must contain at least one
    "host:port@password"
  ]
}
````

```yaml
{
  "username": "yato", <- Database username
  "password": "supersafepassword", <- Database password
  "database-url": "yato-5kjsj.gcp.mongodb.net", <- Database connection url
  "database-name": "yato", <- Database name
  "url-args": "retryWrites=true&w=majority" <- Extra arguments for the database connection
}
```

```yaml
{
  "port": 7263, <- Port that the bot will use to open the REST API server
  "auth-token": "RANDOM_STRING_HERE" <- Put a random string here that will be used for authentication on the api
}
```
When all of that is done you have everything prepared to run yato, the easiest way to run the bot is to install screen (`sudo apt install screen`)  if you're running on a unix based system once screen is in your system follow the next steps:
1. Open a new screen with `screen -S yato`
2. Execute the yato-x.x_x.jar file with `java -jar yato-x.x_x.jar`
3. Detach the screen pressing `Ctrl + A` and then `Ctrl + D`
4. Each time you want to check the yato console you can join the screen using `screen -x yato` and exit of that screen doing the step 3 again

<br><br>

# bot features
- HQ music playback
- Load balancing using Lavalink and the JDA sharding system
- Support for multiple audio sources (YouTube, SoundCloud, Vimeo, Twitch Streams, BandCamp, files on the internet)
- Youtube search
- Customisable prefix for each server
- Song Of The Day (Daily most played song)
- Customisable behaviour for some things
- And more

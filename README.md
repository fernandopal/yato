# Yato | 夜ト
Yato was planned as a closed-source bot but after +6 months of having it running and +1 year of development I decided it was time to make it open to the community so here we are :)<br>
Don't expect the code to have a good quality, I have learned what I know by myself, and I still have a lot of things to learn so if you want to improve something I appreciate all the pull requests.

**Index:**
1. [Self hosting guide](#self-hosting-guide)
2. [Building the code by yourself](#building-the-code)
2. [Bot features](#bot-features)

**Info:**
- [Discord](https://discord.gg/aHJXGb3)
- [mail@fernandopal.es](mailto:mail@fernandopal.es)
- [Yato website](https://yato.fernandopal.es)
- [My website](https://www.fernandopal.es)

<!--
**top.gg:** <br>
[![top.gg](https://top.gg/api/widget/454272495114256394.svg)](https://top.gg/bot/454272495114256394)

<br><br>
-->

# Self hosting guide
**First of all, a list of what you need to have:**
- An app created at [discord developer portal](https://discord.com/developers)
- A MongoDB database (free tier of [mongodb atlas](https://www.mongodb.com/cloud/atlas) it's ok for small bots)
- A Google application with [youtube data api V3](https://developers.google.com/youtube/v3) enabled
- A [spotify application](https://developer.spotify.com) (not in use by the moment but can be implemented in the future)
- [Download Lavalink.jar](https://github.com/Frederikam/Lavalink/releases) (Lavalink is used to distribute the audio sending load)
- [Download the latest yato release](https://github.com/fernandopal/yato/releases/latest) or [build yato by yourself]() using the source code.

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
  ],
  "dbl-token": "your top.gg token", <- Token obtained if your bot is on top.gg [OPTIONAL, needed just for votes]
  "haruna-url": "http://localhost", <- The url where your haruna server is listening [OPTIONAL, needed just for votes]
  "haruna-port": "6969", <- The port of that you have set on your haruna config [OPTIONAL, needed just for votes]
  "haruna-password": "your-password-here", <- The password of your haruna server [OPTIONAL, needed just for votes]
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


# Building the code
**Requisites to compile the java code into a runnable yato.jar:**
- [JDK 11](https://www.oracle.com/java/technologies/javase-jdk11-downloads.html) installed into your computer
- [Gradle](https://gradle.org/install/) installed into your computer

**How do I build the yato.jar file?**
1. [Download or clone](https://docs.github.com/en/free-pro-team@latest/github/creating-cloning-and-archiving-repositories/cloning-a-repository) this repository
2. Join the root folder of the repository, [yato-master](https://github.com/fernandopal/yato)
3. Execute the [build.bat](https://github.com/fernandopal/yato/blob/master/build.bat) script if you're on windows, or the [build.sh](https://github.com/fernandopal/yato/blob/master/build.sh) script if you're on a linux machine (those scripts only contain a single command, `gradlew build` so you can execute this instead if you want)
4. Now you should have a `/yato-master/build/libs folder`, there you can find the generated yato.jar


# Bot features
- HQ music playback
- Load balancing using Lavalink and the JDA sharding system
- Support for multiple audio sources (YouTube, SoundCloud, Vimeo, Twitch Streams, BandCamp, files on the internet)
- Youtube search
- Customisable prefix for each server
- Song Of The Day (Daily most played song)
- Customisable behaviour for some things
- And more

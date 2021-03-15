# Update March 2021

This repository has been archived and is no longer maintained. Russbot had a good life on Slack, and may live again on Discord in the future.

---

# russbot


A Slack bot for KSU CIS Slack Channel. It is maintained by students and faculty at Kansas State University.

---

## Examples

Some of the plugins of russbot include:
* `!xkcd` - Posts a random xkcd comic
* `!rss` - Reads and posts from RSS feeds
* `!lmgtfy` - Let me Google that for you
* `!wolfram` - Wolfram Alpha Search
* `!beocat` - Beocat Break In, a text Based RPG Game


---

## Installation

#### Notes
* You will need to add a bot to your slack channel and get an API key prior to starting the setup.
* russbot uses gradle to build and run the bot.


#### Instructions

1. To get started copy the config file `russbot.cfg.example` to `russbot.cfg`
2. Inside the config file it should have a spot for several settings
    1. token - the Slack token
    2. wolfram - the Wolfram API token
    3. channels - the Channels russbot is allowed to access (plugins can choose from a subset of these)
    4. test - boolean (true, false) to put the system into test mode 
3. Create a **data** folder in the root of the project. `mkdir data`
3. Once you enter those go ahead and build with gradle as normal. `./gradlew build`
4. After you successfully build it you can try running it. `./gradlew run`


#### Testing

Use `./gradlew run -q -PrunArgs="['-t']"` to run in test mode and interact with russbot via command-line. You can also set the test property to "true" to enable test mode.

While in test mode:  
`#<channel>` will switch channel  
`@<user>` will switch user  

---

## License

The MIT License (MIT)

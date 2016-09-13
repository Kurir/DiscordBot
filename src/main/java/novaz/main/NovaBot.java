package novaz.main;

import novaz.core.AbstractEventListener;
import novaz.core.Logger;
import novaz.db.model.OMusic;
import novaz.db.model.OServer;
import novaz.db.table.TServers;
import novaz.guildsettings.DefaultGuildSettings;
import novaz.guildsettings.defaults.SettingActiveChannels;
import novaz.guildsettings.defaults.SettingBotChannel;
import novaz.guildsettings.defaults.SettingEnableChatBot;
import novaz.handler.*;
import novaz.util.Misc;
import org.reflections.Reflections;
import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.*;
import sx.blah.discord.util.*;
import sx.blah.discord.util.audio.AudioPlayer;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class NovaBot {

	public final long startupTimeStamp;
	public IDiscordClient instance;
	public CommandHandler commandHandler;
	public Timer timer = new Timer();
	public String mentionMe;
	public ChatBotHandler chatBotHandler = null;
	public GameHandler gameHandler = null;
	private boolean isReady = false;
	public boolean statusLocked = false;
	private Map<IGuild, IChannel> defaultChannels = new ConcurrentHashMap<>();

	public NovaBot() throws DiscordException {
		registerHandlers();
		instance = new ClientBuilder().withToken(Config.BOT_TOKEN).login();
		registerEvents();
		startupTimeStamp = System.currentTimeMillis() / 1000L;
	}

	public boolean isReady() {
		return isReady;
	}

	/**
	 * check if a user is the owner of a guild or isCreator
	 *
	 * @param channel the channel
	 * @param user    the user to check
	 * @return user is owner
	 */
	public boolean isOwner(IChannel channel, IUser user) {
		if (channel.isPrivate()) {
			return isCreator(user);
		}
		return isCreator(user) || channel.getGuild().getOwner().equals(user);
	}

	/**
	 * checks if user is creator
	 *
	 * @param user user to check
	 * @return is creator?
	 */
	public boolean isCreator(IUser user) {
		return user.getID().equals(Config.CREATOR_ID);
	}

	/**
	 * Gets the default channel to output to
	 * if configured channel can't be found, return the first channel
	 *
	 * @param guild the guild to check
	 * @return default chat channel
	 */
	public IChannel getDefaultChannel(IGuild guild) {
		if (!defaultChannels.containsKey(guild)) {
			String channelName = GuildSettings.get(guild).getOrDefault(SettingBotChannel.class);
			List<IChannel> channelList = guild.getChannels();
			boolean foundChannel = false;
			for (IChannel channel : channelList) {
				if (channel.getName().equalsIgnoreCase(channelName)) {
					foundChannel = true;
					defaultChannels.put(guild, channel);
					break;
				}
			}
			if (!foundChannel) {
				defaultChannels.put(guild, channelList.get(0));
			}
		}
		return defaultChannels.get(guild);
	}

	public void markReady(boolean ready) {
		setUserName(Config.BOT_NAME);
		loadConfiguration();
		mentionMe = "<@" + this.instance.getOurUser().getID() + ">";
		timer = new Timer();
		TextHandler.setBot(this);
		gameHandler = new GameHandler(this);
		this.isReady = ready;
	}

	public void loadConfiguration() {
		commandHandler.load();
		TextHandler.getInstance().load();
		defaultChannels = new ConcurrentHashMap<>();
		chatBotHandler = new ChatBotHandler();
	}

	private void registerEvents() {
		Reflections reflections = new Reflections("novaz.event");
		Set<Class<? extends AbstractEventListener>> classes = reflections.getSubTypesOf(AbstractEventListener.class);
		for (Class<? extends AbstractEventListener> c : classes) {
			try {
				AbstractEventListener eventListener = c.getConstructor(NovaBot.class).newInstance(this);
				if (eventListener.listenerIsActivated()) {
					instance.getDispatcher().registerListener(eventListener);
				}
			} catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
				e.printStackTrace();
			}
		}
	}

	public void addCustomCommand(IGuild server, String command, String output) {
		OServer serv = TServers.findBy(server.getID());
		commandHandler.addCustomCommand(command, output);
	}

	public void removeCustomCommand(IGuild server, String command) {
		OServer serv = TServers.findBy(server.getID());
		commandHandler.removeCustomCommand(command);
	}

	private void registerHandlers() {
		commandHandler = new CommandHandler(this);
	}

	public String getUserName() {
		return instance.getOurUser().getName();
	}

	public boolean setUserName(String newName) {
		if (isReady && !getUserName().equals(newName)) {
			try {
				instance.changeUsername(newName);
				return true;
			} catch (DiscordException | RateLimitException e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	public void addSongToQueue(String filename, IGuild guild) {
		File file = new File(Config.MUSIC_DIRECTORY + filename); // Get file
		AudioPlayer player = AudioPlayer.getAudioPlayerForGuild(guild);
		try {
			player.queue(file);
		} catch (IOException | UnsupportedAudioFileException e) {
			e.printStackTrace();
		}
	}

	public void skipCurrentSong(IGuild guild) {
		MusicPlayerHandler.getAudioPlayerForGuild(guild, this).skipSong();
	}

	public void setVolume(IGuild guild, float vol) {
		AudioPlayer player = AudioPlayer.getAudioPlayerForGuild(guild);
		player.setVolume(vol);
	}


	/**
	 * @param channel channel to send to
	 * @param content the message
	 * @return IMessage or null
	 */
	public IMessage sendMessage(IChannel channel, String content) {
		RequestBuffer.RequestFuture<IMessage> request = sendMessage(channel, new MessageBuilder(instance).withChannel(channel).withContent(content));
		return request.get();
	}

	public RequestBuffer.RequestFuture<IMessage> sendMessage(IChannel channel, MessageBuilder builder) {
		return RequestBuffer.request(() -> {
			try {
				return builder.send();
			} catch (DiscordException e) {
				if (e.getErrorMessage().contains("502")) {
					throw new RateLimitException("Workaround because of 502", 1000, "sendMessage", false);
				}
			} catch (MissingPermissionsException e) {
				Logger.fatal(e, "no permission");
				e.printStackTrace();
			}
			return null;
		});
	}

	public void deleteMessage(IMessage message) {
		RequestBuffer.request(() -> {
			try {
				message.delete();
			} catch (MissingPermissionsException | DiscordException e) {
				e.printStackTrace();
			}
			return null;
		});
	}

	public RequestBuffer.RequestFuture<IMessage> editMessage(IMessage msg, String newText) {
		return RequestBuffer.request(() -> {
			try {
				return msg.edit(newText);
			} catch (DiscordException e) {
				if (e.getErrorMessage().contains("502")) {
					throw new RateLimitException("Workaround because of 502", 1500, "editMessage", false);
				}
			} catch (MissingPermissionsException e) {
				Logger.fatal(e, "no permission");
				e.printStackTrace();
			}
			return null;
		});
	}

	public void handlePrivateMessage(IPrivateChannel channel, IUser author, IMessage message) {
		if (commandHandler.isCommand(channel, message.getContent())) {
			commandHandler.process(channel, author, message);
		} else {
			this.sendMessage(channel, this.chatBotHandler.chat(message.getContent()));
		}
	}

	public void handleMessage(IGuild guild, IChannel channel, IUser author, IMessage message) {
		if (!isReady || author.isBot()) {
			return;
		}

		GuildSettings settings = GuildSettings.get(guild);
		if (settings.getOrDefault(SettingActiveChannels.class).equals("mine") &&
				!channel.getName().equalsIgnoreCase(GuildSettings.get(channel.getGuild()).getOrDefault(SettingBotChannel.class))) {
			return;
		}
		if (gameHandler.isGameInput(channel, author, message.getContent().toLowerCase())) {
			gameHandler.execute(author, channel, message.getContent());
		} else if (commandHandler.isCommand(channel, message.getContent())) {
			commandHandler.process(channel, author, message);
		} else if (Config.BOT_CHATTING_ENABLED && settings.getOrDefault(SettingEnableChatBot.class).equals("true") &&
				!DefaultGuildSettings.getDefault(SettingBotChannel.class).equals(GuildSettings.get(channel.getGuild()).getOrDefault(SettingBotChannel.class)) &&
				channel.getName().equals(GuildSettings.get(channel.getGuild()).getOrDefault(SettingBotChannel.class))) {
			this.sendMessage(channel, this.chatBotHandler.chat(message.getContent()));
		}
	}

	public void sendPrivateMessage(IUser target, String message) {
		RequestBuffer.request(() -> {
			try {
				IPrivateChannel pmChannel = this.instance.getOrCreatePMChannel(target);
				return pmChannel.sendMessage(message);
			} catch (DiscordException e) {
				if (e.getErrorMessage().contains("502")) {
					throw new RateLimitException("Workaround because of 502", 1500, "editMessage", false);
				}
			} catch (MissingPermissionsException e) {
				Logger.fatal(e, "no permission");
				e.printStackTrace();
			}
			return null;
		});
	}

	public void sendErrorToMe(Exception error, Object... extradetails) {
		String errorMessage = "I'm sorry to inform you that I've encountered a **" + error.getClass().getName() + "**" + Config.EOL;
		errorMessage += "Message: " + Config.EOL;
		errorMessage += error.getLocalizedMessage() + Config.EOL;
		String stack = "";
		int maxTrace = 6;
		StackTraceElement[] stackTrace1 = error.getStackTrace();
		for (int i = 0; i < stackTrace1.length; i++) {
			StackTraceElement stackTrace = stackTrace1[i];
			stack += stackTrace.toString() + Config.EOL;
			if (i > maxTrace) {
				break;
			}
		}
		errorMessage += "Accompanied stacktrace: " + Config.EOL + Misc.makeTable(stack) + Config.EOL;
		if (extradetails.length > 0) {
			errorMessage += "Extra information: " + Config.EOL;
			for (int i = 1; i < extradetails.length; i += 2) {
				if (extradetails[i] != null) {
					errorMessage += extradetails[i - 1] + " = " + extradetails[i] + Config.EOL;
				} else if (extradetails[i - 1] != null) {
					errorMessage += extradetails[i - 1];
				}
			}
		}
		sendPrivateMessage(instance.getUserByID(Config.CREATOR_ID), errorMessage);
	}

	public float getVolume(IGuild guild) {
		AudioPlayer player = AudioPlayer.getAudioPlayerForGuild(guild);
		return player.getVolume();
	}

	public void trackEnded(AudioPlayer.Track oldTrack, Optional<AudioPlayer.Track> nextTrack, IGuild guild) {
		MusicPlayerHandler.getAudioPlayerForGuild(guild, this).onTrackEnded(oldTrack, nextTrack);
	}

	public void trackStarted(AudioPlayer.Track track, IGuild guild) {
		MusicPlayerHandler.getAudioPlayerForGuild(guild, this).onTrackStarted(track);
	}

	public void stopMusic(IGuild guild) {
		MusicPlayerHandler.getAudioPlayerForGuild(guild, this).stopMusic();
	}

	public OMusic getCurrentlyPlayingSong(IGuild guild) {
		return MusicPlayerHandler.getAudioPlayerForGuild(guild, this).getCurrentlyPlaying();
	}

	public List<IUser> getCurrentlyListening(IGuild guild) {
		return MusicPlayerHandler.getAudioPlayerForGuild(guild, this).getUsersInVoiceChannel();
	}

	public boolean playRandomSong(IGuild guild) {
		return MusicPlayerHandler.getAudioPlayerForGuild(guild, this).playRandomSong();
	}
}
package com.github.kaaz.emily.audio.commands.current;

import com.github.kaaz.emily.audio.configs.track.DurrationTimeConfig;
import com.github.kaaz.emily.command.AbstractCommand;
import com.github.kaaz.emily.command.ModuleLevel;
import com.github.kaaz.emily.command.annotations.Command;
import com.github.kaaz.emily.config.ConfigHandler;
import com.github.kaaz.emily.config.configs.guild.GuildActivePlaylistConfig;
import com.github.kaaz.emily.discordobjects.helpers.MessageMaker;
import com.github.kaaz.emily.discordobjects.helpers.guildaudiomanager.GuildAudioManager;
import com.github.kaaz.emily.discordobjects.wrappers.Guild;
import com.github.kaaz.emily.audio.Track;
import com.github.kaaz.emily.util.EmoticonHelper;
import com.github.kaaz.emily.util.FormatHelper;
import com.github.kaaz.emily.util.Time;

/**
 * Made by nija123098 on 5/24/2017.
 */
public class CurrentCommand extends AbstractCommand {
    private static final String NOTES = EmoticonHelper.getChars("notes");
    public CurrentCommand() {
        super("current", ModuleLevel.MUSIC, "playing, nowplaying, np", null, "Retrieves information about the song currently playing");
    }
    @Command
    public static void command(GuildAudioManager manager, Guild guild, MessageMaker maker){
        Track track = manager.currentTrack();
        if (track == null) {
            maker.append("Nothing is currently playing");
            return;
        }
        long time = manager.currentTime();
        maker.getAuthorName().appendRaw(NOTES + " " + track.getName());
        maker.appendRaw("[source](" + track.getSource() + ") - " + ConfigHandler.getSetting(GuildActivePlaylistConfig.class, guild).getName());
        maker.getNewFieldPart().withBoth("Duration", Time.getAbbreviatedMusic(ConfigHandler.getSetting(DurrationTimeConfig.class, track), time));
    }
    static String getPlayBar(boolean paused, long current, long total){
        int first = (int) ((float) current/total * 10);
        return EmoticonHelper.getChars(paused ? "pause_button" : "play_button")
                + FormatHelper.repeat('▬', first)
                + EmoticonHelper.getChars("radio_button")
                + FormatHelper.repeat('▬', 10 - first)
                + "[" + Time.getAbbreviatedMusic(total, current) + "]";
    }
}
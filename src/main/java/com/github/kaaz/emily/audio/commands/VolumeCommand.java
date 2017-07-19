package com.github.kaaz.emily.audio.commands;

import com.github.kaaz.emily.command.AbstractCommand;
import com.github.kaaz.emily.command.ContextType;
import com.github.kaaz.emily.command.ModuleLevel;
import com.github.kaaz.emily.command.annotations.Argument;
import com.github.kaaz.emily.command.annotations.Command;
import com.github.kaaz.emily.discordobjects.helpers.MessageMaker;
import com.github.kaaz.emily.discordobjects.helpers.guildaudiomanager.GuildAudioManager;
import com.github.kaaz.emily.discordobjects.wrappers.Guild;

/**
 * Made by nija123098 on 6/10/2017.
 */
public class VolumeCommand extends AbstractCommand {
    public VolumeCommand() {
        super("volume", ModuleLevel.MUSIC, "vol", null, "Gets or sets the volume of the music");
    }
    @Command// manager for requirement
    public void command(GuildAudioManager manager, Guild guild, @Argument(optional = true, replacement = ContextType.NONE) Integer value, MessageMaker maker){
        if (value == null) maker.append(manager.getVolume() + "").appendRaw("%");
        else manager.setVolume(value < 0 ? manager.getVolume() + value : value);
    }
}

package com.github.kaaz.emily.discordobjects.wrappers.event.events;

import com.github.kaaz.emily.discordobjects.wrappers.Channel;
import com.github.kaaz.emily.discordobjects.wrappers.Guild;
import com.github.kaaz.emily.discordobjects.wrappers.User;
import com.github.kaaz.emily.discordobjects.wrappers.VoiceChannel;
import com.github.kaaz.emily.discordobjects.wrappers.event.BotEvent;
import sx.blah.discord.handle.impl.events.UserVoiceChannelMoveEvent;

/**
 * Made by nija123098 on 5/4/2017.
 */
public class DiscordVoiceMove  implements BotEvent {
    private UserVoiceChannelMoveEvent event;
    public DiscordVoiceMove(UserVoiceChannelMoveEvent event) {
        this.event = event;
    }
    public User getUser(){
        return User.getUser(this.event.getUser());
    }
    public VoiceChannel getNewChannel(){
        return (VoiceChannel) Channel.getChannel(this.event.getNewChannel());
    }
    public VoiceChannel getOldChannel(){
        return (VoiceChannel) Channel.getChannel(this.event.getOldChannel());
    }
    public Guild getGuild(){
        return Guild.getGuild(this.event.getGuild());
    }
}

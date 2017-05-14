package com.github.kaaz.emily.discordobjects.wrappers.event.events;

import com.github.kaaz.emily.discordobjects.wrappers.Guild;
import com.github.kaaz.emily.discordobjects.wrappers.User;
import com.github.kaaz.emily.discordobjects.wrappers.VoiceChannel;
import com.github.kaaz.emily.discordobjects.wrappers.event.BotEvent;
import sx.blah.discord.handle.impl.events.UserVoiceChannelJoinEvent;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.IVoiceChannel;

/**
 * Made by nija123098 on 5/4/2017.
 */
public class DiscordVoiceJoin implements BotEvent {
    private VoiceChannel channel;
    private User user;
    private boolean unique;
    public DiscordVoiceJoin(UserVoiceChannelJoinEvent event) {
        this.channel = VoiceChannel.getVoiceChannel(event.getVoiceChannel());
        this.user = User.getUser(event.getUser());
        this.unique = false;
    }
    public DiscordVoiceJoin(IVoiceChannel channel, IUser user) {
        this.channel = VoiceChannel.getVoiceChannel(channel);
        this.user = User.getUser(user);
        this.unique = true;
    }
    public User getUser(){
        return this.user;
    }
    public VoiceChannel getChannel(){
        return this.channel;
    }
    public Guild getGuild(){
        return this.channel.getGuild();
    }
    public boolean isUnique(){
        return this.unique;
    }
}

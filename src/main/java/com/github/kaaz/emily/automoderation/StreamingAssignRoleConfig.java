package com.github.kaaz.emily.automoderation;

import com.github.kaaz.emily.config.AbstractConfig;
import com.github.kaaz.emily.config.ConfigHandler;
import com.github.kaaz.emily.discordobjects.wrappers.DiscordClient;
import com.github.kaaz.emily.discordobjects.wrappers.Guild;
import com.github.kaaz.emily.discordobjects.wrappers.Role;
import com.github.kaaz.emily.discordobjects.wrappers.event.EventListener;
import com.github.kaaz.emily.discordobjects.wrappers.event.events.DiscordPresenceUpdate;
import com.github.kaaz.emily.exeption.ArgumentException;
import com.github.kaaz.emily.perms.BotRole;

import java.util.HashSet;
import java.util.Set;

/**
 * Made by nija123098 on 6/19/2017.
 */
public class StreamingAssignRoleConfig extends AbstractConfig<Role, Guild>{
    public StreamingAssignRoleConfig() {
        super("streaming_role", BotRole.GUILD_TRUSTEE, null, "The role to assign a streaming user");
    }
    @EventListener
    public void handle(DiscordPresenceUpdate event){
        if (event.getNewPresence().getOptionalStreamingUrl().isPresent()) {
            rolesForGuilds(event.getUser().getGuilds()).forEach(role -> {
                try{event.getUser().addRole(role);
                } catch (Exception ignored){}// todo role order moved
            });
        } else {
            rolesForGuilds(event.getUser().getGuilds()).forEach(role -> {
                try{event.getUser().removeRole(role);
                } catch (Exception ignored){}// more role order moved here
            });
        }
    }
    private static Set<Role> rolesForGuilds(Set<Guild> guilds){
        Set<Role> roles = new HashSet<>(guilds.size() / 8);
        guilds.forEach(guild -> {
            Role role = ConfigHandler.getSetting(StreamingAssignRoleConfig.class, guild);
            if (role != null) roles.add(role);
        });
        return roles;
    }
    @Override
    protected void validateInput(Guild configurable, Role role) {
        if (role.getPosition() < DiscordClient.getOurUser().getRolesForGuild(configurable).get(0).getPosition()) throw new ArgumentException("For me to assign roles I must have a higher role");
    }
}

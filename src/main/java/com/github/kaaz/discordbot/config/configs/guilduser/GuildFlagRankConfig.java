package com.github.kaaz.discordbot.config.configs.guilduser;

import com.github.kaaz.discordbot.config.AbstractMultiConfig;
import com.github.kaaz.discordbot.perms.BotRole;

import java.util.Collections;

/**
 * Made by nija123098 on 2/22/2017.
 */
public class GuildFlagRankConfig extends AbstractMultiConfig {
    public GuildFlagRankConfig() {
        super("guild_flag_ranks", BotRole.GUILD_OWNER, Collections.singletonList("none"), "Flags available to guild specific bot roles");
    }
}

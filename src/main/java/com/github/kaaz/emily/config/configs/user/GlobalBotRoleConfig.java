package com.github.kaaz.emily.config.configs.user;

import com.github.kaaz.emily.config.AbstractConfig;
import com.github.kaaz.emily.discordobjects.wrappers.User;
import com.github.kaaz.emily.perms.BotRole;

import java.util.HashSet;
import java.util.Set;

/**
 * Made by nija123098 on 2/22/2017.
 */
public class GlobalBotRoleConfig extends AbstractConfig<Set<String>, User> {
    public GlobalBotRoleConfig() {
        super("global_flag_ranks", BotRole.BOT_OWNER, new HashSet<>(0), "The config for if an user is a contributor to the bot");
    }
}
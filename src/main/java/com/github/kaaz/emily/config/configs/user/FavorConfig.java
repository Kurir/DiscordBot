package com.github.kaaz.emily.config.configs.user;

import com.github.kaaz.emily.config.AbstractConfig;
import com.github.kaaz.emily.perms.BotRole;

/**
 * Made by nija123098 on 2/22/2017.
 */
public class FavorConfig extends AbstractConfig<Float> {
    public FavorConfig() {
        super("favor_amount", BotRole.BOT_ADMIN, 0F, "How much the bot likes you");
    }
}
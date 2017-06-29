package com.github.kaaz.emily.audio.configs.track;

import com.github.kaaz.emily.config.AbstractConfig;
import com.github.kaaz.emily.config.GlobalConfigurable;
import com.github.kaaz.emily.perms.BotRole;

/**
 * Made by nija123098 on 3/28/2017.
 */
public class TrackDeleteTimeConfig extends AbstractConfig<Long, GlobalConfigurable>{
    public TrackDeleteTimeConfig() {
        super("track_delete_time", BotRole.BOT_ADMIN, 432000000L,
                "The time it takes for a song to not be used enough to be considered for file deletion");
    }
}
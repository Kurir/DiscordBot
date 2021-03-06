/*
 * Copyright 2017 github.com/kaaz
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package emily.handler;

import emily.db.WebDb;
import emily.db.controllers.CGuild;
import emily.db.model.OGuild;
import emily.guildsettings.AbstractGuildSetting;
import emily.guildsettings.DefaultGuildSettings;
import emily.guildsettings.music.SettingMusicRole;
import emily.permission.SimpleRank;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Guild specific configurations, such as which channel is for music
 */
public class GuildSettings {
    private final static Map<Long, GuildSettings> settingInstance = new ConcurrentHashMap<>();
    private final Map<String, String> settings;
    private final long guildId;
    private int id = 0;
    private boolean initialized = false;

    private GuildSettings(Long guild) {
        this.settings = new ConcurrentHashMap<>();
        OGuild record = CGuild.findBy(guild);
        if (record.id == 0) {
            record.name = String.valueOf(guild);
            record.discord_id = guild;
            record.owner = 1;
            CGuild.insert(record);
        }
        this.guildId = guild;
        this.id = record.id;
        settingInstance.put(guild, this);
        loadSettings();
    }

    /**
     * Simplified method to get the setting for a channel instead of guild
     *
     * @param channel      the channel to check
     * @param settingClass the Setting
     * @return the setting
     */
    public static String getFor(MessageChannel channel, Class<? extends AbstractGuildSetting> settingClass) {
        if (channel != null && channel instanceof TextChannel) {
            return GuildSettings.get(((TextChannel) channel).getGuild()).getOrDefault(settingClass);
        }
        return DefaultGuildSettings.getDefault(settingClass);
    }

    public static void remove(String guildId) {
        if (settingInstance.containsKey(Long.parseLong(guildId))) {
            settingInstance.remove(Long.parseLong(guildId));
        }
    }

    public static GuildSettings get(Guild guild) {
        return get(Long.parseLong(guild.getId()));
    }

    public static GuildSettings get(String guildId) {
        return get(Long.parseLong(guildId));
    }

    public static GuildSettings get(long guild) {
        if (settingInstance.containsKey(guild)) {
            return settingInstance.get(guild);
        } else {
            return new GuildSettings(guild);
        }
    }

    /**
     * @param clazz class to search
     * @return the setting or default value
     */
    public String getOrDefault(Class<? extends AbstractGuildSetting> clazz) {
        return getOrDefault(DefaultGuildSettings.getKey(clazz));
    }

    public String getOrDefault(String key) {
        return settings.get(key);
    }

    /**
     * (re-)loads settings for guild
     */
    private void loadSettings() {
        if (initialized || id <= 0) {
            return;
        }
        settings.clear();
        Map<String, AbstractGuildSetting> defaults = DefaultGuildSettings.getDefaults();
        for (String key : defaults.keySet()) {
            settings.put(key, defaults.get(key).getDefault());
        }
        try (ResultSet rs = WebDb.get().select(
                "SELECT name, config " +
                        "FROM guild_settings s " +
                        "WHERE guild = ? ", id)) {
            while (rs.next()) {
                String key = rs.getString("name");
                String value = rs.getString("config");
                if (defaults.containsKey(key)) {
                    if (null != value && !value.isEmpty()) {
                        settings.put(key, value);
                    }
                }
            }
            rs.getStatement().close();
            initialized = true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public String[] getDescription(Class<? extends AbstractGuildSetting> settingClass) {
        return getDescription(DefaultGuildSettings.getKey(settingClass));
    }

    public String[] getDescription(String key) {
        if (DefaultGuildSettings.isValidKey(key)) {
            return DefaultGuildSettings.get(key).getDescription();
        }
        return new String[]{};
    }

    public String getSettingsType(String key) {
        return DefaultGuildSettings.get(key).getSettingType();
    }

    public String getDisplayValue(Guild guild, String key) {
        return DefaultGuildSettings.get(key).toDisplay(guild, getOrDefault(key));
    }

    public boolean set(Guild guild, Class<? extends AbstractGuildSetting> settingClass, String value) {
        return set(guild, DefaultGuildSettings.getKey(settingClass), value);
    }

    public boolean set(Guild guild, String key, String value) {
        if (DefaultGuildSettings.isValidKey(key) && DefaultGuildSettings.get(key).isValidValue(guild, value)) {
            try {
                String dbValue = DefaultGuildSettings.get(key).getValue(guild, value);
                WebDb.get().insert("INSERT INTO guild_settings (guild, name, config) VALUES(?, ?, ?) " +
                        "ON DUPLICATE KEY UPDATE config=?", id, key, dbValue, dbValue);
                settings.put(key, dbValue);
                return true;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public Map<String, String> getSettings() {
        return settings;
    }

    public String getDefaultValue(String key) {
        if (DefaultGuildSettings.isValidKey(key)) {
            return DefaultGuildSettings.get(key).getDefault();
        }
        return "";
    }

    public synchronized void reset() {
        try {
            WebDb.get().query("DELETE FROM guild_settings WHERE guild = ? ", id);
            initialized = false;
            loadSettings();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean canUseMusicCommands(User user, SimpleRank userRank) {
        String requiredRole = getOrDefault(SettingMusicRole.class);
        boolean roleFound = true;
        if (!requiredRole.isEmpty() && !"false".equals(requiredRole) && !userRank.isAtLeast(SimpleRank.GUILD_ADMIN)) {
            roleFound = false;
            List<Role> roles = user.getJDA().getGuildById(Long.toString(guildId)).getMember(user).getRoles();
            for (Role role : roles) {
                if (role.getId().equals(requiredRole)) {
                    roleFound = true;
                    break;
                }
            }
        }
        return roleFound;
    }
}
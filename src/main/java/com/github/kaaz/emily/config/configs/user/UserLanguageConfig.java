package com.github.kaaz.emily.config.configs.user;

import com.github.kaaz.emily.config.AbstractConfig;
import com.github.kaaz.emily.discordobjects.wrappers.User;
import com.github.kaaz.emily.exeption.ArgumentException;
import com.github.kaaz.emily.perms.BotRole;
import com.github.kaaz.emily.util.LangString;

/**
 * Made by nija123098 on 3/18/2017.
 */
public class UserLanguageConfig extends AbstractConfig<String, User> {
    public UserLanguageConfig() {
        super("user_language", BotRole.USER, null, "The language the bot uses to communicate with the user");
    }
    @Override
    protected void validateInput(User configurable, String v) {
        if (!LangString.isLangCode(v) && !LangString.isLangName(v)) throw new ArgumentException("Please input a valid language code or name");
    }
    @Override
    public String wrapTypeIn(String e, User configurable) {
        return LangString.isLangCode(e) ? e : LangString.getLangCode(e);
    }
    @Override
    public String wrapTypeOut(String s, User configurable) {
        return LangString.getLangName(s);
    }

    @Override
    public boolean checkDefault() {
        return false;
    }
}

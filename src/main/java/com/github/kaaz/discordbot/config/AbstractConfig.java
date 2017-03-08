package com.github.kaaz.discordbot.config;

import com.github.kaaz.discordbot.perms.BotRole;

/**
 * Made by nija123098 on 2/20/2017.
 */
public class AbstractConfig<E> {
    private E defaul;
    private String name, description;
    private BotRole botRole;
    private ConfigLevel configLevel;
    public AbstractConfig(String name, BotRole botRole, E defaul, String description) {
        this.name = name;
        this.botRole = botRole;
        this.defaul = defaul;
        this.description = description;
    }

    /**
     * A standard getter
     *
     * @return the name of the config
     */
    public String getName() {
        return this.name;
    }

    /**
     * A standard getter
     *
     * @return the multi-line config description
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * A standard getter.
     *
     * @return the bot role required for altering this config value
     */
    public BotRole requiredBotRole() {
        return this.botRole;
    }

    /**
     * A standard getter
     *
     * @return the default value of this config
     */
    public E getDefault(){
        return this.defaul;
    }
    void setConfigLevel(ConfigLevel level){
        this.configLevel = level;
    }
    public ConfigLevel getConfigLevel(){
        return this.configLevel;
    }
    // TODO SQL stuff goes here, more or less
    void setValue(Configurable configurable, E value){

    }

    /**
     * Gets the value for the given value.
     *
     * @param configurable the configurable that the
     *                     setting is being gotten for
     * @return the config's value
     */
    E getValue(Configurable configurable){
        return null;
    }
}
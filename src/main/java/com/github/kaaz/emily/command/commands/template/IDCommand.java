package com.github.kaaz.emily.command.commands.template;

import com.github.kaaz.emily.command.AbstractCommand;
import com.github.kaaz.emily.command.anotations.Command;
import com.github.kaaz.emily.command.anotations.Convert;
import com.github.kaaz.emily.config.Configurable;

/**
 * Made by nija123098 on 4/25/2017.
 */
public class IDCommand extends AbstractCommand {
    public IDCommand() {
        super("id", null, null);
    }
    @Command
    public String command(@Convert Configurable configurable){
        return configurable.getID();
    }
}

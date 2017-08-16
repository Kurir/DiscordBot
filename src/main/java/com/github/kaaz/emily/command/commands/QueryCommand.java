package com.github.kaaz.emily.command.commands;

import com.github.kaaz.emily.command.AbstractCommand;
import com.github.kaaz.emily.command.ModuleLevel;
import com.github.kaaz.emily.command.annotations.Argument;
import com.github.kaaz.emily.command.annotations.Command;
import com.github.kaaz.emily.config.Database;
import com.github.kaaz.emily.discordobjects.helpers.MessageMaker;
import com.github.kaaz.emily.util.FormatHelper;
import com.github.kaaz.emily.util.HastebinUtil;

import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Kaaz
 */
public class QueryCommand extends AbstractCommand {
    public QueryCommand() {
        super("query", ModuleLevel.DEVELOPMENT, null, null, "Gets data from the db");
    }
    @Command
    public void command(@Argument String queary, MessageMaker maker){
        if (queary == null || queary.isEmpty()) {
            maker.append("You should know this requires a SQL code argument");
            return;
        }
        if (queary.startsWith("select")) queary += "query ";
        String output = Database.select(queary, r -> {
            List<String> header = new ArrayList<>();
            List<List<String>> table = new ArrayList<>();
            ResultSetMetaData metaData = r.getMetaData();
            int columnsCount = metaData.getColumnCount();
            for (int i = 0; i < columnsCount; i++) {
                header.add(metaData.getColumnName(i + 1));
            }
            while (r.next()) {
                List<String> row = new ArrayList<>();
                for (int i = 0; i < columnsCount; i++) {
                    String s = String.valueOf(r.getString(i + 1)).trim();
                    row.add(s.substring(0, Math.min(30, s.length())));
                }
                table.add(row);
            }
            return FormatHelper.makeAsciiTable(header, table, null);
        });
        maker.appendRaw(output.length() < 2000 ? output : HastebinUtil.handleHastebin(output));
    }
}

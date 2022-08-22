package com.crazy.scientist.crazyjavascientist.config;

import com.crazy.scientist.crazyjavascientist.commands.CommandManager;
import com.crazy.scientist.crazyjavascientist.commands.FeedBackCommand;
import com.crazy.scientist.crazyjavascientist.commands.HelpMessage;
import com.crazy.scientist.crazyjavascientist.listeners.EventListeners;
import io.github.cdimascio.dotenv.Dotenv;
import lombok.Data;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.AccountType;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;
import net.dv8tion.jda.internal.requests.Route;
import org.springframework.beans.factory.annotation.Value;

import javax.security.auth.login.LoginException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Data
public class DiscordBotConfigJDAStyle {

    private final Dotenv config;

    private final ShardManager shardManager ;
    public DiscordBotConfigJDAStyle() throws LoginException {

        config = Dotenv.configure().load();

        DefaultShardManagerBuilder builder = DefaultShardManagerBuilder.createDefault(config.get("TOKEN"));
        builder.setStatus(OnlineStatus.ONLINE);
        builder.setActivity(Activity.watching("Yo Momma Pole Dance"));


       shardManager = builder.build();

       shardManager.addEventListener(new CommandManager(), new EventListeners());


    }

}

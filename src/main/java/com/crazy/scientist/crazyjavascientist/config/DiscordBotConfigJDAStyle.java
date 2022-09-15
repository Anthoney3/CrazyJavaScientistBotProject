package com.crazy.scientist.crazyjavascientist.config;

import com.crazy.scientist.crazyjavascientist.osu.OAuthToken;
import com.crazy.scientist.crazyjavascientist.commands.CommandManager;
import com.crazy.scientist.crazyjavascientist.commands.Greetings;
import com.crazy.scientist.crazyjavascientist.listeners.MessageEventListeners;
import com.crazy.scientist.crazyjavascientist.osu.OsuApiCall;
import io.github.cdimascio.dotenv.Dotenv;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.managers.GuildManager;
import net.dv8tion.jda.api.managers.Manager;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.security.auth.login.LoginException;
import java.io.IOException;

@Slf4j
@Data
@Component
public class DiscordBotConfigJDAStyle {

    private Dotenv config;

    private ShardManager shardManager ;

    @Autowired
    private OAuthToken oAuthToken;

    @Autowired
    private CommandManager commandManager;

    @Autowired
    private MessageEventListeners messageEventListeners;

    @Autowired
    private Greetings greetings;

    @Autowired
    private OsuApiCall osuApiCall;

    public  void init() throws IOException, LoginException {

        config = Dotenv.configure().load();

        DefaultShardManagerBuilder builder = DefaultShardManagerBuilder.createDefault(config.get("TOKEN"));
        builder.setStatus(OnlineStatus.ONLINE);
        builder.setActivity(Activity.watching("Yo Momma Pole Dance"));
        builder.enableIntents(GatewayIntent.GUILD_MEMBERS,GatewayIntent.GUILD_MESSAGES,GatewayIntent.GUILD_MESSAGE_TYPING,GatewayIntent.GUILD_PRESENCES);


       shardManager = builder.build();

        shardManager.addEventListener(commandManager, messageEventListeners, greetings);




        oAuthToken.getOsuOAuthToken(shardManager);
        oAuthToken.renewOsuOAuthToken(shardManager);
        osuApiCall.populateDBOnStartWithOsuRecords();

    }


}

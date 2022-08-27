package com.crazy.scientist.crazyjavascientist.config;

import com.crazy.scientist.crazyjavascientist.osu.OAuthToken;
import com.crazy.scientist.crazyjavascientist.commands.CommandManager;
import com.crazy.scientist.crazyjavascientist.commands.Greetings;
import com.crazy.scientist.crazyjavascientist.listeners.MessageEventListeners;
import io.github.cdimascio.dotenv.Dotenv;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;

import javax.security.auth.login.LoginException;
import java.io.IOException;

@Slf4j
@Data
public class DiscordBotConfigJDAStyle {

    private final Dotenv config;

    private final ShardManager shardManager ;

    private OAuthToken oAuthToken = new OAuthToken();


    public DiscordBotConfigJDAStyle() throws LoginException, IOException {

        config = Dotenv.configure().load();

        DefaultShardManagerBuilder builder = DefaultShardManagerBuilder.createDefault(config.get("TOKEN"));
        builder.setStatus(OnlineStatus.ONLINE);
        builder.setActivity(Activity.watching("Yo Momma Pole Dance"));
        builder.enableIntents(GatewayIntent.GUILD_MEMBERS,GatewayIntent.GUILD_MESSAGES,GatewayIntent.GUILD_MESSAGE_TYPING);


       shardManager = builder.build();

       shardManager.addEventListener(new CommandManager(), new MessageEventListeners(), new Greetings());

       oAuthToken.getOsuOAuthToken(shardManager);
       oAuthToken.renewOsuOAuthToken(shardManager);

/*

           while(shardManager.getUserById(1010627603352256543L).getJDA().getStatus() == "")
           new OAuthToken().renewOsuOAuthToken(shardManager);
*/



    }

}

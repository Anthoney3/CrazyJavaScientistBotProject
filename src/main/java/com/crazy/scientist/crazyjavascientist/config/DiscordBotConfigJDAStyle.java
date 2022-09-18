package com.crazy.scientist.crazyjavascientist.config;

import com.crazy.scientist.crazyjavascientist.osu.OAuthToken;
import com.crazy.scientist.crazyjavascientist.commands.CommandManager;
import com.crazy.scientist.crazyjavascientist.commands.Greetings;
import com.crazy.scientist.crazyjavascientist.listeners.MessageEventListeners;
import com.crazy.scientist.crazyjavascientist.osu.OsuApiCall;
import io.github.cdimascio.dotenv.Dotenv;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.managers.GuildManager;
import net.dv8tion.jda.api.managers.Manager;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Data
@Component
public class DiscordBotConfigJDAStyle {

    private Dotenv config;

    private static ShardManager shardManager ;

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
        builder.setActivity(Activity.watching("Porn"));
        builder.enableIntents(GatewayIntent.GUILD_MEMBERS,GatewayIntent.GUILD_MESSAGES,GatewayIntent.GUILD_MESSAGE_TYPING,GatewayIntent.GUILD_PRESENCES);

        shardManager = builder.build();

        shardManager.addEventListener(commandManager, messageEventListeners, greetings);

        oAuthToken.getOsuOAuthToken(shardManager);
        oAuthToken.renewOsuOAuthToken(shardManager);
        osuApiCall.populateDBOnStartWithOsuRecords();

    }


    @Scheduled(cron = "0 16 22 *  * FRI")
    public void checkForMembersInVoiceChannel() {

        List<Member> membersInsideVoiceChannel = new ArrayList<>();
        List<Member> guildMembers = new ArrayList<>();
        List<Member> peopleNotInsideVoiceChannel = new ArrayList<>();




       for(int i=0; i < shardManager.getGuildById(939244115722371072L).getMembers().size();i++){

           if(!shardManager.getGuildById(939244115722371072L).getMembers().get(i).getRoles().get(i).getName().equalsIgnoreCase("bot")){

               guildMembers.add(shardManager.getGuildById(939244115722371072L).getMembers().get(i));
           }

       }

       for(int j=0;j < shardManager.getGuildById(939244115722371072L).getVoiceChannelById(939244115722371077L).getMembers().size(); j++){

           if(!shardManager.getGuildById(939244115722371072L).getVoiceChannelById(939244115722371077L).getMembers().get(j).getRoles().get(j).getName().equalsIgnoreCase("bot")){

               membersInsideVoiceChannel.add(shardManager.getGuildById(939244115722371072L).getVoiceChannelById(939244115722371077L).getMembers().get(j));

           }
       }
        log.info(guildMembers.toString());
        log.info(membersInsideVoiceChannel.toString());
        log.info(String.valueOf(membersInsideVoiceChannel.size()));


        if(guildMembers.size() == membersInsideVoiceChannel.size()) {


            shardManager.getUserById(416342612484554752L).openPrivateChannel().queue(channel -> {
                channel.sendMessage("ALL PEOPLE ARE HEREEEEE!").queue();
            });


        }else if (!membersInsideVoiceChannel.isEmpty()) {

            for(int i =0; i < guildMembers.size();i++){

                for(int j=0; j < membersInsideVoiceChannel.size();j++){
                    if(!guildMembers.get(i).getEffectiveName().equalsIgnoreCase(membersInsideVoiceChannel.get(j).getEffectiveName())){
                        peopleNotInsideVoiceChannel.add(guildMembers.get(i));
                    }
                }
            }

            log.info(peopleNotInsideVoiceChannel.toString());

            EmbedBuilder builder = new EmbedBuilder();
            builder.setTitle("Users Not In " + shardManager.getGuildById(939244115722371072L).getJDA().getVoiceChannelById(939244115722371077L).getName() + " Channel");

            for (Member member : peopleNotInsideVoiceChannel) {

                builder.addField(member.getEffectiveName(),"", false);
            }

            MessageEmbed messageEmbed = builder.build();
            shardManager.getUserById(416342612484554752L).openPrivateChannel().queue(channel -> {
                channel.sendMessageEmbeds(messageEmbed).queue();
            });
        } else {

            EmbedBuilder builder = new EmbedBuilder();

            builder.setTitle("Users Who need to Join");

            for(Member member : guildMembers){

                builder.addField(member.getEffectiveName(),"",false );
            }

            MessageEmbed messageEmbed = builder.build();
            shardManager.getUserById(416342612484554752L).openPrivateChannel().queue(channel -> {
                channel.sendMessageEmbeds(messageEmbed).queue();
            });
        }
    }


}

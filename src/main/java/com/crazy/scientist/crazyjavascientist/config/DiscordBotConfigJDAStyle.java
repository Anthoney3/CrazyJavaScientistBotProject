package com.crazy.scientist.crazyjavascientist.config;

import com.crazy.scientist.crazyjavascientist.commands.CommandManager;
import com.crazy.scientist.crazyjavascientist.commands.Greetings;
import com.crazy.scientist.crazyjavascientist.dnd.DNDTesting;
import com.crazy.scientist.crazyjavascientist.dnd.dnd_repos.DNDAttendanceRepo;
import com.crazy.scientist.crazyjavascientist.listeners.MessageEventListeners;
import com.crazy.scientist.crazyjavascientist.osu.api.osu_repos.OsuApiModelI;
import com.crazy.scientist.crazyjavascientist.osu.api.osu_services.OsuUtils;
import com.crazy.scientist.crazyjavascientist.osu.api.osu_utils.OAuthToken;
import com.crazy.scientist.crazyjavascientist.osu.api.osu_utils.OsuApiCall;
import com.jagrosh.jdautilities.command.CommandClientBuilder;
import io.github.cdimascio.dotenv.Dotenv;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static com.crazy.scientist.crazyjavascientist.config.StaticUtils.*;

@Slf4j
@Getter
@Setter
@ToString
@Component
public class DiscordBotConfigJDAStyle {


    @Autowired
    private  OAuthToken o_auth_token;
    @Autowired
    private  CommandManager command_manager;
    @Autowired
    private  MessageEventListeners message_event_listeners;
    @Autowired
    private  Greetings greet;
    @Autowired
    private  OsuApiModelI osu_api_model_interface;
    @Autowired
    private  OsuApiCall osu_api_call;
    @Autowired
    private  OsuUtils osu_utils;
    @Autowired
    private  DNDTesting dnd_testing;
    @Autowired
    private  DNDAttendanceRepo attendance_repo;

    public void init() throws IOException, LoginException {

        config = Dotenv.configure().load();



        DefaultShardManagerBuilder builder = DefaultShardManagerBuilder.createDefault(config.get("TOKEN"));
        builder.setStatus(OnlineStatus.ONLINE);
        builder.setActivity(Activity.watching("TV Static"));
        builder.setMemberCachePolicy(MemberCachePolicy.ALL);
        builder.enableIntents(GatewayIntent.GUILD_MEMBERS,GatewayIntent.GUILD_MESSAGES,GatewayIntent.GUILD_MESSAGE_TYPING,GatewayIntent.GUILD_PRESENCES);

        shardManager = builder.build();

        shardManager.addEventListener(command_manager,message_event_listeners, greet,dnd_testing);

        o_auth_token.getOsuOAuthToken(shardManager);

        if(osu_api_model_interface.getAllMemberInfo().isEmpty()) {
            osu_utils.populateDBOnStartWithOsuRecords(shardManager);
        }

        //Updates Current Week to the
        List<ZonedDateTime> current_week_dates = new ArrayList<>();



        Calendar calendar = Calendar.getInstance();
        ZonedDateTime current_week = ZonedDateTime.now();
        String log_text = "Current Week Updated to : ";

        switch(calendar.get(Calendar.DAY_OF_WEEK)){

            case Calendar.MONDAY ->  {
                attendance_repo.updateCurrentWeek(current_week.format(DateTimeFormatter.ofPattern("MM/dd/yyyy")));
                log.info("{}{}",log_text,current_week.format(DateTimeFormatter.ofPattern("MM/dd/yyyy")));
                break;
            }
            case Calendar.TUESDAY ->  {
                attendance_repo.updateCurrentWeek(current_week.minusDays(1).format(DateTimeFormatter.ofPattern("MM/dd/yyyy")));
                log.info("{}{}",log_text,current_week.minusDays(1).format(DateTimeFormatter.ofPattern("MM/dd/yyyy")));
                break;
            }
            case Calendar.WEDNESDAY ->  {
                attendance_repo.updateCurrentWeek(current_week.minusDays(2).format(DateTimeFormatter.ofPattern("MM/dd/yyyy")));
                log.info("{}{}",log_text,current_week.minusDays(2).format(DateTimeFormatter.ofPattern("MM/dd/yyyy")));
                break;
            }
            case Calendar.THURSDAY->  {
                attendance_repo.updateCurrentWeek(current_week.minusDays(3).format(DateTimeFormatter.ofPattern("MM/dd/yyyy")));
                log.info("{}{}",log_text,current_week.minusDays(3).format(DateTimeFormatter.ofPattern("MM/dd/yyyy")));
                break;
            }
            case Calendar.FRIDAY ->  {
                attendance_repo.updateCurrentWeek(current_week.minusDays(4).format(DateTimeFormatter.ofPattern("MM/dd/yyyy")));
                log.info("{}{}",log_text,current_week.minusDays(4).format(DateTimeFormatter.ofPattern("MM/dd/yyyy")));
                break;
            }
            case Calendar.SATURDAY ->  {
                attendance_repo.updateCurrentWeek(current_week.minusDays(5).format(DateTimeFormatter.ofPattern("MM/dd/yyyy")));
                log.info("{}{}",log_text,current_week.minusDays(5).format(DateTimeFormatter.ofPattern("MM/dd/yyyy")));
                break;
            }
            case Calendar.SUNDAY ->  {
                attendance_repo.updateCurrentWeek(current_week.minusDays(6).format(DateTimeFormatter.ofPattern("MM/dd/yyyy")));
                log.info("{}{}",log_text,current_week.minusDays(6).format(DateTimeFormatter.ofPattern("MM/dd/yyyy")));
                break;
            }

        }


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

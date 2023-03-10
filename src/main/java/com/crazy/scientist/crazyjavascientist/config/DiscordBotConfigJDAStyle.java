package com.crazy.scientist.crazyjavascientist.config;

import com.crazy.scientist.crazyjavascientist.commands.CommandManager;
import com.crazy.scientist.crazyjavascientist.commands.Greetings;
import com.crazy.scientist.crazyjavascientist.dnd.DNDService;
import com.crazy.scientist.crazyjavascientist.dnd.dnd_entities.CurrentWeekOfEntity;
import com.crazy.scientist.crazyjavascientist.dnd.dnd_entities.DNDAttendanceEntity;
import com.crazy.scientist.crazyjavascientist.dnd.dnd_entities.PlayerResponse;
import com.crazy.scientist.crazyjavascientist.dnd.dnd_repos.CurrentWeekOfRepo;
import com.crazy.scientist.crazyjavascientist.dnd.dnd_repos.DNDAttendanceRepo;
import com.crazy.scientist.crazyjavascientist.dnd.dnd_repos.DNDPlayersRepo;
import com.crazy.scientist.crazyjavascientist.dnd.enums.UnicodeResponses;
import com.crazy.scientist.crazyjavascientist.listeners.MessageEventListeners;
import com.crazy.scientist.crazyjavascientist.security.EncryptorAESGCM;
import com.crazy.scientist.crazyjavascientist.security.entities.CJSConfigEntity;
import com.crazy.scientist.crazyjavascientist.security.repos.CJSConfigRepo;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.crazy.scientist.crazyjavascientist.constants.StaticUtils.shardManager;

@Slf4j
@Getter
@Setter
@ToString
@Component
public class DiscordBotConfigJDAStyle {

    private CommandManager command_manager;
    private MessageEventListeners message_event_listeners;
    private Greetings greet;
    private DNDService dnd_testing;
    private CurrentWeekOfRepo currentWeekOfRepo;
    private DNDPlayersRepo dndPlayersRepo;
    private DNDAttendanceRepo dndAttendanceRepo;
    private EncryptorAESGCM encryptor;
    private CJSConfigRepo cjsConfigRepo;
    private PopulateAuthenticationInformation populateAuthenticationInformation;

    public DiscordBotConfigJDAStyle(CommandManager command_manager, MessageEventListeners message_event_listeners, Greetings greet, DNDService dnd_testing,
                                    CurrentWeekOfRepo currentWeekOfRepo, DNDPlayersRepo dndPlayersRepo, DNDAttendanceRepo dndAttendanceRepo,
                                    EncryptorAESGCM encryptor, CJSConfigRepo cjsConfigRepo, PopulateAuthenticationInformation populateAuthenticationInformation) {
        this.command_manager = command_manager;
        this.message_event_listeners = message_event_listeners;
        this.greet = greet;
        this.dnd_testing = dnd_testing;
        this.currentWeekOfRepo = currentWeekOfRepo;
        this.dndPlayersRepo = dndPlayersRepo;
        this.dndAttendanceRepo = dndAttendanceRepo;
        this.encryptor = encryptor;
        this.cjsConfigRepo = cjsConfigRepo;
        this.populateAuthenticationInformation = populateAuthenticationInformation;
    }

    @Value("${aes.info}")
    private String aes_info;

    public static HashMap<String, String> auth_info;

    public void init() throws Exception {
        try {
            //            Only Used On Local
//            if (cjsConfigRepo.count() == 0) populateAuthenticationInformation.populateAuthenticaitonInformation();
            getSetupInformation();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        DefaultShardManagerBuilder builder = DefaultShardManagerBuilder.createDefault(encryptor.decrypt(auth_info.get("BOT"), aes_info));
        builder.setStatus(OnlineStatus.ONLINE);
        builder.setActivity(Activity.watching("TV Static"));
        builder.setMemberCachePolicy(MemberCachePolicy.ALL);
        builder.enableIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_MESSAGE_TYPING, GatewayIntent.GUILD_PRESENCES);

        shardManager = builder.build();

        shardManager.addEventListener(command_manager, message_event_listeners, greet, dnd_testing);

        int current_day = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
        ZonedDateTime current_week = ZonedDateTime.now();

        CurrentWeekOfEntity currentWeekOf;

        if (current_day != Calendar.MONDAY) {
            int days_to_be_sent_back = Math.abs(((current_day == 1) ? 8 : current_day) - Calendar.MONDAY);
            currentWeekOf = new CurrentWeekOfEntity(current_week.minusDays(days_to_be_sent_back).format(DateTimeFormatter.ofPattern("dd-MMMM-yyyy")));
        } else {
            currentWeekOf = new CurrentWeekOfEntity(current_week.format(DateTimeFormatter.ofPattern("dd-MMMM-yyyy")));
        }

        if (currentWeekOfRepo.count() != 0) {
            currentWeekOfRepo.update_current_week_of(currentWeekOf.getCurrent_week());
        } else {
            currentWeekOfRepo.save(currentWeekOf);
        }

        log.info("Current Week Updated to :{}", current_week.format(DateTimeFormatter.ofPattern("dd-MMMM-yyyy")));

        if (!new File("./logs/cjs.log").exists()) {
            log.info("Log Directory Not Found...Attempting to create new log directory");
            log.info("{}", (new File("./logs").mkdir()) ? "New Log Directory Created Successfully" : "Log Directory Creation Failed");
        }
    }

    void getSetupInformation() {
        List<CJSConfigEntity> cjs_entites = cjsConfigRepo.findAll().stream().filter(item -> item.getStatus().equalsIgnoreCase("a")).toList();
        auth_info = new HashMap<>(cjs_entites.stream().collect(Collectors.toMap(CJSConfigEntity::getShort_name, CJSConfigEntity::getKey_value)));
        HashMap<Long, PlayerResponse> discord_response = new HashMap<>();
        new HashMap<>(dndAttendanceRepo.findAll().stream().collect(Collectors.toMap(DNDAttendanceEntity::getDiscord_id, Function.identity()))).forEach((k, v) -> {
            if (v.getNo_show().equalsIgnoreCase("y"))
                discord_response.put(k, new PlayerResponse(v.getPlayers_name(), UnicodeResponses.NO_SHOW_NO_RESPONSE));
            if (v.getExcused().equalsIgnoreCase("y"))
                discord_response.put(k, new PlayerResponse(v.getPlayers_name(), UnicodeResponses.EXCUSED));
            if (v.getAttending().equalsIgnoreCase("y"))
                discord_response.put(k, new PlayerResponse(v.getPlayers_name(), UnicodeResponses.ATTENDING));
        });
        dnd_testing.setDiscord_response(discord_response);
    }

    @Scheduled(cron = "0 16 22 *  * FRI")
    public void checkForMembersInVoiceChannel() {
        List<Member> membersInsideVoiceChannel = new ArrayList<>();
        List<Member> guildMembers = new ArrayList<>();
        List<Member> peopleNotInsideVoiceChannel = new ArrayList<>();

        for (int i = 0; i < shardManager.getGuildById(939244115722371072L).getMembers().size(); i++) {
            if (!shardManager.getGuildById(939244115722371072L).getMembers().get(i).getRoles().get(i).getName().equalsIgnoreCase("bot")) {
                guildMembers.add(shardManager.getGuildById(939244115722371072L).getMembers().get(i));
            }
        }

        for (int j = 0; j < shardManager.getGuildById(939244115722371072L).getVoiceChannelById(939244115722371077L).getMembers().size(); j++) {
            if (!shardManager.getGuildById(939244115722371072L).getVoiceChannelById(939244115722371077L).getMembers().get(j).getRoles().get(j).getName().equalsIgnoreCase("bot")) {
                membersInsideVoiceChannel.add(shardManager.getGuildById(939244115722371072L).getVoiceChannelById(939244115722371077L).getMembers().get(j));
            }
        }
        log.info(guildMembers.toString());
        log.info(membersInsideVoiceChannel.toString());
        log.info(String.valueOf(membersInsideVoiceChannel.size()));

        if (guildMembers.size() == membersInsideVoiceChannel.size()) {
            shardManager.getUserById(416342612484554752L).openPrivateChannel().queue(channel -> {
                channel.sendMessage("ALL PEOPLE ARE HEREEEEE!").queue();
            });
        } else if (!membersInsideVoiceChannel.isEmpty()) {
            for (int i = 0; i < guildMembers.size(); i++) {
                for (int j = 0; j < membersInsideVoiceChannel.size(); j++) {
                    if (!guildMembers.get(i).getEffectiveName().equalsIgnoreCase(membersInsideVoiceChannel.get(j).getEffectiveName())) {
                        peopleNotInsideVoiceChannel.add(guildMembers.get(i));
                    }
                }
            }

            log.info(peopleNotInsideVoiceChannel.toString());

            EmbedBuilder builder = new EmbedBuilder();
            builder.setTitle("Users Not In " + shardManager.getGuildById(939244115722371072L).getJDA().getVoiceChannelById(939244115722371077L).getName() + " Channel");

            for (Member member : peopleNotInsideVoiceChannel) {
                builder.addField(member.getEffectiveName(), "", false);
            }

            MessageEmbed messageEmbed = builder.build();
            shardManager.getUserById(416342612484554752L).openPrivateChannel().queue(channel -> {
                channel.sendMessageEmbeds(messageEmbed).queue();
            });
        } else {
            EmbedBuilder builder = new EmbedBuilder();

            builder.setTitle("Users Who need to Join");

            for (Member member : guildMembers) {
                builder.addField(member.getEffectiveName(), "", false);
            }

            MessageEmbed messageEmbed = builder.build();
            shardManager.getUserById(416342612484554752L).openPrivateChannel().queue(channel -> {
                channel.sendMessageEmbeds(messageEmbed).queue();
            });
        }
    }
}

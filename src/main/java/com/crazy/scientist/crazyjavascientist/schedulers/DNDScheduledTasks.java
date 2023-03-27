package com.crazy.scientist.crazyjavascientist.schedulers;

import com.crazy.scientist.crazyjavascientist.commands.dnd.DNDService;
import com.crazy.scientist.crazyjavascientist.commands.dnd.dnd_entities.DNDAttendanceEntity;
import com.crazy.scientist.crazyjavascientist.commands.dnd.dnd_entities.DNDAttendanceHistoryEntity;
import com.crazy.scientist.crazyjavascientist.commands.dnd.dnd_entities.DNDPlayersEntity;
import com.crazy.scientist.crazyjavascientist.commands.dnd.dnd_entities.PlayerResponse;
import com.crazy.scientist.crazyjavascientist.commands.dnd.dnd_repos.DNDAttendanceHistoryRepo;
import com.crazy.scientist.crazyjavascientist.commands.dnd.dnd_repos.DNDAttendanceRepo;
import com.crazy.scientist.crazyjavascientist.commands.dnd.dnd_repos.DNDPlayersRepo;
import com.crazy.scientist.crazyjavascientist.commands.dnd.enums.UnicodeResponses;
import com.crazy.scientist.crazyjavascientist.exceptions.DNDException;
import com.crazy.scientist.crazyjavascientist.utils.CJSUtils;
import com.google.common.base.Stopwatch;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.ItemComponent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.requests.ErrorResponse;
import net.dv8tion.jda.api.sharding.ShardManager;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.crazy.scientist.crazyjavascientist.commands.dnd.enums.UnicodeResponses.NO_SHOW_NO_RESPONSE;
import static com.crazy.scientist.crazyjavascientist.constants.StaticUtils.*;


@Profile("server")
@Service
@Slf4j
public class DNDScheduledTasks {
    private final DNDPlayersRepo dNDPlayersRepo;

    private final DNDAttendanceRepo attendanceRepo;
    private final DNDAttendanceHistoryRepo attendanceHistoryRepo;
    private final DNDService dndService;
    private final ShardManager shardManager;

    private final CJSUtils cjsUtils;

    public DNDScheduledTasks(DNDAttendanceRepo attendanceRepo, DNDAttendanceHistoryRepo attendanceHistoryRepo, DNDService dndService, ShardManager shardManager,
                             DNDPlayersRepo dNDPlayersRepo, CJSUtils cjsUtils) {
        this.attendanceRepo = attendanceRepo;
        this.attendanceHistoryRepo = attendanceHistoryRepo;
        this.dndService = dndService;
        this.shardManager = shardManager;
        this.dNDPlayersRepo = dNDPlayersRepo;
        this.cjsUtils = cjsUtils;
    }



    @Scheduled(cron = "${dnd.attendance.status.update.cron.job}")
    public void showUpdateForWhoWillBeAttending() throws ExecutionException, InterruptedException, DNDException {
        if (!isDndCancelled) {
            try {
                dndService.handleAttendanceUpdate();
            }catch (Exception e){
                throw new DNDException("Error Occurred When Sending Attendance Update", e);
            }
        }
    }



    @Scheduled(cron = "${dnd.attendance.refresh.cron.job}")
    public void refreshDNDAttendance() throws ExecutionException, InterruptedException {

        if (!isDndCancelled) {

            Stopwatch stopwatch = Stopwatch.createStarted();

            log.info("DND Attendance Refresh Task Starting...");

            TextChannel channel = shardManager.getGuildsByName(THE_JAVA_WAY, true).get(0).getTextChannelsByName(LIVE_CHANNEL, true).get(0);

            //Pulls Player Responses from initialized static list context that has populated values upon application startup
            AtomicInteger num_of_attending_players = new AtomicInteger();
            AtomicInteger num_of_excused_players = new AtomicInteger();
            AtomicInteger num_of_no_show_no_response_players = new AtomicInteger();
            StringBuilder names_of_attending_players = new StringBuilder();
            StringBuilder names_of_excused_players = new StringBuilder();
            StringBuilder names_of_no_show_no_response_players = new StringBuilder();

            if(dndService.getDiscord_response().values().stream().allMatch(response -> response.getResponse_emoji_unicode() == NO_SHOW_NO_RESPONSE))
                dndService.setDiscord_response(cjsUtils.populateDiscordResponses());

            dndService.getDiscord_response().forEach((k, v) -> {
                switch (v.getResponse_emoji_unicode()){
                    case EXCUSED -> {
                        names_of_excused_players.append(v.getPlayer_name());
                        names_of_excused_players.append(" | ");
                        num_of_excused_players.getAndIncrement();
                    }
                    case ATTENDING -> {
                        names_of_attending_players.append(v.getPlayer_name());
                        names_of_attending_players.append(" | ");
                        num_of_attending_players.getAndIncrement();
                    }
                    case NO_SHOW_NO_RESPONSE -> {
                        names_of_no_show_no_response_players.append(v.getPlayer_name());
                        names_of_no_show_no_response_players.append(" | ");
                        num_of_no_show_no_response_players.getAndIncrement();
                    }
                }
            });

            attendanceHistoryRepo.save(DNDAttendanceHistoryEntity.builder()
                    .players_attended(num_of_attending_players.intValue())
                    .players_excused(num_of_excused_players.intValue())
                    .players_no_show(num_of_no_show_no_response_players.intValue())
                    .players_names_attended((num_of_attending_players.intValue() == 0) ? "None" : names_of_attending_players.toString())
                    .players_names_excused((num_of_excused_players.intValue() == 0) ? "None" : names_of_excused_players.toString())
                    .players_names_no_show((num_of_no_show_no_response_players.intValue() == 0) ? "None" : names_of_no_show_no_response_players.toString())
                    .week_of_attendance(ZonedDateTime.now().minusDays(5).format(DateTimeFormatter.ofPattern("dd-MMM-yyyy"))).build());

            //Deletes values in Attendance Table and repopulates it with default values for the next week
            //Also saves fresh empty responses to player_responses static list
            attendanceRepo.deleteAll();
            dndService.getDiscord_response().forEach((k, v) -> attendanceRepo.save(new DNDAttendanceEntity(k, v.getPlayer_name(), "N", "N", "Y")));
            log.info("All Attendance values have been reset for the new week");

            log.info("Deleting Messages For the Week");

            ErrorHandler handler = new ErrorHandler().handle(ErrorResponse.UNKNOWN_INTERACTION, error -> channel.sendMessage("Some shit broke don't know what but yolo").queue());

            if (check_if_messages_exist(channel)) {
                if (channel.getIterableHistory().stream().anyMatch(message -> (message.getIdLong() == dndService.getEmbed_to_be_deleted_msg_id()) && message.getAuthor().isBot() && !message.getEmbeds().isEmpty())) {
                    List<Message> messages_to_delete = channel.getIterableHistory().takeAsync(500).thenApply(list -> list.stream().filter(message -> message.getAuthor().isBot() && message.getEmbeds().size() > 0).collect(Collectors.toList())).get();
                    log.info("Message List Size for Deletion : {}", messages_to_delete.size());
                    log.info("Attempting to Purge Messages...");
                    channel.purgeMessages(messages_to_delete);
                    log.info("Purge Process Finished");
                    channel.sendMessageFormat("All Messages After %s with the title \"DND Attendance Status Update\" or \"DND Session Attendance\" should have been deleted", ZonedDateTime.now().minusDays(5).format(DateTimeFormatter.ofPattern("MM/dd/yyyy"))).queueAfter(5, TimeUnit.SECONDS, null, handler);
                } else {
                    log.error("No Messages Found during Deletion Task!");
                }
            }
            stopwatch.stop();
            log.info("Message Deletion Task Finished");
            log.info("Resetting Discord Responses");
            dndService.getDiscord_response().forEach((k, v) -> dndService.getDiscord_response().replace(k, v, new PlayerResponse(v.getPlayer_name(), NO_SHOW_NO_RESPONSE)));
            log.info("Responses Reset Successfully");
            log.info("Weekly Attendance Refresh Task Completed Successfully!");
            log.info("Time taken for Attendance Refresh Task Completion was {}", stopwatch.elapsed());
        }
        isDndCancelled = false;
    }

    @Scheduled(cron = "${attendance.embed.message.sending.cron.job}")
    public void sendAttendanceRequestEmbed() {
        if (!isDndCancelled) {
            Button excused_button = Button.danger("excused_button", Emoji.fromUnicode("\uD83D\uDE2D"));
            Button attending_button = Button.success("attending_button", Emoji.fromUnicode("\uD83C\uDF5E"));
            Button remove_button = Button.secondary("remove_button", Emoji.fromUnicode("\uD83D\uDDD1"));
            Button alpharius_button = Button.success("alpharius_button", Emoji.fromFormatted("<:alpharius:1045825620300529818>"));

            MessageEmbed builder = new EmbedBuilder().setTitle("DND Session Attendance").setTimestamp(ZonedDateTime.now()).setThumbnail("https://yawningportal.org/wp-content/uploads/2019/09/dnddescentkeyartjpg-1.jpeg").build();


            shardManager.getGuildsByName(THE_JAVA_WAY, true).get(0).getTextChannelsByName(LIVE_CHANNEL, true).get(0).sendMessage(new MessageCreateBuilder().setActionRow((ItemComponent) ActionRow.of(attending_button, alpharius_button, excused_button, remove_button)).setContent("@here").setEmbeds(builder).build()).queue();
        }
    }


    private boolean checkForOtherAttendanceEmbeds() throws ExecutionException, InterruptedException {

        OffsetDateTime firstEmbedsCreatedTime = shardManager.getGuildsByName(THE_JAVA_WAY,true)
                .get(0).getTextChannelsByName(LIVE_CHANNEL,true)
                .get(0).getIterableHistory()
                .takeAsync(1000)
                .thenApply(list -> list.stream()
                        .filter(
                                message -> message.getAuthor().isBot() && !message.getEmbeds().isEmpty() && message.getTimeCreated().isBefore(OffsetDateTime.now()))
                        .filter(message -> message.getEmbeds().get(0).getTitle().contains("DND Session Attendance"))).get().findFirst().get().getTimeCreated();

        return (firstEmbedsCreatedTime.getDayOfMonth() - OffsetDateTime.now().getDayOfMonth() != 3);

    }

    private boolean check_if_messages_exist(TextChannel channel) {
        return channel.getIterableHistory().stream().filter(message -> message.getTimeCreated().isAfter(OffsetDateTime.from(ZonedDateTime.now().minusDays(5))) && message.getAuthor().isBot() && !message.getEmbeds().isEmpty()).anyMatch(message -> message.getEmbeds().get(0).getTitle().equalsIgnoreCase("DND Session Attendance") || message.getEmbeds().get(0).getTitle().equalsIgnoreCase("DND Attendance Status Update"));
    }

    @Scheduled(cron = "0 15 21 * * FRI")
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

    @Scheduled(cron = "${final.attendance.alert.cron.job}")
    public void sendFinalAlertToNonExcusedPlayers() {
        if (!isDndCancelled) {
            EmbedBuilder messageBuilder = new EmbedBuilder()
                    .setAuthor("☕ Java Masochist ☕")
                    .setTitle("Final DND Roll Call")
                    .setThumbnail("https://yawningportal.org/wp-content/uploads/2019/09/dnddescentkeyartjpg-1.jpeg")
                    .appendDescription("The People Listed Below Have not excused themselves from DND and have not been found in the voice channel\n\n");

            List<Member> discord_dnd_members = shardManager.getGuildsByName(THE_JAVA_WAY, true).get(0).getMembersWithRoles(shardManager.getRolesByName("Decent Into Avanti", true).get(0));
            List<Member> cafe_chat_members = shardManager.getGuildsByName(THE_JAVA_WAY, true).get(0).getVoiceChannelsByName("The Cafe", true).get(0).getMembers();
            discord_dnd_members.removeAll(cafe_chat_members);
            discord_dnd_members.parallelStream().forEach(member -> {
                dndService.getDiscord_response().forEach((k, v) -> {
                    if (k == member.getIdLong() && v.getResponse_emoji_unicode().equals(NO_SHOW_NO_RESPONSE))
                        messageBuilder.appendDescription(String.format("<@%s>%n", member.getUser().getId()));
                });
            });
            messageBuilder.appendDescription("\nPlease Make sure you either excuse yourself or join the channel for DND! This is the last call for this weeks attendance.");
            shardManager.getGuildsByName(THE_JAVA_WAY, true).get(0).getTextChannelsByName(LIVE_CHANNEL, true).get(0).sendMessageEmbeds(messageBuilder.build()).queue();
        }
    }
}


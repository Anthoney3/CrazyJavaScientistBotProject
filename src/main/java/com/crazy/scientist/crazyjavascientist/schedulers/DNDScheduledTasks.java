package com.crazy.scientist.crazyjavascientist.schedulers;

import com.crazy.scientist.crazyjavascientist.dnd.DNDService;
import com.crazy.scientist.crazyjavascientist.dnd.dnd_entities.DNDAttendanceEntity;
import com.crazy.scientist.crazyjavascientist.dnd.dnd_entities.DNDAttendanceHistoryEntity;
import com.crazy.scientist.crazyjavascientist.dnd.dnd_entities.PlayerResponse;
import com.crazy.scientist.crazyjavascientist.dnd.dnd_repos.DNDAttendanceHistoryRepo;
import com.crazy.scientist.crazyjavascientist.dnd.dnd_repos.DNDAttendanceRepo;
import com.crazy.scientist.crazyjavascientist.dnd.enums.UnicodeResponses;
import com.google.common.base.Stopwatch;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.ItemComponent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.requests.ErrorResponse;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static com.crazy.scientist.crazyjavascientist.constants.StaticUtils.*;

@Slf4j
@Service
public class DNDScheduledTasks {

    private DNDAttendanceRepo attendanceRepo;
    private DNDAttendanceHistoryRepo attendanceHistoryRepo;
    private DNDService dndService;

    public DNDScheduledTasks(DNDAttendanceRepo attendanceRepo, DNDAttendanceHistoryRepo attendanceHistoryRepo, DNDService dndService) {
        this.attendanceRepo = attendanceRepo;
        this.attendanceHistoryRepo = attendanceHistoryRepo;
        this.dndService = dndService;
    }

    private long embed_to_be_deleted_msg_id = 0;

    @Scheduled(cron = "${dnd.attendance.status.update.cron.job}")
    public void showUpdateForWhoWillBeAttending() throws ExecutionException, InterruptedException {
        if (!isDndCancelled) {
            TextChannel channel = shardManager.getGuildsByName(THE_JAVA_WAY, true).get(0).getTextChannelsByName(LIVE_CHANNEL, true).get(0);

            Stopwatch stopwatch = Stopwatch.createStarted();

            AtomicReference<EmbedBuilder> builder = new AtomicReference<>(new EmbedBuilder().setTitle("DND Attendance Status Update").setThumbnail("https://yawningportal.org/wp-content/uploads/2019/09/dnddescentkeyartjpg-1.jpeg").setTimestamp(Instant.now()));

      /*
            Appends Code Block back-ticks before appending database table information as a status update
            Logic checks to see if any tables are not equal to zero in separate if blocks
            Then appends respective tables as needed to the embed message description
            */

            String column_one_format = "```%-10.10s";
            String column_two_format = "%s```";
            String formatInfo = column_one_format + " " + column_two_format;

            StringBuilder build_message = new StringBuilder();
            dndService.getDiscord_response().forEach((key, value) -> build_message.append(String.format(formatInfo, value.getPlayer_name(), value.getResponse_emoji_unicode().name())));

            //Special addition to state all players should be attending if no other status type have been set to Y
            builder.get().appendDescription(build_message);
            if (dndService.getDiscord_response().entrySet().stream().filter(item -> item.getValue().getResponse_emoji_unicode().equals(UnicodeResponses.ATTENDING)).count() == dndService.getDiscord_response().size())
                builder.get().appendDescription("**All Players Expected To Join!**");

            //Discord Function to find the most recent DND Session Attendance Embed message sent by the bot to get the message's jump link url
            Message foundMessage = channel.getIterableHistory().takeAsync(500).thenApply(list -> list.stream().filter(message -> message.getAuthor().isBot() && !message.getEmbeds().isEmpty() && message.getTimeCreated().isBefore(OffsetDateTime.now())).filter(message -> message.getEmbeds().get(0).getTitle().contains("DND Session Attendance")).findFirst().get()).get();
            embed_to_be_deleted_msg_id = foundMessage.getIdLong();

      /*
            Logic that runs if there are still status types set to Y for no show
            This will check for any players that have a remaining status type of no show and ping them in the status update with an @ mention
            This will also provide the jump link url to the message sent previously by the bot that allows users to update their attendance for the week
            */
            if (dndService.getDiscord_response().entrySet().stream().filter(item -> item.getValue().getResponse_emoji_unicode().equals(UnicodeResponses.NO_SHOW_NO_RESPONSE)).count() != 0) {
                dndService.getDiscord_response().entrySet().stream().filter(item -> item.getValue().getResponse_emoji_unicode().equals(UnicodeResponses.NO_SHOW_NO_RESPONSE)).forEach(item -> {
                    builder.get().appendDescription(String.format("<@%s>%n", item.getKey()));
                });
                builder.get().appendDescription("\n**For anyone listed above, If you don't reply to the message linked below you will be marked as No Show for the week!\nClick the Link below to update your attendance!**");
                builder.get().appendDescription(String.format("%n%n%s", foundMessage.getJumpUrl()));
            }
            MessageEmbed responseMessage = builder.get().build();
            channel.sendMessageEmbeds(responseMessage).queue();
            stopwatch.stop();
            log.info("Status Update Task Finished in : {}", stopwatch.elapsed());
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

            dndService.getDiscord_response().forEach((k, v) -> {
                if (v.getResponse_emoji_unicode().equals(UnicodeResponses.EXCUSED)) {
                    names_of_excused_players.append(v.getPlayer_name());
                    names_of_excused_players.append(" | ");
                    num_of_excused_players.getAndIncrement();
                }
                if (v.getResponse_emoji_unicode().equals(UnicodeResponses.ATTENDING)) {
                    names_of_attending_players.append(v.getPlayer_name());
                    names_of_attending_players.append(" | ");
                    num_of_attending_players.getAndIncrement();
                }
                if (v.getResponse_emoji_unicode().equals(UnicodeResponses.NO_SHOW_NO_RESPONSE)) {
                    names_of_no_show_no_response_players.append(v.getPlayer_name());
                    names_of_no_show_no_response_players.append(" | ");
                    num_of_no_show_no_response_players.getAndIncrement();
                }
            });

            DNDAttendanceHistoryEntity dnd_attendance_history_obj = DNDAttendanceHistoryEntity.builder().players_attended(num_of_attending_players.intValue()).players_excused(num_of_excused_players.intValue()).players_no_show(num_of_no_show_no_response_players.intValue()).players_names_attended((num_of_attending_players.intValue() == 0) ? "None" : names_of_attending_players.toString()).players_names_excused((num_of_excused_players.intValue() == 0) ? "None" : names_of_excused_players.toString()).players_names_no_show((num_of_no_show_no_response_players.intValue() == 0) ? "None" : names_of_no_show_no_response_players.toString()).week_of_attendance(ZonedDateTime.now().minusDays(5).format(DateTimeFormatter.ofPattern("dd-MMM-yyyy"))).build();

            attendanceHistoryRepo.save(dnd_attendance_history_obj);

            //Deletes values in Attendance Table and repopulates it with default values for the next week
            //Also saves fresh empty responses to player_responses static list
            attendanceRepo.deleteAll();
            dndService.getDiscord_response().forEach((k, v) -> attendanceRepo.save(new DNDAttendanceEntity(k, v.getPlayer_name(), "N", "N", "Y")));
            log.info("All Attendance values have been reset for the new week");

            log.info("Deleting Messages For the Week");

            ErrorHandler handler = new ErrorHandler().handle(ErrorResponse.UNKNOWN_INTERACTION, error -> channel.sendMessage("Some shit broke don't know what but yolo").queue());

            if (check_if_messages_exist(channel)) {
                if (channel.getIterableHistory().stream().anyMatch(message -> (message.getIdLong() == embed_to_be_deleted_msg_id) && message.getAuthor().isBot() && !message.getEmbeds().isEmpty())) {
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
            dndService.getDiscord_response().forEach((k, v) -> dndService.getDiscord_response().replace(k, v, new PlayerResponse(v.getPlayer_name(), UnicodeResponses.NO_SHOW_NO_RESPONSE)));
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

    private boolean check_if_messages_exist(TextChannel channel) {
        return channel.getIterableHistory().stream().filter(message -> message.getTimeCreated().isAfter(OffsetDateTime.from(ZonedDateTime.now().minusDays(5))) && message.getAuthor().isBot() && !message.getEmbeds().isEmpty()).anyMatch(message -> message.getEmbeds().get(0).getTitle().equalsIgnoreCase("DND Session Attendance") || message.getEmbeds().get(0).getTitle().equalsIgnoreCase("DND Attendance Status Update"));
    }
}

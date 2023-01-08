package com.crazy.scientist.crazyjavascientist.schedulers;


import com.crazy.scientist.crazyjavascientist.dnd.DNDTesting;
import com.crazy.scientist.crazyjavascientist.dnd.dnd_entities.CurrentWeekOfEntity;
import com.crazy.scientist.crazyjavascientist.dnd.dnd_entities.DNDAttendanceEntity;
import com.crazy.scientist.crazyjavascientist.dnd.dnd_entities.DNDAttendanceHistoryEntity;
import com.crazy.scientist.crazyjavascientist.dnd.dnd_repos.CurrentWeekOfRepo;
import com.crazy.scientist.crazyjavascientist.dnd.dnd_repos.DNDAttendanceHistoryRepo;
import com.crazy.scientist.crazyjavascientist.dnd.dnd_repos.DNDAttendanceRepo;
import com.crazy.scientist.crazyjavascientist.dnd.dnd_repos.DNDPlayersRepo;
import com.crazy.scientist.crazyjavascientist.utils.DBTablePrinter;
import com.google.common.base.Stopwatch;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.requests.ErrorResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.crazy.scientist.crazyjavascientist.config.DiscordBotConfigJDAStyle.player_responses;
import static com.crazy.scientist.crazyjavascientist.config.StaticUtils.shardManager;

@Slf4j
@Service
public class DNDScheduledTasks {


    @Autowired
    private DNDAttendanceRepo attendanceRepo;

    @Autowired
    private DNDPlayersRepo playersRepo;

    @Autowired
    private CurrentWeekOfRepo currentWeekOfRepo;

    @Autowired
    private DNDAttendanceHistoryRepo attendanceHistoryRepo;

    @Autowired
    private DNDTesting dndTesting;

    @Value("${spring.datasource.url}")
    private String db_url;

    @Value("${spring.datasource.username}")
    private String db_username;

    @Value("${spring.datasource.password}")
    private String db_password;

    private long embed_to_be_deleted_msg_id = 0;



//    @Scheduled(cron = "${dnd.attendance.status.update.cron.job}")
    public void showUpdateForWhoWillBeAttending() throws SQLException {

        TextChannel channel = shardManager.getGuildsByName("The Java Way",true).get(0)
                .getTextChannelsByName("dark-n-dangerous-avanti",true).get(0);

        Stopwatch stopwatch = Stopwatch.createStarted();

        AtomicReference<EmbedBuilder> builder = new AtomicReference<>(new EmbedBuilder()
                .setTitle("DND Attendance Status Update")
                .setThumbnail("https://yawningportal.org/wp-content/uploads/2019/09/dnddescentkeyartjpg-1.jpeg")
                .setTimestamp(Instant.now()));

        Connection connection = DriverManager.getConnection(db_url, db_username, db_password);

        //Player Counts Per Status

        long attending_player_count = player_responses.entrySet().stream().filter(item -> item.getValue().getAttending().equalsIgnoreCase("y")).count();
        long excused_player_count = player_responses.entrySet().stream().filter(item -> item.getValue().getExcused().equalsIgnoreCase("y")).count();
        long no_show_player_count = player_responses.entrySet().stream().filter(item -> item.getValue().getNo_show().equalsIgnoreCase("y")).count();

        //Appends Code Block back-ticks before appending database table information as a status update
        //Logic checks to see if any tables are not equal to zero in separate if blocks
        //Then appends respective tables as needed to the embed message description

        builder.get().appendDescription("```");
        if (attending_player_count != 0) {
            String attending_players = DBTablePrinter.printTable(connection, "SELECT PLAYERS_NAME, ATTENDING FROM dnd_attendance_info WHERE ATTENDING='Y'", "dnd_attendance_info");
            builder.get().appendDescription(attending_players.substring(attending_players.indexOf('\n') + 1));
        }
        if (excused_player_count != 0) {
            String excused_players = DBTablePrinter.printTable(connection, "SELECT PLAYERS_NAME, EXCUSED FROM dnd_attendance_info WHERE EXCUSED='Y'", "dnd_attendance_info");
            builder.get().appendDescription(excused_players.substring(excused_players.indexOf('\n') + 1));
        }
        if (no_show_player_count != 0) {
            String no_show_players = DBTablePrinter.printTable(connection, "SELECT PLAYERS_NAME, NO_SHOW_OR_NO_RESPONSE FROM dnd_attendance_info WHERE NO_SHOW_OR_NO_RESPONSE='Y'", "dnd_attendance_info");
            builder.get().appendDescription(no_show_players.substring(no_show_players.indexOf('\n') + 1));
        }
        //Special addition to state all players should be attending if no other status type have been set to Y
        builder.get().appendDescription("```\n");
        if(attending_player_count == player_responses.size())
            builder.get().appendDescription("**All Players Expected To Join!**");


        //Discord Function to find the most recent DND Session Attendance Embed message sent by the bot to get the message's jump link url

        List<Message> messages_found = new ArrayList<>();

                channel.getIterableHistory()
                .stream()
                .iterator()
                .forEachRemaining(message -> {
                    if(message.getAuthor().isBot() && !message.getEmbeds().isEmpty() && message.getTimeCreated().isAfter(currentWeekOfRepo.findAll().get(0).getEmbed_insertion_time())){
                        log.info(message.getEmbeds().get(0).getTitle());
                        if(message.getEmbeds().get(0).getTitle().equalsIgnoreCase("DND Session Attendance"))
                            messages_found.add(message);
                    }
                });

                log.info(messages_found.toString());

        //Gets the first message because this message will be the most recent message sent with the attendance
        embed_to_be_deleted_msg_id = messages_found.get(0).getIdLong();
        Message foundMessage = messages_found.get(0);

        //Logic that runs if there are still status types set to Y for no show
        //This will check for any players that have a remaining status type of no show and ping them in the status update with an @ mention
        //This will also provide the jump link url to the message sent previously by the bot that allows users to update their attendance for the week

        if(no_show_player_count != 0) {
            player_responses.entrySet().stream().filter(item -> item.getValue().getNo_show().equalsIgnoreCase("y")).forEach(item -> {
                builder.get().appendDescription(String.format("<@%s>%n", item.getValue().getDiscord_id()));
            });
            builder.get().appendDescription("\n**For anyone listed above, If you don't reply to the message linked below you will be marked as No Show for the week!\nClick the Link below to update your attendance!**");
            builder.get().appendDescription(String.format("%n%n%s",foundMessage.getJumpUrl()));
        }

        MessageEmbed responseMessage = builder.get().build();

        channel.sendMessageEmbeds(responseMessage).queue();

        //Closes any left open connections to the database that were being used by the DBTablePrinter class
        if(!connection.isClosed())
            connection.close();

        stopwatch.stop();

        log.info("Connection Closed : {}", connection.isClosed());
        log.info("Status Update Task Finished in : {}",stopwatch.elapsed());
    }

//    @Scheduled(cron="${dnd.attendance.refresh.cron.job}")
    public void refreshDNDAttendance(){

        Stopwatch stopwatch = Stopwatch.createStarted();

        log.info("DND Attendance Refresh Task Starting...");

        TextChannel channel = shardManager.getGuildsByName("The Java Way",true).get(0)
                .getTextChannelsByName("dark-n-dangerous-avanti",true).get(0);

        //Pulls Player Responses from initialized static list context that has populated values upon application startup
        AtomicInteger num_of_attending_players = new AtomicInteger();
        AtomicInteger num_of_excused_players = new AtomicInteger();
        AtomicInteger num_of_no_show_no_response_players = new AtomicInteger();
        StringBuilder names_of_attending_players = new StringBuilder();
        StringBuilder names_of_excused_players = new StringBuilder();
        StringBuilder names_of_no_show_no_response_players = new StringBuilder();
        DNDAttendanceHistoryEntity dnd_attendance_history_obj = new DNDAttendanceHistoryEntity();


        player_responses.forEach((k,v) -> {
            if(v.getExcused().equalsIgnoreCase("y")) {
                names_of_excused_players.append(v.getPlayers_name());
                names_of_excused_players.append(" | ");
                num_of_excused_players.getAndIncrement();
            }
            if(v.getAttending().equalsIgnoreCase("y")){
                names_of_attending_players.append(v.getPlayers_name());
                names_of_attending_players.append(" | ");
                num_of_attending_players.getAndIncrement();
            }
            if(v.getNo_show().equalsIgnoreCase("y")) {
                names_of_no_show_no_response_players.append(v.getPlayers_name());
                names_of_no_show_no_response_players.append(" | ");
                num_of_no_show_no_response_players.getAndIncrement();
            }
        });
        dnd_attendance_history_obj.setPlayers_attended(num_of_attending_players.intValue());
        dnd_attendance_history_obj.setPlayers_excused(num_of_excused_players.intValue());
        dnd_attendance_history_obj.setPlayers_no_show(num_of_no_show_no_response_players.intValue());
        dnd_attendance_history_obj.setPlayers_names_attended((num_of_attending_players.intValue() == 0)? "None" : names_of_attending_players.toString());
        dnd_attendance_history_obj.setPlayers_names_excused((num_of_excused_players.intValue() == 0)? "None" : names_of_excused_players.toString());
        dnd_attendance_history_obj.setPlayers_names_no_show((num_of_no_show_no_response_players.intValue() == 0) ? "None" : names_of_no_show_no_response_players.toString());
        dnd_attendance_history_obj.setWeek_of_attendance(ZonedDateTime.now().minusDays(5).format(DateTimeFormatter.ofPattern("dd-MMM-yyyy")));

        attendanceHistoryRepo.save(dnd_attendance_history_obj);

        //Deletes values in Attendance Table and repopulates it with default values for the next week
        //Also saves fresh empty responses to player_responses static list
        attendanceRepo.deleteAll();
        player_responses.forEach((k,v) -> attendanceRepo.save(new DNDAttendanceEntity(k,v.getPlayers_name(),"N","N","Y")));
        player_responses = new HashMap<>(attendanceRepo.findAll().stream().collect(Collectors.toMap(DNDAttendanceEntity::getDiscord_id, Function.identity())));
        log.info("All Attendance values have been reset for the new week");

        log.info("Deleting Messages For the Week");

        ErrorHandler handler = new ErrorHandler().handle(ErrorResponse.UNKNOWN_INTERACTION, (error) -> channel.sendMessage("Some shit broke don't know what but yolo").queue());

        if (check_if_messages_exist(channel)) {

            if(channel.getIterableHistory().stream().anyMatch(message -> message.getIdLong() == embed_to_be_deleted_msg_id && message.getAuthor().isBot() && !message.getEmbeds().isEmpty())) {
                channel.purgeMessages(channel.getIterableHistory().stream().filter(message -> message.getIdLong() == embed_to_be_deleted_msg_id && message.getAuthor().isBot() && !message.getEmbeds().isEmpty()).filter(message -> message.getEmbeds().get(0).getTitle().equalsIgnoreCase("DND Session Attendance") || message.getEmbeds().get(0).getTitle().equalsIgnoreCase("DND Attendance Status Update")).toList());
                channel.sendMessageFormat("All Messages After %s with the title \"DND Attendance Status Update\" or \"DND Session Attendance\" should have been deleted", ZonedDateTime.now().minusDays(5).format(DateTimeFormatter.ofPattern("MM/dd/yyyy"))).queueAfter(5, TimeUnit.SECONDS, null, handler);
            }else {
                log.error("No Messages Found during Deletion Task!");
            }
//            channel.purgeMessages(channel.getIterableHistory().stream().filter(message -> message.getTimeCreated().isAfter(OffsetDateTime.from(ZonedDateTime.now().minusDays(5))) && message.getAuthor().isBot() && !message.getEmbeds().isEmpty()).filter(message -> message.getEmbeds().get(0).getTitle().equalsIgnoreCase("DND Session Attendance") || message.getEmbeds().get(0).getTitle().equalsIgnoreCase("DND Attendance Status Update")).toList());
        }
        stopwatch.stop();
        log.info("Message Deletion Task Finished");
        log.info("Weekly Attendance Refresh Task Completed Successfully!");
        log.info("Time taken for Attendance Refresh Task Completion was {}",stopwatch.elapsed());
    }

//    @Scheduled(cron = "${attendance.embed.message.sending.cron.job}")
    public void sendAttendanceRequestEmbed() {



        CurrentWeekOfEntity updated_insertion_time = currentWeekOfRepo.findById(1L).isEmpty() ? new CurrentWeekOfEntity(ZonedDateTime.now().minusDays(Math.abs(Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - Calendar.MONDAY)).format(DateTimeFormatter.ofPattern("dd-MMMM-yyyy"))) : currentWeekOfRepo.findById(1L).get();
        updated_insertion_time.setEmbed_insertion_time(OffsetDateTime.now());
        currentWeekOfRepo.save(updated_insertion_time);

        Button excused_button = Button.danger("excused_button", Emoji.fromUnicode("\uD83D\uDE2D"));
        Button attending_button = Button.success("attending_button", Emoji.fromUnicode("\uD83C\uDF5E"));
        Button remove_button = Button.secondary("remove_button", Emoji.fromUnicode("\uD83D\uDDD1"));
        Button alpharius_button = Button.success("alpharius_button",Emoji.fromMarkdown("<:alpharius:1045825620300529818>"));

        MessageEmbed builder = new EmbedBuilder().setTitle("DND Session Attendance").setTimestamp(ZonedDateTime.now())
                .setThumbnail("https://yawningportal.org/wp-content/uploads/2019/09/dnddescentkeyartjpg-1.jpeg").build();

        Message message = new MessageBuilder().setActionRows(ActionRow.of(attending_button,alpharius_button, excused_button, remove_button))
                .setContent("@here")
                .setEmbeds(builder).build();

        shardManager.getGuildsByName("The Java Way", true).get(0).getTextChannelsByName("dark-n-dangerous-avanti", true).get(0).sendMessage(message).queue();

    }
    private boolean check_if_messages_exist(TextChannel channel) {
        return channel.getIterableHistory().stream().filter(message -> message.getTimeCreated().isAfter(OffsetDateTime.from(ZonedDateTime.now().minusDays(5))) && message.getAuthor().isBot() && !message.getEmbeds().isEmpty()).anyMatch(message -> message.getEmbeds().get(0).getTitle().equalsIgnoreCase("DND Session Attendance") || message.getEmbeds().get(0).getTitle().equalsIgnoreCase("DND Attendance Status Update"));
    }

}

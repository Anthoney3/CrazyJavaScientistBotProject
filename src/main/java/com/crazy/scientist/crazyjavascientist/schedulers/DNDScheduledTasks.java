package com.crazy.scientist.crazyjavascientist.schedulers;


import com.crazy.scientist.crazyjavascientist.dnd.dnd_entities.DNDAttendanceEntity;
import com.crazy.scientist.crazyjavascientist.dnd.dnd_entities.DNDPlayersEntity;
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
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.transaction.Transactional;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static com.crazy.scientist.crazyjavascientist.config.StaticUtils.shardManager;

@Slf4j
@Service
public class DNDScheduledTasks {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private DNDAttendanceRepo attendanceRepo;

    @Autowired
    private DNDPlayersRepo playersRepo;

    @Value("${spring.datasource.url}")
    private String db_url;

    @Value("${spring.datasource.username}")
    private String db_username;

    @Value("${spring.datasource.password}")
    private String db_password;


//    @Scheduled(cron = "${dnd.attendance.status.update.cron.job}")
    public void showUpdateForWhoWillBeAttending() throws SQLException {

        TextChannel channel = shardManager.getGuildsByName("The Java Way",true).get(0)
                .getTextChannelsByName("dark-n-dangerous-avanti",true).get(0);

        Stopwatch stopwatch = Stopwatch.createStarted();


        AtomicReference<EmbedBuilder> builder = new AtomicReference<>(new EmbedBuilder()
                .setTitle("DND Attendance Status Update")
                .setThumbnail("https://yawningportal.org/wp-content/uploads/2019/09/dnddescentkeyartjpg-1.jpeg")
                .setTimestamp(Instant.now()));


        List<DNDAttendanceEntity> attendanceList = attendanceRepo.getDNDAttendance();

        Connection connection = DriverManager.getConnection(db_url, db_username, db_password);

        //Queries to Get Player Counts for each Status Type

        Query attending_player_count_query = entityManager.createNativeQuery("SELECT COUNT(*) FROM DND_ATTENDANCE_INFO WHERE ATTENDING='Y'");
        Query excused_player_count_query = entityManager.createNativeQuery("SELECT COUNT(*) FROM DND_ATTENDANCE_INFO WHERE EXCUSED='Y'");
        Query no_show_player_count_query = entityManager.createNativeQuery("SELECT COUNT(*) FROM DND_ATTENDANCE_INFO WHERE NO_SHOW_OR_NO_RESPONSE='Y'");
        Query embed_creation_time = entityManager.createNativeQuery("SELECT ATTENDANCE_EMBED_CREATION_TIME FROM CURRENT_WEEK_OF");

        OffsetDateTime embed_time_of_creation = OffsetDateTime.parse(embed_creation_time.getResultList().get(0).toString());

        //Player Counts Per Status

        int attending_player_count = Integer.parseInt(attending_player_count_query.getResultList().get(0).toString());
        int excused_player_count = Integer.parseInt(excused_player_count_query.getResultList().get(0).toString());
        int no_show_player_count = Integer.parseInt(no_show_player_count_query.getResultList().get(0).toString());

        //Appends Code Block back-ticks before appending database table information as a status update
        //Logic checks to see if any tables are not equal to zero in separate if blocks
        //Then appends respective tables as needed to the embed message description

        builder.get().appendDescription("```");
        if (attending_player_count != 0) {
            String attending_players = DBTablePrinter.printTable(connection, "SELECT PLAYERS_NAME, ATTENDING FROM DND_ATTENDANCE_INFO WHERE ATTENDING='Y'", "DND_ATTENDANCE_INFO");
            builder.get().appendDescription(attending_players.substring(attending_players.indexOf('\n') + 1));
        }
        if (excused_player_count != 0) {
            String excused_players = DBTablePrinter.printTable(connection, "SELECT PLAYERS_NAME, EXCUSED FROM DND_ATTENDANCE_INFO WHERE EXCUSED='Y'", "DND_ATTENDANCE_INFO");
            builder.get().appendDescription(excused_players.substring(excused_players.indexOf('\n') + 1));
        }
        if (no_show_player_count != 0) {
            String no_show_players = DBTablePrinter.printTable(connection, "SELECT PLAYERS_NAME, NO_SHOW_OR_NO_RESPONSE FROM DND_ATTENDANCE_INFO WHERE NO_SHOW_OR_NO_RESPONSE='Y'", "DND_ATTENDANCE_INFO");
            builder.get().appendDescription(no_show_players.substring(no_show_players.indexOf('\n') + 1));
        }
        //Special addtion to state all players should be attending if no other status type have been set to Y
        builder.get().appendDescription("```\n");
        if(attending_player_count == 9)
            builder.get().appendDescription("**All Players Expected To Join!**");

        //Query to get list of players base on the info database table

        Query getPlayersDiscordUserId = entityManager.createNativeQuery("SELECT * FROM DND_PLAYERS_INFO", DNDPlayersEntity.class);
        List<DNDPlayersEntity> player_info = getPlayersDiscordUserId.getResultList();

        //Discord Function to find the most recent DND Session Attendance Embed message sent by the bot to get the message's jump link url

        List<Message> messages_found = new ArrayList<>();


                channel.getIterableHistory()
                .stream()
                .iterator()
                .forEachRemaining(message -> {
                    if(message.getAuthor().isBot() && !message.getEmbeds().isEmpty() && message.getTimeCreated().isAfter(embed_time_of_creation)){
                        log.info(message.getEmbeds().get(0).getTitle());
                        if(message.getEmbeds().get(0).getTitle().equalsIgnoreCase("DND Session Attendance"))
                            messages_found.add(message);
                    }
                });

                log.info(messages_found.toString());

        //Gets the first message because this message will be the most recent message sent with the attendance
        Message foundMessage = messages_found.get(0) ;


        //Logic that runs if there are still status types set to Y for no show
        //This will check for any players that have a remaining status type of no show and ping them in the status update with an @ mention
        //This will also provide the jump link url to the message sent previously by the bot that allows users to update their attendance for the week

        if(no_show_player_count != 0) {
            attendanceList.forEach(record -> {
                if (record.getNo_show().equalsIgnoreCase("Y")) {
                    builder.get().appendDescription(String.format("<@%s>%n", player_info.stream().filter(rec -> rec.getPlayer_name().equalsIgnoreCase(record.getPlayers_name())).findFirst().get().getDiscord_user_id()));
                }
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

        TextChannel channel = shardManager.getGuildsByName("The Java Way",true).get(0)
                .getTextChannelsByName("dark-n-dangerous-avanti",true).get(0);

        log.info("DND Attendance Refresh Task Starting...");

        List<DNDAttendanceEntity> dnd_attendance = attendanceRepo.getDNDAttendance();

        StringBuilder attendingPlayers = new StringBuilder();
        StringBuilder excusedPlayers = new StringBuilder();
        StringBuilder noShowPlayers = new StringBuilder();

        dnd_attendance.forEach(player->{
            if(player.getAttending().equalsIgnoreCase("y")){
                attendingPlayers.append(player.getPlayers_name());
                attendingPlayers.append(" | ");

            }else if (player.getExcused().equalsIgnoreCase("Y")){
                excusedPlayers.append(player.getPlayers_name());
                excusedPlayers.append(" | ");
            }else if (player.getNo_show().equalsIgnoreCase("Y")){
                noShowPlayers.append(player.getPlayers_name());
                noShowPlayers.append(" | ");
            }else {
                log.error("There was an error processing Attendance History Value : {}", player);
            }

        });

        if(attendingPlayers.isEmpty()){
            attendingPlayers.append("None");
        }
        if(excusedPlayers.isEmpty()){
            excusedPlayers.append("None");
        }
        if(noShowPlayers.isEmpty()){
            noShowPlayers.append("None");
        }

        //@TODO add code to remove current weeks attendance embed and status update embed so the new embeds for the next
        //  week don't get confused with the ones sent previously


        //Updates Current Week
        attendanceRepo.updateCurrentWeek(ZonedDateTime.now().minusDays(5).format(DateTimeFormatter.ofPattern("MM/dd/yyyy")));
        //Updates Attendance History Table With Attendance History for the week using a Function call
        attendanceRepo.updateAttendanceHistoryTable(attendingPlayers.toString(),excusedPlayers.toString(),noShowPlayers.toString());
        //Calls Stored Procedure and Truncates table with an insertion to refresh the table values
        attendanceRepo.resetAttendanceTableValues();
        log.info("All Attendance values have been reset for the new week");
        delete_attendance_and_status_update_messages_for_the_week(channel);
    }

    private void delete_attendance_and_status_update_messages_for_the_week(TextChannel channel) {


        log.info("Deleting Messages For the Week");

        ErrorHandler handler = new ErrorHandler().handle(ErrorResponse.UNKNOWN_INTERACTION, (error) -> channel.sendMessage("Some shit broke don't know what but yolo").queue());

        if (check_if_messages_exist(channel)) {

            channel.purgeMessages(channel.getIterableHistory().stream().filter(message -> message.getTimeCreated().isAfter(OffsetDateTime.from(ZonedDateTime.now().minusDays(5))) && message.getAuthor().isBot() && !message.getEmbeds().isEmpty()).filter(message -> message.getEmbeds().get(0).getTitle().equalsIgnoreCase("DND Session Attendance") || message.getEmbeds().get(0).getTitle().equalsIgnoreCase("DND Attendance Status Update")).toList());

            channel.sendMessageFormat("All Messages After %s with the title \"DND Attendance Status Update\" or \"DND Session Attendance\" should have been deleted", ZonedDateTime.now().minusDays(5).format(DateTimeFormatter.ofPattern("MM/dd/yyyy"))).queueAfter(5, TimeUnit.SECONDS, null, handler);
        }
        log.info("Message Deletion Task Complete");
    }

    @Transactional
    @Modifying
//    @Scheduled(cron = "${attendance.embed.message.sending.cron.job}")
    public void sendAttendanceRequestEmbed() {

        Query update_embed_creation_time = entityManager.createNativeQuery("UPDATE CURRENT_WEEK_OF SET ATTENDANCE_EMBED_CREATION_TIME=? WHERE ID=1");

        update_embed_creation_time.setParameter(1,OffsetDateTime.now().toString());
        update_embed_creation_time.executeUpdate();

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

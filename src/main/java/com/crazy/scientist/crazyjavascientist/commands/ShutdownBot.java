package com.crazy.scientist.crazyjavascientist.commands;

import com.crazy.scientist.crazyjavascientist.dnd.DNDService;
import com.crazy.scientist.crazyjavascientist.dnd.dnd_entities.DNDAttendanceEntity;
import com.crazy.scientist.crazyjavascientist.dnd.dnd_repos.DNDAttendanceRepo;
import com.crazy.scientist.crazyjavascientist.dnd.enums.UnicodeResponses;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import javax.annotation.Nonnull;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@NoArgsConstructor
@Slf4j
@Component
public class ShutdownBot extends ListenerAdapter {


    private DNDAttendanceRepo dndAttendanceRepo;
    private DNDService dndService;

    public ShutdownBot(DNDAttendanceRepo dndAttendanceRepo, DNDService dndService) {
        this.dndAttendanceRepo = dndAttendanceRepo;
        this.dndService = dndService;
    }

    public void shutdownBot(boolean hasPermission, @Nonnull SlashCommandInteraction event) {
        if (hasPermission) {
            event.reply("Shutting down...").queue();
            log.info("Bot Shutting down...");
            event.getJDA().shutdown();
            log.info("Saving Player Responses to Database...");
            dndService.getDiscord_response().forEach((k, v) -> {
                if (v.getResponse_emoji_unicode().equals(UnicodeResponses.NO_SHOW_NO_RESPONSE)) {
                    dndAttendanceRepo.save(new DNDAttendanceEntity(k, v.getPlayer_name(), "N", "N", "Y"));
                    log.info("Entity {} Saved to the DB Successfully", new DNDAttendanceEntity(k, v.getPlayer_name(), "N", "N", "Y"));
                }
                if (v.getResponse_emoji_unicode().equals(UnicodeResponses.ATTENDING)) {
                    dndAttendanceRepo.save(new DNDAttendanceEntity(k, v.getPlayer_name(), "Y", "N", "N"));
                    log.info("Entity {} Saved to the DB Successfully", new DNDAttendanceEntity(k, v.getPlayer_name(), "Y", "N", "N"));
                }
                if (v.getResponse_emoji_unicode().equals(UnicodeResponses.EXCUSED)) {
                    dndAttendanceRepo.save(new DNDAttendanceEntity(k, v.getPlayer_name(), "N", "Y", "N"));
                    log.info("Entity {} Saved to the DB Successfully", new DNDAttendanceEntity(k, v.getPlayer_name(), "N", "Y", "N"));
                }
            });
            log.info("DB Upload Task Finished...");
            log.info("Bot Manually Shutdown at : {}", Timestamp.from(Instant.now()).toLocalDateTime().format(DateTimeFormatter.ofPattern("dd MMMM yyyy hh:mm:ss")));

            System.exit(0);
        } else {
            event.replyFormat("You are not allowed to use slash commands%n Please reach out to  %s  and he can allow you to use commands ", event.getJDA().getUserById(416342612484554752L).getName()).queue();
        }
    }
}

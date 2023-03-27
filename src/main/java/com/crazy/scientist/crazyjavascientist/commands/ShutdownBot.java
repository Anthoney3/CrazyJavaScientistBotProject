package com.crazy.scientist.crazyjavascientist.commands;

import com.crazy.scientist.crazyjavascientist.commands.dnd.DNDService;
import com.crazy.scientist.crazyjavascientist.commands.dnd.dnd_entities.DNDAttendanceEntity;
import com.crazy.scientist.crazyjavascientist.commands.dnd.dnd_repos.DNDAttendanceRepo;
import com.crazy.scientist.crazyjavascientist.commands.dnd.enums.UnicodeResponses;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import javax.annotation.Nonnull;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.stereotype.Component;

import static com.crazy.scientist.crazyjavascientist.constants.StaticUtils.BOT_OWNER_ID;

@NoArgsConstructor
@Slf4j
@Component
public class ShutdownBot extends ListenerAdapter {


    private DNDAttendanceRepo dndAttendanceRepo;
    private DNDService dndService;
    private Environment environment;
    private ApplicationContext context;

    public ShutdownBot(DNDAttendanceRepo dndAttendanceRepo, DNDService dndService, Environment environment, ApplicationContext context) {
        this.dndAttendanceRepo = dndAttendanceRepo;
        this.dndService = dndService;
        this.environment = environment;
        this.context = context;
    }

    public void shutdownBot(boolean hasPermission, @Nonnull SlashCommandInteraction event) {
        if (hasPermission) {
            event.reply("Shutting down...").setEphemeral(true).queue();
            if (environment.acceptsProfiles(Profiles.of("local")))
                    SpringApplication.exit(context,()-> 0);
            else {
                log.info("Bot Shutting down...");
                event.getJDA().shutdown();
                log.info("Saving Player Responses to Database...");
                dndService.getDiscord_response().forEach((k, v) -> {
                    switch(v.getResponse_emoji_unicode()){
                        case NO_SHOW_NO_RESPONSE -> {
                            dndAttendanceRepo.save(new DNDAttendanceEntity(k, v.getPlayer_name(), "N", "N", "Y"));
                            log.info("Entity {} Saved to the DB Successfully", new DNDAttendanceEntity(k, v.getPlayer_name(), "N", "N", "Y"));
                        }
                        case ATTENDING -> {
                            dndAttendanceRepo.save(new DNDAttendanceEntity(k, v.getPlayer_name(), "Y", "N", "N"));
                            log.info("Entity {} Saved to the DB Successfully", new DNDAttendanceEntity(k, v.getPlayer_name(), "Y", "N", "N"));
                        }
                        case EXCUSED -> {
                            dndAttendanceRepo.save(new DNDAttendanceEntity(k, v.getPlayer_name(), "N", "Y", "N"));
                            log.info("Entity {} Saved to the DB Successfully", new DNDAttendanceEntity(k, v.getPlayer_name(), "N", "Y", "N"));
                        }
                    }
                });
                log.info("DB Upload Task Finished...");
                log.info("Bot Manually Shutdown at : {}", Timestamp.from(Instant.now()).toLocalDateTime().format(DateTimeFormatter.ofPattern("dd MMMM yyyy hh:mm:ss")));

                SpringApplication.exit(context, () -> 0);
            }
        } else {
            event.reply("You are not permitted to use this slash command.").setEphemeral(true).queue();
        }
    }
}

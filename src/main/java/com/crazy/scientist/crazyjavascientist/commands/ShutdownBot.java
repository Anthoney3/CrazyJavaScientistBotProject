package com.crazy.scientist.crazyjavascientist.commands;

import com.crazy.scientist.crazyjavascientist.dnd.dnd_repos.DNDAttendanceRepo;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.format.DateTimeFormatter;

import static com.crazy.scientist.crazyjavascientist.config.DiscordBotConfigJDAStyle.player_responses;


@NoArgsConstructor
@Slf4j
@Component
public class ShutdownBot extends ListenerAdapter {

    @Autowired
    private DNDAttendanceRepo dndAttendanceRepo;

    public void shutdownBot(boolean hasPermission,@Nonnull SlashCommandInteraction event){

        if(hasPermission) {

            event.reply("Shutting down...").queue();
            log.info("Bot Shutting down...");
            event.getJDA().shutdown();
            log.info("Saving Player Responses to Database...");
            dndAttendanceRepo.saveAll(player_responses.values());
            log.info("DB Upload Task Finished...");
            log.info("Bot Manually Shutdown at : {}", Timestamp.from(Instant.now()).toLocalDateTime().format(DateTimeFormatter.ofPattern("dd MMMM yyyy hh:mm:ss")));

            System.exit(0);
        }else{
            event.replyFormat("You are not allowed to use slash commands%n Please reach out to  %s  and he can allow you to use commands ", event.getJDA().getUserById(416342612484554752L).getName()).queue();
        }


    }
}

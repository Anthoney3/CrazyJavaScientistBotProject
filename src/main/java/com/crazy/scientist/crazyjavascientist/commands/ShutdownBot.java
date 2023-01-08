package com.crazy.scientist.crazyjavascientist.commands;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.sql.Timestamp;
import java.time.Instant;


@NoArgsConstructor
@Slf4j
@Component
public class ShutdownBot extends ListenerAdapter {

    public void shutdownBot(boolean hasPermission,@Nonnull SlashCommandInteraction event){

        if(hasPermission) {

            event.reply("Shutting down...").queue();
            log.info("Bot Shutting down...");
            event.getJDA().shutdown();

            log.info("Bot Shutdown at :{}", Timestamp.from(Instant.now()));

            System.exit(0);
        }else{
            event.replyFormat("You are not allowed to use slash commands%n Please reach out to  %s  and he can allow you to use commands ", event.getJDA().getUserById(416342612484554752L).getName()).queue();
        }


    }
}

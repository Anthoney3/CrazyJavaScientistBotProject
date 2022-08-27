package com.crazy.scientist.crazyjavascientist.commands;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;


@NoArgsConstructor
@Slf4j
public class ShutdownBot extends ListenerAdapter {

    public void shutdownBot(@Nonnull SlashCommandInteraction event){

            event.reply("Shutting down...").queue();
            log.info("Bot Shutting down...");
            event.getJDA().shutdown();

            log.info("Bot Shutdown at :{}", Timestamp.from(Instant.now()));

            System.exit(0);


    }
}

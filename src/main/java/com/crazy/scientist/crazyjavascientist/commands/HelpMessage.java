package com.crazy.scientist.crazyjavascientist.commands;

import lombok.NoArgsConstructor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;


@NoArgsConstructor
public class HelpMessage extends ListenerAdapter {



    public void onHelpSlashCommand(@NotNull SlashCommandInteractionEvent event) {


        if(event.getName().equalsIgnoreCase("help")){

            EmbedBuilder helpMessage = new EmbedBuilder();

            helpMessage.setAuthor("☕ Java Masochist ☕")
                    .setTitle("Crazy Java Scientist Commands")
                    .addField("/feedback email-id: (true, false)"," test ",false);

            MessageEmbed helpMessageEmbed = helpMessage.build();





            event.replyEmbeds(helpMessageEmbed).queue();

        }
    }


}

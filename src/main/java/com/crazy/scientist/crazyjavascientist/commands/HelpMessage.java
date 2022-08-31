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
@Component
public class HelpMessage extends ListenerAdapter {


    public void onHelpSlashCommand(@NotNull SlashCommandInteractionEvent event) {


        if(event.getName().equalsIgnoreCase("help")){

            switch(event.getGuild().getName()){
                case "The Java Way"->{
                    EmbedBuilder helpMessage = new EmbedBuilder();

                    helpMessage.setAuthor("☕ Java Masochist ☕")
                            .setTitle("Crazy Java Scientist Commands")
                            .addField("/feedback (Options)email [true:false]","Opens a dialog box allowing you to send feedback to the bot owner, Set the option to true for an email to be sent and false to a dm to be sent ",false)
                            .addField("/search (Required)prompt ","Allows you to make a google image search based on your prompt and will return to you an image",false)
                            .addField("/add-to-showcase (Required)message-id","Allows you to put your images from the AI generation bot into the show-n-tell channel",false);
                    MessageEmbed helpMessageEmbed = helpMessage.build();
                    event.replyEmbeds(helpMessageEmbed).queue();
                }
                case "Osu Chads"->{
                    EmbedBuilder helpMessage = new EmbedBuilder();

                    helpMessage.setAuthor("☕ Java Masochist ☕")
                            .setTitle("Crazy Java Scientist Commands")
                            .addField("/feedback (Options)email [true:false]","Opens a dialog box allowing you to send feedback to the bot owner, Set the option to true for an email to be sent and false to a dm to be sent ",false)
                            .addField("/get-osu-stats (Required)username [servername]","Allows you to obtain a user's osu stats based on their server name",false);
                    MessageEmbed helpMessageEmbed = helpMessage.build();
                    event.replyEmbeds(helpMessageEmbed).queue();
                }
            }


        }
    }


}

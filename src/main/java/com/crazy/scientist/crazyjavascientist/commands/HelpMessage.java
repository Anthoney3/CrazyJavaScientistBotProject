package com.crazy.scientist.crazyjavascientist.commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class HelpMessage extends ListenerAdapter {


    private final List<CommandData> commands = new ArrayList<>(List.of(Commands.slash("help","Shows a list of commands for Crazy Java Scientist bot")));

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {

        if(event.getName().equalsIgnoreCase("help")){

            EmbedBuilder helpMessage = new EmbedBuilder();

            helpMessage.setAuthor("Java Masochist")
                    .addField("This is a Test Field","25",false)
                    .appendDescription("This is an appended Description")
                    .setDescription("This is the main set Description")
                    .setTitle("This is going to be the help message!");



            event.getChannel().sendMessageFormat("%s",helpMessage.build()).queue();

        }
    }

    @Override
    public void onGuildReady(@NotNull GuildReadyEvent event) {
        event.getGuild().updateCommands().addCommands(this.commands).queue();
    }
}

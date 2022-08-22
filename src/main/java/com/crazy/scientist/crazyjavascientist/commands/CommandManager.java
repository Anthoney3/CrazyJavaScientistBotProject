package com.crazy.scientist.crazyjavascientist.commands;

import com.crazy.scientist.crazyjavascientist.SendMail;
import com.crazy.scientist.crazyjavascientist.config.DiscordBotConfigJDAStyle;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.exceptions.RateLimitedException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.managers.channel.concrete.TextChannelManager;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.internal.requests.Route;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.security.auth.login.LoginException;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.stream.Collector;
import java.util.stream.Collectors;
@Slf4j
@Data

public class CommandManager extends ListenerAdapter {


    private FeedBackCommand feedBackCommand = new FeedBackCommand();
    private HelpMessage helpMessage = new HelpMessage();

    private List<CommandData> commands = new ArrayList<>(List.of(Commands.slash("add-to-showcase", "Adds the last thing in the channel to the show case")
            .addOption(OptionType.STRING,"message-id","The message id of what you wish to showcase", true),
            Commands.slash("get-message-history","Shows Server Message History.")
                    .addOption(OptionType.STRING,"msg-id","ID of the message you wish to find",true),
            Commands.slash("help","Shows a list of commands for Crazy Java Scientist bot"),
            Commands.slash("feedback", "Send feedback to the bot owner.")
                    .addOption(OptionType.BOOLEAN,"email","Sends an email to the bot owner with the feedback given",true)));
    public CommandManager() {

    }




    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        String command = event.getName();


        feedBackCommand.onFeedbackSlashCommand(event);
        helpMessage.onHelpSlashCommand(event);


        if(command.equalsIgnoreCase("get-message-history")){


            Message lastMessage = event.getChannel().retrieveMessageById(Objects.requireNonNull(event.getOption("msg-id")).getAsString()).complete();

            if(lastMessage.getReferencedMessage() == null){
                if(lastMessage.getAttachments().isEmpty()){
                    event.getChannel().sendMessageFormat("%s%n%nTake a look Here!%n%s", lastMessage.getContentDisplay(),lastMessage.getJumpUrl()).queue();
                }else {
                    event.getChannel().sendMessageFormat("%s%n%nTake a look Here!%n%s", lastMessage.getContentDisplay(), lastMessage.getAttachments().get(0).getUrl()).queue();
                    log.info(lastMessage.toString());
                }
            }else{
                event.getChannel().sendMessageFormat("Messages Found:%n" + "%s%n%s", lastMessage.getReferencedMessage().getContentDisplay(), lastMessage.getAttachments().get(0).getUrl()).queue();
                log.info(lastMessage.toString());
            }



        }else if(command.equalsIgnoreCase("add-to-showcase")) {

            log.info("add-to-showcase was called for some fuckin reason");


            TextChannel textChannel1 = (TextChannel) Objects.requireNonNull(event.getGuild()).getGuildChannelById(1010606877236789319L);

            log.info(event.getOption("message-id").getAsString());

            if (textChannel1 != null) {

                Message messageToBeMoved = event.getChannel().retrieveMessageById(Objects.requireNonNull(event.getOption("message-id")).getAsString()).complete();
                event.reply("Your message was sent to " + textChannel1.getName() + "!").queue();
                textChannel1.sendMessage(event.getUser().getAsTag() + " Added this beauty to Show case! \n").queue();
                textChannel1.sendMessageFormat("%s%n%s",messageToBeMoved.getReferencedMessage(), messageToBeMoved.getAttachments().get(0).getUrl()).queue();
            }
        }
    }


    @Override
    public void onModalInteraction(@NotNull ModalInteractionEvent event) {

       feedBackCommand.onFeedbackModal(event);
    }



    @Override
    public void onGuildReady(@NotNull GuildReadyEvent event) {
       event.getGuild().updateCommands().addCommands(this.commands).queue();
    }

    @Override
    public void onGuildJoin(@NotNull GuildJoinEvent event) {
       event.getGuild().updateCommands().addCommands(this.commands).queue();
    }

}

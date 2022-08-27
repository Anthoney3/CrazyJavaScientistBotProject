package com.crazy.scientist.crazyjavascientist.commands;

import com.crazy.scientist.crazyjavascientist.OAuthToken;
import com.crazy.scientist.crazyjavascientist.OsuApiCall;
import com.crazy.scientist.crazyjavascientist.SendMail;
import com.crazy.scientist.crazyjavascientist.config.DiscordBotConfigJDAStyle;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
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
import net.dv8tion.jda.api.sharding.ShardManager;
import net.dv8tion.jda.internal.entities.RoleImpl;
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
@Component
public class CommandManager extends ListenerAdapter {



    private FeedBackCommand feedBackCommand = new FeedBackCommand();
    private HelpMessage helpMessage = new HelpMessage();
    private GoogleSearch googleSearch = new GoogleSearch();
    private ShutdownBot shutdownBot = new ShutdownBot();
    private OsuApiCall osuApiCall = new OsuApiCall();





    private List<CommandData> commands = new ArrayList<>(List.of(Commands.slash("add-to-showcase", "Adds the last thing in the channel to the show case")
            .addOption(OptionType.STRING,"message-id","The message id of what you wish to showcase", true),
            Commands.slash("get-message-history","Shows Server Message History.")
                    .addOption(OptionType.STRING,"msg-id","ID of the message you wish to find",true),
            Commands.slash("help","Shows a list of commands for Crazy Java Scientist bot"),
            Commands.slash("feedback", "Send feedback to the bot owner.")
                    .addOption(OptionType.BOOLEAN,"email","Sends an email to the bot owner with the feedback given",true),
            Commands.slash("search","Google Search: In testing").addOption(OptionType.STRING,"prompt","what images you're looking for",true),
            Commands.slash("logout","Kills the bot and shuts it down"),
            Commands.slash("get-search-history","Retrieves the bots google search history"),
            Commands.slash("get-osu-stats","Gets a user's stats for osu").addOption(OptionType.STRING,"user-id","The user id can be found in the address bar of the user",true)));
    public CommandManager() {

    }





    public CommandManager(FeedBackCommand feedBackCommand, HelpMessage helpMessage, GoogleSearch googleSearch, ShutdownBot shutdownBot) {
        this.feedBackCommand = feedBackCommand;
        this.helpMessage = helpMessage;
        this.googleSearch = googleSearch;
        this.shutdownBot = shutdownBot;
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        String command = event.getName();

        boolean isAllowedToUseCommand = event.getUser().getIdLong() == 416342612484554752L;







        try {
            if (isAllowedToUseCommand) {
                switch (event.getName()) {
                    case "feedback" -> feedBackCommand.onFeedbackSlashCommand(event);
                    case "help" -> helpMessage.onHelpSlashCommand(event);
                    case "get-search-history" -> googleSearch.onSearchHistoryCommand(event);
                    case "search" -> googleSearch.onSearchCommand(event);
                    case "logout" -> shutdownBot.shutdownBot(event);
                    case "get-osu-stats" -> osuApiCall.makeOsuAPICall(event);
                    default -> {
                       /* if (command.equalsIgnoreCase("get-message-history")) {


                            Message lastMessage = event.getChannel().retrieveMessageById(Objects.requireNonNull(event.getOption("msg-id")).getAsString()).complete();

                            if (lastMessage.getReferencedMessage() == null) {
                                if (lastMessage.getAttachments().isEmpty()) {
                                    event.getChannel().sendMessageFormat("%s%n%nTake a look Here!%n%s", lastMessage.getContentDisplay(), lastMessage.getJumpUrl()).queue();
                                } else {
                                    event.getChannel().sendMessageFormat("%s%n%nTake a look Here!%n%s", lastMessage.getContentDisplay(), lastMessage.getAttachments().get(0).getUrl()).queue();
                                    log.info(lastMessage.toString());
                                }
                            } else {
                                event.getChannel().sendMessageFormat("%s%n%s", lastMessage.getReferencedMessage().getContentDisplay(), lastMessage.getAttachments().get(0).getUrl()).queue();
                                log.info(lastMessage.toString());
                            }


                        }*/
                        if (command.equalsIgnoreCase("add-to-showcase")) {


                            List<String> responseMessages = new ArrayList<>();

                            String userName = event.getUser().getName();
                            responseMessages.add(userName + " Added this beauty to Show off!");
                            responseMessages.add(userName + " is such a show off!");
                            responseMessages.add(userName + " You're gonna have to teach me how you did this one!");
                            responseMessages.add(userName + " I'm so Jelly!");

                            int messagePick = (int) (Math.random() * responseMessages.size());

                            TextChannel textChannel1 = (TextChannel) Objects.requireNonNull(event.getGuild()).getGuildChannelById(1010606877236789319L);


                            if (textChannel1 != null) {
                                log.info("Message " + event.getOption("message-id").getAsString() + " was added to the " + textChannel1.getName());
                                Message messageToBeMoved = event.getChannel().retrieveMessageById(Objects.requireNonNull(event.getOption("message-id")).getAsString()).complete();
                                event.reply("Your message was sent to " + textChannel1.getName() + "!").queue();
                                textChannel1.sendMessage(responseMessages.get(messagePick)).queue();
                                textChannel1.sendMessageFormat("%s%n%s", messageToBeMoved.getReferencedMessage(), messageToBeMoved.getAttachments().get(0).getUrl()).queue();
                            }
                        }
                    }
                }

            }else{
                event.replyFormat("You are not allowed to use slash commands%n Please reach out to  %s  and he can allow you to use commands ", event.getJDA().getUserById(416342612484554752L).getName()).queue();
            }
            }catch(Exception e){

                event.reply("Something went wrong, " + event.getGuild().getMemberById(416342612484554752L) + " will take a look into it...").queue();
                e.printStackTrace();
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

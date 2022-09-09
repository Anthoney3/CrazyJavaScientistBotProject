package com.crazy.scientist.crazyjavascientist.commands;

import com.crazy.scientist.crazyjavascientist.enums.TaskManagerStatus;
import com.crazy.scientist.crazyjavascientist.models.UserTaskTable;
import com.crazy.scientist.crazyjavascientist.osu.OsuApiCall;
import com.crazy.scientist.crazyjavascientist.repos.UserTaskTableI;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Slf4j
@Component
public class CommandManager extends ListenerAdapter {


    @Autowired
    private FeedBackCommand feedBackCommand;
    @Autowired
    private HelpMessage helpMessage;
    @Autowired
    private GoogleSearch googleSearch;
    @Autowired
    private ShutdownBot shutdownBot;
    @Autowired
    private OsuApiCall osuApiCall;
    @Autowired
    private TaskManager taskManager;

    @Autowired
    private UserTaskTableI userTaskTableI;


    private final List<CommandData> globalCommands = new ArrayList<>(List.of(Commands.slash("feedback", "Send feedback to the bot owner.")
                    .addOption(OptionType.BOOLEAN, "email", "Sends an email to the bot owner with the feedback given", true),
            Commands.slash("help", "Shows a list of commands for Crazy Java Scientist bot"),
            Commands.slash("delete-task-list", "Allows you to delete a task list by its title")
                    .addOption(OptionType.STRING, "title", "The title of the task list you wish to delete", true)
    ));
    private final List<CommandData> osuChadGuildCommands = new ArrayList<>(List.of(Commands.slash("get-osu-stats", "Gets a user's stats for osu").addOption(OptionType.STRING, "username", "Uses the users server name to search for stats; Ex. 1 searches for 1's stats", true)));


    private final List<CommandData> theJavaWayGuildCommands = new ArrayList<>(List.of(Commands.slash("add-to-showcase", "Adds the last thing in the channel to the show case")
                    .addOption(OptionType.STRING, "message-id", "The message id of what you wish to showcase", true),
            Commands.slash("get-message-history", "Shows Server Message History.")
                    .addOption(OptionType.STRING, "msg-id", "ID of the message you wish to find", true),
            Commands.slash("search", "Google Search: In testing").addOption(OptionType.STRING, "prompt", "what images you're looking for", true),
            Commands.slash("logout", "Kills the bot and shuts it down"),
            Commands.slash("get-search-history", "Retrieves the bots google search history")));


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

            switch (event.getName()) {
                case "feedback" -> feedBackCommand.onFeedbackSlashCommand(event);
                case "help" -> helpMessage.onHelpSlashCommand(event);
                case "get-search-history" -> googleSearch.onSearchHistoryCommand(isAllowedToUseCommand, event);
                case "search" -> googleSearch.onSearchCommand(isAllowedToUseCommand, event);
                case "logout" -> shutdownBot.shutdownBot(isAllowedToUseCommand, event);
                case "get-osu-stats" -> osuApiCall.makeOsuAPICall(event);
                case "create-task-list" -> taskManager.createNewUserList(isAllowedToUseCommand, event);
                case "delete-task-list" -> taskManager.deleteUserTaskListByTitle(isAllowedToUseCommand, event);
                default -> {
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



        } catch (Exception e) {

                event.reply("Something went wrong, " + event.getJDA().getUserById(416342612484554752L).getName() + " will take a look into it...").queue();
            e.printStackTrace();
        }


    }


    @Override
    public void onModalInteraction(@NotNull ModalInteractionEvent event) {

        feedBackCommand.onFeedbackModal(event);
    }

    @Override
    public void onGuildReady(@NotNull GuildReadyEvent event) {

        switch (event.getGuild().getName()) {
            case "The Java Way" -> {
                event.getGuild().updateCommands().addCommands(this.theJavaWayGuildCommands).queue();
//                event.getGuild().updateCommands().addCommands(this.osuChadGuildCommands).queue();
            }
            case "Osu Chads" -> event.getGuild().updateCommands().addCommands(this.osuChadGuildCommands).queue();
        }

    }


    @Override
    public void onReady(@NotNull ReadyEvent event) {

        OptionData statusOption = new OptionData(OptionType.STRING, "status", "The status of completion for the first task", false)
                .addChoice("Not Started", TaskManagerStatus.NOT_STARTED.getStatus())
                .addChoice("In Progress", TaskManagerStatus.IN_PROGRESS.getStatus())
                .addChoice("Completed", TaskManagerStatus.COMPLETED.getStatus());


        globalCommands.add(Commands.slash("create-task-list", "Allows you to create a Task List where you can store any tasks you may need to do")
                .addOption(OptionType.STRING, "title", "The title for the Task List", false)
                .addOption(OptionType.STRING, "description", "The description for the first task", false)
                .addOptions(statusOption)
                .addOption(OptionType.STRING, "comments", "Allows you to add a comment to the task, good for when a task is in progress and has an update", false));


        event.getJDA().updateCommands().addCommands(this.globalCommands).queue();
    }
}

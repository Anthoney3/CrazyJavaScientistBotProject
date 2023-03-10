package com.crazy.scientist.crazyjavascientist.commands;

import com.crazy.scientist.crazyjavascientist.dnd.CancelDND;
import com.crazy.scientist.crazyjavascientist.dnd.DNDService;
import com.crazy.scientist.crazyjavascientist.schedulers.DNDScheduledTasks;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Getter
@Setter
@ToString
@Component
public class CommandManager extends ListenerAdapter {
    private FeedBackCommand feedBackCommand;

    private EventCreator eventCreator;
    private HelpMessage helpMessage;
    private ShutdownBot shutdownBot;
    private AIArtGeneration aiArtGeneration;
    private DNDScheduledTasks dndScheduledTasks;
    private DNDService dndService;
    private CancelDND cancelDND;
    private final List<CommandData> globalCommands = new ArrayList<>(List.of(Commands.slash("feedback", "Send feedback to the bot owner.").addOption(OptionType.BOOLEAN, "email", "Sends an email to the bot owner with the feedback given", true), Commands.slash("help", "Shows a list of commands for Crazy Java Scientist bot"), Commands.slash("logout", "Kills the bot and shuts it down"), Commands.slash("delete-task-list", "Allows you to delete a task list by its title").addOption(OptionType.STRING, "title", "The title of the task list you wish to delete", true), Commands.slash("get-message-history", "Shows Server Message History.").addOption(OptionType.STRING, "msg-id", "ID of the message you wish to find", true)));
    private final List<CommandData> theJavaWayGuildCommands = new ArrayList<>(
            List.of(
                    Commands.slash("add-to-showcase", "Adds the last thing in the channel to the show case").addOption(OptionType.STRING, "message-id", "The message id of what you wish to showcase", true),
                    Commands.slash("search", "Google Search: In testing").addOption(OptionType.STRING, "prompt", "what images you're looking for", true),
                    Commands.slash("get-search-history", "Retrieves the bots google search history"),
                    Commands.slash("cancel-dnd", "Cancels DND For the week, turning off DND features"),
                    Commands.slash("create-new-event", "Creates a new Discord Server Event")
            )
    );


    public CommandManager(FeedBackCommand feedBackCommand, HelpMessage helpMessage, ShutdownBot shutdownBot,
                          AIArtGeneration aiArtGeneration, DNDScheduledTasks dndScheduledTasks, DNDService dndService,
                          CancelDND cancelDND, EventCreator eventCreator) {
        this.feedBackCommand = feedBackCommand;
        this.helpMessage = helpMessage;
        this.shutdownBot = shutdownBot;
        this.aiArtGeneration = aiArtGeneration;
        this.dndScheduledTasks = dndScheduledTasks;
        this.dndService = dndService;
        this.cancelDND = cancelDND;
        this.eventCreator = eventCreator;
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {

        boolean isAllowedToUseCommand = event.getUser().getIdLong() == 416342612484554752L;

        try {
            switch (event.getName()) {
                case "feedback" -> feedBackCommand.onFeedbackSlashCommand(event);
                case "help" -> helpMessage.onHelpSlashCommand(event);
                case "logout" -> shutdownBot.shutdownBot(isAllowedToUseCommand, event);
                case "dnd-test" -> dndService.testingEmbedsWithActionRows(isAllowedToUseCommand, event);
                case "cancel-dnd" -> cancelDND.cancelDNDViaSlashCommand(event);
                case "add-to-showcase" -> aiArtGeneration.sendArtToShowcaseChannel(event);
                case "create-new-event" -> eventCreator.createNewDiscordEvent(event);
            }
        } catch (Exception e) {
            event.reply("Something went wrong, " + event.getJDA().getUserById(416342612484554752L).getName() + " will take a look into it...").queue();
            e.printStackTrace();
        }
    }

    @Override
    public void onModalInteraction(@NotNull ModalInteractionEvent event) {
        if (event.getModalId().contains("feedback")) feedBackCommand.onFeedbackModal(event);
        if (event.getModalId().equalsIgnoreCase("dnd-cancellation-modal")) cancelDND.onDNDModalCancellationEvent(event);

    }

    @Override
    public void onGuildReady(@NotNull GuildReadyEvent event) {
        switch (event.getGuild().getName()) {
            case "The Java Way" -> {
                event.getGuild().updateCommands().addCommands(this.theJavaWayGuildCommands).queue();
                //                event.getGuild().updateCommands().addCommands(this.osuChadGuildCommands).queue();
            }
        }
    }

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        event.getJDA().updateCommands().addCommands(this.globalCommands).queue();
    }

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {

        if (List.of("excused_button", "attending_button", "remove_button", "alpharius_button").contains(event.getButton().getId()))
            dndService.dndAttendanceButtonInteractionEvent(event);
        if (event.getButton().getId().equalsIgnoreCase("cancel-dnd"))
            cancelDND.cancellationButtonInteractionEvent(event);
        else if (event.getButton().getId().equalsIgnoreCase("cancel-dnd-cancellation"))
            event.reply("Yayyy DND lives on!").queue();

    }

}

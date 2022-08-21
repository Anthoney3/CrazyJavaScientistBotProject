package com.crazy.scientist.crazyjavascientist.commands;

import com.crazy.scientist.crazyjavascientist.SendMail;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.PrivateChannel;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Modal;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.utils.data.DataObject;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class FeedBackCommand extends ListenerAdapter {


    private List<CommandData> commands = new ArrayList<>(List.of(Commands.slash("feedback", "Send feedback to the bot owner.")
            .addOption(OptionType.BOOLEAN,"email","Sends an email to the bot owner with the feedback given")));
    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {

         if(event.getOption("email").getAsBoolean()){
            TextInput name = TextInput.create("email-feedback-name","Name", TextInputStyle.SHORT)
                    .setMinLength(1)
                    .setRequired(true)
                    .build();

            TextInput feedBackMessage = TextInput.create("email-feedback-message","Feedback Message",TextInputStyle.PARAGRAPH)
                    .setRequired(true)
                    .setMinLength(10)
                    .setMaxLength(500)
                    .setPlaceholder("Tell us what features you'd like to have added or how I am doing as a bot!")
                    .build();



            Modal feedBackModal = Modal.create("email-feedback-modal","Give Some Feedback!")
                    .addActionRows(ActionRow.of(name), ActionRow.of(feedBackMessage))
                    .build();

            event.replyModal(feedBackModal).queue();


        }else if(event.getName().equalsIgnoreCase("feedback")){

            TextInput name = TextInput.create("feedback-name","Name", TextInputStyle.SHORT)
                    .setMinLength(1)
                    .setRequired(true)
                    .build();

            TextInput feedBackMessage = TextInput.create("feedback-message","Feedback Message",TextInputStyle.PARAGRAPH)
                    .setRequired(true)
                    .setMinLength(10)
                    .setMaxLength(500)
                    .setPlaceholder("Tell us what features you'd like to have added or how I am doing as a bot!")
                    .build();



            Modal feedBackModal = Modal.create("feedback-modal","Give Some Feedback!")
                    .addActionRows(ActionRow.of(name), ActionRow.of(feedBackMessage))
                    .build();

            event.replyModal(feedBackModal).queue();


        }

    }





    @Override
    public void onModalInteraction(@NotNull ModalInteractionEvent event) {

        if(event.getModalId().equalsIgnoreCase("feedback-modal")){

            String name = event.getValue("feedback-name").getAsString();
            String feedbackMessage = event.getValue("feedback-message").getAsString();

            User botOwner = event.getJDA().retrieveUserById(416342612484554752L).complete();
            botOwner.openPrivateChannel().queue(privateChannel -> {

                privateChannel.sendMessageFormat("%s%n%n%s",name,feedbackMessage).queue();
            });




            event.reply("Your feedback has been heard! Thank you for sending feedback :)").queue();

        }else if(event.getModalId().equalsIgnoreCase("email-feedback-modal")){

            String name = event.getValue("email-feedback-name").getAsString();
            String feedbackMessage = event.getValue("email-feedback-message").getAsString();

            SendMail sendMail = new SendMail();

            sendMail.createAndSendEmailToMyEmail(String.format("%s%n%n%s",name,feedbackMessage));


            event.reply("Your feedback has been heard! Thank you for sending feedback :)").queue();
        }
    }

    @Override
    public void onGuildReady(@NotNull GuildReadyEvent event) {
        event.getGuild().updateCommands().addCommands(this.commands).queue();
    }


}

package com.crazy.scientist.crazyjavascientist.dnd;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.ItemComponent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import net.dv8tion.jda.internal.interactions.component.ButtonImpl;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import static com.crazy.scientist.crazyjavascientist.constants.StaticUtils.isDndCancelled;
import static com.crazy.scientist.crazyjavascientist.constants.StaticUtils.shardManager;

@Component
public class CancelDND extends ListenerAdapter {


    public void cancelDNDViaSlashCommand(@NotNull SlashCommandInteractionEvent event) {


        if(event.getUser().getIdLong() == 416342612484554752L || event.getUser().getIdLong() == 448620591944171521L) {

            Button cancel_dnd_cancellation_request = Button.danger(
                    "cancel-dnd-cancellation", "No"
            );
            Button cancel_dnd = Button.success(
                    "cancel-dnd", "Yes"
            );

            if (event.getName().equals("cancel-dnd")) {
                event.reply("Are You Sure you want to Cancel Dnd?").addActionRow(cancel_dnd,cancel_dnd_cancellation_request).queue();

            }
        }else{
            event.reply("You Don't Have Access to use this command!").setEphemeral(true).queue();
        }

    }

    public void cancellationButtonInteractionEvent(@NotNull ButtonInteractionEvent event){


        TextInput reason_for_cancellation = TextInput.create("dnd-cancellation-reason", "Reason For Cancellation", TextInputStyle.PARAGRAPH)
                .setMinLength(5)
                .setMaxLength(300)
                .setRequired(true)
                .setPlaceholder("Put the reason for why DND is cancelled here.")
                .build();

            Modal dnd_cancellation_modal = Modal.create("dnd-cancellation-modal", "DND Cancellation Form")
                    .addActionRows(ActionRow.of(reason_for_cancellation))
                    .build();
            event.replyModal(dnd_cancellation_modal).queue();
            isDndCancelled = true;
    }



    public void onDNDModalCancellationEvent(@NotNull ModalInteractionEvent event) {
        if (event.getModalId().equalsIgnoreCase("dnd-cancellation-modal")) {

            EmbedBuilder builder = new EmbedBuilder()
                    .setTitle("DND Cancelled")
                    .appendDescription("@here")
                    .addField("Reason For Cancellation",event.getValue("dnd-cancellation-reason").getAsString(),true)
                    .setThumbnail("https://yawningportal.org/wp-content/uploads/2019/09/dnddescentkeyartjpg-1.jpeg");
            shardManager.getTextChannelsByName("dark-n-dangerous-avanti", true).get(0).sendMessageEmbeds(builder.build()).queue();
            event.reply("Sorry to hear DND was cancelled, However the good new is your Message has been sent Successfully!").setEphemeral(true).queue();
        }
    }
}

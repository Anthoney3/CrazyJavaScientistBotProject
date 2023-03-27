package com.crazy.scientist.crazyjavascientist.commands.dnd;

import com.crazy.scientist.crazyjavascientist.constants.StaticUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class CancelDND extends ListenerAdapter {

    private final ApplicationContext context;

    public CancelDND(ApplicationContext context) {
        this.context = context;
    }

    public void cancelDNDViaSlashCommand(@NotNull SlashCommandInteractionEvent event) {
        if(event.getUser().getIdLong() != 448620591944171521L || event.getUser().getIdLong() != 416342612484554752L)
            event.reply("You Don't Have Access to use this command :P").setEphemeral(true).queue();
        else
            event.reply("Are You Sure you want to Cancel Dnd?").addActionRow(Button.success("cancel-dnd","Yes"),Button.danger("cancel-dnd-cancellation","No")).queue();
    }

    public void cancellationButtonInteractionEvent(@NotNull ButtonInteractionEvent event){
        if(event.getUser().getIdLong() != 448620591944171521L || event.getUser().getIdLong() != 416342612484554752L)
            event.reply("You don't have the authority to cancel DND :P").setEphemeral(true).queue();
        else{
            event.replyModal(
                            Modal.create("dnd-cancellation-modal", "DND Cancellation Form")
                                    .addActionRows(
                                            ActionRow.of(
                                                    TextInput.create("dnd-cancellation-reason", "Reason For Cancellation", TextInputStyle.PARAGRAPH)
                                                            .setMinLength(5)
                                                            .setMaxLength(300)
                                                            .setRequired(true)
                                                            .setPlaceholder("Put the reason for why DND is cancelled here.")
                                                            .build()
                                            )
                                    )
                                    .build()
                    )
                    .queue();
            StaticUtils.isDndCancelled = true;
        }
    }



    public void onDNDModalCancellationEvent(@NotNull ModalInteractionEvent event) {
        ShardManager shardManager = (ShardManager) context.getBean("shardManager");

        shardManager.getTextChannelsByName("dark-n-dangerous-avanti", true).get(0).sendMessageEmbeds(new EmbedBuilder()
                    .setTitle("DND Cancelled")
                    .appendDescription("@here")
                    .addField("Reason For Cancellation",event.getValue("dnd-cancellation-reason").getAsString(),true)
                    .setThumbnail("https://yawningportal.org/wp-content/uploads/2019/09/dnddescentkeyartjpg-1.jpeg").build())
                .queue();
        event.reply("Sorry to hear DND was cancelled, However the good new is your Message has been sent Successfully!").setEphemeral(true).queue();

    }
}

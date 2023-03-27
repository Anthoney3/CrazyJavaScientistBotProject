package com.crazy.scientist.crazyjavascientist.commands;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.interactions.modals.Modal;
import net.dv8tion.jda.internal.interactions.component.StringSelectMenuImpl;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@Slf4j
public class EventCreator {

    public void createNewDiscordEvent(@NotNull SlashCommandInteractionEvent event) {

//        event.reply("Please Select an Option Below")
//                        .addActionRow(StringSelectMenu.create("select-test-menu","placeholder",1,1,false,
//                                Collections.singletonList(SelectOption.of("test-option","This is a test Option")))
//                        .build())
//                .addCheck(check -> event.)


    }




}

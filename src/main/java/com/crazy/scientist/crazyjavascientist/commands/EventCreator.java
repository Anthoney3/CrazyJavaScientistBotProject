package com.crazy.scientist.crazyjavascientist.commands;

import com.crazy.scientist.crazyjavascientist.constants.StaticUtils;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ActionComponent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Component;
import net.dv8tion.jda.api.interactions.components.LayoutComponent;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import net.dv8tion.jda.internal.interactions.component.SelectMenuImpl;
import net.dv8tion.jda.internal.interactions.component.StringSelectMenuImpl;
import org.checkerframework.checker.units.qual.C;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;

import static com.crazy.scientist.crazyjavascientist.constants.StaticUtils.shardManager;

@Service
@Slf4j
public class EventCreator {

    public void createNewDiscordEvent(@NotNull SlashCommandInteractionEvent event){
        List<List<SelectOption>> option_lists = populateManualModalCalendarMenuDropdownItems();

        TextInput event_name = TextInput.create("event-title", "new-event-title", TextInputStyle.SHORT).build();
        TextInput event_location = TextInput.create("event-location", "new-event-location", TextInputStyle.SHORT).build();
        TextInput day = TextInput.create("event-day-numerical", "numerical-day-for-event", TextInputStyle.SHORT).build();
        StringSelectMenu month_select = new StringSelectMenuImpl("month-select","January",1,1,false,option_lists.get(0));

        StringSelectMenu year_select = new StringSelectMenuImpl("year-select",String.valueOf(ZonedDateTime.now().getYear()),1,1,false,option_lists.get(2));

        EmbedBuilder builder = new EmbedBuilder()
                .setTitle("Test");
//        .addActionRow(month_select).addActionRow(day).addActionRow(year_select).queue();
        event.reply("Test").addActionRow(day).queue();
    }

    private List<List<SelectOption>> populateManualModalCalendarMenuDropdownItems(){
        String[] name_of_months = {"January","February","March","April","May","June","July","August","September","October","November","December"};
        List<SelectOption> months = new ArrayList<>();
        List<SelectOption> days = new ArrayList<>();
        List<SelectOption> years = new ArrayList<>();

        for(String month_declaration : name_of_months){
            months.add(SelectOption.of(month_declaration.toLowerCase(),month_declaration));
        }
        for(int i = 0; i < 31; i++){
            days.add(SelectOption.of(String.valueOf(i),String.valueOf(i)));
        }

        for(int i=ZonedDateTime.now().getYear() - 5; i < ZonedDateTime.now().getYear() + 5; i++){
            years.add(SelectOption.of(String.valueOf(i), String.valueOf(i)));
        }

        List<List<SelectOption>> option_lists = new ArrayList<>();
        option_lists.add(months);
        option_lists.add(days);
        option_lists.add(years);

        return option_lists;


    }



}

package com.crazy.scientist.crazyjavascientist.commands;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.internal.requests.Route;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class Greetings extends ListenerAdapter {

    /**
     * Create a command that greets users based on role status and ensure that the greeting is dynamic
     * so you can add multiple responses for each role.
     */


    @Override
    public void onGuildMemberJoin(@NotNull GuildMemberJoinEvent event) {


        if(event.getJDA().getGuildById(952394376640888853L).getName().equalsIgnoreCase(event.getGuild().getName())) {
            event.getGuild().getDefaultChannel().sendMessageFormat("Welcome to %s!  %s  will you be able to climb your way up the chain",event.getGuild().getName(), event.getUser().getName()).queue();
        }
        if(event.getJDA().getGuildById(1008992976090963988L).getName().equalsIgnoreCase(event.getGuild().getName())) {
            event.getGuild().getDefaultChannel().sendMessageFormat("Welcome to !  %s  get settled right in! We won't bite  :eyes:",event.getGuild().getName(), event.getUser().getName()).queue();
        }


        }



}

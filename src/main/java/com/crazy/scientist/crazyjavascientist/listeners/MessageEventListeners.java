package com.crazy.scientist.crazyjavascientist.listeners;


import com.crazy.scientist.crazyjavascientist.commands.GoogleSearch;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;


import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

@Component
public class MessageEventListeners extends ListenerAdapter {

    private int imageNum=0;
    private GoogleSearch googleSearch = new GoogleSearch();

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {

        String fullMessage = event.getMessage().getContentStripped();

        Pattern filter = Pattern.compile("(?:y.*is.*tch)+|(?:.*a.*tch)+|(?:y.*tch)+|(?:bitch)+|(?:hoe)+",Pattern.CASE_INSENSITIVE);

        List<String> urlNoUResponses = new ArrayList<>();

        urlNoUResponses.add("https://c.tenor.com/pcUQG4C8TYoAAAAd/no-u-reverse.gif");
        urlNoUResponses.add("https://c.tenor.com/LAcYOwpSIpcAAAAC/yugioh-no-u.gif");
        urlNoUResponses.add("https://c.tenor.com/qN7TyYik_ssAAAAC/chad-no-you.gif");
        urlNoUResponses.add("https://c.tenor.com/aNU9cV5kJHUAAAAC/no-u-no-u-anime.gif");
        urlNoUResponses.add("https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcR2m8hBFmagfzZmcxP6eHRqRhq17mGB1TuEew&usqp=CAU");
        urlNoUResponses.add("https://c.tenor.com/LegsP6p4ef0AAAAd/doggo-eyebrow.gif");
        urlNoUResponses.add("https://c.tenor.com/0UUZY1ioDUYAAAAM/no-you.gif");


        if(filter.matcher(fullMessage).matches()){

            MessageEmbed responseMessage = new EmbedBuilder()
                    .setColor(Color.red)
                    .setImage(urlNoUResponses.get((int)(Math.random()*urlNoUResponses.size())))
                    .build();

            event.getChannel().sendMessageEmbeds(responseMessage).queue();
        }


        if(fullMessage.matches("!next")){


            googleSearch.onNextCall(event);


        }




    }

    @Override
    public void onMessageReactionAdd(@NotNull MessageReactionAddEvent event) {

    }


}

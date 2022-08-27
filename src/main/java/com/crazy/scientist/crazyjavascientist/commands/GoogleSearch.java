package com.crazy.scientist.crazyjavascientist.commands;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.WebhookClientBuilder;
import club.minnced.discord.webhook.external.JDAWebhookClient;
import club.minnced.discord.webhook.send.WebhookEmbed;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import com.crazy.scientist.crazyjavascientist.models.UserHistoryItem;
import com.google.gson.Gson;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import net.dv8tion.jda.api.requests.RestAction;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;

@Slf4j
@NoArgsConstructor
public class GoogleSearch extends ListenerAdapter {

    public static List<String> imageUrls = new ArrayList<>();
    public static List<UserHistoryItem> history = new ArrayList<>();
    private  int imageNum = 1;


    public void onNextCall(@NotNull MessageReceivedEvent event) {



        if(event.getMessage().getContentStripped().matches("!next")){



            EmbedBuilder responseMessage = new EmbedBuilder();

            responseMessage.setTitle("Next Google Image");

            responseMessage.setImage(imageUrls.get(imageNum));


            MessageEmbed builtMessage = responseMessage.build();


            event.getChannel().sendMessageEmbeds(builtMessage).queue();

            imageNum++;

        }
    }

    public void onSearchHistoryCommand(@Nonnull SlashCommandInteraction event){

        if(event.getName().equalsIgnoreCase("get-search-history")) {

            if (history.isEmpty()) {

                event.reply("There have been no searches so far").queue();
            } else {

                EmbedBuilder builder = new EmbedBuilder()
                        .setTitle("Bot Google Search History");

               history.forEach(item ->{

                   builder.addField(item.getUserName(),item.getSearchPrompt(),false);

               });




                MessageEmbed messageEmbed = builder.build();



                event.replyEmbeds(messageEmbed).queue();
            }
        }
    }

    public void onSearchCommand(@Nonnull SlashCommandInteraction event) {


        if(event.getName().equalsIgnoreCase("search")){

            imageUrls.clear();
            try {

                String commandStringSanitized =  event.getCommandString().replace("/search prompt:","").replace(" ", "%20");

                history.add(new UserHistoryItem(event.getUser().getName(),commandStringSanitized.replaceAll("%20"," ") ));

                log.info(event.getCommandString());

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("https://google-image-search1.p.rapidapi.com/v2/?q="+commandStringSanitized + "&hl=en"))
                        .header("X-RapidAPI-Key", "d3df70e112mshecb68b37e756f03p1238d6jsnd9f6c87c3a02")
                        .header("X-RapidAPI-Host", "google-image-search1.p.rapidapi.com")
                        .method("GET", HttpRequest.BodyPublishers.noBody())
                        .build();
                HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

                JSONObject beforeJSONArray = new JSONObject(response.body());




               JSONArray incomingJson = new JSONArray(beforeJSONArray.getJSONObject("response").getJSONArray("images"));


               for(int i=0; i<incomingJson.length();i++){


                   imageUrls.add(incomingJson.getJSONObject(i).getJSONObject("image").getString("url"));

               }

                EmbedBuilder responseMessage = new EmbedBuilder();

                responseMessage.setTitle("Google Search Results: " + event.getCommandString().replace("/search prompt:", "").trim());


                List<MessageEmbed> images = new ArrayList<>();


                EmbedBuilder embedBuilder = new EmbedBuilder().setTitle("First 10 Image Results:" + commandStringSanitized.replaceAll("%20", " "));

                for(int i=0;i < 10;i++){

                    images.add(new EmbedBuilder().setImage(imageUrls.get(i)).build());
                }










                for(int i=0; i < 10; i++){

                  embedBuilder.addField("Image" + (i + 1) + ": ", imageUrls.get(i) + "\n\n",false);
                }





                              event.replyEmbeds(images).queue();



            }catch (Exception e){

                event.reply("Something went wrong, " + event.getJDA().getUserById(416342612484554752L).getName() + " will take a look into it...").queue();
                e.printStackTrace();
            }
        }

    }
}

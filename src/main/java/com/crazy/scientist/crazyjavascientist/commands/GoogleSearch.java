package com.crazy.scientist.crazyjavascientist.commands;

import com.google.gson.Gson;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

@Slf4j
@NoArgsConstructor
public class GoogleSearch extends ListenerAdapter {

    public void onSearchCommand(@Nonnull SlashCommandInteraction event) {


        if(event.getName().equalsIgnoreCase("search")){
            try {

                log.info(event.getCommandString());

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("https://google-image-search1.p.rapidapi.com/v2/?q=" + event.getCommandString().replace("/search prompt:","").replaceAll(" ","%20").trim() + "&hl=en"))
                        .header("X-RapidAPI-Key", "964e31fcecmshe56858c69654838p19fec7jsn440abb1f8009")
                        .header("X-RapidAPI-Host", "google-image-search1.p.rapidapi.com")
                        .method("GET", HttpRequest.BodyPublishers.noBody())
                        .build();
                HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

                JSONObject beforeJSONArray = new JSONObject(response.body());


               JSONArray incomingJson = new JSONArray(beforeJSONArray.getJSONObject("response").getJSONArray("images"));
                List<String> imageUrls = new ArrayList<>();

               for(int i=0; i<10;i++){


                   imageUrls.add(incomingJson.getJSONObject(i).getJSONObject("image").getString("url"));

               }


                EmbedBuilder responseMessage = new EmbedBuilder();

                responseMessage.setTitle("Google Search Results: " + event.getCommandString().replace("/search prompt:", "").trim());

                responseMessage.setImage(imageUrls.get(0));

                MessageEmbed builtMessage = responseMessage.build();

                event.replyEmbeds(builtMessage).queue();

            }catch (Exception e){

                event.reply("Oops something went wrong :(").queue();
                e.printStackTrace();
            }
        }

    }
}

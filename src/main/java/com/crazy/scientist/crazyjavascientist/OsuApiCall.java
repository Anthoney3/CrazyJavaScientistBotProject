package com.crazy.scientist.crazyjavascientist;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import org.json.JSONObject;

import javax.annotation.Nonnull;
import java.awt.*;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
public class OsuApiCall {



    public void makeOsuAPICall(@Nonnull SlashCommandInteraction event)  {

        if (event.getName().equalsIgnoreCase("get-osu-stats")) {

            try {

                URL url = new URL("https://osu.ppy.sh/api/v2/users/"+ event.getOption("user-id").getAsString() + "/osu?key=id");

                StringBuilder returnResponse = new StringBuilder();


                HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                connection.setRequestMethod("GET");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("Authorization", "Bearer " + OAuthToken.token);


                try (BufferedReader bf = new BufferedReader(new InputStreamReader(
                        connection.getInputStream()))) {
                    String line;
                    while ((line = bf.readLine()) != null) {

                        returnResponse.append(line);

                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                JSONObject responseObject = new JSONObject(returnResponse.toString());

                if (!responseObject.isEmpty()) {

                    String username = responseObject.get("username").toString();
                    String ppAmount = NumberFormat.getNumberInstance().format(responseObject.getJSONObject("statistics").get("pp"));
                    String userLevel = responseObject.getJSONObject("statistics").getJSONObject("level").get("current").toString();
                    String globalRanking = NumberFormat.getNumberInstance().format(responseObject.getJSONObject("statistics").get("global_rank"));
                    String hitAccuracy = "%"+responseObject.getJSONObject("statistics").get("hit_accuracy").toString();
                    String avatarUrl = responseObject.get("avatar_url").toString();

                    MessageEmbed messageEmbed = new EmbedBuilder()
                            .setTitle(username + "'s Osu Stats")
                            .setColor(Color.magenta)
                            .setImage(avatarUrl)
                            .addField(new MessageEmbed.Field("PP", ppAmount, false))
                            .addField(new MessageEmbed.Field("User Level",userLevel,false ))
                            .addField(new MessageEmbed.Field("Global Ranking", globalRanking, false))
                            .addField(new MessageEmbed.Field("Hit Accuracy", hitAccuracy, false))
                            .build();


                    event.replyEmbeds(messageEmbed).queue();

                } else {

                    throw new RuntimeException("Response Object Returned Empty");
                }
            }catch (Exception e){
                log.error("An Error Occurred during an API call to Osu :{}",e.getLocalizedMessage());
                Objects.requireNonNull(event.getJDA().getUserById(416342612484554752L)).openPrivateChannel().queue(user->{

                    user.sendMessageFormat("An Error Occurred during an API call to Osu %n%s%n%s",e.getLocalizedMessage(),e.getMessage()).queue();

                });

                event.getChannel().sendMessageFormat("An Error has occurred but no worries, a report has been sent to  %s", event.getJDA().getUserById(416342612484554752L).getName()).queue();
            }



        }
    }


}

package com.crazy.scientist.crazyjavascientist.osu;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.*;
import java.util.List;


@Slf4j
@Component
public class OsuApiCall {

    private List<OsuApiModel> request;

    @Autowired
    private OsuApiModelI osuApiModelI;

    @Autowired
    private OsuTokenModelI osuTokenModelI;


    public void makeOsuAPICall(@Nonnull SlashCommandInteraction event) {

        if (event.getName().equalsIgnoreCase("get-osu-stats")) {

            String userID = "";
            boolean sendAPICall = true;

            switch (event.getOption("username").getAsString()) {
                case "1" -> userID = OsuMembers.ONE.getUserID();
                case "2" -> userID = OsuMembers.TWO.getUserID();
                case "3" -> userID = OsuMembers.THREE.getUserID();
                case "4" -> userID = OsuMembers.FOUR.getUserID();
                case "5" -> userID = OsuMembers.FIVE.getUserID();
                case "6" -> userID = OsuMembers.SIX.getUserID();
                default -> {
                    sendAPICall = false;
                    event.replyFormat("%s is not setup for obtaining records or does not exist, please reach out to  %s  to have the role added to the bot or double check that you've entered the role correctly", event.getOption("username").getAsString(), event.getJDA().getUserById(416342612484554752L).getName()).queue();
                }
            }

            if (sendAPICall) {

                try {

                    URL url = new URL("https://osu.ppy.sh/api/v2/users/" + userID + "/osu?key=id");

                    StringBuilder returnResponse = new StringBuilder();


                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                    connection.setRequestMethod("GET");
                    connection.setRequestProperty("Content-Type", "application/json");
                    connection.setRequestProperty("Authorization", "Bearer " + new String(osuTokenModelI.retrieveTokenObjectInstance().getToken(), StandardCharsets.UTF_8));


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

                        String month = (LocalDate.now().getMonthValue() < 10) ? ("0" + LocalDate.now().getMonthValue()) : String.valueOf(LocalDate.now().getMonthValue());
                        int year = LocalDate.now().getYear();


                        int hours = (Integer.parseInt(responseObject.getJSONObject("statistics").get("play_time").toString()) / 3600) % 24;
                        int mins = (Integer.parseInt(responseObject.getJSONObject("statistics").get("play_time").toString()) % 3600) / 60;
                        int days = (Integer.parseInt(responseObject.getJSONObject("statistics").get("play_time").toString()) / 3600) / 24;


                        double ppAmountNonFormatted = Double.parseDouble(responseObject.getJSONObject("statistics").get("pp").toString());
                        int totalChokesNonFormatted = Integer.parseInt(responseObject.getJSONObject("statistics").getJSONObject("grade_counts").get("sh").toString());
                        int monthlyPlayCountsUnformatted = 0;
                        int globalRankingNonFormatted = Integer.parseInt(responseObject.getJSONObject("statistics").get("global_rank").toString());
                        double hitAccNonFormatted = Double.parseDouble(responseObject.getJSONObject("statistics").get("hit_accuracy").toString());


                        String username = responseObject.get("username").toString();
                        String ppAmount = NumberFormat.getNumberInstance().format(responseObject.getJSONObject("statistics").get("pp"));
                        String totalChokes = responseObject.getJSONObject("statistics").getJSONObject("grade_counts").get("sh").toString();
                        String monthlyPlayCounts = "";
                        String totalTimePlayed = String.format("%01dd %2dh %02dm", days, hours, mins);
                        String globalRanking = NumberFormat.getNumberInstance().format(responseObject.getJSONObject("statistics").get("global_rank"));
                        String hitAccuracy = "%" + responseObject.getJSONObject("statistics").get("hit_accuracy").toString();
                        String avatarUrl = responseObject.get("avatar_url").toString();

                        for (Object o : responseObject.getJSONArray("monthly_playcounts")) {

                            JSONObject playCount = (JSONObject) o;

                            if (playCount.get("start_date").equals(year + "-" + month + "-01")) {

                                monthlyPlayCounts = NumberFormat.getNumberInstance().format(playCount.get("count"));
                                monthlyPlayCountsUnformatted = Integer.parseInt(playCount.get("count").toString());

                            }

                        }


                        MessageEmbed messageEmbed;
                        EmbedBuilder builder;

                        OsuApiModel lastRequest = osuApiModelI.getLastRequestByOsuUsername(username);

                        if (lastRequest == null) {
                            osuApiModelI.save(new OsuApiModel(username, ppAmountNonFormatted, monthlyPlayCountsUnformatted, totalTimePlayed, globalRankingNonFormatted, totalChokesNonFormatted, hitAccNonFormatted, ZonedDateTime.now()));
                        }
                        if (lastRequest != null && (lastRequest.getPp() != ppAmountNonFormatted || lastRequest.getGlobalRanking() != globalRankingNonFormatted || lastRequest.getTotalChokes() != lastRequest.getTotalChokes() || lastRequest.getHitAcc() != hitAccNonFormatted)) {

                            builder = new EmbedBuilder()
                                    .setTitle(username + "'s Osu Stats")
                                    .setColor(Color.magenta)
                                    .setThumbnail(avatarUrl)
                                    .setFooter("Official Osu Records, created by " + event.getJDA().getUserById(416342612484554752L).getName() + "\nRecords Based on Last API Call: " + DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT).format(lastRequest.getLastRequestDateAndTime()))
                                    .addBlankField(true)
                                    .addBlankField(true)
                                    .addBlankField(true);

                            OsuApiModel updatedRequest = new OsuApiModel();


                            if (lastRequest.getPp() < ppAmountNonFormatted) {

                                double positivePPAmount = ppAmountNonFormatted - lastRequest.getPp();
                                builder.addField(new MessageEmbed.Field("PP", ppAmount + "```diff\n+" + DecimalFormat.getInstance().format(positivePPAmount) + "```", true));
                                updatedRequest.setPp(ppAmountNonFormatted);
                            } else if (lastRequest.getPp() > ppAmountNonFormatted) {

                                double negativePPAmount = lastRequest.getPp() - ppAmountNonFormatted;
                                builder.addField(new MessageEmbed.Field("PP", ppAmount + "```diff\n-" + DecimalFormat.getInstance().format(negativePPAmount) + "```", true));
                                updatedRequest.setPp(ppAmountNonFormatted);
                            } else {
                                builder.addField(new MessageEmbed.Field("PP", ppAmount, true));
                            }

                            builder.addField(new MessageEmbed.Field("Total Time Played", totalTimePlayed, true));
                            builder.addField(new MessageEmbed.Field("Monthly Play Count", monthlyPlayCounts, true));

                            if (lastRequest.getGlobalRanking() < globalRankingNonFormatted) {

                                int negativeGlobalRanking = globalRankingNonFormatted - lastRequest.getGlobalRanking();
                                builder.addField(new MessageEmbed.Field("Global Ranking", globalRanking + "```diff\n+" + DecimalFormat.getInstance().format(negativeGlobalRanking) + "```", true));
                                updatedRequest.setGlobalRanking(globalRankingNonFormatted);
                            } else if (lastRequest.getGlobalRanking() > globalRankingNonFormatted) {

                                int positiveGlobalRanking = lastRequest.getGlobalRanking() - globalRankingNonFormatted;
                                builder.addField(new MessageEmbed.Field("Global Ranking", globalRanking + "```diff\n-" + DecimalFormat.getInstance().format(positiveGlobalRanking) + "```", true));
                                updatedRequest.setGlobalRanking(globalRankingNonFormatted);
                            } else {
                                builder.addField(new MessageEmbed.Field("Global Ranking", globalRanking, true));
                            }

                            if (lastRequest.getHitAcc() < hitAccNonFormatted) {

                                double positiveHitAccAmount = hitAccNonFormatted - lastRequest.getHitAcc();
                                builder.addField(new MessageEmbed.Field("Hit Accuracy", hitAccuracy + "```diff\n+%" + DecimalFormat.getInstance().format(positiveHitAccAmount) + "```", true));
                                updatedRequest.setHitAcc(hitAccNonFormatted);
                            } else if (lastRequest.getHitAcc() > hitAccNonFormatted) {

                                double negativeHitAccAmount = lastRequest.getHitAcc() - hitAccNonFormatted;
                                builder.addField(new MessageEmbed.Field("Hit Accuracy", hitAccuracy + "```diff\n-%" + DecimalFormat.getInstance().format(negativeHitAccAmount) + "```", true));
                                updatedRequest.setHitAcc(hitAccNonFormatted);
                            } else {

                                builder.addField((new MessageEmbed.Field("Hit Accuracy", hitAccuracy, true)));
                            }

                            if (lastRequest.getTotalChokes() < totalChokesNonFormatted) {

                                int negativeTotalChokeAmount = totalChokesNonFormatted - lastRequest.getTotalChokes();
                                builder.addField(new MessageEmbed.Field("Total Chokes", totalChokes + "```diff\n+" + DecimalFormat.getInstance().format(negativeTotalChokeAmount) + "```", true));
                                updatedRequest.setTotalChokes(totalChokesNonFormatted);
                            } else {

                                builder.addField(new MessageEmbed.Field("Total Chokes", totalChokes, true));
                            }


                            osuApiModelI.updateLastRequestWithChangedOSUStats(event.getUser().getName(), ppAmountNonFormatted, globalRankingNonFormatted, hitAccNonFormatted, totalChokesNonFormatted, ZonedDateTime.now());

                            builder.appendDescription("**Legend:**\n\n**Global Ranking:** If Global Ranking is Red its good, if its green its bad\n\n**PP:** If PP is red its bad, and if green its good\n\n**Chokes:** If Chokes is green at all its bad\n\n" +
                                    "**Hit Accuracy:** If Hit Acc is red it's bad, if green it's good\n\n*These Records update depending on your frequency of play and when you call this function of the bot.*");
                            builder.addBlankField(true)
                                    .addBlankField(true)
                                    .addBlankField(true);

                            messageEmbed = builder.build();

                            event.replyEmbeds(messageEmbed).queue();

                        } else {


                            messageEmbed = new EmbedBuilder()
                                    .setTitle(username + "'s Osu Stats")
                                    .setColor(Color.magenta)
                                    .setThumbnail(avatarUrl)
                                    .setFooter("Official Osu Records, created by " + event.getJDA().getUserById(416342612484554752L).getName())
                                    .addField(new MessageEmbed.Field("PP", ppAmount, true))
                                    .addField(new MessageEmbed.Field("Total Time Played", totalTimePlayed, true))
                                    .addField(new MessageEmbed.Field("Monthly Play Count", monthlyPlayCounts, true))
                                    .addField(new MessageEmbed.Field("Global Ranking", globalRanking, true))
                                    .addField(new MessageEmbed.Field("Total Chokes", totalChokes, true))
                                    .addField(new MessageEmbed.Field("Hit Accuracy", hitAccuracy, true))
                                    .build();

                            event.replyEmbeds(messageEmbed).queue();
                        }


                    } else {

                        throw new RuntimeException("Response Object Returned Empty");
                    }
                } catch (Exception e) {
                    log.error("An Error Occurred during an API call to Osu :{}", e.getLocalizedMessage());
                    Objects.requireNonNull(event.getJDA().getUserById(416342612484554752L)).openPrivateChannel().queue(user -> {

                        user.sendMessageFormat("An Error Occurred during an API call to Osu %n%s%n%s", e.getLocalizedMessage(), e.getMessage()).queue();

                    });

                    event.getChannel().sendMessageFormat("An Error has occurred but no worries, a report has been sent to  %s", event.getJDA().getUserById(416342612484554752L).getName()).queue();
                }

            }
        }
    }


}

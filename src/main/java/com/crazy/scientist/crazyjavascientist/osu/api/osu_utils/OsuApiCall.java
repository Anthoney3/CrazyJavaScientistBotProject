package com.crazy.scientist.crazyjavascientist.osu.api.osu_utils;

import com.crazy.scientist.crazyjavascientist.osu.api.osu_models.OsuApiModel;
import com.crazy.scientist.crazyjavascientist.osu.api.osu_models.OsuBestPlayModel;
import com.crazy.scientist.crazyjavascientist.osu.api.osu_models.OsuMembers;
import com.crazy.scientist.crazyjavascientist.osu.api.osu_models.OsuMembersByNickname;
import com.crazy.scientist.crazyjavascientist.osu.api.osu_repos.BestPlayRepo;
import com.crazy.scientist.crazyjavascientist.osu.api.osu_repos.OsuApiModelI;
import com.crazy.scientist.crazyjavascientist.osu.api.osu_repos.OsuTokenModelI;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.crazy.scientist.crazyjavascientist.config.DiscordBotConfigJDAStyle.shardManager;


@Slf4j
@Data
@Component
public class OsuApiCall {

    private List<OsuApiModel> request;

    @Autowired
    private OsuApiModelI osuApiModelI;

    @Autowired
    private OsuTokenModelI osuTokenModelI;


    @Autowired
    private BestPlayRepo bestPlayRepo;

    private List<OsuBestPlayModel> currentBestPlays;

    private List<OsuMembersByNickname> osuMembersWithNicknameAttached = new ArrayList<>();
    private List<OsuApiModel> osuDBMemberInfo;
    private List<Member> osuGuildMembers;


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

                    //Displays Roles and Usernames when Uncommented
//                    testRoleOutput();

                    JSONObject responseObject = new JSONObject(getOsuStatsAPICall(userID));

                    OsuBestPlayModel playersBestPlay = bestPlayRepo.getBestPlayByOsuUsername(responseObject.get("username").toString());


                    if (!responseObject.isEmpty()) {

                        String month = (LocalDate.now().getMonthValue() < 10) ? ("0" + LocalDate.now().getMonthValue()) : String.valueOf(LocalDate.now().getMonthValue());
                        int year = LocalDate.now().getYear();


                        int hours = (Integer.parseInt(responseObject.getJSONObject("statistics").get("play_time").toString()) / 3600) % 24;
                        int mins = (Integer.parseInt(responseObject.getJSONObject("statistics").get("play_time").toString()) % 3600) / 60;
                        int days = (Integer.parseInt(responseObject.getJSONObject("statistics").get("play_time").toString()) / 3600) / 24;


                        double ppAmountNonFormatted = Double.parseDouble(responseObject.getJSONObject("statistics").get("pp").toString());
                        int totalChokesNonFormatted = Integer.parseInt(responseObject.getJSONObject("statistics").getJSONObject("grade_counts").get("sh").toString());
                        int monthlyPlayCountsUnformatted = 0;
                        int globalRankingNonFormatted = (!responseObject.getJSONObject("statistics").get("global_rank").toString().equalsIgnoreCase("null"))
                                ? Integer.parseInt(responseObject.getJSONObject("statistics").get("global_rank").toString()) : 0;


                        double hitAccNonFormatted = Double.parseDouble(responseObject.getJSONObject("statistics").get("hit_accuracy").toString());

                        //Default User Information
                        String username = responseObject.get("username").toString();
                        String ppAmount = NumberFormat.getNumberInstance().format(responseObject.getJSONObject("statistics").get("pp"));
                        String totalChokes = responseObject.getJSONObject("statistics").getJSONObject("grade_counts").get("sh").toString();
                        String monthlyPlayCounts = "";
                        String totalTimePlayed = String.format("%01dd %2dh %02dm", days, hours, mins);
                        String globalRanking = (!responseObject.getJSONObject("statistics").get("global_rank").toString().equalsIgnoreCase("null")) ? NumberFormat.getNumberInstance().format(responseObject.getJSONObject("statistics").get("global_rank")) : "No Global Rank Found";
                        String hitAccuracy = "%" + responseObject.getJSONObject("statistics").get("hit_accuracy").toString();
                        String avatarUrl = responseObject.get("avatar_url").toString();

                        //Player Best Play Information
                        String mapRank = (playersBestPlay.getMapRank().equalsIgnoreCase("x")) ? "SS" : playersBestPlay.getMapRank();
                        double mapHitAcc = ((playersBestPlay.getMapHitAcc() == 1.0) ? 100.00 : playersBestPlay.getMapHitAcc());
                        int mapPPAmount = (int) Math.round(playersBestPlay.getMapPpAmount());
                        String beatMapUrl = playersBestPlay.getBeatMapUrl();
                        String mapTitle = playersBestPlay.getMapTitle();
                        String beatMapCardImage = playersBestPlay.getBeatMapCardImage();

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
                            osuApiModelI.save(new OsuApiModel(username, ppAmountNonFormatted, monthlyPlayCountsUnformatted, totalTimePlayed, globalRankingNonFormatted, totalChokesNonFormatted, hitAccNonFormatted,userID, ZonedDateTime.now()));
                        } else if (lastRequest.getPp() != ppAmountNonFormatted || lastRequest.getGlobalRanking() != globalRankingNonFormatted || lastRequest.getTotalChokes() != lastRequest.getTotalChokes() || lastRequest.getHitAcc() != hitAccNonFormatted) {

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
                                log.info("Positive Amount for PP was set to: {}", positivePPAmount);
                            } else if (lastRequest.getPp() > ppAmountNonFormatted) {

                                double negativePPAmount = lastRequest.getPp() - ppAmountNonFormatted;
                                builder.addField(new MessageEmbed.Field("PP", ppAmount + "```diff\n-" + DecimalFormat.getInstance().format(negativePPAmount) + "```", true));
                                updatedRequest.setPp(ppAmountNonFormatted);
                                log.info("Negative Amount for PP was set to: {}", negativePPAmount);
                            } else {
                                builder.addField(new MessageEmbed.Field("PP", ppAmount, true));
                            }

                            builder.addField(new MessageEmbed.Field("Total Time Played", totalTimePlayed, true));
                            builder.addField(new MessageEmbed.Field("Monthly Play Count", monthlyPlayCounts, true));

                            if (lastRequest.getGlobalRanking() < globalRankingNonFormatted) {

                                int negativeGlobalRanking = globalRankingNonFormatted - lastRequest.getGlobalRanking();
                                builder.addField(new MessageEmbed.Field("Global Ranking", globalRanking + "```diff\n+" + DecimalFormat.getInstance().format(negativeGlobalRanking) + "```", true));
                                updatedRequest.setGlobalRanking(globalRankingNonFormatted);
                                log.info("Negative Amount for Global Ranking was set to: {}", negativeGlobalRanking);
                            } else if (lastRequest.getGlobalRanking() > globalRankingNonFormatted) {

                                int positiveGlobalRanking = lastRequest.getGlobalRanking() - globalRankingNonFormatted;
                                builder.addField(new MessageEmbed.Field("Global Ranking", globalRanking + "```diff\n-" + DecimalFormat.getInstance().format(positiveGlobalRanking) + "```", true));
                                updatedRequest.setGlobalRanking(globalRankingNonFormatted);
                                log.info("Positive Amount for Global Ranking was set to: {}", positiveGlobalRanking);
                            } else {
                                builder.addField(new MessageEmbed.Field("Global Ranking", globalRanking, true));
                            }

                            if (lastRequest.getHitAcc() < hitAccNonFormatted) {

                                double positiveHitAccAmount = hitAccNonFormatted - lastRequest.getHitAcc();
                                builder.addField(new MessageEmbed.Field("Hit Accuracy", hitAccuracy + "```diff\n+%" + DecimalFormat.getInstance().format(positiveHitAccAmount) + "```", true));
                                updatedRequest.setHitAcc(hitAccNonFormatted);
                                log.info("Positive Amount for Hit Acc was set to: {}", positiveHitAccAmount);
                            } else if (lastRequest.getHitAcc() > hitAccNonFormatted) {

                                double negativeHitAccAmount = lastRequest.getHitAcc() - hitAccNonFormatted;
                                builder.addField(new MessageEmbed.Field("Hit Accuracy", hitAccuracy + "```diff\n-%" + DecimalFormat.getInstance().format(negativeHitAccAmount) + "```", true));
                                updatedRequest.setHitAcc(hitAccNonFormatted);
                                log.info("Negative Amount for Hit Acc was set to: {}", negativeHitAccAmount);
                            } else {

                                builder.addField((new MessageEmbed.Field("Hit Accuracy", hitAccuracy, true)));
                            }

                            if (lastRequest.getTotalChokes() < totalChokesNonFormatted) {

                                int negativeTotalChokeAmount = totalChokesNonFormatted - lastRequest.getTotalChokes();
                                builder.addField(new MessageEmbed.Field("Total Chokes", totalChokes + "```diff\n+" + DecimalFormat.getInstance().format(negativeTotalChokeAmount) + "```", true));
                                updatedRequest.setTotalChokes(totalChokesNonFormatted);
                                log.info("Total Chokes was set to: {}", negativeTotalChokeAmount);
                            } else {

                                builder.addField(new MessageEmbed.Field("Total Chokes", totalChokes, true));
                            }

                            log.info("Username: {} | PP Amount: {} | Global Ranking: {} | Hit Acc: {} | Total Chokes: {} | Timestamp Of Request: {}"
                                    , userID,
                                    ppAmountNonFormatted,
                                    globalRankingNonFormatted,
                                    hitAccNonFormatted,
                                    totalChokesNonFormatted,
                                    ZonedDateTime.now());

                            osuApiModelI.updateLastRequestWithChangedOSUStats(username, ppAmountNonFormatted, globalRankingNonFormatted, hitAccNonFormatted, totalChokesNonFormatted, ZonedDateTime.now());
                            builder.appendDescription("**Legend:**\n\n**Global Ranking:** If Global Ranking is Red its good, if its green its bad\n\n**PP:** If PP is red its bad, and if green its good\n\n**Chokes:** If Chokes is green at all its bad\n\n" +
                                    "**Hit Accuracy:** If Hit Acc is red it's bad, if green it's good\n\n*These Records update depending on your frequency of play and when you call this function of the bot.*");
                            builder.addBlankField(true)
                                    .addBlankField(true)
                                    .addBlankField(true)
                                    .addField("Best Play", "", true)
                                    .addBlankField(true)
                                    .addBlankField(true)
                                    .addField(new MessageEmbed.Field("Map Rank", mapRank, true))
                                    .addField(new MessageEmbed.Field("PP Amount", String.valueOf(mapPPAmount), true))
                                    .addField(new MessageEmbed.Field("Map Hit Acc", "%" + String.format("%.02f", (mapHitAcc != 100.00)? mapHitAcc * 100 : mapHitAcc), true))
                                    .addField(new MessageEmbed.Field("Name", mapTitle, true))
                                    .addField(new MessageEmbed.Field("Beat Map Url", beatMapUrl, true))
                                    .setImage(beatMapCardImage);


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
                                    .addBlankField(true)
                                    .addBlankField(true)
                                    .addBlankField(true)
                                    .addField("Best Play", "", true)
                                    .addBlankField(true)
                                    .addBlankField(true)
                                    .addField(new MessageEmbed.Field("Map Rank", mapRank, true))
                                    .addField(new MessageEmbed.Field("PP Amount", String.valueOf(mapPPAmount), true))
                                    .addField(new MessageEmbed.Field("Map Hit Acc", "%" + String.format("%.02f",(mapHitAcc != 100.00)? mapHitAcc * 100 : mapHitAcc), true))
                                    .addField(new MessageEmbed.Field("Name", mapTitle, true))
                                    .addField(new MessageEmbed.Field("Beat Map Url", beatMapUrl, true))
                                    .setImage(beatMapCardImage)
                                    .build();

                            event.replyEmbeds(messageEmbed).queue();
                        }


                    } else {

                        throw new RuntimeException("Response Object Returned Empty");
                    }
                } catch (Exception e) {
                    log.error("An Error Occurred during an API call to Osu :{}", e.getLocalizedMessage());
                    e.printStackTrace();
                    Objects.requireNonNull(event.getJDA().getUserById(416342612484554752L)).openPrivateChannel().queue(user -> {

                        user.sendMessageFormat("An Error Occurred during an API call to Osu %n%s%n%s", e.getLocalizedMessage(), e.getMessage()).queue();

                    });

                    event.getChannel().sendMessageFormat("An Error has occurred but no worries, a report has been sent to  %s", event.getJDA().getUserById(416342612484554752L).getName()).queue();
                }

            }
        }
    }

    @Scheduled(fixedDelay = 5000, initialDelay = 5000)
    public void checkForNewBestPlays() {

        currentBestPlays = bestPlayRepo.getAllCurrentBestPlays();

        for (OsuBestPlayModel bestPlay : currentBestPlays) {

            try {

                JSONObject bestPlayObject = new JSONObject(getOsuBestPlay(String.valueOf(bestPlay.getId())));

                if (Double.parseDouble(bestPlayObject.get("accuracy").toString()) != bestPlay.getMapHitAcc() ||
                        Double.parseDouble(bestPlayObject.getJSONObject("weight").get("pp").toString()) != bestPlay.getMapPpAmount() ||
                        !bestPlayObject.get("rank").toString().equalsIgnoreCase(bestPlay.getMapRank()) ||
                        !bestPlayObject.getJSONObject("beatmapset").get("title").toString().equalsIgnoreCase(bestPlay.getMapTitle())) {

                    OsuBestPlayModel newBestPlay = new OsuBestPlayModel(Long.parseLong(bestPlayObject.getJSONObject("user").get("id").toString()), bestPlayObject.getJSONObject("user").get("username").toString(),
                            bestPlayObject.get("rank").toString(),
                            Double.parseDouble(bestPlayObject.get("accuracy").toString()),
                            Double.parseDouble(bestPlayObject.getJSONObject("weight").get("pp").toString()),
                            bestPlayObject.getJSONObject("beatmapset").get("title").toString(),
                            bestPlayObject.getJSONObject("beatmap").get("url").toString(),
                            bestPlayObject.getJSONObject("beatmapset").getJSONObject("covers").get("card").toString());

                    String newBestPlayTitle = newBestPlay.getMapTitle();
                    String newBestPlayMapUrl = newBestPlay.getBeatMapUrl();
                    String newBestPlayCard = newBestPlay.getBeatMapCardImage();
                    String newBestPlayRank = (newBestPlay.getMapRank().equalsIgnoreCase("x")) ? "SS" : newBestPlay.getMapRank();
                    int newBestPlayPP = (int) Math.round(newBestPlay.getMapPpAmount());
                    double newBestPlayHitAcc = (newBestPlay.getMapHitAcc() == 1.0) ? 100.00 : newBestPlay.getMapHitAcc();

                    bestPlayRepo.updateNewBestPlay(newBestPlay.getId(),
                            newBestPlay.getUsername(),
                            newBestPlay.getMapRank()
                            , newBestPlay.getMapHitAcc(),
                            newBestPlay.getMapPpAmount()
                            , newBestPlay.getMapTitle(),
                            newBestPlay.getBeatMapUrl()
                            , newBestPlay.getBeatMapCardImage());


                    MessageEmbed messageEmbed = new EmbedBuilder()
                            .setTitle(bestPlayObject.getJSONObject("user").get("username").toString() + "'s New Best Play!")
                            .addField("Rank", newBestPlayRank, true)
                            .addField("PP Amount", String.valueOf(newBestPlayPP), true)
                            .addField("Accuracy", "%" + String.format("%.02f", (newBestPlayHitAcc != 100.00)? newBestPlayHitAcc * 100 : newBestPlayHitAcc), true)
                            .addField("Name", newBestPlayTitle, true)
                            .addField("Beat Map Url", newBestPlayMapUrl, true)
                            .setImage(newBestPlayCard)
                            .setThumbnail(bestPlayObject.getJSONObject("user").get("avatar_url").toString())
                            .build();


                    for(OsuApiModel user : osuDBMemberInfo){
                        if(user.getUsername().equalsIgnoreCase(newBestPlay.getUsername())){


                            if(!user.getNickname().equalsIgnoreCase("1")){
                                shardManager.getGuildById(952394376640888853L).getTextChannelById(1023754936426692649L).sendMessage("<@" + user.getDiscordUserID() + ">" + " " + "<@" + osuApiModelI.getUserByNickName(String.valueOf((Integer.parseInt(user.getNickname()))-1)) + ">").queue();
                            }else{
                                shardManager.getGuildById(952394376640888853L).getTextChannelById(1023754936426692649L).sendMessage("<@" + user.getDiscordUserID() + ">").queue();
                            }
                        }
                    }
                    shardManager.getGuildById(952394376640888853L).getTextChannelById(1023754936426692649L).sendMessageEmbeds(messageEmbed).queue();

                }

            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }

    }

    public void testRoleOutput() {

        osuGuildMembers = shardManager.getGuildById(952394376640888853L).getMembers();
        osuDBMemberInfo = osuApiModelI.getAllMemberInfo();


        for (Member member : osuGuildMembers) {

            if(!member.getUser().isBot()) {
                for(OsuApiModel user : osuDBMemberInfo){

                    if(user.getNickname().equalsIgnoreCase(member.getNickname())){
                        osuMembersWithNicknameAttached.add(new OsuMembersByNickname(member.getUser().getName(), user.getNickname()));
                    }
                }
            }

        }



        shardManager.getUserById(416342612484554752L).openPrivateChannel().queue(message -> {

            EmbedBuilder builder = new EmbedBuilder()
                    .setTitle("Members and Their Corresponding Roles")
                    .addField("Member", "", true)
                    .addBlankField(true)
                    .addField("Nickname", "", true)
                    .addBlankField(true)
                    .addBlankField(true)
                    .addBlankField(true);

            MessageEmbed embed;


            for (OsuMembersByNickname member : osuMembersWithNicknameAttached) {
                builder.addField(member.getOsuMemberName(), "", true)
                        .addBlankField(true)
                        .addField(member.getNickname(), "", true);
            }
            embed = builder.build();

            message.sendMessageEmbeds(embed).queue();

        });
    }


    public void populateDBOnStartWithOsuRecords(ShardManager manager) throws IOException {




        List<OsuMembers> osuMembers = new ArrayList<>();

        osuMembers.add(OsuMembers.ONE);
        osuMembers.add(OsuMembers.TWO);
        osuMembers.add(OsuMembers.THREE);
        osuMembers.add(OsuMembers.FOUR);
        osuMembers.add(OsuMembers.FIVE);
        osuMembers.add(OsuMembers.SIX);


        for (OsuMembers osuMember : osuMembers) {

            try {
                String userID = null;
                if(osuMember.equals(OsuMembers.ONE)){
                    userID = "1";
                }else if(osuMember.equals(OsuMembers.TWO)){
                    userID = "2";
                }else if(osuMember.equals(OsuMembers.THREE)){
                    userID = "3";
                }
                else if(osuMember.equals(OsuMembers.FOUR)){
                    userID = "4";
                }else if(osuMember.equals(OsuMembers.FIVE)){
                    userID = "5";
                }
                else if(osuMember.equals(OsuMembers.SIX)){
                    userID = "6";
                }


                JSONObject responseObject = new JSONObject(getOsuStatsAPICall(osuMember.getUserID()));
                JSONObject bestPlayObject = new JSONObject(getOsuBestPlay(osuMember.getUserID()));


                if (!responseObject.isEmpty() && !bestPlayObject.isEmpty()) {

                    saveBestPlayToDB(bestPlayObject);

                    String month = (LocalDate.now().getMonthValue() < 10) ? ("0" + LocalDate.now().getMonthValue()) : String.valueOf(LocalDate.now().getMonthValue());
                    int year = LocalDate.now().getYear();
                    int hours = (Integer.parseInt(responseObject.getJSONObject("statistics").get("play_time").toString()) / 3600) % 24;
                    int mins = (Integer.parseInt(responseObject.getJSONObject("statistics").get("play_time").toString()) % 3600) / 60;
                    int days = (Integer.parseInt(responseObject.getJSONObject("statistics").get("play_time").toString()) / 3600) / 24;
                    double ppAmountNonFormatted = Double.parseDouble(responseObject.getJSONObject("statistics").get("pp").toString());
                    int totalChokesNonFormatted = Integer.parseInt(responseObject.getJSONObject("statistics").getJSONObject("grade_counts").get("sh").toString());
                    int monthlyPlayCountsUnformatted = 0;
                    int globalRankingNonFormatted = (!responseObject.getJSONObject("statistics").get("global_rank").toString().equalsIgnoreCase("null"))
                            ? Integer.parseInt(responseObject.getJSONObject("statistics").get("global_rank").toString()) : 0;
                    double hitAccNonFormatted = Double.parseDouble(responseObject.getJSONObject("statistics").get("hit_accuracy").toString());

                    String username = responseObject.get("username").toString();
                    String totalTimePlayed = String.format("%01dd %2dh %02dm", days, hours, mins);

                    for (Object o : responseObject.getJSONArray("monthly_playcounts")) {

                        JSONObject playCount = (JSONObject) o;

                        if (playCount.get("start_date").equals(year + "-" + month + "-01")) {
                            monthlyPlayCountsUnformatted = Integer.parseInt(playCount.get("count").toString());

                        }

                    }

                    OsuApiModel lastRequest = osuApiModelI.getLastRequestByOsuUsername(username);

                    if (lastRequest == null) {

                                osuApiModelI.save(new OsuApiModel(username, ppAmountNonFormatted, monthlyPlayCountsUnformatted, totalTimePlayed, globalRankingNonFormatted, totalChokesNonFormatted, hitAccNonFormatted,userID, ZonedDateTime.now()));


                        log.info("{} added to the DB Successfully", username);
                    }
                } else {

                    throw new RuntimeException("Response Object Returned Empty");
                }
            } catch (Exception e) {
                log.error("An Error Occurred during an API call to Osu :{}", e.getLocalizedMessage());
                e.printStackTrace();
            }
        }
        osuGuildMembers = manager.getGuildById(952394376640888853L).getMembers();
        for(Member member: osuGuildMembers){
            if(!member.getUser().isBot()){
                osuApiModelI.updateDiscordUserID(member.getUser().getId(),member.getNickname());
            }
        }
        log.info("All Users Have been Added to the DB Successfully!");
        osuDBMemberInfo = osuApiModelI.getAllMemberInfo();
    }


    public String getOsuStatsAPICall(String userID) throws IOException {

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
        return returnResponse.toString();
    }

    public String getOsuBestPlay(String userID) throws IOException {

        URL bestPlayUrl = new URL("https://osu.ppy.sh/api/v2/users/" + userID + "/scores/best?include_fails=1&mode=osu&limit=1");

        StringBuilder bestPlayResponse = new StringBuilder();

        HttpURLConnection connection = (HttpURLConnection) bestPlayUrl.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Authorization", "Bearer " + new String(osuTokenModelI.retrieveTokenObjectInstance().getToken(), StandardCharsets.UTF_8));


        try (BufferedReader bf = new BufferedReader(new InputStreamReader(
                connection.getInputStream()))) {
            String line;
            while ((line = bf.readLine()) != null) {

                bestPlayResponse.append(line);

            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return bestPlayResponse.substring(1, bestPlayResponse.length() - 1);
    }


    public void saveBestPlayToDB(JSONObject object) {

        OsuBestPlayModel playerOsuBestPlay = new OsuBestPlayModel(Long.parseLong(object.getJSONObject("user").get("id").toString()), object.getJSONObject("user").get("username").toString(), object.get("rank").toString(),
                Double.parseDouble(object.get("accuracy").toString()),
                Double.parseDouble(object.getJSONObject("weight").get("pp").toString()),
                object.getJSONObject("beatmapset").get("title").toString(),
                object.getJSONObject("beatmap").get("url").toString(),
                object.getJSONObject("beatmapset").getJSONObject("covers").get("card").toString());

        bestPlayRepo.save(playerOsuBestPlay);
    }


}

package com.crazy.scientist.crazyjavascientist.osu.api.osu_utils;

import com.crazy.scientist.crazyjavascientist.osu.api.osu_entities.OsuApiEntity;
import com.crazy.scientist.crazyjavascientist.osu.api.osu_entities.OsuBestPlayEntity;
import com.crazy.scientist.crazyjavascientist.osu.api.osu_enums.OsuMembers;
import com.crazy.scientist.crazyjavascientist.osu.api.osu_models.OsuMembersByNickname;
import com.crazy.scientist.crazyjavascientist.osu.api.osu_repos.BestPlayRepo;
import com.crazy.scientist.crazyjavascientist.osu.api.osu_repos.OsuApiModelI;
import com.crazy.scientist.crazyjavascientist.osu.api.osu_services.OsuUtils;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.awt.*;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.crazy.scientist.crazyjavascientist.config.StaticUtils.shardManager;


@Slf4j
@Data
@Component
public class OsuApiCall {

    @Autowired
    private OsuUtils osuService;

    @Autowired
    private BestPlayRepo bestPlayRepo;

    @Autowired
    private OsuApiModelI osuApiModelI;

    private List<OsuMembersByNickname> osuMembersWithNicknameAttached = new ArrayList<>();


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





                    OsuApiEntity incomingData = osuService.getOsuStatsUsingJackson(userID);


                    OsuBestPlayEntity playersBestPlay = bestPlayRepo.getBestPlayByOsuUsername(incomingData.getUsername());


                    int hours = incomingData.getTotalTimePlayed().toHoursPart();
                    int mins = incomingData.getTotalTimePlayed().toMinutesPart();
                    long days = incomingData.getTotalTimePlayed().toDaysPart();

                    double ppAmountNonFormatted = incomingData.getPp();
                    int totalChokesNonFormatted = incomingData.getTotalChokes();
                    int globalRankingNonFormatted = incomingData.getGlobalRanking();
                    double hitAccNonFormatted = incomingData.getHitAcc();


                    DecimalFormat decimalFormat = new DecimalFormat();
                    decimalFormat.setGroupingUsed(true);
                    decimalFormat.setGroupingSize(3);

                    //Default User Information
                    String username = incomingData.getUsername();
                    String ppAmount = decimalFormat.format(incomingData.getPp());
                    String totalChokes = String.valueOf(incomingData.getTotalChokes());
                    String monthlyPlayCounts = "";
                    String totalTimePlayed = String.format("%01dd %2dh %02dm", days, hours, mins);
                    String globalRanking = (incomingData.getGlobalRanking() != 0) ? NumberFormat.getNumberInstance().format(incomingData.getGlobalRanking()) : "No Global Rank Found";
                    String hitAccuracy = "%" + decimalFormat.format(incomingData.getHitAcc());
                    String avatarUrl = incomingData.getPfpPictureUrl();

                    //Player Best Play Information
                    String mapRank = (playersBestPlay.getMapRank().equalsIgnoreCase("x")) ? "SS" : playersBestPlay.getMapRank();
                    double mapHitAcc = ((playersBestPlay.getMapHitAcc() == 1.0) ? 100.00 : playersBestPlay.getMapHitAcc());
                    int mapPPAmount = (int) Math.round(playersBestPlay.getMapPpAmount());
                    String beatMapUrl = playersBestPlay.getBeatMapUrl();
                    String mapTitle = playersBestPlay.getMapTitle();
                    String beatMapCardImage = playersBestPlay.getBeatMapCardImage();

                    monthlyPlayCounts = NumberFormat.getNumberInstance().format(incomingData.getMonthlyPlaycount());


                    MessageEmbed messageEmbed;
                    EmbedBuilder builder;

                    OsuApiEntity lastRequest = osuApiModelI.getLastRequestByOsuUsername(username);

                    if (lastRequest == null) {
                        osuApiModelI.save(incomingData);
                    } else if (lastRequest.getPp() != ppAmountNonFormatted || lastRequest.getGlobalRanking() != globalRankingNonFormatted || lastRequest.getTotalChokes() != lastRequest.getTotalChokes() || lastRequest.getHitAcc() != hitAccNonFormatted) {

                        builder = new EmbedBuilder()
                                .setTitle(username + "'s Osu Stats")
                                .setColor(Color.magenta)
                                .setThumbnail(avatarUrl)
                                .setFooter("Official Osu Records, created by " + event.getJDA().getUserById(416342612484554752L).getName() + "\nRecords Based on Last API Call: " + DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT).format(lastRequest.getLastRequestDateAndTime()))
                                .addBlankField(true)
                                .addBlankField(true)
                                .addBlankField(true);

                        OsuApiEntity updatedRequest = new OsuApiEntity();


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


    public void testRoleOutput() {

        for (Member member : osuService.getOsuGuildMembers()) {

            if(!member.getUser().isBot()) {
                for(OsuApiEntity user : osuService.getOsuDBMemberInfo()){

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



}

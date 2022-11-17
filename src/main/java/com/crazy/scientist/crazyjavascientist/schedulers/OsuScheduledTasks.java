package com.crazy.scientist.crazyjavascientist.schedulers;


import com.crazy.scientist.crazyjavascientist.osu.api.osu_entities.OsuApiEntity;
import com.crazy.scientist.crazyjavascientist.osu.api.osu_entities.OsuBestPlayEntity;
import com.crazy.scientist.crazyjavascientist.osu.api.osu_repos.BestPlayRepo;
import com.crazy.scientist.crazyjavascientist.osu.api.osu_repos.OsuApiModelI;
import com.crazy.scientist.crazyjavascientist.osu.api.osu_services.OsuUtils;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static com.crazy.scientist.crazyjavascientist.config.StaticUtils.shardManager;

@Slf4j
@Service
public class OsuScheduledTasks {

    @Autowired
    private BestPlayRepo bestPlayRepo;

    @Autowired
    private OsuUtils osuService;

    @Autowired
    private OsuApiModelI osuApiModelI;

    private List<OsuBestPlayEntity> currentBestPlays;

    @Scheduled(fixedDelayString = "PT5S", initialDelayString = "PT2S")
    public void checkForNewBestPlays() {

        currentBestPlays = bestPlayRepo.getAllCurrentBestPlays();


        for (OsuBestPlayEntity bestPlay : currentBestPlays) {

            try {

                OsuBestPlayEntity bestPlayEntity =  osuService.getOsuBestPlay(String.valueOf(bestPlay.getId()));

                if (bestPlayEntity.getMapHitAcc() != bestPlay.getMapHitAcc() ||
                        bestPlayEntity.getMapPpAmount() != bestPlay.getMapPpAmount() ||
                        !bestPlayEntity.getMapRank().equalsIgnoreCase(bestPlay.getMapRank()) ||
                        !bestPlayEntity.getMapTitle().equalsIgnoreCase(bestPlay.getMapTitle())) {


                    String newBestPlayTitle = bestPlayEntity.getMapTitle();
                    String newBestPlayMapUrl = bestPlayEntity.getBeatMapUrl();
                    String newBestPlayCard = bestPlayEntity.getBeatMapCardImage();
                    String newBestPlayRank = (bestPlayEntity.getMapRank().equalsIgnoreCase("x")) ? "SS" : bestPlayEntity.getMapRank();
                    int newBestPlayPP = (int) Math.round(bestPlayEntity.getMapPpAmount());
                    double newBestPlayHitAcc = (bestPlayEntity.getMapHitAcc() == 1.0) ? 100.00 : bestPlayEntity.getMapHitAcc();

                    bestPlayRepo.updateNewBestPlay(bestPlayEntity.getId(),
                            bestPlayEntity.getUsername(),
                            bestPlayEntity.getMapRank()
                            , bestPlayEntity.getMapHitAcc(),
                            bestPlayEntity.getMapPpAmount()
                            , bestPlayEntity.getMapTitle(),
                            bestPlayEntity.getBeatMapUrl()
                            , bestPlayEntity.getBeatMapCardImage(), ZonedDateTime.now().format(DateTimeFormatter.ofPattern("MM/dd/yyyy hh:mm:ss a").withZone(ZoneId.of("America/New_York"))));


                    MessageEmbed messageEmbed = new EmbedBuilder()
                            .setTitle(bestPlayEntity.getUsername() + "'s New Best Play!")
                            .addField("Rank", newBestPlayRank, true)
                            .addField("PP Amount", String.valueOf(newBestPlayPP), true)
                            .addField("Accuracy", "%" + String.format("%.02f", (newBestPlayHitAcc != 100.00)? newBestPlayHitAcc * 100 : newBestPlayHitAcc), true)
                            .addField("Name", newBestPlayTitle, true)
                            .addField("Beat Map Url", newBestPlayMapUrl, true)
                            .setImage(newBestPlayCard)
                            .setThumbnail(osuApiModelI.getUsersThumbnailByUsername(bestPlayEntity.getUsername()))
                            .build();


                    for(OsuApiEntity user : osuService.getOsuDBMemberInfo()){
                        if(user.getUsername().equalsIgnoreCase(bestPlayEntity.getUsername())){
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

}

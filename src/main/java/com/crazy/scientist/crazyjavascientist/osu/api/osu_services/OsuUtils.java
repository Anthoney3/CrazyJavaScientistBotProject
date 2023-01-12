package com.crazy.scientist.crazyjavascientist.osu.api.osu_services;

import com.crazy.scientist.crazyjavascientist.osu.api.osu_entities.OsuApiEntity;
import com.crazy.scientist.crazyjavascientist.osu.api.osu_entities.OsuBestPlayEntity;
import com.crazy.scientist.crazyjavascientist.osu.api.osu_enums.OsuMembers;
import com.crazy.scientist.crazyjavascientist.osu.api.osu_models.OsuMonthlyPlaycountsModel;
import com.crazy.scientist.crazyjavascientist.osu.api.osu_models.deserializers.OsuMonthlyPlayCountDeserializer;
import com.crazy.scientist.crazyjavascientist.osu.api.osu_repos.BestPlayRepo;
import com.crazy.scientist.crazyjavascientist.osu.api.osu_repos.OsuApiModelI;
import com.crazy.scientist.crazyjavascientist.osu.api.osu_repos.OsuTokenModelI;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
@Slf4j
@Getter
@Setter
@ToString
@Service
public class OsuUtils {

    @Autowired
    private OsuMonthlyPlayCountDeserializer monthlyPlayCountDeserializer;

    @Autowired
    private OsuApiModelI osuApiModelI;

    @Autowired
    private OsuTokenModelI osuTokenModelI;

    @Autowired
    private BestPlayRepo bestPlayRepo;

    private List<OsuApiEntity> osuDBMemberInfo;
    private List<Member> osuGuildMembers  ;

    public void populateDBOnStartWithOsuRecords(ShardManager manager) throws IOException {

        log.info("Running DB Population Task for OSU...");

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

                OsuApiEntity incomingOsuUserInfo = getOsuStatsUsingJackson(osuMember.getUserID());
                incomingOsuUserInfo.setNickname(userID);
                OsuBestPlayEntity bestplay = getOsuBestPlay(osuMember.getUserID());

                if (incomingOsuUserInfo != null && bestplay != null) {
                    saveBestPlayToDB(bestplay);

                    log.info("Avatar URL Found: {}",incomingOsuUserInfo.getPfpPictureUrl());

                    OsuApiEntity lastRequest = osuApiModelI.getLastRequestByOsuUsername(incomingOsuUserInfo.getUsername());

                    if (lastRequest == null) {
                        osuApiModelI.save(incomingOsuUserInfo);
                        log.info("{} added to the DB Successfully", incomingOsuUserInfo.getUsername());
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
        osuDBMemberInfo = osuApiModelI.findAll();
    }

    public OsuApiEntity getOsuStatsUsingJackson(String userID){

        OsuApiEntity osuModel;

        try {

            URL url = new URL("https://osu.ppy.sh/api/v2/users/" + userID + "/osu?key=id");

            StringBuilder returnResponse = new StringBuilder();


            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("GET");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Authorization", "Bearer " + new String(osuTokenModelI.findById(1L).get().getToken(), StandardCharsets.UTF_8));


            try (BufferedReader bf = new BufferedReader(new InputStreamReader(
                    connection.getInputStream()))) {
                String line;
                while ((line = bf.readLine()) != null) {

                    returnResponse.append(line);

                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            ObjectMapper objectMapper = new ObjectMapper();


            osuModel = objectMapper.readValue(returnResponse.toString(),OsuApiEntity.class);


            List<OsuMonthlyPlaycountsModel> playcountsModels = monthlyPlayCountDeserializer.deserialize(returnResponse.toString());

            String month = (LocalDate.now().getMonthValue() < 10) ? ("0" + LocalDate.now().getMonthValue()) : String.valueOf(LocalDate.now().getMonthValue());
            int year = LocalDate.now().getYear();

            playcountsModels.forEach(months ->{
                if(months.getStartDate().equalsIgnoreCase(year + "-" + month + "-01"))
                    osuModel.setMonthlyPlaycount(months.getPlayCount());
            });


        }catch (Exception e){
            throw new RuntimeException(e);
        }

        return osuModel;

    }


    public OsuBestPlayEntity getOsuBestPlay(String userID) throws IOException {

        URL bestPlayUrl = new URL("https://osu.ppy.sh/api/v2/users/" + userID + "/scores/best?include_fails=1&mode=osu&limit=1");

        StringBuilder bestPlayResponse = new StringBuilder();

        HttpURLConnection connection = (HttpURLConnection) bestPlayUrl.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Authorization", "Bearer " + new String(osuTokenModelI.findById(1L).get().getToken(), StandardCharsets.UTF_8));


        try (BufferedReader bf = new BufferedReader(new InputStreamReader(
                connection.getInputStream()))) {
            String line;
            while ((line = bf.readLine()) != null) {

                bestPlayResponse.append(line);

            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return new ObjectMapper().readValue(bestPlayResponse.toString(),OsuBestPlayEntity.class);
    }


    public void saveBestPlayToDB(OsuBestPlayEntity bestPlay) {
        bestPlayRepo.save(bestPlay);
    }


}

package com.crazy.scientist.crazyjavascientist.osu.api.osu_utils;

import com.crazy.scientist.crazyjavascientist.osu.api.osu_entities.OsuTokenEntity;
import com.crazy.scientist.crazyjavascientist.osu.api.osu_repos.OsuTokenModelI;
import io.github.cdimascio.dotenv.Dotenv;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.*;

@Slf4j
@Component
public class OAuthToken {

    private final Dotenv config = Dotenv.load();
    private Timer timer = new Timer();
    private ZonedDateTime nextRenewalTime;

    @Autowired
    private OsuTokenModelI osuTokenModelI;


    public void getOsuOAuthToken(ShardManager event) throws IOException {


        try {


            JSONObject sendingJSON = new JSONObject();
            sendingJSON.put("client_id", config.get("CLIENTID"));
            sendingJSON.put("client_secret", config.get("CLIENTSECRET"));
            sendingJSON.put("grant_type", "client_credentials");
            sendingJSON.put("scope", "public");


            URL url = new URL(" https://osu.ppy.sh/oauth/token");


            URLConnection conn = url.openConnection();

            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Content-Length", sendingJSON.toString());

            try (DataOutputStream dos = new DataOutputStream(conn.getOutputStream())) {
                dos.writeBytes(sendingJSON.toString());
            }

            StringBuilder builder = new StringBuilder();

            try (BufferedReader bf = new BufferedReader(new InputStreamReader(
                    conn.getInputStream()))) {
                String line;
                while ((line = bf.readLine()) != null) {
                    builder.append(line);
                }
            }

            JSONObject responseObject = new JSONObject(builder.toString());


            if (!responseObject.isEmpty()) {

                OsuTokenEntity tokenObject = new OsuTokenEntity();


                tokenObject.setTokenRenewalTime(ZonedDateTime.now());

                log.info("The Token Retrieved Successfully");
                log.info("The Token Expires in :{} hours", Integer.parseInt(responseObject.get("expires_in").toString()) / 3600);
                log.info("The Token Type is :{}", responseObject.get("token_type"));
                log.info("Task Scheduled to renew token on: {}/{}/{} at {}", (ZonedDateTime.now().getMonth().getValue() < 10) ? "0"+ZonedDateTime.now().getMonth().getValue() : ZonedDateTime.now().getMonth().getValue(), ZonedDateTime.now().getDayOfWeek().plus(1L).getValue(), ZonedDateTime.now().getYear(), "8:00 AM");

                tokenObject.setToken(responseObject.get("access_token").toString().getBytes(StandardCharsets.UTF_8));

                osuTokenModelI.save(tokenObject);

            } else {
                throw new RuntimeException("Response Object Returned Empty");
            }
        } catch (Exception e) {


            log.error("An Error Occurred during OAuth Token Attempt :{}", e.getLocalizedMessage());
            Objects.requireNonNull(event.getUserById(416342612484554752L)).openPrivateChannel().queue(user -> {

                user.sendMessageFormat("An Error Occured during Inital OAuth Token Attempt is%n%s%n%s", e.getLocalizedMessage(), e.getMessage()).queue();
            });
        }


    }


    @Scheduled(cron = "0 0 8 * * *")
    public void renewOsuOAuthToken() throws IOException {


        try {


            JSONObject sendingJSON = new JSONObject();
            sendingJSON.put("client_id", config.get("CLIENTID"));
            sendingJSON.put("client_secret", config.get("CLIENTSECRET"));
            sendingJSON.put("grant_type", "client_credentials");
            sendingJSON.put("scope", "public");


            URL url = new URL(" https://osu.ppy.sh/oauth/token");


            URLConnection conn = url.openConnection();

            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Content-Length", sendingJSON.toString());

            try (DataOutputStream dos = new DataOutputStream(conn.getOutputStream())) {
                dos.writeBytes(sendingJSON.toString());
            }

            StringBuilder builder = new StringBuilder();

            try (BufferedReader bf = new BufferedReader(new InputStreamReader(
                    conn.getInputStream()))) {
                String line;
                while ((line = bf.readLine()) != null) {
                    builder.append(line);
                }
            }

            JSONObject responseObject = new JSONObject(builder.toString());

            if (!responseObject.isEmpty()) {

                OsuTokenEntity renewedTokenObject = new OsuTokenEntity();

                renewedTokenObject.setTokenRenewalTime(ZonedDateTime.now());

                log.info("Token Renewed Successfully");
                log.info("Token Renewed at : {}", DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT).format(renewedTokenObject.getTokenRenewalTime()));
                log.info("The Token Expires in :{} hours", Integer.parseInt(responseObject.get("expires_in").toString()) / 3600);
                log.info("The Token Type is :{}", responseObject.get("token_type"));

                renewedTokenObject.setToken(responseObject.get("access_token").toString().getBytes(StandardCharsets.UTF_8));

                osuTokenModelI.updateTokenAndRenewalTime(renewedTokenObject.getToken(), renewedTokenObject.getTokenRenewalTime());



            } else {
                throw new RuntimeException("Response Object Returned Empty");
            }
        } catch (Exception e) {
            log.error("An Error Occurred during OAuth Token Renewal: {}", e.getLocalizedMessage());
        }

        log.info("Task Scheduled to renew token on: {}/{}/{} at {}", (ZonedDateTime.now().getMonth().getValue() < 10) ? ZonedDateTime.now().getMonth().getValue() : "0"+ZonedDateTime.now().getMonth().getValue(), ZonedDateTime.now().getDayOfWeek().plus(1L).getValue(), ZonedDateTime.now().getYear(), "8:00 AM");
    }
}




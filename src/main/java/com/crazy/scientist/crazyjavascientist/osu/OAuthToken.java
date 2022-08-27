package com.crazy.scientist.crazyjavascientist.osu;

import io.github.cdimascio.dotenv.Dotenv;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.json.JSONObject;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.*;

@Slf4j
public class OAuthToken  {

  private final Dotenv config = Dotenv.load();
  private Timer timer = new Timer();

  public static String token;
  public static ZonedDateTime tokenRenewalTime;
  private ZonedDateTime nextRenewalTime;


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
                        conn.getInputStream())))
                {
                    String line;
                    while ((line = bf.readLine()) != null) {
                       builder.append(line);
                    }
                }

                JSONObject responseObject = new JSONObject(builder.toString());

                Files.writeString(Paths.get(System.getProperty("user.home")+"/"+"response.json"), responseObject.toString());

                if(!responseObject.isEmpty()) {

                    tokenRenewalTime = ZonedDateTime.now();

                    log.info("The Token Retrieved Successfully");
                    log.info("The Token Expires in :{} hours", Integer.parseInt(responseObject.get("expires_in").toString())/3600);
                    log.info("The Token Type is :{}", responseObject.get("token_type"));

                    OAuthToken.token = responseObject.get("access_token").toString();

                }else{
                    throw new RuntimeException("Response Object Returned Empty");
                }
            }catch (Exception e){


                log.error("An Error Occurred during OAuth Token Attempt :{}",e.getLocalizedMessage());
                Objects.requireNonNull(event.getUserById(416342612484554752L)).openPrivateChannel().queue(user->{

                    user.sendMessageFormat("An Error Occured during Inital OAuth Token Attempt is%n%s%n%s",e.getLocalizedMessage(),e.getMessage()).queue();
                });



            }




    }


    public void renewOsuOAuthToken(ShardManager manager) throws IOException {


        nextRenewalTime = tokenRenewalTime.plusDays(1L).minusHours(1L);

        log.info("Task Scheduled to renew token on: {}", DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT).format(nextRenewalTime));

        TimerTask renewTokenTask = new TimerTask() {
            @Override
            public void run() {

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

                        tokenRenewalTime = ZonedDateTime.now();

                        log.info("Token Renewed Successfully");
                        log.info("Token Renewed at : {}", DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT).format(tokenRenewalTime));
                        log.info("The Token Expires in :{} hours", Integer.parseInt(responseObject.get("expires_in").toString()) / 3600);
                        log.info("The Token Type is :{}", responseObject.get("token_type"));

                        OAuthToken.token = responseObject.get("access_token").toString();


                    } else {
                        throw new RuntimeException("Response Object Returned Empty");
                    }
                } catch (Exception e) {


                    log.error("An Error Occurred during OAuth Token Renewal: {}", e.getLocalizedMessage());

                    Objects.requireNonNull(manager.getUserById(416342612484554752L)).openPrivateChannel().queue(user -> {

                        user.sendMessageFormat("Error Occurred during Token Renewal%n%s", e.getMessage()).queue();
                    });


                }

            }

        };


        timer.schedule(renewTokenTask, Date.from(Instant.from(nextRenewalTime)));


    }





}

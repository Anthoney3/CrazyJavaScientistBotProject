package com.crazy.scientist.crazyjavascientist.schedulers;

import com.crazy.scientist.crazyjavascientist.config.DiscordBotConfigJDAStyle;
import com.crazy.scientist.crazyjavascientist.osu.api.osu_entities.OsuTokenEntity;
import com.crazy.scientist.crazyjavascientist.osu.api.osu_repos.OsuTokenModelI;
import com.crazy.scientist.crazyjavascientist.security.EncryptorAESGCM;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

import static com.crazy.scientist.crazyjavascientist.config.DiscordBotConfigJDAStyle.auth_info;

@Service
@Slf4j
public class OAuthTokenRenewalScheduler {

    @Autowired
    private OsuTokenModelI osuTokenModelI;

    @Autowired
    private EncryptorAESGCM encryptorAESGCM;

    @Value("${aes.info}")
    private String aes_info;


    @Scheduled(cron = "0 0 8 * * *")
    public void renewOsuOAuthToken() throws IOException {


        try {


            JSONObject sendingJSON = new JSONObject();
            sendingJSON.put("client_id", encryptorAESGCM.decrypt(auth_info.get("OSU_CLIENT_ID"),aes_info));
            sendingJSON.put("client_secret", encryptorAESGCM.decrypt(auth_info.get("OSU_CLIENT_SECRET"),aes_info));
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

            try (BufferedReader bf = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
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

        log.info("Task Scheduled to renew token on: {}/{}/{} at {}", (ZonedDateTime.now().getMonth().getValue() < 10) ? ZonedDateTime.now().getMonth().getValue() : "0" + ZonedDateTime.now().getMonth().getValue(), ZonedDateTime.now().getDayOfWeek().plus(1L).getValue(), ZonedDateTime.now().getYear(), "8:00 AM");
    }
}

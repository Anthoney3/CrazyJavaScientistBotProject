package com.crazy.scientist.crazyjavascientist.schedulers;


import com.crazy.scientist.crazyjavascientist.osu.api.osu_entities.OsuApiEntity;
import com.crazy.scientist.crazyjavascientist.osu.api.osu_entities.OsuBestPlayEntity;
import com.crazy.scientist.crazyjavascientist.osu.api.osu_entities.OsuTokenEntity;
import com.crazy.scientist.crazyjavascientist.osu.api.osu_models.OAuthBody;
import com.crazy.scientist.crazyjavascientist.osu.api.osu_models.TokenResponse;
import com.crazy.scientist.crazyjavascientist.osu.api.osu_repos.BestPlayRepo;
import com.crazy.scientist.crazyjavascientist.osu.api.osu_repos.OsuApiModelI;
import com.crazy.scientist.crazyjavascientist.osu.api.osu_repos.OsuTokenModelI;
import com.crazy.scientist.crazyjavascientist.osu.api.osu_services.OsuUtils;
import com.crazy.scientist.crazyjavascientist.security.EncryptorAESGCM;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import org.apache.http.HttpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Arrays;
import java.util.HashMap;

import static com.crazy.scientist.crazyjavascientist.config.DiscordBotConfigJDAStyle.auth_info;
import static com.crazy.scientist.crazyjavascientist.config.StaticUtils.shardManager;

@Slf4j
@Service
@Setter
public class OsuScheduledTasks {


    @Autowired
    private EncryptorAESGCM encryptorAESGCM;

    @Autowired
    private BestPlayRepo bestPlayRepo;

    @Autowired
    private OsuUtils osuService;

    @Autowired
    private OsuApiModelI osuApiModelI;

    @Autowired
    private OsuTokenModelI osuTokenModelI;

    @Value("${aes.info}")
    private String aes_info;

    private HashMap<Long, OsuBestPlayEntity> currentBestPlays;

    @Scheduled(cron = "0 */5 * * * *")
    public void checkForNewBestPlays() {

        if(osuService.getOsuDBMemberInfo().isEmpty() || osuService.getOsuDBMemberInfo() == null)
            osuService.setOsuDBMemberInfo(osuApiModelI.findAll());

        //Goes through hash map to check and see if any new best plays exist using object checking
        //If the current object in the table does not match the object that is coming back from the osu api request
        //It will then replace the object in the hash map before saving it to the database and announcing the new Best Play
        currentBestPlays.forEach((k, v) -> {
            try {
                OsuBestPlayEntity bestPlayEntity = osuService.getOsuBestPlay(String.valueOf(v.getId()));

                if (k == bestPlayEntity.getId() && !v.equals(bestPlayEntity)) {

                    log.info("New Best Play Found! Running Update Best Play Task");
                    currentBestPlays.replace(k, v, bestPlayEntity);

                    bestPlayRepo.save(bestPlayEntity);

                    double newBestPlayHitAcc = (bestPlayEntity.getMapHitAcc() == 1.0) ? 100.00 : bestPlayEntity.getMapHitAcc();

                    MessageEmbed messageEmbed = new EmbedBuilder().setTitle(bestPlayEntity.getUsername() + "'s New Best Play!").addField("Rank", (bestPlayEntity.getMapRank().equalsIgnoreCase("x")) ? "SS" : bestPlayEntity.getMapRank(), true).addField("PP Amount", String.valueOf(Math.round(bestPlayEntity.getMapPpAmount())), true).addField("Accuracy", "%" + String.format("%.02f", (newBestPlayHitAcc != 100.00) ? bestPlayEntity.getMapHitAcc() * 100 : bestPlayEntity.getMapHitAcc()), true).addField("Name", bestPlayEntity.getMapTitle(), true).addField("Beat Map Url", bestPlayEntity.getBeatMapUrl(), true).setImage(bestPlayEntity.getBeatMapUrl()).setThumbnail(osuApiModelI.getUsersThumbnailByUsername(bestPlayEntity.getUsername())).build();

                    TextChannel channel = shardManager.getGuildsByName("Osu Chads", true).get(0).getTextChannelsByName("top-plays", true).get(0);

                    //Goes through list of Osu Players and Pings the Osu Player Who got the new play and the player above them
                    for (OsuApiEntity user : osuService.getOsuDBMemberInfo()) {
                        if (user.getUsername().equalsIgnoreCase(bestPlayEntity.getUsername())) {
                            if (!user.getNickname().equalsIgnoreCase("1")) {
                                channel.sendMessage("<@" + user.getDiscordUserID() + ">" + " " + "<@" + osuService.getOsuDBMemberInfo().stream().filter(person -> person.getNickname().equalsIgnoreCase(String.valueOf(Integer.parseInt(user.getNickname()) - 1))).findFirst().get().getDiscordUserID() + ">").queue();
                            } else {
                                channel.sendMessage("<@" + user.getDiscordUserID() + ">").queue();
                            }
                        }
                    }
                    //Sends New Best Play Embed Update to Channel
                    channel.sendMessageEmbeds(messageEmbed).queue();

                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }


    @Scheduled(cron = "0 0 5 * * *")
    public void renewOsuOAuthToken() throws IOException {

        try {
            TokenResponse token_returned = WebClient.create("https://osu.ppy.sh/oauth/token")
                    .post()
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .body(Mono.just(new OAuthBody(Integer.parseInt(encryptorAESGCM.decrypt(auth_info.get("OSU_CLIENT_ID"),aes_info)), encryptorAESGCM.decrypt(auth_info.get("OSU_CLIENT_SECRET"),aes_info),"client_credentials","public" )), OAuthBody.class)
                    .retrieve()
                    .bodyToMono(TokenResponse.class).block();

            if (token_returned.getAccess_token() != null) {

                OsuTokenEntity renewedTokenObject = new OsuTokenEntity();

                renewedTokenObject.setTokenRenewalTime(ZonedDateTime.now());

                log.info("Token Renewed Successfully");
                log.info("Token Renewed at : {}", DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT).format(renewedTokenObject.getTokenRenewalTime()));
                log.info("The Token Expires in : {} hours", token_returned.getExpires_in() / 3600);
                log.info("The Token Type is : {}", token_returned.getToken_type());

                renewedTokenObject.setToken(token_returned.getAccess_token().getBytes(StandardCharsets.UTF_8));

                if(osuTokenModelI.count() != 0){
                    osuTokenModelI.update_token(renewedTokenObject.getToken(),renewedTokenObject.getTokenRenewalTime());
                }else{
                    osuTokenModelI.save(renewedTokenObject);
                }

            } else {
                throw new RuntimeException("Response Object Returned Empty");
            }
        } catch (Exception e) {
            log.error("An Error Occurred during OAuth Token Renewal: {}", e.getLocalizedMessage());
            shardManager.getUserById(416342612484554752L).openPrivateChannel().queue(channel -> {
                channel.sendMessage("Error Occurred During Token Renewal\n" + Arrays.toString(e.getStackTrace())).queue();
            });
        }

        log.info("Task Scheduled to renew token on: {}/{}/{} at {}", (ZonedDateTime.now().getMonth().getValue() < 10) ? ZonedDateTime.now().getMonth().getValue() : "0"+ZonedDateTime.now().getMonth().getValue(), ZonedDateTime.now().getDayOfWeek().plus(1L).getValue(), ZonedDateTime.now().getYear(), "5:00 AM");
    }

}

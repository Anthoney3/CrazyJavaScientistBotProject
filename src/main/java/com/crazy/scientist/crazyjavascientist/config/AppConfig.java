package com.crazy.scientist.crazyjavascientist.config;

import com.crazy.scientist.crazyjavascientist.commands.CommandManager;
import com.crazy.scientist.crazyjavascientist.commands.dnd.DNDService;
import com.crazy.scientist.crazyjavascientist.commands.dnd.dnd_repos.DNDAttendanceRepo;
import com.crazy.scientist.crazyjavascientist.security.EncryptorAESGCM;
import com.crazy.scientist.crazyjavascientist.security.entities.AESAuth;
import com.crazy.scientist.crazyjavascientist.security.entities.CJSConfigEntity;
import com.crazy.scientist.crazyjavascientist.security.repos.AES_Auth;
import com.crazy.scientist.crazyjavascientist.security.repos.CJSConfigRepo;
import com.crazy.scientist.crazyjavascientist.utils.CJSUtils;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.stream.Collectors;

@Configuration
public class AppConfig {


    private final AES_Auth aesAuth;
    private AESAuth aes_info;
    private final DNDService dndService;
    private final EncryptorAESGCM encryptor;
    private final CJSConfigRepo cjsConfigRepo;
    public static HashMap<String, String> auth_info;
    private final CommandManager command_manager;
    private final CJSUtils cjsUtils;

    public AppConfig(AES_Auth aesAuth, DNDAttendanceRepo dndAttendanceRepo, EncryptorAESGCM encryptor, CJSConfigRepo cjsConfigRepo, DNDService dndService, CommandManager commandManager, CJSUtils cjsUtils) {
        this.aesAuth = aesAuth;
        this.encryptor = encryptor;
        this.cjsConfigRepo = cjsConfigRepo;
        this.dndService = dndService;
        command_manager = commandManager;
        this.cjsUtils = cjsUtils;
    }

    @Bean(name = "shardManager")
    public ShardManager createShard() throws Exception {
        ShardManager shardManager;
        try {
            getSetupInformation();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        DefaultShardManagerBuilder builder = DefaultShardManagerBuilder.createDefault(encryptor.decrypt(auth_info.get("BOT"), aes_info.getAuth_key()));
        builder.setStatus(OnlineStatus.ONLINE);
        builder.setActivity(Activity.watching("TV Static"));
        builder.setMemberCachePolicy(MemberCachePolicy.ALL);
        builder.enableIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_MESSAGE_TYPING, GatewayIntent.GUILD_PRESENCES);
        shardManager = builder.build();

        shardManager.addEventListener(command_manager);
        return shardManager;
    }
    @Bean("aesAuthInfo")
    public String createAESAuthInfoBean(){
        return aesAuth.findAll().get(0).getAuth_key();
    }

    void getSetupInformation() {
        aes_info = aesAuth.findAll().get(0);
        auth_info = new HashMap<>(cjsConfigRepo.findAll().stream().parallel().collect(Collectors.toMap(CJSConfigEntity::getShort_name, CJSConfigEntity::getKey_value)));
        dndService.setDiscord_response(cjsUtils.populateDiscordResponses());
    }



}

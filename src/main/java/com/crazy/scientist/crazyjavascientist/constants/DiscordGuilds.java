package com.crazy.scientist.crazyjavascientist.constants;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@NoArgsConstructor
public enum DiscordGuilds {

    DECENT_INTO_YOUR_ANUS("939244115722371072"),
    THE_JAVA_WAY("1008992976090963988"),
    OSU_CHADS("952394376640888853");

    private String discord_guild_id;

     DiscordGuilds(String discord_guild_id){
        this.discord_guild_id = discord_guild_id;
    }

}

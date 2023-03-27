package com.crazy.scientist.crazyjavascientist.constants;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@NoArgsConstructor
public enum DiscordGuilds {
  THE_JAVA_WAY("1008992976090963988");

  private String discord_guild_id;

  DiscordGuilds(String discord_guild_id) {
    this.discord_guild_id = discord_guild_id;
  }
}

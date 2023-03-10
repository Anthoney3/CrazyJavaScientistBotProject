package com.crazy.scientist.crazyjavascientist.constants;

import com.crazy.scientist.crazyjavascientist.dnd.dnd_entities.DNDPlayersEntity;
import java.util.List;
import net.dv8tion.jda.api.sharding.ShardManager;

public final class StaticUtils {

  public static ShardManager shardManager;

  public static List<DNDPlayersEntity> dnd_players;

  public static boolean isDndCancelled = false;

  public static String TEST_CHANNEL = "private-bot-testing-channel";
  public static String LIVE_CHANNEL = "dark-n-dangerous-avanti";
  public static String THE_JAVA_WAY = "The Java Way";

  public StaticUtils() {}
}

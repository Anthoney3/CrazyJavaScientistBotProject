package com.crazy.scientist.crazyjavascientist.config;

import com.crazy.scientist.crazyjavascientist.dnd.dnd_entities.DNDPlayersEntity;
import net.dv8tion.jda.api.sharding.ShardManager;

import java.util.List;


public final class StaticUtils {

    public static ShardManager shardManager;

    public static List<DNDPlayersEntity> dnd_players;

    public static String TEST_CHANNEL = "private-bot-testing-channel";
    public static String LIVE_CHANNEL = "dark-n-dangerous-avanti";
    public static String THE_JAVA_WAY = "The Java Way";

    public StaticUtils() {
    }
}

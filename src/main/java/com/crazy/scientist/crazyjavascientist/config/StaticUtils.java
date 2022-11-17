package com.crazy.scientist.crazyjavascientist.config;

import com.crazy.scientist.crazyjavascientist.commands.CommandManager;
import com.crazy.scientist.crazyjavascientist.commands.Greetings;
import com.crazy.scientist.crazyjavascientist.dnd.DNDTesting;
import com.crazy.scientist.crazyjavascientist.dnd.dnd_repos.DNDAttendanceRepo;
import com.crazy.scientist.crazyjavascientist.listeners.MessageEventListeners;
import com.crazy.scientist.crazyjavascientist.osu.api.osu_repos.OsuApiModelI;
import com.crazy.scientist.crazyjavascientist.osu.api.osu_services.OsuUtils;
import com.crazy.scientist.crazyjavascientist.osu.api.osu_utils.OAuthToken;
import com.crazy.scientist.crazyjavascientist.osu.api.osu_utils.OsuApiCall;
import io.github.cdimascio.dotenv.Dotenv;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import net.dv8tion.jda.api.sharding.ShardManager;


public final class StaticUtils {

    public static Dotenv config = Dotenv.configure().load();
    public static ShardManager shardManager;

    public StaticUtils() {
    }
}

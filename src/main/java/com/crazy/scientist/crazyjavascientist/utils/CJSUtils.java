package com.crazy.scientist.crazyjavascientist.utils;

import com.crazy.scientist.crazyjavascientist.commands.dnd.dnd_entities.DNDAttendanceEntity;
import com.crazy.scientist.crazyjavascientist.commands.dnd.dnd_entities.PlayerResponse;
import com.crazy.scientist.crazyjavascientist.commands.dnd.dnd_repos.DNDAttendanceRepo;
import com.crazy.scientist.crazyjavascientist.commands.dnd.enums.UnicodeResponses;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class CJSUtils {

    private final ApplicationContext context;

    private final DNDAttendanceRepo dndAttendanceRepo;

    public CJSUtils(ApplicationContext context, DNDAttendanceRepo dndAttendanceRepo) {
        this.context = context;
        this.dndAttendanceRepo = dndAttendanceRepo;
    }

    public HashMap<Long,PlayerResponse> populateDiscordResponses(){
        HashMap<Long, PlayerResponse> discord_response = new HashMap<>();
        dndAttendanceRepo.findAll().stream().collect(Collectors.toMap(DNDAttendanceEntity::getDiscord_id, Function.identity())).forEach((k, v) -> {
            if (v.getNo_show().equalsIgnoreCase("y"))
                discord_response.put(k, new PlayerResponse(v.getPlayers_name(), UnicodeResponses.NO_SHOW_NO_RESPONSE));
            if (v.getExcused().equalsIgnoreCase("y"))
                discord_response.put(k, new PlayerResponse(v.getPlayers_name(), UnicodeResponses.EXCUSED));
            if (v.getAttending().equalsIgnoreCase("y"))
                discord_response.put(k, new PlayerResponse(v.getPlayers_name(), UnicodeResponses.ATTENDING));
        });
        return discord_response;
    }
}

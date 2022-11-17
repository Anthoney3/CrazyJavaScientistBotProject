package com.crazy.scientist.crazyjavascientist.dnd.dnd_repos;

import com.crazy.scientist.crazyjavascientist.dnd.dnd_entities.DNDAttendanceEntity;
import com.crazy.scientist.crazyjavascientist.dnd.dnd_entities.DNDPlayersEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DNDPlayersRepo extends JpaRepository<DNDPlayersEntity,Long> {

    @Query("select players from DNDPlayersEntity players")
    List<DNDPlayersEntity> getDNDPlayers();

    @Query(value = "SELECT PLAYERS_NAME FROM DND_PLAYERS_INFO WHERE DISCORD_USER_ID=:discord_user_id ",nativeQuery = true)
    String getPlayerNameByDiscordUserID(@Param("discord_user_id") String discord_user_id);


}

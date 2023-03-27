package com.crazy.scientist.crazyjavascientist.commands.dnd.dnd_repos;

import com.crazy.scientist.crazyjavascientist.commands.dnd.dnd_entities.DNDPlayersEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DNDPlayersRepo extends JpaRepository<DNDPlayersEntity, Long> {
  @Query("select players from DNDPlayersEntity players")
  List<DNDPlayersEntity> getDNDPlayers();

  @Query(
    value = "SELECT PLAYERS_NAME FROM DND_PLAYERS_INFO WHERE DISCORD_USER_ID=:discord_user_id ",
    nativeQuery = true
  )
  String getPlayerNameByDiscordUserID(
    @Param("discord_user_id") String discord_user_id
  );
}

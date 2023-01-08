package com.crazy.scientist.crazyjavascientist.dnd.dnd_repos;

import com.crazy.scientist.crazyjavascientist.dnd.dnd_entities.DNDAttendanceEntity;
import com.crazy.scientist.crazyjavascientist.dnd.dnd_entities.DNDPlayersEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.time.ZonedDateTime;
import java.util.List;

@Repository
public interface DNDAttendanceRepo extends JpaRepository<DNDAttendanceEntity,Long> {

    @Transactional
    @Modifying
    @Query(value = "CALL REFRESH_DND_ATTENDANCE()", nativeQuery = true)
    void resetAttendanceTableValues();

    @Transactional
    @Modifying
    @Query(value = "INSERT INTO CURRENT_WEEK_OF(CURRENT_WEEK) VALUES(:week_of) ON DUPLICATE KEY UPDATE CURRENT_WEEK=:week_of", nativeQuery = true)
    void updateCurrentWeek(String week_of);

    @Query(value = "UPDATE DNDAttendanceEntity attendance SET attendance.attending='N' WHERE attendance.players_name=:players_name")
    void updateAttendanceTable(String players_name);

    @Query(value = "SELECT INSERT_DND_ATTENDANCE_TYPE_COUNTS_AND_CURRENT_WEEK_OF(:attending_players_list,:excused_players_list,:no_show_players_list) ", nativeQuery = true)
    void updateAttendanceHistoryTable(@Param("attending_players_list")String attending_players_list,
                                      @Param("excused_players_list")String excused_players_list,
                                      @Param("no_show_players_list")String no_show_players_list);

    @Query(value = "SELECT dnd_player_info from DNDPlayersEntity dnd_player_info")
    List<DNDPlayersEntity> getDNDPlayersInfo();

    @Transactional
    @Query(value = "SELECT INFO FROM DNDAttendanceEntity INFO")
    List<DNDAttendanceEntity> getDNDAttendance();

    @Transactional
    @Modifying
    @Query(value = "UPDATE DND_ATTENDANCE_INFO SET EXCUSED='Y',NO_SHOW_OR_NO_RESPONSE='N'",nativeQuery = true)
    void updateAllValuesToExcusedForTesting();

    @Transactional
    @Modifying
    @Query(value = "UPDATE DND_ATTENDANCE_INFO SET ATTENDING='Y',NO_SHOW_OR_NO_RESPONSE='N'",nativeQuery = true)
    void updateAllValuesToAttendingForTesting();

}

package com.crazy.scientist.crazyjavascientist.osu.api.osu_repos;


import com.crazy.scientist.crazyjavascientist.osu.api.osu_entities.OsuApiEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.time.ZonedDateTime;
import java.util.List;

@Repository
public interface OsuApiModelI extends JpaRepository<OsuApiEntity,Long> {

    @Query("select o from OsuApiEntity o where o.username=:username")
    OsuApiEntity getLastRequestByOsuUsername(String username);

    @Query("select o.pfpPictureUrl from OsuApiEntity o where o.username=:username")
    String getUsersThumbnailByUsername(String username);

    @Query("select o.username from OsuApiEntity o where o.nickname=:nickname")
    String getUsernameByNickname(String nickname);

    @Query("select o.globalRanking from OsuApiEntity o where o.nickname=:nickname")
    int getGlobalRankingByDiscordNickname(String nickname);


    @Modifying
    @Transactional
    @Query("update OsuApiEntity o set o.discordUserID=:userID where o.nickname=:nickname")
    void updateDiscordUserID(String userID,String nickname);

    @Query("update OsuApiEntity o set o.pp=:ppAmount,o.globalRanking=:globalRanking,o.hitAcc=:hitAcc,o.totalChokes=:totalChokes,o.lastRequestDateAndTime=:lastRequestDateAndTime where o.username=:username")
    @Modifying
    @Transactional
    void updateLastRequestWithChangedOSUStats(String username, double ppAmount, int globalRanking, double hitAcc, int totalChokes, ZonedDateTime lastRequestDateAndTime);



}

package com.crazy.scientist.crazyjavascientist.osu;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.time.ZonedDateTime;

@Repository
public interface OsuApiModelI extends JpaRepository<OsuApiModel,Long> {

    @Query("select o from OsuApiModel o where o.username=:username")
    OsuApiModel getLastRequestByOsuUsername(String username);

    @Query("update OsuApiModel o set o.pp=:ppAmount,o.globalRanking=:globalRanking,o.hitAcc=:hitAcc,o.totalChokes=:totalChokes,o.lastRequestDateAndTime=:lastRequestDateAndTime where o.username=:username")
    @Modifying
    @Transactional
    void updateLastRequestWithChangedOSUStats(String username, double ppAmount, int globalRanking, double hitAcc, int totalChokes, ZonedDateTime lastRequestDateAndTime);



}

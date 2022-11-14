package com.crazy.scientist.crazyjavascientist.osu.api.osu_repos;

import com.crazy.scientist.crazyjavascientist.osu.api.osu_entities.OsuBestPlayEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;

@Repository
public interface BestPlayRepo extends JpaRepository<OsuBestPlayEntity,Long> {

    @Query("select o from OsuBestPlayEntity o where o.username=:username")
     OsuBestPlayEntity getBestPlayByOsuUsername(String username);

    @Query("select o from OsuBestPlayEntity o")
     List<OsuBestPlayEntity> getAllCurrentBestPlays();


    @Modifying
    @Transactional
    @Query("update OsuBestPlayEntity o set o.id=:id, o.username=:username, o.mapRank=:mapRank,o.mapHitAcc=:mapHitAcc,o.mapPpAmount=:ppAmount,o.mapTitle=:title,o.beatMapUrl=:url,o.beatMapCardImage=:card, o.timeUpdated=:time where o.username=:username")
     void updateNewBestPlay(long id, String username, String mapRank, double mapHitAcc, double ppAmount, String title, String url, String card, String time);
}

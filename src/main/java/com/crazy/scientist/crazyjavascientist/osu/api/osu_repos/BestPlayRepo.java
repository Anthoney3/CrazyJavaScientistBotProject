package com.crazy.scientist.crazyjavascientist.osu.api.osu_repos;

import com.crazy.scientist.crazyjavascientist.osu.api.osu_models.OsuBestPlayModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.time.ZonedDateTime;
import java.util.List;

@Repository
public interface BestPlayRepo extends JpaRepository<OsuBestPlayModel,Long> {

    @Query("select o from OsuBestPlayModel o where o.username=:username")
    public OsuBestPlayModel getBestPlayByOsuUsername(String username);

    @Query("select o from OsuBestPlayModel o")
    public List<OsuBestPlayModel> getAllCurrentBestPlays();

    @Modifying
    @Transactional
    @Query("update OsuBestPlayModel o set o.id=:id, o.username=:username, o.mapRank=:mapRank,o.mapHitAcc=:mapHitAcc,o.mapPpAmount=:ppAmount,o.mapTitle=:title,o.beatMapUrl=:url,o.beatMapCardImage=:card, o.timeUpdated=:time where o.username=:username")
    public void updateNewBestPlay(long id, String username, String mapRank, double mapHitAcc, double ppAmount, String title, String url, String card, String time);
}

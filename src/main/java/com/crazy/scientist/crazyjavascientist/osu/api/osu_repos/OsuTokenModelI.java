package com.crazy.scientist.crazyjavascientist.osu.api.osu_repos;

import com.crazy.scientist.crazyjavascientist.osu.api.osu_entities.OsuTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.time.ZonedDateTime;

@Repository
public interface OsuTokenModelI extends JpaRepository<OsuTokenEntity,Long> {


    @Query("select o from OsuTokenEntity o")
    OsuTokenEntity retrieveTokenObjectInstance();

    @Query("update OsuTokenEntity o set o.token=:token,o.tokenRenewalTime=:tokenRenewalTime")
    @Transactional
    @Modifying
    void updateTokenAndRenewalTime(byte[] token, ZonedDateTime tokenRenewalTime);
}

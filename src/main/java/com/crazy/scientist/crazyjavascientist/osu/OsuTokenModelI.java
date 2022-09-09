package com.crazy.scientist.crazyjavascientist.osu;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.time.ZonedDateTime;

@Repository
public interface OsuTokenModelI extends JpaRepository<OsuTokenModel,Long> {


    @Query("select o from OsuTokenModel o")
    OsuTokenModel retrieveTokenObjectInstance();

    @Query("update OsuTokenModel o set o.token=:token,o.tokenRenewalTime=:tokenRenewalTime")
    @Transactional
    @Modifying
    void updateTokenAndRenewalTime(byte[] token, ZonedDateTime tokenRenewalTime);
}

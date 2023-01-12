package com.crazy.scientist.crazyjavascientist.osu.api.osu_repos;

import com.crazy.scientist.crazyjavascientist.osu.api.osu_entities.OsuTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.time.ZonedDateTime;

@Repository
public interface OsuTokenModelI extends JpaRepository<OsuTokenEntity,Long> {

    @Transactional
    @Modifying
    @Query("update OsuTokenEntity token set token.token=:token,token.tokenRenewalTime=:token_renewal_time where token.id=1")
    void update_token(@Param("token")byte[] token, @Param("token_renewal_time")ZonedDateTime renewal_time);
}

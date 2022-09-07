package com.crazy.scientist.crazyjavascientist.osu;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface OsuApiModelI extends JpaRepository<OsuApiModel,Long> {

    @Query("select o from OsuApiModel o where o.username=:username")
    OsuApiModel getLastRequestByOsuUsername(String username);



}

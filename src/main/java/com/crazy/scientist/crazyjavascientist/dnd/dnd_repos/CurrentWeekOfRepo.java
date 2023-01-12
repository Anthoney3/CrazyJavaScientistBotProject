package com.crazy.scientist.crazyjavascientist.dnd.dnd_repos;


import com.crazy.scientist.crazyjavascientist.dnd.dnd_entities.CurrentWeekOfEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;

@Repository
public interface CurrentWeekOfRepo extends JpaRepository<CurrentWeekOfEntity,Long> {

    @Transactional
    @Modifying
    @Query("update CurrentWeekOfEntity current_week set current_week.current_week = :current_week_value where current_week.id=1")
    void update_current_week_of(@Param("current_week_value") String current_week);
}

package com.crazy.scientist.crazyjavascientist.dnd.dnd_repos;


import com.crazy.scientist.crazyjavascientist.dnd.dnd_entities.CurrentWeekOfEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CurrentWeekOfRepo extends JpaRepository<CurrentWeekOfEntity,Long> {

}

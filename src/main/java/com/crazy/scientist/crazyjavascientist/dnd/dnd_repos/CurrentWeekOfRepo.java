package com.crazy.scientist.crazyjavascientist.dnd.dnd_repos;


import com.crazy.scientist.crazyjavascientist.dnd.dnd_entities.CurrentWeekOfEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.persistence.Entity;
import javax.persistence.Table;

@Repository
public interface CurrentWeekOfRepo extends JpaRepository<CurrentWeekOfEntity,Long> {


}

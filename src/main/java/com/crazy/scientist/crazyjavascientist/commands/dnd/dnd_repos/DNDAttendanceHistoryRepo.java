package com.crazy.scientist.crazyjavascientist.commands.dnd.dnd_repos;

import com.crazy.scientist.crazyjavascientist.commands.dnd.dnd_entities.DNDAttendanceHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DNDAttendanceHistoryRepo
  extends JpaRepository<DNDAttendanceHistoryEntity, Long> {}

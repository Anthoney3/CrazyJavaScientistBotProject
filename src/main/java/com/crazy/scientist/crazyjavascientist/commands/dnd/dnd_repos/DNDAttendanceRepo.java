package com.crazy.scientist.crazyjavascientist.commands.dnd.dnd_repos;

import com.crazy.scientist.crazyjavascientist.commands.dnd.dnd_entities.DNDAttendanceEntity;

import javax.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository(value = "DNDAttendanceRepo")
public interface DNDAttendanceRepo
  extends JpaRepository<DNDAttendanceEntity, Long> {
  @Transactional
  @Modifying
  @Query(
    value = "UPDATE DND_ATTENDANCE_INFO SET EXCUSED='Y',NO_SHOW_OR_NO_RESPONSE='N'",
    nativeQuery = true
  )
  void updateAllValuesToExcusedForTesting();

  @Transactional
  @Modifying
  @Query(
    value = "UPDATE DND_ATTENDANCE_INFO SET ATTENDING='Y',NO_SHOW_OR_NO_RESPONSE='N'",
    nativeQuery = true
  )
  void updateAllValuesToAttendingForTesting();
}

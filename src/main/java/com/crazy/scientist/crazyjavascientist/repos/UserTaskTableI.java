package com.crazy.scientist.crazyjavascientist.repos;

import com.crazy.scientist.crazyjavascientist.models.UserTaskTable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;

@Repository
public interface UserTaskTableI extends JpaRepository<UserTaskTable,Long> {


    @Query("SELECT COUNT(u) FROM UserTaskTable u WHERE u.username=:username")
    int getUserTaskListCount(String username);

    @Query("DELETE FROM UserTaskTable u WHERE u.taskTitle=:taskListTitle AND u.username=:username")
    @Modifying
    @Transactional
    void deleteTaskList(String taskListTitle, String username);


    @Query("SELECT u FROM UserTaskTable u WHERE u.taskTitle=:titleName AND u.username=:username")
    List<UserTaskTable> checkIfUserTaskListExists(String titleName, String username);
}

package com.crazy.scientist.crazyjavascientist.repos;

import com.crazy.scientist.crazyjavascientist.models.UserTaskTable;
import com.crazy.scientist.crazyjavascientist.models.UserTaskTableI;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;
import javax.persistence.Query;
import javax.transaction.Transactional;
import java.util.List;

@Component
public class TaskManagerRepo implements UserTaskTableI {

    @PersistenceUnit
    private EntityManagerFactory entityManagerFactory;


    @Override
    public UserTaskTable getUserTaskTableBy(String titleName) {

        EntityManager em = entityManagerFactory.createEntityManager();

        UserTaskTable userTaskTable = null;

        String queryString = "SELECT * FROM USER_TASK_TABLE WHERE TITLE=?";

        Query query = em.createNativeQuery(queryString,UserTaskTable.class);
        List<UserTaskTable> userTaskTableList = query.getResultList();
        userTaskTable = userTaskTableList.get(0);
        return userTaskTable;
    }

    @Transactional
    @Override
    public void saveUserTaskTable(UserTaskTable userTaskTable) {

        EntityManager em = entityManagerFactory.createEntityManager();

        String queryString = "INSERT INTO USER_TASK_TABLE(USERNAME,TASK_COMMENTS,TASK_DESCRIPTION,TASK_STATUS,TASK_TITLE)" +
                "VALUES (?,?,?,?,?)";

        Query query = em.createNativeQuery(queryString,UserTaskTable.class);
        query.setParameter(1,userTaskTable.getUsername());
        query.setParameter(2,userTaskTable.getTaskComments());
        query.setParameter(3,userTaskTable.getTaskDescription());
        query.setParameter(4,userTaskTable.getTaskStatus());
        query.setParameter(5,userTaskTable.getTaskTitle());
        em.joinTransaction();
        query.executeUpdate();

    }
}

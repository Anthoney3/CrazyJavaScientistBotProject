package com.crazy.scientist.crazyjavascientist.models;

import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface UserTaskTableI {

    UserTaskTable getUserTaskTableBy(String titleName);
    void saveUserTaskTable(UserTaskTable userTaskTable);
}

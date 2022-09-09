package com.crazy.scientist.crazyjavascientist.models;

import lombok.Data;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.persistence.*;

@Entity
public class UserTaskTable {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name="USERNAME")
    private String username;

    @Column(name = "TASK_TITLE")
    private String taskTitle;

    @Column(name = "TASK_DESCRIPTION")
    private String taskDescription;

    @Column(name = "TASK_STATUS")
    private String taskStatus;

    @Column(name = "TASK_COMMENTS")
    private String taskComments;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getTaskTitle() {
        return taskTitle;
    }

    public void setTaskTitle(String taskTitle) {
        this.taskTitle = taskTitle;
    }

    public String getTaskDescription() {
        return taskDescription;
    }

    public void setTaskDescription(String taskDescription) {
        this.taskDescription = taskDescription;
    }

    public String getTaskStatus() {
        return taskStatus;
    }

    public void setTaskStatus(String taskStatus) {
        this.taskStatus = taskStatus;
    }

    public String getTaskComments() {
        return taskComments;
    }

    public void setTaskComments(String taskComments) {
        this.taskComments = taskComments;
    }
}

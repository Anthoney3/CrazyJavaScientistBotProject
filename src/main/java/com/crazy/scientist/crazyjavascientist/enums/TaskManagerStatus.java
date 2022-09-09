package com.crazy.scientist.crazyjavascientist.enums;

import lombok.Getter;

@Getter
public enum TaskManagerStatus {

    NOT_STARTED("Not Started"),
    IN_PROGRESS("In Progress"),
    COMPLETED("Completed");

    private String status;

    TaskManagerStatus(String status){
        this.status = status;
    }

}

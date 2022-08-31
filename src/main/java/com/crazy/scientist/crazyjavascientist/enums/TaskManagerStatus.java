package com.crazy.scientist.crazyjavascientist.enums;

import lombok.Data;
import lombok.Getter;
import org.springframework.web.bind.annotation.GetMapping;

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

package com.crazy.scientist.crazyjavascientist.osu.api.osu_models;

import lombok.Getter;

@Getter
public enum OsuMembers {
    ONE("21120079"),
    TWO("19915043"),
    THREE("29162849"),
    FOUR("30250822"),
    FIVE("22833841"),
    SIX("29705922");

    private String userID;
    OsuMembers(String userID){
        this.userID = userID;

    }
}

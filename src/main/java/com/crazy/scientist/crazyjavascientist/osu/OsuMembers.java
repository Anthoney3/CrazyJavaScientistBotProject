package com.crazy.scientist.crazyjavascientist.osu;

import lombok.Getter;

@Getter
public enum OsuMembers {
    ONE("21120079"),
    TWO("19915043"),
    THREE("29162849"),
    FOUR("22833841"),
    FIVE("30250822"),
    SIX("29705922");

    private String userID;
    OsuMembers(String userID){
        this.userID = userID;

    }
}

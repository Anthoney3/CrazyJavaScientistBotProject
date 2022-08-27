package com.crazy.scientist.crazyjavascientist.osu;

import lombok.Getter;

@Getter
public enum OsuMembers {
    ONE("21120079"),
    TWO(""),
    THREE(""),
    FOUR(""),
    FIVE("30250822"),
    SIX("");

    private String userID;
    OsuMembers(String userID){
        this.userID = userID;

    }
}

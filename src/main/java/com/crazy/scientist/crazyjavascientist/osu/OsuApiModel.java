package com.crazy.scientist.crazyjavascientist.osu;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.sql.Timestamp;
import java.time.ZonedDateTime;

@Entity
@Data
@NoArgsConstructor
public class OsuApiModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "USERNAME")
    private String username;

    @Column(name = "PP")
    private String pp;

    @Column(name = "TOTAL_TIME_PLAYED")
    private String totalTimePlayed;

    @Column(name = "GLOBAL_RANKING")
    private String globalRanking;

    @Column(name = "TOTAL_CHOKES")
    private String totalChokes;

    @Column(name = "HIT_ACC")
    private String hitAcc;

    @Column
    private Timestamp lastRequestDateAndTime;

    public OsuApiModel(String username, String pp, String totalTimePlayed, String globalRanking, String totalChokes, String hitAcc, Timestamp lastRequestDateAndTime) {
        this.username = username;
        this.pp = pp;
        this.totalTimePlayed = totalTimePlayed;
        this.globalRanking = globalRanking;
        this.totalChokes = totalChokes;
        this.hitAcc = hitAcc;
        this.lastRequestDateAndTime = lastRequestDateAndTime;
    }
}

package com.crazy.scientist.crazyjavascientist.osu.api;



import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.sql.Timestamp;
import java.time.Instant;
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
    private double pp;

    @Column(name = "MONTHLY_PLAYCOUNT")
    private int monthlyPlaycount;

    @Column(name = "TOTAL_TIME_PLAYED")
    private String totalTimePlayed;

    @Column(name = "GLOBAL_RANKING")
    private int globalRanking;

    @Column(name = "TOTAL_CHOKES")
    private int totalChokes;

    @Column(name = "HIT_ACC")
    private double hitAcc;

    @Column
    private ZonedDateTime lastRequestDateAndTime;

    public OsuApiModel(String username, double pp, int monthlyPlaycount, String totalTimePlayed, int globalRanking, int totalChokes, double hitAcc, ZonedDateTime lastRequestDateAndTime) {
        this.username = username;
        this.pp = pp;
        this.monthlyPlaycount = monthlyPlaycount;
        this.totalTimePlayed = totalTimePlayed;
        this.globalRanking = globalRanking;
        this.totalChokes = totalChokes;
        this.hitAcc = hitAcc;
        this.lastRequestDateAndTime = lastRequestDateAndTime;
    }

}

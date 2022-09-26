package com.crazy.scientist.crazyjavascientist.osu.api.osu_models;



import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.ZonedDateTime;


@Entity
@Data
@NoArgsConstructor
@Table(name = "OSU_MEMBER_INFO")
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

    @Column(name = "DISCORD_NICKNAME")
    private String nickname;

    @Column(name = "DISCORD_USER_ID")
    private String discordUserID;

    @Column
    private ZonedDateTime lastRequestDateAndTime;

    public OsuApiModel(String username, double pp, int monthlyPlaycount, String totalTimePlayed, int globalRanking, int totalChokes, double hitAcc, String nickname, String discordUserID, ZonedDateTime lastRequestDateAndTime) {
        this.username = username;
        this.pp = pp;
        this.monthlyPlaycount = monthlyPlaycount;
        this.totalTimePlayed = totalTimePlayed;
        this.globalRanking = globalRanking;
        this.totalChokes = totalChokes;
        this.hitAcc = hitAcc;
        this.nickname = nickname;
        this.discordUserID = discordUserID;
        this.lastRequestDateAndTime = lastRequestDateAndTime;
    }

    public OsuApiModel(String username, double pp, int monthlyPlaycount, String totalTimePlayed, int globalRanking, int totalChokes, double hitAcc, String nickname, ZonedDateTime lastRequestDateAndTime) {
        this.username = username;
        this.pp = pp;
        this.monthlyPlaycount = monthlyPlaycount;
        this.totalTimePlayed = totalTimePlayed;
        this.globalRanking = globalRanking;
        this.totalChokes = totalChokes;
        this.hitAcc = hitAcc;
        this.nickname = nickname;
        this.lastRequestDateAndTime = lastRequestDateAndTime;
    }
}

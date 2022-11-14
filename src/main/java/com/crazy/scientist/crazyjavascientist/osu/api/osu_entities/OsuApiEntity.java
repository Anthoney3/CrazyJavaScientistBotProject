package com.crazy.scientist.crazyjavascientist.osu.api.osu_entities;



import com.crazy.scientist.crazyjavascientist.osu.api.osu_models.deserializers.OsuApiDeserializer;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;

import javax.persistence.*;
import java.time.Duration;
import java.time.ZonedDateTime;


@Entity
@Data
@Table(name = "OSU_MEMBER_INFO")
@JsonDeserialize(using = OsuApiDeserializer.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class OsuApiEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "USERNAME")
    @JsonProperty("username")
    private String username;

    @Column(name = "PP")
    @JsonProperty("pp")
    private double pp;

    @Column(name = "MONTHLY_PLAYCOUNT")
    private int monthlyPlaycount;

    @Column(name = "TOTAL_TIME_PLAYED")
    private Duration totalTimePlayed;

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

    @Column(name = "OSU_PFP_PICTURE")
    private String pfpPictureUrl;

    @Column
    private ZonedDateTime lastRequestDateAndTime;



    public OsuApiEntity() {
    }

    public OsuApiEntity(String username, double pp, int monthlyPlaycount, Duration totalTimePlayed, int globalRanking, int totalChokes, double hitAcc, String nickname, String pfpPictureUrl, ZonedDateTime lastRequestDateAndTime) {
        this.username = username;
        this.pp = pp;
        this.monthlyPlaycount = monthlyPlaycount;
        this.totalTimePlayed = totalTimePlayed;
        this.globalRanking = globalRanking;
        this.totalChokes = totalChokes;
        this.hitAcc = hitAcc;
        this.nickname = nickname;
        this.pfpPictureUrl = pfpPictureUrl;
        this.lastRequestDateAndTime = lastRequestDateAndTime;
    }

/*    public OsuApiModel(String username, double pp, int monthlyPlaycount, String totalTimePlayed, int globalRanking, int totalChokes, double hitAcc, String nickname, String discordUserID, ZonedDateTime lastRequestDateAndTime) {
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
    }*/

    public OsuApiEntity(String username, double pp, int monthlyPlaycount, Duration totalTimePlayed, int globalRanking, int totalChokes, double hitAcc, String nickname, ZonedDateTime lastRequestDateAndTime) {
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

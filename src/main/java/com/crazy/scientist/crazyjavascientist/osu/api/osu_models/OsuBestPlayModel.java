package com.crazy.scientist.crazyjavascientist.osu.api.osu_models;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@NoArgsConstructor
@Entity
@Table(name = "OSU_BEST_PLAYS")
public class OsuBestPlayModel{

    @Id
    private long id;

    @Column(name = "USERNAME")
    private String username;

    @Column(name = "MAP_RANK")
    private String mapRank;

    @Column(name = "MAP_HIT_ACC")
    private double mapHitAcc;

    @Column(name = "MAP_PP_AMOUNT")
    private double mapPpAmount;

    @Column(name = "MAP_TITLE")
    private String mapTitle;

    @Column(name = "BEAT_MAP_URL")
    private String beatMapUrl;

    @Column(name = "BEAT_MAP_CARD")
    private String beatMapCardImage;

    public OsuBestPlayModel(long id, String username, String mapRank, double mapHitAcc, double mapPpAmount, String mapTitle, String beatMapUrl, String beatMapCardImage) {
        this.id = id;
        this.username = username;
        this.mapRank = mapRank;
        this.mapHitAcc = mapHitAcc;
        this.mapPpAmount = mapPpAmount;
        this.mapTitle = mapTitle;
        this.beatMapUrl = beatMapUrl;
        this.beatMapCardImage = beatMapCardImage;
    }
}

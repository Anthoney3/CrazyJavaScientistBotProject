package com.crazy.scientist.crazyjavascientist.osu.api.osu_entities;

import com.crazy.scientist.crazyjavascientist.osu.api.osu_models.deserializers.OsuBestPlayDeserializer;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;
import lombok.NoArgsConstructor;


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@NoArgsConstructor
@Entity
@Table(name = "OSU_BEST_PLAYS")
@JsonDeserialize(using = OsuBestPlayDeserializer.class)
@JsonIgnoreProperties
public class OsuBestPlayEntity {

    @Id
    private long id;
    @JsonProperty("username")
    @Column(name = "USERNAME")
    private String username;
    @JsonProperty("rank")
    @Column(name = "MAP_RANK")
    private String mapRank;

    @JsonProperty("accuracy")
    @Column(name = "MAP_HIT_ACC")
    private double mapHitAcc;
    @JsonProperty("pp")
    @Column(name = "MAP_PP_AMOUNT")
    private double mapPpAmount;
    @JsonProperty("title")
    @Column(name = "MAP_TITLE")
    private String mapTitle;
    @JsonProperty("url")
    @Column(name = "BEAT_MAP_URL")
    private String beatMapUrl;
    @JsonProperty("card")
    @Column(name = "BEAT_MAP_CARD")
    private String beatMapCardImage;

    @Column(name = "TIME_UPDATED")
    private String timeUpdated;

    public OsuBestPlayEntity(long id, String username, String mapRank, double mapHitAcc, double mapPpAmount, String mapTitle, String beatMapUrl, String beatMapCardImage, String timeUpdated) {
        this.id = id;
        this.username = username;
        this.mapRank = mapRank;
        this.mapHitAcc = mapHitAcc;
        this.mapPpAmount = mapPpAmount;
        this.mapTitle = mapTitle;
        this.beatMapUrl = beatMapUrl;
        this.beatMapCardImage = beatMapCardImage;
        this.timeUpdated = timeUpdated;
    }
}

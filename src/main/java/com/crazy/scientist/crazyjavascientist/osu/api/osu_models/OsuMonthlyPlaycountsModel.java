package com.crazy.scientist.crazyjavascientist.osu.api.osu_models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class OsuMonthlyPlaycountsModel {

    @JsonProperty("start_date")
    private String  startDate;

    @JsonProperty("count")
    private int playCount;

}

package com.crazy.scientist.crazyjavascientist.osu.api.osu_models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OsuMembersByNickname {

    private String osuMemberName;
    private String nickname;

}

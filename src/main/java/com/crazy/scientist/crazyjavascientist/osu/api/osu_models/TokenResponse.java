package com.crazy.scientist.crazyjavascientist.osu.api.osu_models;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class TokenResponse {

    private String token_type;
    private int expires_in;
    private String access_token;
}

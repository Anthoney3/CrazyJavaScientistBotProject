package com.crazy.scientist.crazyjavascientist.osu.api.osu_models;

import lombok.*;

@Setter
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class OAuthBody {

    private int client_id;
    private String client_secret;
    private String grant_type;
    private String scope;


}

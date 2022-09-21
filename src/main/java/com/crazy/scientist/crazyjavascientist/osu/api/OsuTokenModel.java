package com.crazy.scientist.crazyjavascientist.osu.api;

import lombok.Data;

import javax.persistence.*;
import java.time.ZonedDateTime;

@Entity
@Data
@Table(name = "TOKEN")
public class OsuTokenModel  {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "OAUTH_TOKEN", length = 2048)
    private byte[] token;

    @Column(name="TOKEN_RENEWAL_TIME")
    private ZonedDateTime tokenRenewalTime;

    public OsuTokenModel(byte[] token, ZonedDateTime tokenRenewalTime) {
        this.token = token;
        this.tokenRenewalTime = tokenRenewalTime;
    }

    public OsuTokenModel() {

    }
}

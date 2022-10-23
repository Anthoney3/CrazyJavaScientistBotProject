package com.crazy.scientist.crazyjavascientist.osu.api.osu_entities;

import lombok.Data;

import javax.persistence.*;
import java.time.ZonedDateTime;

@Entity
@Data
@Table(name = "TOKEN")
public class OsuTokenEntity {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "OAUTH_TOKEN", length = 2048)
    private byte[] token;

    @Column(name="TOKEN_RENEWAL_TIME")
    private ZonedDateTime tokenRenewalTime;

    public OsuTokenEntity(byte[] token, ZonedDateTime tokenRenewalTime) {
        this.token = token;
        this.tokenRenewalTime = tokenRenewalTime;
    }

    public OsuTokenEntity() {

    }
}
